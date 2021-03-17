package ForceGraph;

import java.util.ArrayList;

/* Class for vertices of a graph. */
public class Vertex extends Cell {

    ArrayList inEdges = new ArrayList();
    ArrayList outEdges = new ArrayList();
    ArrayList inNeighbors = new ArrayList();
    ArrayList outNeighbors = new ArrayList();
    /* NOTE: the above are made package-accessible for reasons of
    efficiency.  They should NOT, however, be modified except by
    insertNeighbor and deleteNeighbor methods below. */
    private boolean fixed = false;
    private double repulsionModifier=1.0;

    public boolean isFixed() {
        return fixed;
    }

    public Vertex(Graph g) {
        super();
        setContext(g);
        setWeight(1);
        setDimensions(g.getDimensions());
    }

    public void setFixed(boolean f) {
        fixed = f;
    }

    void insertNeighbor(Edge e) {
        Vertex from = e.getFrom(), to = e.getTo();
        Vertex v = null;
        if (this == from) {
            v = to;
        } else if (this == to) {
            v = from;
        } else {
            throw new Error(e + " not incident to " + this);
        }
        if (this == from) {
            outEdges.add(e);
            outNeighbors.add(to);
        } else {
            inEdges.add(e);
            inNeighbors.add(from);
        }
    }

    void deleteNeighbor(Edge e) {
        Vertex from = e.getFrom(), to = e.getTo();
        Vertex v = null;
        if (this == from) {
            v = to;
        } else if (this == to) {
            v = from;
        } else {
            throw new Error(e + " not incident to " + this);
        }
        if (this == from) {
            outEdges.remove(e);
            outNeighbors.remove(to);
        } else {
            inEdges.remove(e);
            inNeighbors.remove(from);
        }
    }
    public void setRepulsionModifier(double d) {
        repulsionModifier = d;
    }

    public double getRepulsionModifier() {
        return repulsionModifier;
    }
}
