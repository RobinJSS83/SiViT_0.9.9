%{
% Modified by A. Boiko for SiViT
% Copyright (C) 2021 Abertay University

% Original code Copyright 2015 The MathWorks, Inc.
% All rights reserved.

% The code is covered by the BSD License

Copyright (c) 2015, The MathWorks, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in
      the documentation and/or other materials provided with the distribution

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
%}

function SBmodelObj = sbmlimportFunExternal(filename)
%SBMLIMPORTFUNEXTERNAL imports a model stored in a SBML file and stores its
% associated user-defined functions in individual separated files in the
% current folder.
% The resulting SimBiology model is then loaded in the SimBiology desktop
% and supports the 'Model acceleration' mode.
% Functions definitions containing the MathML function 'piecewise' are also
% supported. A MATLAB function('piecewise') is created to that purpose. 
% To ensure that the ODE solver registers the transitions, events are added
% to the resulting SimBiology model.
%
%   SBMLIMPORTFUNEXTERNAL('FILENAME') imports the SBML file 'FILENAME' into 
%   the SimBiology desktop. All SBML function calls contained in the 
%   original model are stored in individual separate files in the current 
%   folder. If no 'FILENAME' is provided, a dialog box opens up to select 
%   a file.
%
% Requirement: Function TranslateSBML from libSBML.

% Copyright 2015 The MathWorks, Inc.

% retrieve file name
if nargin == 0 || isempty(filename)
    [filename, pathname] = uigetfile({'*.xml';'*.sbml';'*.*'},'Select a SBML file');
    if filename == 0, return, end
    SBMLfilename = fullfile(pathname,filename);
else
    SBMLfilename = filename;
end

% import model itself in a SimBiology object and suppress original warning
warning('off', 'SimBiology:SB_FUNCTIONDEFS_NOT_SUPPORTED_FOR_VERSION');
SBmodelObj = sbmlimport(SBMLfilename);
warning('on',  'SimBiology:SB_FUNCTIONDEFS_NOT_SUPPORTED_FOR_VERSION');

%sanitize the model prior to conversion - AB
SBmodelObj = sanitizeNamesInModel(SBmodelObj);

% replace math operator in event trigger functions 
replaceMathOperatorsInEventTrigger(SBmodelObj);

% import user-defined functions
importSBMLfun(SBmodelObj,SBMLfilename);

% no need to do this for SiViT
% load model in Simbiology desktop
%simbiology(SBmodelObj);

end

%% Help functions
function replaceMathOperatorsInEventTrigger(SBmodelObj)
% replace math operator in event triggers to avoid warning with dimensioanl
% analysis

events   = SBmodelObj.Events;
triggers = get(events,'Trigger');

if iscell(triggers)
    triggers = cellfun(@replaceMathOperatorsInString,triggers,'UniformOutput',false);
    
    fun = @(x,newtrig) set(x,'Trigger',newtrig{:});
    arrayfun(fun,events,triggers);
elseif ischar(triggers)
    set(events,'Trigger',replaceMathOperatorsInString(triggers));
end

end

function importSBMLfun(SBmodelObj,SBMLfilename)
% function status = importSBMLfun(SBMLfilename)
%
% Imports function definitions contained in a SBML file and saves each
% function in a separate file in the current folder.
% Returns true if succeeded, false otherwise.

if exist('TranslateSBML','file') == 0
    error('SimBiology:importSBMLfun:libSBMLNotFound','TranslateSBML was not found. Please add libSBML to your MATLAB path.')
end

% reads SBML file using libSBML
[sbmlObj, err] = TranslateSBML(SBMLfilename);

% checks for errors while parsing
if not(isempty(err))
    error('SimBiology:importSBMLfun:SBMLParseError', 'Error while parsing SBML file. Please check the messages displayed above.')
end

try
% checks if any function is found
nbFun = numel(sbmlObj.functionDefinition);
if nbFun == 0
    warning('SimBiology:importSBMLfun:NoFun','No function found in SBML file.');
    return;
end

