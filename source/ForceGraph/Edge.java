package ForceGraph;

/* Class for edges of a graph.  NOTE: the only mutable characteristics
of an edge are its label, directedness, and preferred length. */
public class Edge extends JiggleObject {

    private Vertex from, to; /* endpoints of the edge */
    private double lengthModifier = 1.0;
    private double fluxModifier = 1.0;

    public Edge(Graph g, Vertex f, Vertex t, double lm) {
        lengthModifier = lm;
        from = f;
        to = t;
        setContext(g);
    }

    public Vertex getFrom() {
        return from;
    }

    public Vertex getTo() {
        return to;
    }

    double getLengthSquared() {
        return Vertex.getDistanceSquared(from, to);
    }

    double getLength() {
        return Vertex.getDistance(from, to);
    }

    double getLengthModifier() {
        return lengthModifier;
    }
    
    public void setLengthModifier(double d) {
        lengthModifier = d;
    }
    
    public void setFluxModifier(double d){
        fluxModifier = lengthModifier*d;
    }

    double getFluxModifier() {
        return fluxModifier;
    }
}
