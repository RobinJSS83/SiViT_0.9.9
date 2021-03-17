%{ 

Signalling Visualisation Toolkit (SiViT)
Copyright (C) 2021  Abertay University

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License or any later
version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

%}


function [t, p, v, pnorm, vnorm, snames, rnames, vmin, vmax, pmin, pmax] = calculateFlux(m, optionObj, timeSymbolName)

% this should solve issue with nested reactions in rules,
% but not reactions within reactions if such thing exists

% 1. find all reaction name occurances in rules
% 2. replace longer names before shorter to avoid situatios 
%    where for ex. r2 is replaced in r21, etc.
for rule=1:length(m.Rules)
    r_refs = {};
    r_count = 0;
    for react=1:length(m.Reactions)
        % if rule contains references to any of the reaction names
         if ~isempty(strfind(m.Rules(rule).Rule, m.Reactions(react).Name))
             r_count = r_count + 1;
             r_refs{r_count} = m.Reactions(react).Name;
        end
    end
    %sort and flip array
    r_refs = flip(sort(r_refs));
    %disp(r_refs);
    rule_str = m.Rules(rule).Rule;
    %for each reaction reference in rule
    disp(['Old rule:', m.Rules(rule).Rule]);
    for ref=1:length(r_refs)
        ref_name = r_refs{ref};
        disp(ref_name);
        ref_react = sbioselect(m.Reactions, 'Name', ref_name);
        %add brackets around reaction rate formula
        ref_rate = ['(',ref_react.ReactionRate,')'];
        disp(ref_rate);
        %replace reaction name with reaction rate
        m.Rules(rule).Rule = strrep(m.Rules(rule).Rule, ref_name, ref_rate);
        %disp(rule_str);
    end
    %save new rule in the model
    %set(m.Rules(rule), 'Rule', rule_str);
    disp(['New rule:', m.Rules(rule).Rule]);
end  
%%

[t,p,snames] = sbiosimulate(m, optionObj);

rnames = cell(length(m.Reaction),1);
cnames = cell(length(m.Compartments),1);
cvals = cell(length(m.Compartments),1);

%assignin('caller', 'time', 1);

v = nan(length(t), length(m.Reaction));

for i=1:length(m.Reaction)
    rnames{i} =  m.Reaction(i).Name;
end

for i=1:(length(m.Compartments)) 
    cnames{i} = m.Compartment(i).Name;
    cvals{i} = m.Compartment(i).Capacity;
    name = m.Compartment(i).Name;
    name = strrep(name, ' ', '_');
    comp = Compartment;
    for j=1:(length(m.Compartments(i).Species))
        s_name = m.Compartments(i).Species(j).Name;
        s_val = m.Compartments(i).Species(j).initialAmount;
        comp.addprop(s_name);
        comp.(s_name) = s_val;
        comp.Capacity = m.Compartments(i).Capacity;
    end
    assignin('caller', name, comp);
end

for i=1:(length(m.Parameters)) 
    p_name = m.Parameter(i).Name;
    p_val = m.Parameter(i).Value;
    assignin('caller', p_name, p_val);
end

for j=1:length(snames)
    % BUG: does not work if a name contains a dot - legal in SBML)
    % FIXED! - introduced Compartment objects - NEEDS MORE TESTING
    name = snames{j};
    %below is needed to save variables from compartments to workspace
    name = strrep(name, '.', '_');
    assignin('caller', name, p(:,j));
end

for j=1:length(rnames)
    kl = m.Reaction(j).ReactionRate;
    %check that KineticLaw is not an empty object before proceeding
    if ~isempty(m.Reaction(j).KineticLaw)
        for params_count=1:length(m.Reaction(j).KineticLaw.Parameters)
            par_name = m.Reaction(j).KineticLaw.Parameters(params_count).Name;
            par_val = m.Reaction(j).KineticLaw.Parameters(params_count).Value;
            assignin('caller', par_name, par_val);
        end
    end
    kl = strrep(kl,'*','.*');
    kl = strrep(kl,'/','./');
    kl = strrep(kl,'^','.^');
    kl = strrep(kl,' ','_');
    display(kl);
    %setting time
    %NEED TO VALIDATE RESULTS TO CONFRM THAT THIS WORKS - AB
    if ~isempty(timeSymbolName)
        assignin('caller', timeSymbolName, t);
    end
    v(:,j) = evalin('caller', kl);
end

pmin = min(p,[],1);
pmax = max(p,[],1);
vmin = min(v,[],1);
vmax = max(v,[],1);

pmaxq = ones(size(p,1),1)*pmax;
vmaxq = ones(size(v,1),1)*max(vmax, -vmin);

%pnorm = (p-pminq)./(pmaxq-pminq);
pnorm = p./pmaxq;
pnorm(isnan(pnorm) & pmaxq) = 1;
pnorm(isnan(pnorm) & pmaxq==0) = 0;

%vnorm = (v-vminq)./(vmaxq-vminq);
vnorm = v./vmaxq;

vnorm(isnan(vnorm)&vmaxq) = 1;
vnorm(isnan(vnorm)& (vmaxq==0)) = 0;

%this should fix the issue with names being saved differently in this
%function and sivitLoadSBMLModel
%NOTE1: TEMPORARY FIX, MAY NOT WORK IN CASES WHERE dot IS A PART OF THE NAME
%NOTE2: should work now that name sanitization is in place
for sname=1:length(snames)
    parts = regexp(snames(sname), '[.]', 'split');
    %don't names that do not contain compartment
    cell2array = parts{1};
    if length(cell2array) == 2
        snames(sname) = parts{1}(2);
        display('Species output name sanitized!');
    elseif length(cell2array) == 1
        %do nothing
        %warning('No need to sanitize output species names...');
    else
        %HANDLE THIS PROPERLY IF THIS EVER OCCURS
        error('Attention: the name was split in more than 2 parts!!!')
    end
end

end