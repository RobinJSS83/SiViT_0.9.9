package ForceGraph;

import java.util.ArrayList;
import java.util.Iterator;

public class ForceModel {

    protected Graph graph = null;
    protected double preferredEdgeLength;
    private final ArrayList forceLaws = new ArrayList();
    private final ArrayList constraints = new ArrayList();

    public ForceModel(Graph g) {
        graph = g;
    }

    public double getPreferredEdgeLength() {
        return preferredEdgeLength;
    }

    public void setPreferredEdgeLength(double k) {
        preferredEdgeLength = k;
    }

    public void addForceLaw(ForceLaw fl) {
        forceLaws.add(fl);
    }

    public void removeForceLaw(ForceLaw fl) {
        forceLaws.remove(fl);
    }

    public void addConstraint(Constraint c) {
        constraints.add(c);
    }

    public void removeConstraint(Constraint c) {
        constraints.remove(c);
    }

    void getNegativeGradient(double[][] negativeGradient) {
        int d = graph.getDimensions();
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < d; j++) {
                negativeGradient[i][j] = 0;
            }
            ((Vertex) graph.vertices.get(i)).intField = i;
        }
        for (Iterator en = forceLaws.iterator(); en.hasNext();) {
            ((ForceLaw) (en.next())).apply(negativeGradient);
        }
    }

    void getPenaltyVector(double[][] penaltyVector) {
        int d = graph.getDimensions();
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < d; j++) {
                penaltyVector[i][j] = 0;
            }
            ((Vertex) graph.vertices.get(i)).intField = i;
        }
        for (Iterator en = constraints.iterator(); en.hasNext();) {
            ((Constraint) (en.next())).apply(penaltyVector);
        }
    }
}
