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

import ForceGraph.ConjugateGradients;
import ForceGraph.Constraint;
import ForceGraph.Edge;
import ForceGraph.FirstOrderOptimizationProcedure;
import ForceGraph.ForceModel;
import ForceGraph.Graph;
import ForceGraph.HybridVertexVertexRepulsionLaw;
import ForceGraph.InverseSquareVertexVertexRepulsionLaw;
import ForceGraph.LinearSpringLaw;
import ForceGraph.PointConstraint;
import ForceGraph.ProjectionConstraint;
import ForceGraph.QuadraticInequalSpringLaw;
import ForceGraph.QuadraticSpringLaw;
import ForceGraph.SpringLaw;
import ForceGraph.SurfaceOfSphereConstraint;
import ForceGraph.Vertex;
import ForceGraph.VertexVertexRepulsionLaw;

public class LayoutManager {

    public ForceModel forceModel = null;
    public ConjugateGradients optimizationProcedure = null;
    private final Graph graph = new Graph();
    private boolean constrainToSphere = false;
    private final Constraint surfaceOfSphereConstraint;
    private final Constraint planeConstraint;
    private boolean constrainTo2D = false;
    private final PointConstraint nucleusConstraint;
    private boolean constrainToNucleus = false;
    // the following are advanced settings and should not be changed by a novice user
    // ... said Mark and Andrei changed it anyway
    private final double edgeLength = 0.08;
    private final double accuracyOfLineSearch = 0.5;//0.5;
    private final double restartThreshold = 0.2;//0.2;
    
    public LayoutManager() {
        SpringLaw spring_law = new QuadraticInequalSpringLaw(graph, edgeLength);
        //SpringLaw spring_law = new LinearSpringLaw(graph, edgeLength
        //SpringLaw spring_law = new QuadraticSpringLaw(graph, edgeLength) {};
        surfaceOfSphereConstraint = new SurfaceOfSphereConstraint(graph);
        planeConstraint = new ProjectionConstraint(graph, 2);
        nucleusConstraint = new PointConstraint(graph);

        VertexVertexRepulsionLaw vvRepulsionLaw = new InverseSquareVertexVertexRepulsionLaw(graph, edgeLength);
        //VertexVertexRepulsionLaw vvRepulsionLaw = new HybridVertexVertexRepulsionLaw(graph, edgeLength);

        forceModel = new ForceModel(graph);
        forceModel.addForceLaw(spring_law);
        forceModel.addForceLaw(vvRepulsionLaw);

        if (constrainToSphere) {
            forceModel.addConstraint(surfaceOfSphereConstraint);
        }

        if (constrainTo2D) {
            forceModel.addConstraint(planeConstraint);
        }
        if (constrainToNucleus) {
            forceModel.addConstraint(nucleusConstraint);
        }
        optimizationProcedure = new ConjugateGradients(graph, forceModel, accuracyOfLineSearch, restartThreshold);

        optimizationProcedure.setConstrained(constrainTo2D || constrainToSphere || constrainToNucleus);
        optimizationProcedure.reset();
    }

    public Graph getGraph() {
        return graph;
    }

    public boolean isConstrainToNucleus() {
        return constrainToNucleus;
    }

    public void setConstrainToNucleus(boolean constrainToNucleus) {
        this.constrainToNucleus = constrainToNucleus;
        if (forceModel != null) {
            if (constrainToNucleus) {
                forceModel.addConstraint(nucleusConstraint);
            } else {
                forceModel.removeConstraint(nucleusConstraint);
            }
            optimizationProcedure.setConstrained(constrainTo2D || constrainToSphere || constrainToNucleus);
            resetOptimisation();
        }
    }

    public boolean isConstrainTo2D() {
        return constrainTo2D;
    }

    public void setConstrainTo2D(boolean constrainTo2D) {
        this.constrainTo2D = constrainTo2D;
        if (forceModel != null) {
            if (constrainTo2D) {
                forceModel.addConstraint(planeConstraint);
            } else {
                forceModel.removeConstraint(planeConstraint);
            }
            optimizationProcedure.setConstrained(constrainTo2D || constrainToSphere || constrainToNucleus);
            resetOptimisation();
        }
    }

    public boolean isConstrainToSphere() {
        return constrainToSphere;
    }

    public void setConstrainToSphere(boolean constrainToSphere) {
        this.constrainToSphere = constrainToSphere;
        if (forceModel != null) {
            if (constrainToSphere) {
                forceModel.addConstraint(surfaceOfSphereConstraint);
            } else {
                forceModel.removeConstraint(surfaceOfSphereConstraint);
            }
            optimizationProcedure.setConstrained(constrainTo2D || constrainToSphere || constrainToNucleus);
            resetOptimisation();
        }
    }

    void scrambleGraph() {
        if (graph == null) {
            return;
        }

        int d = graph.getDimensions();
        double coords[];

        //randomise
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            coords = ((Vertex) (graph.vertices.get(i))).getCoords();
            for (int j = 0; j < d; j++) {
                if (!((Vertex) (graph.vertices.get(i))).isFixed()) {
                    coords[j] = (Math.random() * 2 - 1) * 0.4;
                }
            }
        }
        resetOptimisation();
    }
    
    public void resetOptimisation(){
        if (optimizationProcedure != null) {
            optimizationProcedure.reset();
        }
    }

    void recentre() {
        double coords[];

        double sumX = 0, sumY = 0, sumZ = 0;

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            coords = ((Vertex) (graph.vertices.get(i))).getCoords();
            sumX += coords[0];
            sumY += coords[1];
            sumZ += coords[2];
        }
        //recentre 
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            coords = ((Vertex) graph.vertices.get(i)).getCoords();
            coords[0] -= (sumX / graph.getNumberOfVertices());
            coords[1] -= (sumY / graph.getNumberOfVertices());
            coords[2] -= (sumZ / graph.getNumberOfVertices());
        }
    }

    void improveGraph() {
        try {
            optimizationProcedure.improveGraph();
        } catch (NullPointerException ex) {
            
        }
    }

    void clear() {
      while(graph.getNumberOfVertices()>0) {
            graph.deleteVertex((Vertex) (graph.vertices.get(0)));
        }
      while(graph.getNumberOfEdges()>0) {
            graph.deleteEdge((Edge) (graph.edges.get(0)));
        }
    }

    boolean isGraphFullyOptimised() {
        return this.optimizationProcedure.isFullyOptimised();
    }
}
