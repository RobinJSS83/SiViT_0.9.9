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

function [configsetObj] = setMaxSimTime( model, maxSimTime )
    configsetObj = getconfigset(model,'active');
    %disp('current config');
    %get(configsetObj);
    %disp('setting new config');
    set(configsetObj, 'StopTime', maxSimTime);
    %set(configsetObj, 'SolverOptions.MaxStep', 0.1);
    %disp('new config');
    %get(configsetObj);
end