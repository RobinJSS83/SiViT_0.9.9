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

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;

import com.sun.j3d.utils.behaviors.interpolators.KBKeyFrame;
import com.sun.j3d.utils.behaviors.interpolators.KBRotPosScaleSplinePathInterpolator;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Shape3D;
import ForceGraph.Vertex;
import javax.media.j3d.OrderedGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Tuple3d;

public class LinkViz extends OrderedGroup {

    protected KBKeyFrame[] splineKeyFrames;
    protected float length; //length of curve, in meters
    protected Color3f lineColour1, lineColour2;
    protected int nLineSegments = 20;
    protected LineStripArray line;
    protected KBRotPosScaleSplinePathInterpolator interpolator;
    protected LineAttributes lineAttributes;
    protected Appearance lineAppearance;
    private javax.media.j3d.Node pickableNode;
    protected Vertex[] vertices;
    protected Color3f vertexColour = new Color3f();

    LinkViz(Vertex[] vertices) {
        super();
        this.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        this.setCapability(Group.ALLOW_CHILDREN_WRITE);
        this.vertices = vertices;
        
        setupKeyFrames();

        interpolator = new KBRotPosScaleSplinePathInterpolator(null, null, new Transform3D(), splineKeyFrames);

        
        createLine();
        updateLinePath();
    }

    public javax.media.j3d.Node getPickableNode() {
        return pickableNode;
    }
        
    public void updatePath() {
        setupKeyFrames();
        interpolator.setKeyFrames(splineKeyFrames);
        updateLinePath();
    }

    public void setFlux(double flux) {
        lineAttributes.setLineWidth((float) flux * 5 + 4);
    }

    final public void setColor(Color3f cStart, Color3f cEnd) {

        lineColour1 = cStart;
        lineColour2 = cEnd;

        for (int i = 0; i < nLineSegments; i++) {
            vertexColour.interpolate(lineColour1, lineColour2, (float) i / nLineSegments);
            line.setColor(i, vertexColour);
        }
    }

    private void createLine() {
        int[] stripData = {nLineSegments};

        line = new LineStripArray(nLineSegments, GeometryArray.COORDINATES | GeometryArray.COLOR_3, stripData);

        line.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        line.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
        line.setCapability(GeometryArray.ALLOW_COLOR_WRITE);
        Shape3D s = new Shape3D(line);

        pickableNode = s;
        s.setPickable(true);

        lineAppearance = new Appearance();
        //lineAppearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
        
        //TransparencyAttributes transparency = new TransparencyAttributes(TransparencyAttributes.NICEST, 0.2f);
        //transparency.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
        
       // lineAppearance.setTransparencyAttributes(transparency);
        
        lineAttributes = new LineAttributes(1, LineAttributes.PATTERN_SOLID, true);
        lineAttributes.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);
        lineAppearance.setLineAttributes(lineAttributes);
        
        s.setAppearance(lineAppearance);
        
        //insertChild(s, 1);
        addChild(s);
    }
    
    private static final float head = 0, pitch = 0, bank = 0, tension = 0, continuity = 0, bias = 0;
    private static final Point3f scale = new Point3f(1.0f, 1.0f, 1.0f); // uniform scale
    private final Tuple3d p3dPosition = new Tuple3d(){};

    private void setupKeyFrames() {
        if (splineKeyFrames == null || splineKeyFrames.length != vertices.length) {
            splineKeyFrames = new KBKeyFrame[vertices.length];
        }

        for (int i = 0; i < vertices.length; i++) {

            if (splineKeyFrames[i] == null) {
                splineKeyFrames[i] = new KBKeyFrame((float) i / (vertices.length - 1), 0, new Point3f(), head, pitch, bank, scale, tension, continuity, bias);
            }
            p3dPosition.set(vertices[i].getCoords());
            splineKeyFrames[i].position.set(p3dPosition);
        }
    }
    // these are used only in the following function, but to save on allocations were extruded out
    private final Vector3f pos = new Vector3f(), prev = new Vector3f();
    private float newLength = 0;
    private final float[] to = {0, 0, 0};
    private final Transform3D xf = new Transform3D();

    private void updateLinePath() { // also updates line length
        newLength = 0;
        for (int i = 0; i < nLineSegments; i++) {
            interpolator.computeTransform((float) i / (nLineSegments - 1), xf);
            xf.get(pos);
            pos.get(to);

            line.setCoordinate(i, to);

            if (i > 0) {
                prev.sub(pos);
                newLength += prev.length();
            }
            prev.set(pos);
        }
        length = newLength;
    }
}
