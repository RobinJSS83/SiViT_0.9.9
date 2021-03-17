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

import ForceGraph.*;
import java.util.*;
import javax.media.j3d.BranchGroup;
import javax.xml.transform.sax.TransformerHandler;
import matlabcontrol.MatlabInvocationException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public final class LinkChart {

    private final ArrayList<Node> nodes = new ArrayList<Node>();
    private final ArrayList links = new ArrayList();
    private final HashMap<String, Node> id2node = new HashMap<String, Node>();

    LinkChart(BranchGroup viz, Graph graph) throws MatlabInvocationException {
        ModelManager modelManager = App.getInstance().getModelManager();

        Node n;

        String speciesNames[] = modelManager.getSpeciesNames();
        String speciesIDs[] = modelManager.getSpeciesIDs();

      
        for (int i = 0; i < speciesNames.length; i++) {
            n = new Node(speciesNames[i], viz, graph);
            nodes.add(n);
            id2node.put(speciesIDs[i], n);

            n.lock();
        }

        String reactionNames[] = modelManager.getReactionNames();

        for (int i = 0; i < reactionNames.length; i++) {

            links.add(new MultiLink(reactionNames[i], viz, graph,
                    names2NodeArray(modelManager.getReactantNames(i)),
                    names2NodeArray(modelManager.getProductNames(i)),
                    names2NodeArray(modelManager.getModifierNames(i))));
        }

        for (int i = 0; i < nodes.size(); i++) {
            n = (Node) nodes.get(i);
            n.unlock();
        }
    }

    Node[] names2NodeArray(String[] names) {
        Node[] res = new Node[names.length];

        for (int i = 0; i < names.length; i++) {
            res[i] = id2node.get(names[i]);
        }

        return res;
    }

    void updatePosition() {
        Node n;
        for (int i = 0; i < nodes.size(); i++) {
            n = (Node) nodes.get(i);
            n.updatePosition();
        }
        MultiLink l;
        for (int j = 0; j < links.size(); j++) {
            l = (MultiLink) links.get(j);
            l.updatePosition();
        }
    }

    void updateSimData() {
        Node n;
        MultiLink l;
        ModelRun mr = App.getInstance().getExperimentModelRun();
        ModelRun mrControl = App.getInstance().getControlModelRun();
        for (int i = 0; i < nodes.size(); i++) {
            n = (Node) nodes.get(i);
            n.setConcentration(mr, mrControl);
        }

        for (int j = 0; j < links.size(); j++) {
            l = (MultiLink) links.get(j);
            l.setFlux(mr, mrControl);
        }
    }

    void savePositions(TransformerHandler hd) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        double[] pos;

        hd.startElement("", "", "nodes", atts);

        for (int i = 0; i < nodes.size(); i++) {
            atts.clear();
            atts.addAttribute("", "", "id", "CDATA", (nodes.get(i)).getSpeciesId());
            pos = nodes.get(i).getCoords();
            atts.addAttribute("", "", "x", "CDATA", Double.toString(pos[0]));
            atts.addAttribute("", "", "y", "CDATA", Double.toString(pos[1]));
            atts.addAttribute("", "", "z", "CDATA", Double.toString(pos[2]));
            hd.startElement("", "", "node", atts);
            hd.endElement("", "", "node");
        }
        hd.endElement("", "", "nodes");
    }

    void setNodePosition(String id, double x, double y, double z) {
        id2node.get(id).setCoords(new double[]{x, y, z});
    }
}
