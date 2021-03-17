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

import javax.media.j3d.*;
import javax.vecmath.Vector3d;
import ForceGraph.*;

public class Node {
    
    private double THRESHOLD_POS = 0.228;
    private double THRESHOLD_NEG = 0.261;
    
  // private final double THRESHOLD_POS = 0.0;
  // private final double THRESHOLD_NEG = 0.0;

    private Vertex vertex = null;
    private final NodeViz viz;
    private final String name;

    public Node(String n, BranchGroup bg, Graph g) {
        if(App.UI.isNewLighting()){
            this.THRESHOLD_NEG = 0.243;
            this.THRESHOLD_POS = 0.235;
        } else {
            this.THRESHOLD_NEG = 0.261;
            this.THRESHOLD_POS = 0.228;
        }
        name = n;

        vertex = g.insertVertex();
        viz = new NodeViz(n);
        viz.setPosition(new Vector3d(vertex.getCoords()));
        bg.addChild(this.viz);
        register();
    }

    public Vertex getVertex() {
        return vertex;
    }
    
    public NodeViz getViz(){
        return this.viz;
    }

    public double[] getCoords() {
        return vertex.getCoords();
    }

    public void setCoords(double[] c) {
        vertex.setCoords(c);
    }

    public void updatePosition() {
        viz.setPosition(new Vector3d(vertex.getCoords()));
    }

    void lock() {
        vertex.setFixed(true);
    }

    void unlock() {
        vertex.setFixed(false);
    }

    public boolean isLocked() {
        return vertex.isFixed();
    }

    void setLabelVisible(boolean b) {
        viz.setLabelVisible(b);
    }

    boolean isVisible() {
        return viz.isLive();
    }

    String getSpeciesId() {
        return name;
    }
    double p, pcontrol, pnorm, pmax, pmaxControl, c, hue;
    static final double k1 = 0.1, k2 = 0.6; // ramp coefficients for saturation stretch
    
    // NOTE to self
    // k1 is when colour should chnage to THRESHOLD value (originally start gradient here)
    // k2 is when the full colour is shown (keep as is)

    void setConcentration(ModelRun mr, ModelRun mrControl) {
        p = mr.getConcentration(name);
        pcontrol = mrControl.getConcentration(name);
        pnorm = mr.getNormalisedConcentration(name);
        pmax = mr.getMaxConcentration(name);
        pmaxControl = mrControl.getMaxConcentration(name);

        viz.setScale(pnorm);

        c = (pmax + pmaxControl) == 0 ? 0 : (p - pcontrol) / (pmax > pmaxControl ? pmax : pmaxControl);
        double orig_c = c;
        double t = 0; // current threshold
        if (c > 0) {
            hue = 0; // red
            t = this.THRESHOLD_POS;
        } else {
            hue = 0.6; // blue
            t = this.THRESHOLD_NEG;
            c = -c;
            // answer to question below... flop the c back to positive value!!!
        }

        // NOTE to self
        // this works only for positive c's 
        // so only red is affected? -> double check results of T1 (Experiment 1A)
        // checked -> works for both colours, but why???? (see above for answer!)
        
        if (c < k1) {
            c = 0;
        } else if (c > k2) {
            c = 1;
        } else {
            if(App.getInstance().isUseThresholds())
                c = t+(c-k1)/(k2-k1)*(1-t);
            else
                c = (c-k1)/(k2-k1);
        }
        
        if(App.colourOutput == 1 && App.getInstance().isShowOutput()){
                for(int i = 0; i < nodesToCheck.length; i++){
                    if(nodesToCheck[i].equals(name)){
                        //System.out.println("Node: " + name + "  R: " + diffColor.getRed() + ", G: " + diffColor.getGreen() + ", B: " + diffColor.getBlue());   
                        //System.out.println("Node: " + name + "  H: " + h + ", S: " + s + ", B: " + v);
                        //System.err.println(name + " c: " + orig_c);
                        System.out.println(name + " saturation: " + c + "  |  c: " + orig_c );
                    }
                } 
                //System.out.println("-------------------------------------------");
            }
        
        viz.setHSV(hue, c, 1);
    }
    
    private String[] nodesToCheck = {"ERK"};
    
    private void register() {
        App.getInstance().getNode2objMap().put(viz.getPickableNode(), this);
    }
}
