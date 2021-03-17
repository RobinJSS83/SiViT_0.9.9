/*
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
*/

package Utils;

import javax.swing.DefaultComboBoxModel;

public class SortableComboBoxModel extends DefaultComboBoxModel {
     private static final long serialVersionUID = -2876936283403221384L;
     @Override
     public void addElement(Object object) {
          int size=getSize();
          for(int i=0;i<size;i++)
               if(((String)getElementAt(i)).compareTo(object.toString())>0) {
                    super.insertElementAt(object, i);
                    return;
               }
          super.addElement(object);
     }
     
     public void justAddAnElement(Object object) {
          super.addElement(object);
     }
     
     @Override
     public void insertElementAt(Object anObject, int index) {
          addElement(anObject);
     }
}