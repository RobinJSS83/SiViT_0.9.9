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

function sivit = sivitLoadSBMLModel( fname )
%SIVITLOADSBMLMODEL load SBML model from file and prepare it for SiViT

%disp('Importing SBML model');
%sivit.m = sbmlimport(fname);
%
[sivit.m, sivit.time_symbol] = sbmlimportFunReplace(fname);

%sivit.m = sbmlimportFunExternal(fname);

%moved the following line sbmlImportFunReplace
%sivit.m = sanitizeNamesInModel(sivit.m);

disp(sivit.m);
sivit.speciesNames = cell(length(sivit.m.Species),1);
sivit.speciesIds = cell(length(sivit.m.Species),1);
for i=1:length(sivit.m.Species)
    sivit.speciesNames{i} = sivit.m.Species(i).Name;
    sivit.speciesIds{i} = sivit.m.Species(i).Id;
end

sivit.reactionNames = cell(length(sivit.m.Reactions),1);
sivit.reactionFormulae = cell(length(sivit.m.Reactions),1);
sivit.reactionReactants = cell(length(sivit.m.Reactions),1);
sivit.reactionProducts = cell(length(sivit.m.Reactions),1);
sivit.reactionModifiers = cell(length(sivit.m.Reactions),1);

for i=1:length(sivit.m.Reactions)
    sivit.reactionNames{i} = sivit.m.Reactions(i).Name;
    sivit.reactionFormulae{i} = sivit.m.Reactions(i).Reaction;
    
    sivit.reactionReactants{i} = species2String(sivit.m.Reactions(i).Reactants);
    sivit.reactionProducts{i} = species2String(sivit.m.Reactions(i).Products);
    sivit.reactionModifiers{i} = getModifiers(sivit.m, sivit.m.Reactions(i));
end

sivit.parameterNames = cell(length(sivit.m.Parameters),1);

for i=1:length(sivit.m.Parameters)
    %disp('Original parameters');
    %disp(sivit.m.Parameters(i).ConstantValue);
    
    %make sure constant is set to false  
    % toensure compatibility with SBML level 1 version 1 models
    set(sivit.m.Parameters(i), 'ConstantValue', false);
    sivit.parameterNames{i} = sivit.m.Parameters(i).Name;
    
    %disp('New parameters');
    %disp(sivit.m.Parameters(i).ConstantValue);
end

end

function res = species2String( c )
res = cell(c.length,1);
for i=1:length(c)
    res{i} = c(i).Id;
end
end

function modifiers = getModifiers(m, r)
%disp(r.Reaction);
modifiers = {};
allUsed = r.parserate;
for n = allUsed
    %if it's a species, but not a reactant or a product - add it to the list
    %of modifiers - used later for visualisation of catalysts or inhibitors
    isModifier = 1;
    for i = 1:length(r.Reactants)
        if strcmp(r.Reactants(i).Name,n)
            %disp(strcat(n,': reactant'))
            isModifier = 0;
            break;
        end
    end
    if ~isModifier, continue; end
    for i = 1:length(r.Products)
        if strcmp(r.Products(i).Name,n)
            %disp(strcat(n,': product'))
            isModifier = 0;
            break;
        end
    end
    if ~isModifier, continue; end
    %isModifier = 0;
    for i = 1:length(m.Species)
        if strcmp(m.Species(i).Name,n)
            %disp(strcat(n,': modifier'))
            modifiers{end+1} = m.Species(i).Id;
            %isModifier = 1;
            break;
        end
    end 
    %if ~isModifier,  disp(strcat(n,': parameter')); end
end
end
