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

function new_m = sanitizeNamesInModel( m )
%SANITIZENAMESINMODEL sanitize names of all elements of the model

% Compartments
disp('* Sanitizing compartment names');
for cmp=1:length(m.Compartments)
    sanitizeName(m.Compartments(cmp));
end
% Parameters
disp('* Sanitizing parameter names');
for par=1:length(m.Parameters)
    sanitizeName(m.Parameters(par));
end
% Species
disp('* Sanitizing species names');
for spc=1:length(m.Species)
    sanitizeName(m.Species(spc));
end
% Reactions
disp('* Sanitizing reaction names');
for rct=1:length(m.Reactions)
    sanitizeName(m.Reactions(rct));
    % Local Parameters in Reactions
    if ~isempty(m.Reactions(rct).KineticLaw)
        disp('** Sanitizing local parameter names');
        for lpar=1:length(m.Reactions(rct).KineticLaw.Parameters)
            sanitizeName(m.Reactions(rct).KineticLaw.Parameters(lpar));
        end
    end
end

new_m = m;

disp('   ');
end

