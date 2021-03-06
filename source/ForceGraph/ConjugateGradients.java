package ForceGraph;

/* Class for conjugate gradient method. */
public class ConjugateGradients extends FirstOrderOptimizationProcedure {

    private double magnitudeOfPreviousGradientSquared;
    private double previousDescentDirection[][] = null;
    private double restartThreshold = 0;
    
    // Added by Andrei Boiko for dynamic layout implementation
    private boolean fullyOptimised = false;
    private final double optimisationLimit = 0.000001;

    public ConjugateGradients(Graph g, ForceModel fm, double acc) {
        super(g, fm, acc);
        restartThreshold = 0;
    }

    public ConjugateGradients(Graph g, ForceModel fm, double acc, double rt) {
        super(g, fm, acc);
        restartThreshold = rt;
    }

    @Override
    public void reset() {
        negativeGradient = null;
        descentDirection = null;
        fullyOptimised = false;
    }

    public boolean isFullyOptimised(){
        return fullyOptimised;
    }
    
    protected void computeDescentDirection() {
        int n = graph.getNumberOfVertices(), d = graph.getDimensions();
        double magnitudeOfCurrentGradientSquared = 0;
        if ((descentDirection == null) || (descentDirection.length != n)) {
            descentDirection = new double[n][d];
            previousDescentDirection = new double[n][d];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < d; j++) {
                    double temp = negativeGradient[i][j];
                    descentDirection[i][j] = temp;
                    magnitudeOfCurrentGradientSquared += square(temp);
                }
            }
        } else {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < d; j++) {
                    double temp = negativeGradient[i][j];
                    magnitudeOfCurrentGradientSquared += square(temp);
                }
            }
            if (magnitudeOfCurrentGradientSquared < optimisationLimit) {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < d; j++) {
                        previousDescentDirection[i][j] = 0;
                        descentDirection[i][j] = 0;
                    }
                }
                fullyOptimised = true;
                return;
            }
            double w = magnitudeOfCurrentGradientSquared / magnitudeOfPreviousGradientSquared;
            double dotProduct = 0, magnitudeOfDescentDirectionSquared = 0, m;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < d; j++) {
                    descentDirection[i][j] = negativeGradient[i][j] +
                            w * previousDescentDirection[i][j];
                    dotProduct += descentDirection[i][j] * negativeGradient[i][j];
                    magnitudeOfDescentDirectionSquared += square(descentDirection[i][j]);
                }
            }
            m = magnitudeOfCurrentGradientSquared * magnitudeOfDescentDirectionSquared;
            if (dotProduct / Math.sqrt(m) < restartThreshold) {
                descentDirection = null;
                computeDescentDirection();
                return;
            }
        }
        magnitudeOfPreviousGradientSquared = magnitudeOfCurrentGradientSquared;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                previousDescentDirection[i][j] = descentDirection[i][j];
            }
        }
    }
}
