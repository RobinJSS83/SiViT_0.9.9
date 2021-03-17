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

import java.awt.event.MouseEvent;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class App {

    private LinkChart linkChart = null;
    private Runnable task;
    private final int refreshRate = 60;
    private final String titleString = "SiViT";
    public static UIManager UI = new UIManager();
    private final DisplayManager3D displayManager3D;
    private static App app = App.getInstance();
    final LayoutManager layoutManager = new LayoutManager();
    private ModelManager modelManager = null;
    private DrugsFileReader dfr = new DrugsFileReader();

    private ModelRun experimentRun, controlRun;
    private boolean flEnableDisplayUpdates = false;
    private double simTime;
    private final HashMap node2objMap = new HashMap();
    private final ContextMenu contextMenu = new ContextMenu();
    private HashMap drugSet = new HashMap();
    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
    public static int colourOutput = 1;
    
    private final int runtimeOptimisationFactor = 6;
    private final int preOptimisationFactor = 500;

    public App() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            classLoader.loadClass("javax.media.j3d.BoundingSphere");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(UI, "Package required: Java3D is not installed.\nPlease download and install from:\nhttp://java.sun.com/javase/technologies/desktop/java3d/downloads/", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        try {
            modelManager = new ModelManager();
        } catch (MatlabConnectionException ex) {
            JOptionPane.showMessageDialog(UI, "MATLAB installation is required (R2014b or later for full support)", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } catch (MatlabInvocationException ex) {
            JOptionPane.showMessageDialog(UI, "Cannot find MATLAB scripts", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        if (dfr.readFile() == true) {
            drugSet = dfr.getDrugSet();
        }

        System.out.println(drugSet);

        displayManager3D = new DisplayManager3D(UI.jpViewCanvas);
        UI.setExtendedState(UI.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        setupUpdateThread();
    }

    private long prevTimeStamp = System.currentTimeMillis();
    float frameTime = 40;

    private long startTimeStamp = 0;
    private boolean isPlaying = false;
    double dt = 0;
    private void setupUpdateThread() {
        task = new Runnable() {

            @Override
            public void run() {

                try {
                    if (flEnableDisplayUpdates) {                 
                        
                        if (experimentRun == null || experimentRun.isDirty()) {
                            UI.setWaitingForModel(true);
                            experimentRun = modelManager.calculateModelRun(UI.getExperimentEvents());
                            System.gc();
                            UI.setWaitingForModel(false);
                        }
                        if (controlRun == null || controlRun.isDirty()) {
                            UI.setWaitingForModel(true);
                            controlRun = modelManager.calculateModelRun(UI.getControlEvents());
                            System.gc();
                            UI.setWaitingForModel(false);
                        }
                        simTime = modelManager.getTime();

                        if (UI.getIsTimeSliderAdjusting()) {
                            simTime = modelManager.getMaxSimTime() * UI.getTimeSliderFraction();
                        } else if (UI.isTimeRunning()) {
                            if(isPlaying == false){
                                startTimeStamp = System.currentTimeMillis();
                                isPlaying = true;
                            }
                            simTime = modelManager.getTime() + UI.getSpeedCoefficient() * (System.currentTimeMillis() - prevTimeStamp) / 1000 / 60;
                            if (simTime > modelManager.getMaxSimTime()) {
                                simTime = modelManager.getMaxSimTime();
                                simTime = 0;
                                if (UI.isLooping()) {
                                    //loop is true, continue playing   
                                } else {
                                    //looping false, stop playing
                                    UI.setRunning(false);
                                    isPlaying = false;
                                    dt = (double)(System.currentTimeMillis() - startTimeStamp)/1000;
                                    System.out.println("Simulation completed in " + (double)(System.currentTimeMillis() - startTimeStamp)/1000 + "sec");
                                }
                            }
                        }
                        
                        if(UI.isDemo()){
                            displayManager3D.demoAnimation(1);
                            System.out.println(dt);
                        }
                        prevTimeStamp = System.currentTimeMillis();
                       
                        UI.setTime(simTime);
                        modelManager.setTime(simTime);
                        experimentRun.setTime(simTime);
                        controlRun.setTime(simTime);
                        linkChart.updateSimData();
                        if (UI.getIsTimeSliderAdjusting() || UI.isTimeRunning()){
                            if(layoutManager.isGraphFullyOptimised()){
                                layoutManager.resetOptimisation();
                            }
                        }
                        if (UI.isViewUpdating()) {
                            for(int i = 0; i < runtimeOptimisationFactor; i++){
                                layoutManager.improveGraph();
                            }
                            linkChart.updatePosition();
                        }
                        displayManager3D.updateLight();

                    }
                } catch (NullPointerException ex) {
                    Logger.getLogger(UIManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MatlabInvocationException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
         //exec.scheduleAtFixedRate(task, 0, 1000 / refreshRate, TimeUnit.MILLISECONDS);
        exec.scheduleWithFixedDelay(task, 0, 1, TimeUnit.MILLISECONDS);
    }

    public LinkChart getChart() {
        return linkChart;
    }

    public static App getInstance() {
        if (app == null) {
            app = new App();
        }
        return app;
    }

    public boolean isNewLighting() {
        return UI.isNewLighting();
    }

    public boolean isUseThresholds() {
        //System.err.println(UI.isUseThresholds());
        return UI.isUseThresholds();
    }

    public boolean isShowOutput() {
        return UI.isShowOutput();
    }

    public void refreshUI() {
        // TODO
    }

    public void loadChart() {
        JFileChooser fc = new JFileChooser("data/");

        if (fc.showOpenDialog(UI) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                flEnableDisplayUpdates = false;

                modelManager.loadModel(file);
                controlRun = modelManager.calculateModelRun(null);
                experimentRun = modelManager.calculateModelRun(null);
                System.gc();

                displayManager3D.removeGraph();
                layoutManager.clear();

                linkChart = new LinkChart(displayManager3D.getLinkChartViz(), layoutManager.getGraph());

                layoutManager.scrambleGraph();
                layoutManager.recentre();
                for (int i = 0; i < this.preOptimisationFactor; i++) {
                    layoutManager.improveGraph();
                }
                linkChart.updatePosition();

                String filename = file.getName();
                UI.setTitle(titleString + " - " + filename.substring(0, filename.length() - 3));
                UI.resetModel();
                displayManager3D.resetView();
                displayManager3D.showGraph();

                flEnableDisplayUpdates = true;
                //displayManager3D.setZoomLevel(20);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(UI, "Error reading data from " + file, "Error", JOptionPane.ERROR_MESSAGE);
            } catch (MatlabInvocationException ex) {
                JOptionPane.showMessageDialog(UI, "Error reading data from " + file, "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
    }

    public void scrambleGraph() {
        layoutManager.scrambleGraph();
        layoutManager.recentre();
    }

    public void ConstrainTo2D() {
        boolean conTo2D = !layoutManager.isConstrainTo2D();
        layoutManager.setConstrainTo2D(conTo2D);
        this.displayManager3D.vpb.setRotateEnable(!conTo2D);
        this.displayManager3D.constrainTo2D = conTo2D;
        if (conTo2D) {
            this.displayManager3D.resetView();
        }
        //this.displayManager3D.vpb.

    }

    public void ConstrainToSphere() {
        layoutManager.setConstrainToSphere(!layoutManager.isConstrainToSphere());
    }

    public void exitApplication() {
        try {
            this.exec.shutdown();
            this.modelManager.cleanup();

        } catch (MatlabInvocationException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    public void resetView() {
        displayManager3D.resetView();
    }

    public void recentreChart() {
        layoutManager.recentre();
    }

    public void saveImage() {
        JFileChooser fc = new JFileChooser("./images/");

        FileFilter ff = new FileFilter() {

            public String getDescription() {
                return "JPEG Images";
            }

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }

                String extension = (f.getName().substring(f.getName().lastIndexOf('.'))).toLowerCase();
                if (extension != null) {
                    extension = extension.substring(1);
                    if (extension.equals("jpeg") || extension.equals("jpg")) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }
        };

        fc.addChoosableFileFilter(ff);

        if (fc.showSaveDialog(UI) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.exists()) {
                Object[] options = {"OK", "Cancel"};
                if (JOptionPane.showOptionDialog(UI,
                        "File already exists: " + file.getName() + "\nDo you want to overwrite it?",
                        "Warning!",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[1]) == 1) {
                    return;
                }
            }
            try {
                displayManager3D.capture(file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(UI, "Internal error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    ModelManager getModelManager() {
        return modelManager;
    }

    void addEvent(String id, double value, double time) {
        try {
            modelManager.addInterventionEvent(id, value, time);
            UI.addEvent(id, value, time);
            experimentRun.setDirty(true);
        } catch (MatlabInvocationException ex) {
            JOptionPane.showMessageDialog(UI, "MATLAB Connection error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        layoutManager.resetOptimisation(); // added for dynamic layout to force update on events change
    }

    HashMap getDrugSet() {

        return this.drugSet;
    }

    ModelRun getExperimentModelRun() {
        return experimentRun;
    }

    ModelRun getControlModelRun() {
        return controlRun;
    }

    void deleteInterventionEvent(int n) {
        try {
            modelManager.deleteInterventionEvent(n);
        } catch (MatlabInvocationException ex) {
            JOptionPane.showMessageDialog(UI, "MATLAB Connection error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        layoutManager.resetOptimisation(); // added for dynamic layout to force update on events change
    }

    public HashMap getNode2objMap() {
        return this.node2objMap;
    }

    void showContextMenu(javax.media.j3d.Node n, MouseEvent evt) {
        Object obj = node2objMap.get(n);
        if (obj != null) {
            if (Node.class.equals(obj.getClass())) {
                Node node = (Node) obj;
                contextMenu.refreshMenuItems(simTime, modelManager.getMaxSimTime(), node.getSpeciesId(), controlRun.getAllTime(), controlRun.getAllConcentrations(node.getSpeciesId()), experimentRun.getAllTime(), experimentRun.getAllConcentrations(node.getSpeciesId()), ContextMenu.TYPE_NODE);
            } else { // it's a link
                MultiLink l = (MultiLink) obj;
                contextMenu.refreshMenuItems(simTime, modelManager.getMaxSimTime(), l.getReactionFormula(), controlRun.getAllTime(), controlRun.getAllFluxes(l.getReactionId()), experimentRun.getAllTime(), experimentRun.getAllFluxes(l.getReactionId()), ContextMenu.TYPE_EDGE);
                System.out.println("Edge info:");
                System.out.println("     direction: " + l.getReactionFormula());
                System.out.println("     lengthMod: " + l.lengthMod);
            }
            contextMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        } else {
            //System.out.println("unknown pick: " + n.getClass());
        }
    }

    void saveChartView() {
        JFileChooser fc = new JFileChooser(".");

        if (fc.showSaveDialog(UI) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.exists()) {
                Object[] options = {"OK", "Cancel"};
                if (JOptionPane.showOptionDialog(UI,
                        "File already exists: " + file.getName() + "\nDo you want to overwrite it?",
                        "Warning!",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[1]) == 1) {
                    return;
                }
            }
            try {
                TransformerHandler hd = ((SAXTransformerFactory) SAXTransformerFactory.newInstance()).newTransformerHandler();

                hd.setResult(new StreamResult(file));
                hd.startDocument();
                hd.startElement("", "", "view", null);
                linkChart.savePositions(hd);
                hd.endElement("", "", "view");
                hd.endDocument();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(UI, "Internal error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (TransformerConfigurationException ex) {
                JOptionPane.showMessageDialog(UI, "Internal error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SAXException ex) {
                JOptionPane.showMessageDialog(UI, "Internal error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean useDynamicLayout() {
        return UI.useDynamicLayout();
    }

    void loadChartView() {
        JFileChooser fc = new JFileChooser(".");
        if (fc.showOpenDialog(UI) == JFileChooser.APPROVE_OPTION) {
            try {
                File f = fc.getSelectedFile();
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                DefaultHandler handler = new DefaultHandler() {
                    public void startElement(String uri, String localName, String qName, Attributes att) throws SAXException {
                        if (qName.equalsIgnoreCase("node")) {
                            linkChart.setNodePosition(att.getValue("id"), Double.parseDouble(att.getValue("x")),
                                    Double.parseDouble(att.getValue("y")),
                                    Double.parseDouble(att.getValue("z")));
                        }
                    }
                };

                parser.parse(f, handler);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(UI, "Internal error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ParserConfigurationException ex) {
                JOptionPane.showMessageDialog(UI, "Internal error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SAXException ex) {
                JOptionPane.showMessageDialog(UI, "Internal error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                UI.setVisible(true);
            }
        });
    }

}
