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

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.behaviors.vp.ViewPlatformBehavior;
import com.sun.j3d.utils.pickfast.PickCanvas;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.*;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GraphicsContext3D;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.PickInfo;
import javax.media.j3d.Raster;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class DisplayManager3D extends MouseAdapter implements MouseMotionListener{

    static boolean application = false; // by default, loaded as an applet
    private final BranchGroup graphBG = new BranchGroup();
    private TransformGroup tg = new TransformGroup();
    private final SimpleUniverse u;
    private final PickCanvas pick;
    private PickInfo pickInfo;
    private final Canvas3D canvas3D;
    public OrbitBehavior vpb;
    private BranchGroup bgbg;
    private Background bg;
    private final BoundingSphere limes;
    private final Point clickPos = new Point();
    private BranchGroup lbg;//lights
    boolean constrainTo2D = false;

    public DisplayManager3D(Container canvas) {
        canvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());

        canvas3D.setMaximumSize(new Dimension(1000, 1000));
        canvas3D.setMinimumSize(new Dimension(200, 200));
        canvas3D.setPreferredSize(new Dimension(800, 800));

        limes = new BoundingSphere(new Point3d(0, 0, 0), 100);

        canvas.setLayout(new BorderLayout());
        canvas.add(canvas3D, BorderLayout.CENTER);
        
        canvas3D.addMouseMotionListener(this);
        canvas.validate();
        u = new SimpleUniverse(canvas3D);
        canvas3D.getView().setBackClipDistance(60);
        canvas3D.getView().setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);
        //System.out.println("FOV: " + canvas3D.getView().getFieldOfView());
        canvas3D.getView().setFieldOfView(Math.toRadians(60));
        
        u.getViewingPlatform().setNominalViewingTransform();
        
        lbg = makeLights();
        u.addBranchGraph(lbg);
        
        u.getViewingPlatform().setViewPlatformBehavior(makeBehaviour());
        
        u.addBranchGraph(makeBackground());
        
        graphBG.setCapability(BranchGroup.ALLOW_DETACH);
        graphBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        graphBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        pick = new PickCanvas(u.getCanvas(), graphBG);
        pick.setMode(PickInfo.PICK_GEOMETRY);
        pick.setFlags(PickInfo.NODE | PickInfo.CLOSEST_INTERSECTION_POINT);
        pick.setTolerance(0.1f);

        //this.setZoomLevel(-20);
        u.getCanvas().addMouseListener(this);        
    }

    public BranchGroup getLinkChartViz() {
        return graphBG;
    }

    
    private ViewPlatformBehavior makeBehaviour() {
        vpb = new OrbitBehavior(this.canvas3D, OrbitBehavior.STOP_ZOOM);
        vpb.setSchedulingBounds(limes);
        vpb.setReverseTranslate(true);
        vpb.setReverseRotate(true);
        // changed this to false as participants complained about the inverted zoom
        // don't know why it was true by defaut????
        vpb.setReverseZoom(false);
        vpb.setMinRadius(5);

        return vpb;
    }
    
    public void setZoomLevel(double d){
        Transform3D t = new Transform3D();
        Transform3D trans = new Transform3D();
        
        u.getViewingPlatform().getViewPlatformTransform().getTransform(t);
        Vector3d position = new Vector3d();
        trans.get(position);
        //System.out.println("Current zoom level: " + position.z);
        trans.set(new Vector3d (0, 0, d));
        trans.get(position);
        //System.out.println("New zoom level: " + position.z);
        t.mul(trans); // translate
        t.normalize();

        u.getViewingPlatform().getViewPlatformTransform().setTransform(t);
    }
     
    private void rotateZ(double value){
        Transform3D t = new Transform3D();
        Transform3D rot = new Transform3D();
        
        u.getViewingPlatform().getViewPlatformTransform().getTransform(t);
        rot.rotZ(value); // calculate rotation matrix
        t.mul(rot); // rotate
        t.normalize();

        u.getViewingPlatform().getViewPlatformTransform().setTransform(t);
    }

    public void showGraph() {
        tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg.addChild(graphBG);
        BranchGroup root = new BranchGroup();
        root.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        root.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        root.addChild(tg);
        u.addBranchGraph(root);
    }

    public void removeGraph() {
        graphBG.detach();
        graphBG.removeAllChildren();
    }

    private BranchGroup makeLights() {
     BranchGroup l = new BranchGroup();
     l.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
     l.setCapability(BranchGroup.ALLOW_CHILDREN_READ);     
     
        if (App.UI.isNewLighting()) {
            DirectionalLight dl = new DirectionalLight(new Color3f(Color.WHITE), new Vector3f(0, 0, -1f));
            dl.setCapability(DirectionalLight.ALLOW_DIRECTION_WRITE);
            dl.setInfluencingBounds(limes);
            l.addChild(dl);
            
        } else {
            DirectionalLight dl = new DirectionalLight(new Color3f(Color.WHITE), new Vector3f(-1f, -1f, -1f));
            dl.setInfluencingBounds(limes);
            l.addChild(dl);

            dl = new DirectionalLight(new Color3f(Color.WHITE), new Vector3f(1f, 1f, 1f));
            dl.setInfluencingBounds(limes);
            l.addChild(dl);

            AmbientLight al = new AmbientLight(new Color3f(Color.WHITE));
            al.setInfluencingBounds(limes);
            l.addChild(al);
        }

        return l;
    }

    public Canvas3D getCanvas() {
        return u.getCanvas();
    }

    void resetView() {
        u.getViewingPlatform().setNominalViewingTransform();
        this.setZoomLevel(20);
    }

    private BranchGroup makeBackground() {
        bgbg = new BranchGroup();
        bg = new Background(0f, 0f, 0f);
        bg.setBounds(limes);
        bg.setApplicationBounds(limes);
        bgbg.addChild(bg);
        
        return bgbg;
    }

    private boolean mouse1down = false;
    private int mouseStartX = 0;
    
    @Override
    public void mousePressed(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            showContextMenu(evt);   
        }
        if (evt.getButton() == MouseEvent.BUTTON1){
            mouse1down = true;
            mouseDeltaX = 0;
            mouseStartX = evt.getX();
        }
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            showContextMenu(evt);
        }
        
        if (evt.getButton() == MouseEvent.BUTTON1)
            mouse1down = false;
        
    }
   
    private int mouseDeltaX = 0;
    
       
    @Override 
    public void mouseDragged(MouseEvent evt){
        if(mouse1down && constrainTo2D){
            mouseDeltaX = evt.getX() - mouseStartX;
           // System.out.println(mouseDeltaY);
            this.rotateZ(((double)mouseDeltaX)*0.005);
            mouseStartX = evt.getX();
        }
    } 

    void showContextMenu(MouseEvent evt) {
        clickPos.x = evt.getX();
        clickPos.y = evt.getY();

        pick.setShapeLocation(evt);
        pick.setTolerance(10);
        pickInfo = pick.pickClosest();

        if (pickInfo != null) {
            javax.media.j3d.Node n = pickInfo.getNode();
            App.getInstance().showContextMenu(n, evt);
            
            //System.out.println("");
        }
    }

    
    //Vector3f vec = new Vector3f();
    void setCameraView(boolean b) {
        vpb.setEnable(b);           
    }
   // Vector3f old_vec = (Vector3f)vec.clone();
    
    boolean vecEquals(Vector3f v1, Vector3f v2){
        if(v1.x == v2.x && v1.y == v2.y && v1.z == v2.z){
            return true;
        }
        return false;
    }
    
    Transform3D t3d = new Transform3D();
    Matrix3f mat = new Matrix3f();
    Matrix3f oldMat = new Matrix3f();
    
    void updateLight(){
        u.getViewingPlatform().getViewPlatformTransform().getTransform(t3d);           
        t3d.get(mat);

        Vector3f dir = new Vector3f(0, 0, -1);
        
        if (App.UI.isNewLighting() && !mat.equals(oldMat)) {
            dir = mat_by_vec(mat, dir);
            dir.normalize();
            //System.out.println(dir);
           ((DirectionalLight)lbg.getChild(0)).setDirection(dir);          
        }
        
        oldMat = (Matrix3f)mat.clone();
    }
    
    Vector3f mat_by_vec(Matrix3f m, Vector3f v){
        float x = m.m00*v.x+m.m01*v.y+m.m02*v.z;
        float y = m.m10*v.x+m.m11*v.y+m.m12*v.z;
        float z = m.m20*v.x+m.m21*v.y+m.m22*v.z;
        
        return new Vector3f(x, y, z);        
    }

    void capture(String absolutePath) {
        GraphicsContext3D ctx = canvas3D.getGraphicsContext3D();
        
        // The raster components need to be set!
        Raster ras = new Raster(
                new Point3f(-1.0f, -1.0f, -1.0f),
                Raster.RASTER_COLOR, 0, 0, canvas3D.getWidth(), canvas3D.getHeight(),
                new ImageComponent2D(ImageComponent.FORMAT_RGB, new BufferedImage(canvas3D.getWidth(), canvas3D.getHeight(), BufferedImage.TYPE_INT_RGB)),
                null);
        
        ctx.readRaster(ras);
        
        BufferedImage img = ras.getImage().getImage();
        
        

        try {
            ImageIO.write(img, "jpg", new File(absolutePath+".jpg"));
        } catch (Exception e) {

        }

    }
    
    double demoX, demoY, demoZ = 0;
    int state = 1;
    double x = 0, y = 0, z = 0;
    
    void demoAnimation(double dt) {
//        Transform3D t = new Transform3D();
//        Transform3D rot = new Transform3D();
//        
//        //u.getViewingPlatform().getViewPlatformTransform().getTransform(t);
//        
//        
//        switch(state){
//            case 1: 
//                rot.rotZ(0.005); 
//                //rot.rotX(-0.005);
//        }
//        
//       // pos.x = pos.x*Math.cos(0.005) - pos.z*Math.sin(0.005);
//       // pos.z = pos.z*Math.cos(0.005) + pos.x*Math.sin(0.005);     
//     
//        
//       // t.lookAt(pos, new Point3d(0, 0, 0), new Vector3d(0,1,0));
//        t.mul(rot); // rotate
//        
//        t.normalize();
        
        //tg.setTransform(t);
        
        Transform3D tx = new Transform3D();
        Transform3D ty = new Transform3D();
        Transform3D tz = new Transform3D();

        Transform3D tc = new Transform3D();

        double x = 0, y = 0, z = 0;
        x = Math.PI * this.x / 180 * dt;
        y = Math.PI * this.y / 180 * dt;
        z = Math.PI * this.z / 180 * dt;

        tx.rotX(x);
        tc.mul(tx);

        ty.rotY(y);
        tc.mul(ty);

        tz.rotZ(z);     
        tc.mul(tz);
        
        tg.setTransform(tc);
        
        
        if(state == 1){
            this.z+=0.25;
            this.y+=0.25;
            counter++;
        } else if (state == 3){
            this.x+=0.25;
            this.y+=0.25;
            counter++;
        } else if (state == 5){
            this.x+=0.25;
            this.z+=0.25;
            counter++;
        } else if(state == 2){
            this.z-=0.25;
            this.y-=0.25;
            counter++;
        } else if (state == 4){
            this.x-=0.25;
            this.y-=0.25;
            counter++;
        } else if (state == 6){
            this.x-=0.25;
            this.z-=0.25;
            counter++;
        }
        
        if(counter == 500){
            counter = 0;
            state++;
        }
        
        if(state >= 7){
            state = 1;
        }
        
    }
    
    int counter = 0;
}
