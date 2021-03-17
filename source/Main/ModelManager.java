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

import Utils.WindowsRegistry;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;

public class ModelManager {
    private final MatlabProxy proxy;
    private boolean isModelLoaded = false;
    private double simTime = 0, maxSimTime = 10; // in minutes
    private String matlabPath = "";
    
    public ModelManager() throws MatlabConnectionException, MatlabInvocationException {
        // Get MATLAB executable path from registry
        String value = null;
        try {
            // check all supported versions of matlab
            if(value == null){ // R2011b
                value = WindowsRegistry.readString(WindowsRegistry.HKEY_LOCAL_MACHINE, "Software\\MathWorks\\MATLAB\\7.13", "MATLABROOT", WindowsRegistry.KEY_WOW64_64KEY);
            }
            if(value == null){ // R2012a
                value = WindowsRegistry.readString(WindowsRegistry.HKEY_LOCAL_MACHINE, "Software\\MathWorks\\MATLAB\\7.14", "MATLABROOT", WindowsRegistry.KEY_WOW64_64KEY);
            }
            if(value == null){ // R2012b
                value = WindowsRegistry.readString(WindowsRegistry.HKEY_LOCAL_MACHINE, "Software\\MathWorks\\MATLAB\\8", "MATLABROOT", WindowsRegistry.KEY_WOW64_64KEY);
            }
            if(value == null){ // R2013a
                value = WindowsRegistry.readString(WindowsRegistry.HKEY_LOCAL_MACHINE, "Software\\MathWorks\\MATLAB\\8.1", "MATLABROOT", WindowsRegistry.KEY_WOW64_64KEY);
            }
            if(value == null){ // R2013b
                value = WindowsRegistry.readString(WindowsRegistry.HKEY_LOCAL_MACHINE, "Software\\MathWorks\\MATLAB\\8.2", "MATLABROOT", WindowsRegistry.KEY_WOW64_64KEY);
            }
            if(value == null){ // R2014a
                value = WindowsRegistry.readString(WindowsRegistry.HKEY_LOCAL_MACHINE, "Software\\MathWorks\\MATLAB\\8.3", "MATLABROOT", WindowsRegistry.KEY_WOW64_64KEY);
            }
            if(value == null){ // R2014b
                value = WindowsRegistry.readString(WindowsRegistry.HKEY_LOCAL_MACHINE, "Software\\MathWorks\\MATLAB\\8.4", "MATLABROOT", WindowsRegistry.KEY_WOW64_64KEY);
            }
            matlabPath = value + "\\bin\\matlab.exe";
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ModelManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ModelManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(ModelManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Build proxy for communicating with MATLAB
        MatlabProxyFactory factory = new MatlabProxyFactory(new MatlabProxyFactoryOptions.Builder()
                .setHidden(true).setMatlabLocation(matlabPath).
                build());        
        
        proxy = factory.getProxy();
        // Set directory to point to MATLAB scripts
        proxy.eval("cd '"+System.getProperty("user.dir")+"\\MATLAB_scripts\\'");
    }

    public double getMaxSimTime() {
        return maxSimTime;
    }

    public void setMaxSimTime(double maxSimTime) {
        this.maxSimTime = maxSimTime;
        if(this.isModelLoaded){
            try {
                calculateModelRun(App.UI.getControlEvents());
                calculateModelRun(App.UI.getExperimentEvents());
                System.gc();
            } catch (MatlabInvocationException ex) {
                Logger.getLogger(ModelManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ModelRun calculateModelRun(ArrayList<Boolean> eventList) throws MatlabInvocationException {
        if (eventList != null) {
            for (int i = 0; i < eventList.size(); i++) {
                proxy.eval("sivit.m.Events(" + (i + 1) + ").Active = " + (eventList.get(i) ? "1" : "0"));
            }
        } else {
           // System.out.println(". NO events.");
        }

        proxy.eval("configsetObj = setMaxSimTime(sivit.m, "+maxSimTime+ ")");
        proxy.eval("tic,[t, p, v, pnorm, vnorm, snames, rnames, vmin, vmax, pmin, pmax] = calculateFlux(sivit.m, configsetObj, sivit.time_symbol);toc;");

        return new ModelRun(
                (String[]) proxy.getVariable("snames"),
                (String[]) proxy.getVariable("rnames"),
                (double[]) proxy.getVariable("t"),
                (double[]) proxy.getVariable("p"), (double[]) proxy.getVariable("v"),
                (double[]) proxy.getVariable("pnorm"), (double[]) proxy.getVariable("vnorm"),
                (double[]) proxy.getVariable("pmin"), (double[]) proxy.getVariable("pmax"),
                (double[]) proxy.getVariable("vmin"), (double[]) proxy.getVariable("vmax"));
    }

    public void cleanup() throws MatlabInvocationException {
        //Disconnect the proxy from MATLAB
        proxy.exit();
        proxy.disconnect();
    }

    public void loadModel(File f) throws IOException, MatlabInvocationException {
        simTime = 0;

        String filename = f.getCanonicalPath();
        proxy.eval("clear all");
        proxy.eval("sivit = sivitLoadSBMLModel('" + filename + "');");
        isModelLoaded = true;
    }

    public void addInterventionEvent(String speciesId, double value, double atTime) throws MatlabInvocationException {
        proxy.eval("sivit.m.addevent('time>=" + (atTime + 0.0001) + "', '" + speciesId + " = " + value + "');");
        proxy.eval("disp('adding');sivit.m.Events(end)");
    }

    void deleteInterventionEvent(int n) throws MatlabInvocationException {
        proxy.eval("disp('deleting...'); sivit.m.Events(" + (n + 1) + ")");
        proxy.eval("delete(sivit.m.Events(" + (n + 1) + "));");
    }

    public void setTime(double atTime) {
        this.simTime = atTime;
    }

    public double getTime() {
        return simTime;
    }

    String[] getSpeciesNames() throws MatlabInvocationException {
        return (String[]) proxy.getVariable("sivit.speciesNames");
    }

    String[] getSpeciesIDs() throws MatlabInvocationException {
        return (String[]) proxy.getVariable("sivit.speciesIds");
    }

    String[] getReactionNames() throws MatlabInvocationException {
        return (String[]) proxy.getVariable("sivit.reactionNames");
    }

    String[] getReactionFormulae() throws MatlabInvocationException {
        return (String[]) proxy.getVariable("sivit.reactionFormulae");
    }

    String getReactionFormula(String name) {
        try {
            proxy.eval("res = sivit.reactionFormulae{find(strcmp('" + name + "', sivit.reactionNames))};");
            return (String) proxy.getVariable("res");
        } catch (MatlabInvocationException ex) {
            // NB will happen for pseudo-reactions leading to the DNA
            System.err.println("Error retrieving formula for reaction " + name + ": " + ex);
            return null;
        }
    }

    String[] getParameterNames() throws MatlabInvocationException {
        return (String[]) proxy.getVariable("sivit.parameterNames");
    }

    String[] getReactantNames(int i) throws MatlabInvocationException {
        return (String[]) proxy.getVariable("sivit.reactionReactants{"+(i+1)+"}");
    }

    String[] getModifierNames(int i) throws MatlabInvocationException {
        return (String[]) proxy.getVariable("sivit.reactionModifiers{"+(i+1)+"}");
    }

    String[] getProductNames(int i) throws MatlabInvocationException {
        return (String[]) proxy.getVariable("sivit.reactionProducts{"+(i+1)+"}");
    }
}