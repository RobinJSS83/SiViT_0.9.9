package ForceGraph;

/**
 *
 * @author Andrei Boiko
 */
public class FlatPlaneConstraint extends Constraint {

    Graph graph = null;
    
    public FlatPlaneConstraint(Graph g) {
        super(g);
        graph = g;
    }
     
    @Override
    void apply(double[][] penalty) {
        int vCount = graph.getNumberOfVertices();
        for(int i = 0; i < vCount; i++){
            double[] coords = graph.getVertex(i).getCoords();
            coords[3] = 0;
            graph.getVertex(i).setCoords(coords);
        }
        
    }
}
