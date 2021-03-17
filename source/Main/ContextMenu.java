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

package Main;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

/** custom context menu for displaying charts **/


public class ContextMenu extends JPopupMenu {
    public static final int TYPE_NODE = 1;
    public static final int TYPE_EDGE = 2;

    JLabel jlName = new JLabel();
    LineChartPanel g = new LineChartPanel();      

    public ContextMenu() {
        super();
        setLightWeightPopupEnabled(false);
        jlName.setFont(g.getFont());
        add(jlName);
        // removed for experiment
        addSeparator();
        add(g);
       
    }

    public void refreshMenuItems(double simTime, double maxTime, String name, double[] t1, double[] d1, double[] t2, double[] d2, int type) {
        jlName.setText(name.replace("->", "\u2192"));
        g.setData(t1, d1, t2, d2, type);
        g.setTime(simTime, maxTime);
    }
}