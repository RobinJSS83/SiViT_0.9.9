package ForceGraph;

public class QuadraticInequalSpringLaw extends SpringLaw{

    public QuadraticInequalSpringLaw(Graph g, double k) {
        super(g, k);
    }

    double springAttraction(Edge e) {
        double r = Cell.sumOfRadii(e.getFrom(), e.getTo());
        double len = e.getLength();
       // System.out.println(e.getFrom().getLabel()+"->"+e.getTo().getLabel()+": "+e.getSet().getLengthModifier());
        return ((len - r) / preferredEdgeLength*e.getLengthModifier()*e.getFluxModifier());
    }
}
