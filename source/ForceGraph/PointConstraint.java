package ForceGraph;

import java.util.List;

public class PointConstraint extends Constraint {
    private List<Vertex> vlist;
    private final double[] centre;

    public PointConstraint(Graph g) {
        super(g);
        centre = new double[graph.getDimensions()];
        for (int i = 0; i < graph.getDimensions(); i++) {
            centre[i]=0;
        }
    }

    public void addVertex(Vertex v) {
        vlist.add(v);
    }

    public void apply(double[][] penalty) {
        int n = vlist.size(), d = graph.getDimensions();
        double[] sum = new double[d], coords;
        double distanceSquared;

        for (int i = 0; i < n; i++) {
            coords = ((Vertex) vlist.get(i)).getCoords();
            distanceSquared = 0;
            for (int j = 0; j < d; j++) {
                distanceSquared += square(coords[j] - centre[j]);
            }
            double p = Math.sqrt(distanceSquared);
            for (int j = 0; j < d; j++) {
                penalty[i][j] += p * (coords[j] - centre[j]);
                sum[j] += p * (coords[j] - centre[j]);
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                penalty[i][j] -= sum[j] / n;
            }
        }
    }
}
