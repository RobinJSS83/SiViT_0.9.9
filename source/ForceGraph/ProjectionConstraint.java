package ForceGraph;

public class ProjectionConstraint extends Constraint {

    private int dimensions = 0;

    public ProjectionConstraint(Graph g, int d) {
        super(g);
        dimensions = d;
    }

    void apply(double[][] penalty) {
        int d = graph.getDimensions();
        double[] coords;
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            coords = ((Vertex) graph.vertices.get(i)).getCoords();
            for (int j = dimensions; j < d; j++) {
                penalty[i][j] += (-coords[j]);
            }
        }
    }
}