% saves each function in separate file
savePiecewiseDef = false;
for i = 1:nbFun
    [statFile, funcFilename, containsPiecewise] = saveInFile(SBmodelObj, sbmlObj.functionDefinition(i), SBMLfilename);
    if statFile == 1
        disp(['Created file ''' funcFilename '''.'])
    elseif statFile == 0
        disp(['File ''' funcFilename ''' already exists. Nothing done.'])
    else
        warning('SimBiology:importSBMLfun:CantWrite',['Could not create file ''' funcFilename '''.'])
    end
    
    savePiecewiseDef = savePiecewiseDef | containsPiecewise;
end

if savePiecewiseDef
    addPiecewiseFun();
end

catch %ME
    %rethrow(ME)
    fclose('all');
    error('SimBiology:importSBMLfun:unknownError','Error while importing functions.')
end

end

function [status,funcFilename,containsPiecewise] = saveInFile(SBmodelObj, funcDef, origSBMLfname)
% Saves function in a dedicated file.
% status = 1 if succeded, 0 if file already exists, -1 if file could not be
% created.

status = 1;
containsPiecewise = false;

% retrieve function name
funcName     = funcDef.id;
funcFilename = [funcName '.m'];

% check if file already exists
if exist(funcFilename, 'file')
    status = 0;
end

% retrieve inputs and output of function
[successIO, InFun, OutFun] = getInOutFunc(funcDef.math);
if not(successIO)
    status = -1;
    warning('SimBiology:importSBMLfun:NoLambdaForm','Function not defined in lambda form.')
    % return;
end

% create file containing function definition
if status > 0
    fid = fopen(funcFilename, 'w+');
    
    fprintf(fid, 'function out = %s(%s)\n', funcName, strjoin(InFun,', '));
    if ~isempty(funcDef.name)
        fprintf(fid, '%% Description: %s\n',funcDef.name);
        fprintf(fid, '%%\n');
    end
    fprintf(fid, '%% Function imported from SMBL file ''%s''\n',origSBMLfname);
    fprintf(fid, '%% %s\n\n', datestr(now));
end

% replace logical functions by their equivalent operators in string s
OutFun = replaceMathOperatorsInString(OutFun);

% if OutFun contains 'piecewise', then add piecewise function
piecewiseCalls = regexp(OutFun,'\<piecewise\(','once');
if not(isempty(piecewiseCalls))
    % add dummy parameter to create events
    % events are added to make sure the solver captures transitions in
    % the piecewise function
    dummyPar = 'dummyParPW';
    if ~any(strcmp(dummyPar, get(SBmodelObj.Parameters, 'Name')))
        addparameter(SBmodelObj, dummyPar, 1, ...
            'ValueUnits', 'dimensionless', 'ConstantValue', false);
    end
    
    % get events conditions
    conditions = getPiecewiseConditions(OutFun);
    
    % get variables in conditions
    varsDef = regexp(conditions,'\<[a-zA-Z]\w*\>','match');
    varsDef = unique([varsDef{:}]);
    
    % replace var in def conditions by var in function calls
    varsInFunCalls = getVarsInFunCalls(SBmodelObj,funcName);
    conditions = generateConditions(conditions,varsDef,varsInFunCalls);
    
    % check if newcond contains non-constant parameter, species
    conditions = removeConstantConditions(SBmodelObj,conditions);
    
    % since we don't know in which direction the transitions should be, we
    % generate events with complimentary transitions
    conditions = generateReverseConditions(conditions);
    
    % add model events
    addeventsPiecewise(SBmodelObj, conditions)
    
    % set containsPiecewise to true
    containsPiecewise = true;
end

if status > 0
    fprintf(fid, 'out = %s;\n', OutFun);
    fclose(fid);
end
end

function [status, InFun, OutFun] = getInOutFunc(str)
% retrieves input and output of lambda function
% status = true if the function is in a lambda form, false otherwise

lambdaStart  = 'lambda(';
[C, matches] = strsplit(str,{lambdaStart, ','});

if not(strcmp(matches{1}, lambdaStart))
    status = false;
    InFun  = [];
    OutFun = [];
    return;
else
    % removes empty strings
    C(cellfun(@isempty,C)) = [];
    
    % looks for the first entry with a bracket and concatenate it with all
    % subsequent entries since it is the function's definition and not a
    % variable
    checkbrackets   = @(x) not(isempty(strfind(x,'(')));
    idxFirstBracket = find(cellfun(checkbrackets, C), 1, 'first');
    
    if not(isempty(idxFirstBracket))
        InFun  = C(1:idxFirstBracket-1);
        OutFun = strjoin(C(idxFirstBracket:end),',');
        OutFun(end) = []; % remove last closing bracket coming from 'lambda('    
    else
        InFun  = C(1:end-1);
        OutFun = C{end}(1:end-1);
    end
    
    status = true;
end

end
    
function s = replaceMathOperatorsInString(s)
% replaces logical and power functions by their equivalent operators in string s
% replacing call to built-in power function by corresponding operator ^
% avoids warnings issued by dimensional analysis in SimBiology

    function s = addbrackets(s)
        if ~isempty(regexp(s,'+|-|*|/', 'once'))
            s = ['(' s ')'];
        end
    end

fun = @addbrackets; %#ok<NASGU>
s = regexprep(s,'power\((.+?),(.+?)\)', '${fun($1)}^${fun($2)}');

s = regexprep(s,'lt\((.+?),(.+?)\)', '$1<$2');
s = regexprep(s,'le\((.+?),(.+?)\)', '$1<=$2');
s = regexprep(s,'gt\((.+?),(.+?)\)', '$1>$2');
s = regexprep(s,'ge\((.+?),(.+?)\)', '$1>=$2');

end

function conditions = getPiecewiseConditions(s)
% retrieves piecewise conditions

% take the arguments of all piecewise calls in the string
args = regexp(s,'piecewise\((.*?\))','tokens');
args = [args{:}];
args = cellfun(@getFunctionCallArgs,args,'UniformOutput',false);

% process all calls
conds = cell(numel(args),1);
for i=1:numel(args)
    
    % retrieve conditions to return
    conds{i} = strsplit(args{i},',');
    conds{i} = conds{i}(2:2:end);
end

conditions = unique([conds{:}]);

end

function inputarguments = getFunctionCallArgs(ss)
% find first closing bracket with no corresponding opening bracket

    parIn  = strfind(ss,'(');
    parOut = strfind(ss,')');
    one = zeros(size(ss));
    one(parIn)  = 1;
    one(parOut) = -1;
    closingBracketIdx = find(cumsum(one) == -1, 1);
    inputarguments = ss(1:closingBracketIdx-1);
end

function out = getVarsInFunCalls(SBmodelObj, funname)
% retrieves variable names in all function calls in the model
    rr = [get(SBmodelObj.Reactions, 'ReactionRate');...
          get(SBmodelObj.Rules, 'Rule')];
      
    allinputs = regexp(rr, ['\<' funname '\(([a-zA-Z0-9_,]+)\)'],'tokens');  
    allinputs = [allinputs{:}]; allinputs = [allinputs{:}];  
    
    out = cellfun(@(x) strsplit(x,','), allinputs,'UniformOutput',false);
end

function outStr = replaceVarsInFun(str, InFunDef, InFunCall)
% This function replaces the variable names in a mathematical expression.
%
% str = original expression
% InFunDef = names of the variables in the function definition
% InfunCall = names of the variables in the function call
% outStr = resulting mathematical expression


% Remarks:
% The original string is split to work on each part individually. Each part 
% will then only contain one variable and we stop the replacement as soon 
% as one is done.
% This ensures that one does not work on the whole string all the time and
% avoids that new parameter names are the same as (distinct) original
% parameter names.

% splits the original string and retrieve the indices
expression2 = ['(\W+|\<)(' strjoin(InFunDef,'|') ')(\W+|\>)'];
[~, idParts] = regexp(str, expression2, 'split');

newStr = cell(1,numel(idParts));

% adds the index 1 and an extra index to ensure the whole string is read in
% the following loop
if idParts(1) ~= 1
    idParts = [1 idParts];
end
idParts(end+1) = length(str)+1;

% replaces the variable contained in each part and stop to look for
% variable names as soon as one replacement is done
expression = cellfun(@(x) ['(\W+|\<)' x '(\W+|\>)'],InFunDef','UniformOutput', false);
replace    = cellfun(@(x) ['$1' x '$2'],InFunCall','UniformOutput', false);

for j=1:numel(idParts)-1
    newStr{j} = str(idParts(j):idParts(j+1)-1);
    for l=1:numel(expression)
        nnstr = regexprep(newStr{j}, expression{l}, replace{l});
        if not(strcmp(nnstr, newStr{j}))
            newStr{j} = nnstr;
            break;
        end
    end
    
end

% concatenates the resulting strings to build the final mathematical
% expression
outStr = strjoin(newStr,'');

end

function finalconditions = generateConditions(conditions,varsDef,varsInFunCalls)
% generates trigger events for all transitions derived from calls to the 
% function 'piecewise'

    finalconditions = [];
    for i=1:numel(conditions)
        for j=1:numel(varsInFunCalls)
        newcond = replaceVarsInFun(conditions{i},varsDef,varsInFunCalls{j});
        finalconditions = [finalconditions; {newcond}]; %#ok<AGROW>
        end
    end
    
    finalconditions = unique(finalconditions);
end

function conditions = removeConstantConditions(SBmodelObj,conditions)
% we keep only events which are triggered by a condition that can change
% over time. So we check if the trigger functions are depending on
% non-constant parameters, species or compartment volumes. If not, we
% remove them from the list.

condvars = regexp(conditions,'\<[a-zA-Z_]\w*\>','match');
specnames = get(SBmodelObj.Species,'Name');
parnames  = get(SBmodelObj.Parameters,'Name');
compnames = get(SBmodelObj.Compartments,'Name');

removeIdx = true(size(conditions));

for i=1:numel(conditions) 
    [isSpec, loc1] = ismember(condvars{i}, specnames);
    [isPar,  loc2] = ismember(condvars{i}, parnames);
    [isComp, loc3] = ismember(condvars{i}, compnames);
    
    isConstSpec = get(SBmodelObj.Species(loc1(isSpec)),     'ConstantAmount');
    isConstPar  = get(SBmodelObj.Parameters(loc2(isPar)),   'ConstantValue');
    isConstComp = get(SBmodelObj.Compartments(loc3(isComp)),'ConstantCapacity');
    if iscell(isConstSpec),  isConstSpec = [isConstSpec{:}]; end
    if iscell(isConstPar),   isConstPar  = [isConstPar{:}];  end
    if iscell(isConstComp),  isConstComp = [isConstComp{:}]; end

    if any(~isConstSpec) || any(~isConstPar) || any(~isConstComp)
        removeIdx(i) = false;
    end
end

conditions(removeIdx) = [];
end

function finalconditions = generateReverseConditions(conditions)
% generates trigger functions complimentary to the ones given in
% 'conditions' (e.g. x>0, x<=0)

newconditions = conditions;
for i=1:numel(newconditions)
    % reverse condition
    if ~isempty(strfind(newconditions{i},'>='))
        newconditions{i} = strrep(newconditions{i}, '>=','<');
    elseif ~isempty(strfind(newconditions{i},'>'))
        newconditions{i} = strrep(newconditions{i}, '>','<=');
    elseif ~isempty(strfind(newconditions{i},'<='))
        newconditions{i} = strrep(newconditions{i}, '<=','>');
    elseif ~isempty(strfind(newconditions{i},'<'))
        newconditions{i} = strrep(newconditions{i}, '<','>=');
    end
end
finalconditions = [conditions; newconditions];
end

function addeventsPiecewise(SBmodelObj, conditions)
% adds events with triggers listes in conditions to SimBiology model

triggers   = get(SBmodelObj.Events,'Trigger');
if ~isempty(triggers), triggers = strrep(triggers,' ', ''); end
conditions = strrep(conditions,' ', '');

for i=1:numel(conditions)
        % check if event with same trigger exist
        existTrigger = strfind(triggers,conditions{i});
        
        if iscell(existTrigger)
            existTrigger = ~isempty([existTrigger{:}]);
        else
            existTrigger = ~isempty(existTrigger);
        end
        if ~existTrigger
            addevent(SBmodelObj, conditions{i}, 'dummyParPW = 1');
        end
end

end

%NOTE: this will exit every time now as piecewise is now a constant
%external function used for models that do not have custom function but
%have piecewise in them
function addPiecewiseFun()
% adds function definition for piecewise as subfunction in the current file

    filename = 'piecewise.m';

    % check if file already exist and exist if positive
    if exist(filename,'file'), return; end

    % open file
    fid = fopen(filename, 'w+');
    if fid == -1, return;end

    fprintf(fid, 'function output = piecewise(varargin)\n');
    fprintf(fid, '%% This function emulates the MathML piecewise function.\n\n');
    fprintf(fid, '%% retrieve conditions and values\n');
    fprintf(fid, 'inputs     = [varargin{:}];\n');
    fprintf(fid, 'nbCond     = floor(numel(inputs)/2);\n');
    fprintf(fid, 'conditions = logical(inputs(2:2:2*nbCond));\n');
    fprintf(fid, 'values     = inputs(1:2:2*nbCond-1);\n');
    fprintf(fid, '\n');
    fprintf(fid, '%% default value\n');
    fprintf(fid, 'if nbCond ~= numel(inputs)/2\n');
    fprintf(fid, '    default = inputs(end);\n');
    fprintf(fid, 'else\n');
    fprintf(fid, '    default = NaN;\n');
    fprintf(fid, 'end\n');
    fprintf(fid, '\n');
    fprintf(fid, '%% retrieve values for conditions that are true\n');
    fprintf(fid, 'values = values(conditions);\n');
    fprintf(fid, '\n');
    fprintf(fid, '%% set output to associated value or to default if no condition is true\n');
    fprintf(fid, 'if isempty(values)\n');
    fprintf(fid, '    output = default;\n');
    fprintf(fid, 'else %% take last true condition\n');
    fprintf(fid, '    output = values(end);\n');
    fprintf(fid, 'end\n');

    % close file
    fclose(fid);
    
end