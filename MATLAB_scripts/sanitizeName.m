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

function [ new_name ] = sanitizeName( Obj )
%SANITIZENAME 
% removes all special characters from the name
% and renames the sbml object properly, including all the references
    wasChanged = 0;
    
    %illegal characters to be replaced 
    illegal_chars = {    '&', '/', '*', '+', '-', '.', ',', ' ', '(', ')', '{', '}', '[', ']', '<', '>', '''', '"'};
    %replacement characters for the above
    rep_chars =     {'_and_', '_', '_', '_', '_', '_', '_', '_', '' ,  '',  '',  '',  '',  '',  '',  '',  '_', '_'};
    %as it is illegal in matlab to start var names with digits, these
    %should be prefixed by a ligal character or string
    leading_digits = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    %prefix digits in names by this string
    digit_prefix = 'n_';
    
    %replace all illegal characters with replacements
    name = Obj.Name;
    orig_name = name;
    %display(strcat('Original Name: ', name));
    
    for char=1:length(illegal_chars)
        if ~isempty(strfind(name, illegal_chars{char}))
            disp(illegal_chars{char});
            disp(rep_chars{char});
            name = strrep(name, illegal_chars{char}, rep_chars{char});
            disp(['new name: ', name]);
            wasChanged = 1;
        end
    end
    %if the name starts with a digit, prefix it with legal character
    if ~isempty(name)
        %below is true if name(1) matches any of the characters in
        %leading_digits cell
        while (any(ismember(leading_digits, name(1))))
            name = [digit_prefix, name]; 
            wasChanged = 1;
        end
    end
    %if the resulting or original name has leading underscores, remove them
    %also make sure this is not an empty string before using while-loop
    if ~isempty(name)
        while (name(1) == '_')
            name(1) = '';
            wasChanged = 1;
        end
    end
    
    %make sure to only rename those fields that require renaming
    if wasChanged
       % disp(name);
        display(['   ', orig_name, '  -->  ', name]);
        rename(Obj, name);
    end  
end

