% Modified by A. Boiko for SiViT
% Copyright (C) 2021 Abertay University

% Original code Copyright 2015 The MathWorks, Inc.
% All rights reserved.

% The code is covered by the BSD License

% Redistribution and use in source and binary forms, with or without
% modification, are permitted provided that the following conditions are
% met:
% 
%     * Redistributions of source code must retain the above copyright
%       notice, this list of conditions and the following disclaimer.
%     * Redistributions in binary form must reproduce the above copyright
%       notice, this list of conditions and the following disclaimer in
%       the documentation and/or other materials provided with the distribution
% 
% THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
% AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
% IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
% ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
% LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
% CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
% SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
% INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
% CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
% ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
% POSSIBILITY OF SUCH DAMAGE.

%Also return time symbol name to provide support for non-autonomous models
function [SBmodelObj, timeSymbolName] = sbmlimportFunReplace(filename)
%SBMLIMPORTFUNREPLACE imports a model stored in a SBML file and replaces all
% function calls in the reaction rates.
% Does not support the use of MathML function 'piecewise' in the function 
% definitions.
%
%   SBMLIMPORTFUNREPLACE('FILENAME') imports the SBML file 'FILENAME' into  
%   the SimBiology desktop. All SBML function calls contained in the original
%   model are replaced by the associated expression so that the loaded
%   model does not rely on external functions. If no 'FILENAME' is
%   provided, a dialog box opens up to select a file.
%
% Requirement: Function TranslateSBML from libSBML.

%!!!! tidy up the dialog box section - Andrei
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

% replace calls of user-defined functions
% and get time symbol name while reading the file
timeSymbolName = importSBMLfun(SBmodelObj, SBMLfilename);

% load model in SimBiology desktop
%DON'T NEED THAT IN SIVIT
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

function [timeSymbolName] = importSBMLfun(SBmodel, SBMLfilename)
% function importSBMLfun(SBmodel, SBMLfilename)
%
% Imports function definitions contained in a SBML file and replaces each
% function call by the associated expression in the SimBiology model
% SBmodelIn.
% Returns modified model and time_symbol name for support. -AB

if exist('TranslateSBML','file') == 0
    error('SimBiology:importSBMLfun:libSBMLNotFound','TranslateSBML was not found. Please add libSBML to your MATLAB path.')
end

% reads SBML file using libSBML
[sbmlObj, err] = TranslateSBML(SBMLfilename);
timeSymbolName = sbmlObj.time_symbol;
% checks for errors while parsing
if not(isempty(err))
    error('SimBiology:importSBMLfun:SBMLParseError',['Error while parsing ' ...
    'SBML file. Please check the messages displayed above.'])
end

try
    
    % checks if any function is found
    nbFun = numel(sbmlObj.functionDefinition);
    if nbFun == 0
        warning('SimBiology:importSBMLfun:NoFun','No function found in SBML file.');
        return;
    end
    
    % replaces each function by associated expression
    for i=1:nbFun
        disp(sbmlObj.functionDefinition(i));
        replaceFunInRates(SBmodel, sbmlObj.functionDefinition(i));
    end

catch
    error('SimBiology:importSBMLfun:unknownError','Error while importing functions.')
end

end

function replaceFunInRates(SBmodel, sbmlFun)
% This function replaces all calls of one SBML function in all reaction
% rates of a SimBiology model.
%
% SBmodel = original Simbiology model
% sbmlfun = structure associated to one function as returned by
% TranslateSBML. (sbmlFun = sbmlObj.functionDefinition(i))

% retrieves all reaction rates
rate = get(SBmodel.Reactions, 'ReactionRate');

% retrieves function definition
fun = sbmlFun.id;
[InFunDef, OutFunDef] = getInOutLambdaFunc(sbmlFun.math);

% issues warning if function definition contains 'piecewise' calls
if ~isempty(regexp(OutFunDef,'\<piecewise\(', 'once'))
    warning('SimBiology:importSBMLfun:Piecewise',['Function ''%s'' contains calls to ''piecewise'' mathML function.\n' ...
        'This is currently not supported. Please adapt your model accordingly.'], fun)
end

% looks for function calls in rates
funCall = regexp(rate,[fun '\(.*?\)'],'match');
funCall = cellfun(@char, funCall, 'UniformOutput', false);

% retrieves parameters used in function calls
InFunCall = regexprep(funCall, '(\w+\(|\))','');
InFunCall = regexp(InFunCall, ',', 'split');

% replaces those parameters in the mathematical expression and replaces the
% function calls by the resulting expressions
for i = 1:numel(rate)
    if not(isempty(funCall{i}))
        OutFunCall = replaceVarsInFun(OutFunDef, InFunDef, InFunCall{i});

        OutFunCall = replaceMathOperatorsInString(OutFunCall);
        
        % add brackets around expression if necessary
        if strcmp(funCall{i}, rate{i}) || isempty(regexp(OutFunCall, '+|-|*|/', 'once'))
            rate{i} = strrep(rate{i}, funCall{i}, OutFunCall);
        else
            rate{i} = strrep(rate{i}, funCall{i}, ['(' OutFunCall ')']);
        end
        
        % update reaction rate
        set(SBmodel.Reactions(i), 'ReactionRate', rate{i})
    end
end

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
idParts(end+1) = length(str) + 1;

% replaces the variable contained in each part and stop to look for
% variable names as soon as one replacement is done
expression = cellfun(@(x) ['(\W+|\<)' x '(\W+|\>)'],InFunDef','UniformOutput',false);
replace    = cellfun(@(x) ['$1' x '$2'],InFunCall','UniformOutput',false);

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

function [InFun, OutFun] = getInOutLambdaFunc(str)
% retrieves input and output of lambda function
% status = true if the function is in a lambda form, false otherwise

lambdaStart  = 'lambda(';
[C, matches] = strsplit(str,{lambdaStart, ','});

if not(strcmp(matches{1}, lambdaStart))
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
    
end

end
