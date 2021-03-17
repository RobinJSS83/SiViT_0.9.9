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

classdef Compartment < dynamicprops
    %COMPARTMENT Class to help with reactions including compartmentalised species
       
    properties
        Capacity = 1;
    end
    
    methods
        function obj = Compartment()

        end
        function product = times(obj1, obj2)
            %check if first element is a Compartment object
            if isa(obj1,'Compartment')
                a = obj1.Capacity;
                b = obj2;
                product = a.*b;
            elseif isa(obj2,'Compartment')
                a = obj2.Capacity;
                b = obj1;
                product = b.*a;
            else
                a= obj1.Capacity;
                b= obj2.Capacity;
                product = a.*b;
            end
        end
        function product = rdivide(obj1, obj2)
            %check if first element is a Compartment object
            if isa(obj1,'Compartment')
                a = obj1.Capacity;
                b = obj2;
                product = a./b;
            elseif isa(obj2,'Compartment')
                a = obj2.Capacity;
                b = obj1;
                product = b./a;
            else
                a= obj1.Capacity;
                b= obj2.Capacity;
                product = a./b;
            end
        end
        function product = minus(obj1, obj2)
            %check if first element is a Compartment object
            if isa(obj1,'Compartment')
                a = obj1.Capacity;
                b = obj2;
                product = a-b;
            elseif isa(obj2,'Compartment')
                a = obj2.Capacity;
                b = obj1;
                product = b-a;
            else
                a= obj1.Capacity;
                b= obj2.Capacity;
                product = a-b;
            end
        end
        function product = plus(obj1, obj2)
            %check if first element is a Compartment object
            if isa(obj1,'Compartment')
                a = obj1.Capacity;
                b = obj2;
                product = a+b;
            elseif isa(obj2,'Compartment')
                a = obj2.Capacity;
                b = obj1;
                product = b+a;
            else
                a= obj1.Capacity;
                b= obj2.Capacity;
                product = a+b;
            end
        end
    end
end