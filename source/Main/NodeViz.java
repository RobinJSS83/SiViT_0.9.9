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

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.geometry.Text2D;
import java.awt.Color;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import javax.media.j3d.Appearance;
import javax.media.j3d.Billboard;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Material;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.OrderedGroup;
import javax.media.j3d.RenderingAttributes;
import javax.vecmath.Vector3f;

public final class NodeViz extends BranchGroup {
    

    private Text2D text = null;
    private final Transform3D nodeXF;
    private final TransformGroup tg;
    private static final Bounds limes = new BoundingSphere(new Point3d(0, 0, 0), 100);
    // note: if the path is defined using / instead of \\, the material file cannot be read
    private static final String objDir = "..\\OBJ\\";
    private BranchGroup textBG;
    private TransparencyAttributes transparency;
    private Material material;
    private final String name;
    private Transform3D xfConcentrationGlyph;
    private TransformGroup tgConcentrationGlyph;
    private javax.media.j3d.Node pickableNode;
    
    public javax.media.j3d.Node getPickableNode() {
        return pickableNode;
    }
    
    NodeViz(String name) {
        setCapability(BranchGroup.ALLOW_DETACH);
        setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        this.name = name;
        
        tg = new TransformGroup();
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        
        nodeXF = new Transform3D();
        nodeXF.setScale(0.2);
        tg.setTransform(nodeXF);
        
        addTextTG(name, Color.GREEN);
        addModelTG(name);
        
        addConcentrationGlyphTG(Color.getHSBColor(0, 0, 0.5f));
        addChild(tg);
    }
    
    void addTextTG(String name, Color color) {
        textBG = new BranchGroup();
        textBG.setCapability(BranchGroup.ALLOW_DETACH);
        
        TransformGroup billboardTG = new TransformGroup();
        billboardTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Billboard billboard = new Billboard(billboardTG, Billboard.ROTATE_ABOUT_POINT, new Point3f());
        billboard.setSchedulingBounds(limes);
        
        
        TransformGroup textTG = new TransformGroup();
        textTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Transform3D textXF = new Transform3D();
        textXF.setTranslation(new Vector3d(0.2, 0, 1)); // the distance of the text from the object
        textTG.setTransform(textXF);
        textTG.setPickable(false);
        
        
        text = new Text2D(name, new Color3f(color), "Arial", 100, Font.PLAIN);
        

        textTG.addChild(text);
        
        billboardTG.addChild(textTG);
        textBG.addChild(billboard);
        textBG.addChild(billboardTG);
        
        tg.addChild(textBG);
    }
   
    private void addModelTG(String name) {
        int flags = ObjectFile.RESIZE | ObjectFile.TRIANGULATE;
        float creaseAngle = 60.0f;
        
        ObjectFile f = new ObjectFile(flags, (float) (creaseAngle * Math.PI / 180.0));
        Scene s = null;
        
        try {
            s = f.load(objDir + name + ".obj");
             TransformGroup modelTG;
            modelTG = new TransformGroup();
            modelTG.addChild(s.getSceneGroup());
            modelTG.setBoundsAutoCompute(true);
            Transform3D xf = new Transform3D();
            xf.setScale(1.0 / 3 / ((BoundingSphere) modelTG.getBounds()).getRadius());
            modelTG.setTransform(xf);
            modelTG.setPickable(false);
            tg.addChild(modelTG);
            
        } catch (FileNotFoundException e) {
            s = null;
        } catch (ParsingErrorException e) {
            System.err.println(e);
            s = null;
        } catch (IncorrectFormatException e) {
            System.err.println(e);
            s = null;
        }
    }
    
    private void addConcentrationGlyphTG(Color color) {
        Appearance appearance = new Appearance();
        material = new Material();
        material.setCapability(Material.ALLOW_COMPONENT_WRITE);
        material.setDiffuseColor(new Color3f(color));
        material.setLightingEnable(true);
        appearance.setMaterial(material);
        transparency = new TransparencyAttributes(TransparencyAttributes.NICEST, 0.2f);
        transparency.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
        appearance.setTransparencyAttributes(transparency);
       // app.setRenderingAttributes(new RenderingAttributes(true, true, 0, RenderingAttributes.ALWAYS));
        
        /* Original code */
        //Sphere o = new Sphere(0.35f, Primitive.GENERATE_NORMALS, 20, app);
        Sphere o;
        if(App.UI.isNewLighting())
            o = new Sphere(0.35f, Primitive.GENERATE_NORMALS, 40, appearance);
        else
            o = new Sphere(0.35f, Primitive.GENERATE_NORMALS, 20, appearance);
        
        pickableNode = o.getShape();
        pickableNode.setPickable(true);
        pickableNode.setName(name);
        
        tgConcentrationGlyph = new TransformGroup();
        tgConcentrationGlyph.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgConcentrationGlyph.addChild(o);
        xfConcentrationGlyph = new Transform3D();
        tgConcentrationGlyph.setTransform(xfConcentrationGlyph);
        
        tg.addChild(tgConcentrationGlyph);
    }
    
    public void setPosition(Vector3d pos) {
        nodeXF.setTranslation(pos);
        tg.setTransform(nodeXF);
    }
    
    public void setScale(double s) {
        try {
            xfConcentrationGlyph.setScale(s * 1.5 + 1);
            tgConcentrationGlyph.setTransform(xfConcentrationGlyph);
            
        } catch (javax.media.j3d.BadTransformException e) {
            System.err.println(e.getMessage() + ":" + name + " scale set to " + s);
        }
    }
    
    void setLabelVisible(boolean b) {
        if (b) {
            tg.addChild(textBG);
        } else {
            textBG.detach();
        }
    }
    
    private int hash(String in) {
        byte[] bs = in.getBytes();
        int r = 0;
        for (int i = 0; i < bs.length; i++) {
            r = (r ^ bs[i]);
        }
        return r; //0..127
    }

    
        
    void setHSV(double h, double s, double v) {
        /* Original code */
//        if (material != null) {
//            Color diffColor = Color.getHSBColor((float) h, (float) s, (float) v);
//            material.setDiffuseColor(new Color3f(diffColor));
//            material.setSpecularColor(new Color3f(Color.getHSBColor((float) h, (float) s, 1)));
//        }
        if (material != null) {
            Color diffColor = Color.getHSBColor((float) h, (float) s, (float) v);
            material.setDiffuseColor(new Color3f(diffColor));
            if(App.UI.isNewLighting())
                //material.setSpecularColor(new Color3f(diffColor));
                material.setSpecularColor(new Color3f(Color.getHSBColor((float) h, 0, 0)));
            else
                material.setSpecularColor(new Color3f(Color.getHSBColor((float) h, (float) s, 1)));
        }
    }
}