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

import ForceGraph.Edge;
import ForceGraph.Graph;
import ForceGraph.Vertex;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.media.j3d.BranchGroup;
import javax.vecmath.Color3f;

class MultiLink {

    private double THRESHOLD_POS = 0.228;
    private double THRESHOLD_NEG = 0.261;
    protected String name;
    private String formula;
    private List<Node> from, to, side;
    private ArrayList<Vertex> fromAux, toAux, sideAux;
    private Vertex midVertex;
    private ArrayList<LinkViz> pipes = new ArrayList<LinkViz>();
    protected final double length = 0.5;
    Color3f colour = new Color3f();
    Color3f color1 = new Color3f();

    public MultiLink(String name, BranchGroup viz, Graph g, Node[] f, Node[] t, Node[] s) {
        if (App.UI.isNewLighting()) {
            this.THRESHOLD_NEG = 0.243;
            this.THRESHOLD_POS = 0.235;
        } else {
            this.THRESHOLD_NEG = 0.261;
            this.THRESHOLD_POS = 0.228;
        }
        this.name = name;
        formula = App.getInstance().getModelManager().getReactionFormula(name);
        midVertex = g.insertVertex();

        this.setupScaffolding(g, f, from, fromAux, true);
        this.setupScaffolding(g, t, to, toAux, true);
        this.setupScaffolding(g, s, side, sideAux, false);

        for (LinkViz pipe : pipes) {
            viz.addChild(pipe);
            App.getInstance().getNode2objMap().put(pipe.getPickableNode(), this);
        }
    }

    public void updatePosition() {
        for (LinkViz pipe : pipes) {
            pipe.updatePath();
        }
    }
    double vnorm;
    double v;
    double vControl;
    double vmax;
    double vmaxControl;
    double vmin;
    double vminControl;
    double min, max;
    double saturation, hue;
    static final double k1 = 0.1, k2 = 0.6; // ramp coefficients for saturation stretch

    String[] linksToCheck = {
        //        "PI3Ka -> PI3K",
        //        "RasGTP -> RasGDP",
        "ERK -> ERKP", //        "E23HP -> E23H",
    //        "Rafa -> Raf",
    //        "PTEN -> PTENP",
    //        "ERKPP -> ERKP",
    //        "Akt_PI_P + PP2A -> Akt_PI_P_PP2A",
    //        "PIP3 + PTEN -> PTEN_PIP3",
    //        "MEKP -> MEKPP",
    //        "RasGDP -> RasGTP",
    //        "MEK_PP2A -> MEK + PP2A",
    //        "PTENP_PTEN -> PTEN_PTEN"
    };

    void setFlux(ModelRun mr, ModelRun mrControl) {
        vnorm = mr.getNormalisedFlux(name);
        v = mr.getFlux(name);
        vControl = mrControl.getFlux(name);

        vmax = mr.getMaxFlux(name);
        vmaxControl = mrControl.getMaxFlux(name);

        vmin = mr.getMinFlux(name);
        vminControl = mrControl.getMinFlux(name);

        min = (vmin < vminControl ? vmin : vminControl);
        max = (vmax > vmaxControl ? vmax : vmaxControl);

        saturation = (max - min) == 0 ? 0 : ((v - vControl) / (max - min) * ((v > vControl) ? 1 : -1));

        if (v >= 0 && vControl >= 0) {
            hue = v > vControl ? 0 : 0.6;
        } else if (v <= 0 && vControl <= 0) {
            hue = v > vControl ? 0.6 : 0;
        } else {
            hue = 0.8; // flux reverse
        }
        double t = 0; //current threshold

        if (hue == 0.6) {
            t = this.THRESHOLD_NEG;
        } else if (hue == 0) {
            t = this.THRESHOLD_POS;
        } else {
            t = 0;
        }

        if (saturation < k1) {
            saturation = 0;
        } else if (saturation > k2) {
            saturation = 1;
        } else if (App.getInstance().isUseThresholds()) {
            saturation = t + (saturation - k1) / (k2 - k1) * (1 - t);
        } else {
            saturation = (saturation - k1) / (k2 - k1);
        }

        Color c = Color.getHSBColor((float) hue, (float) saturation, 1);
        colour.set(c);

        if (App.colourOutput == 1 && App.getInstance().isShowOutput()) {
            for (int i = 0; i < linksToCheck.length; i++) {
                if (linksToCheck[i].equalsIgnoreCase(formula)) {
                    // System.out.println("Link: " + formula + "  R: " + c.getRed() + ", G: " + c.getGreen() + ", B: " + c.getBlue());   
//                        System.out.println("Link: " + formula + "  H: " + hue + ", S: " + saturation + ", B: " + 1.0f);
                    System.out.println(formula + " saturation: " + saturation + "  |  flux: " + v + "  |  control: " + vControl);
                }
            }
            //System.out.println("-------------------------------------------");
        }

        this.updatePipes(colour, vnorm > 0 ? vnorm : -vnorm, vnorm > 0);
    }

    public double lengthMod = 1.0;
            
    void updatePipes(Color3f color, double width, boolean dir) {
        //System.out.println(this.length);
        double dirMod = 1.0; // for positive change x, for negative 1/x
        
        if(hue == 0.6) { // blue
            dirMod = 1/(1+saturation*2);
        } else if (hue == 0.0) { // red
            dirMod = 1+saturation*4;
        } else {
            dirMod = 1;
        }
        
        this.lengthMod = dirMod;
        //this.lengthMod = 1 + width * 5;

        //System.out.println(this.formula + ", l-mod: " + lengthMod);
        color1.set(color);
        color1.scale(0.5f);

        for (LinkViz pipe : pipes) {
            if (dir) {
                pipe.setColor(color1, color);
            } else {
                pipe.setColor(color, color1);
            }
            pipe.setFlux(width);
        }

        if (App.getInstance().useDynamicLayout()) {
            for (Edge edge : edges) {
                edge.setFluxModifier(lengthMod);
            }
        }
    }

    String getReactionFormula() {
        return formula;
    }

    String getReactionId() {
        return name;
    }

    ArrayList<Edge> edges = new ArrayList<Edge>();

    private void setupScaffolding(Graph g, Node[] f, List<Node> mNodes, ArrayList<Vertex> mAux, boolean flBunch) {
        if (f == null) {
            mNodes = null;
            mAux = null;
            return;
        }

        mNodes = new ArrayList<Node>(Arrays.asList(f));
        mAux = new ArrayList<Vertex>(f.length);

        for (int i = 0; i < f.length; i++) {
            mAux.add(i, g.insertVertex());
            edges.add(g.insertEdge(mNodes.get(i).getVertex(), mAux.get(i), length / 2));
            edges.add(g.insertEdge(mAux.get(i), midVertex, length / 2));
            pipes.add(new LinkViz(new Vertex[]{mNodes.get(i).getVertex(), mAux.get(i), midVertex}));
            if (flBunch) {
                if (f.length > 1) {
                    if (i > 0) {
                        edges.add(g.insertEdge(mAux.get(i - 1), mAux.get(i), length / 6));
                    }
                    if (f.length > 2 && i == (f.length - 1)) {
                        //fixes f.length to f.length - 1
                        edges.add(g.insertEdge(mAux.get(f.length - 1), mAux.get(0), length / 6));
                    }
                }
            }
        }
    }
}
