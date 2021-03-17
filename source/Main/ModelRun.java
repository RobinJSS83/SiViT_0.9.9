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

import java.util.HashMap;

public class ModelRun {

    private final double[] t, p, v, pnorm, vnorm;
    private final String[] snames, rnames;
    private final HashMap<String, Integer> hashSpeciesNames = new HashMap<String, Integer>(), hashReactionNames = new HashMap<String, Integer>();
    private double simTime;
    private boolean dirty;
    private final double[] pmin;
    private final double[] pmax;
    private final double[] vmin;
    private final double[] vmax;

    ModelRun(String[] s, String[] r, double[] t, double[] p, double[] v, double[] pn, double[] vn, double[] pmin, double[] pmax, double[] vmin, double[] vmax) {
        this.snames = s;
        this.rnames = r;
        this.t = t;
        this.p = p;
        this.v = v;

        this.pnorm = pn;
        this.vnorm = vn;

        this.pmin = pmin;
        this.pmax = pmax;
        this.vmin = vmin;
        this.vmax = vmax;

        hashSpeciesNames.clear();
        for (int i = 0; i < snames.length; i++) {
            hashSpeciesNames.put(snames[i], i);
        }

        hashReactionNames.clear();
        for (int i = 0; i < rnames.length; i++) {
            hashReactionNames.put(rnames[i], i);
        }
        this.dirty = false;
    }

    public double getMinConcentration(String id) {
        return pmin[hashSpeciesNames.get(id)];
    }

    public double getMaxConcentration(String id) {
        return pmax[hashSpeciesNames.get(id)];
    }

    public double getMinFlux(String id) {
        return vmin[hashReactionNames.get(id)];
    }

    public double getMaxFlux(String id) {
        return vmax[hashReactionNames.get(id)];
    }

    public double getNormalisedConcentration(String speciesName, double time) {
        return getDatum(hashSpeciesNames, pnorm, speciesName, time);
    }

    public double getNormalisedFlux(String reactionName, double time) {
        return getDatum(hashReactionNames, vnorm, reactionName, time);
    }

    public double getConcentration(String speciesId, double time) {
        return getDatum(hashSpeciesNames, p, speciesId, time);
    }

    public double getFlux(String id, double time) {
        return getDatum(hashReactionNames, v, id, time);
    }

    public void setTime(double atTime) {
        this.simTime = atTime;
    }

    public double getTime() {
        return simTime;
    }

    public double getNormalisedConcentration(String speciesId) {
        return getNormalisedConcentration(speciesId, simTime);
    }

    public double getNormalisedFlux(String reactionId) {
        return getNormalisedFlux(reactionId, simTime);
    }

    double getConcentration(String speciesId) {
        return getConcentration(speciesId, simTime);
    }

    double getFlux(String id) {
        return getFlux(id, simTime);
    }

    private double getDatum(HashMap<String, Integer> hashName2Idx, double[] data, String id, double time) {
        int i = hashName2Idx.get(id);

        double j = findClosestTimeIdx(time);

        double min = data[i * t.length + (int) j];
        if (j > (int) j) {
            double max = data[i * t.length + (int) j + 1];
            return min + (max - min) * (j - (int) j);
        } else {
            return min;
        }
    }

    private double findClosestTimeIdx(double time) {
        if (time <= t[0]) {
            return 0;
        }

        double r = t.length - 1;
        for (int i = 0; i < t.length - 1; i++) {
            if (time < t[i + 1]) {
                r = i + (time - t[i]) / (t[i + 1] - t[i]);
                break;
            }
        }
        return r;
    }

    boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(boolean b) {
        this.dirty = b;
        
    }

    double[] getAllTime() {
       // System.out.println(t[0]);
        return t;
    }

    double[] getAllConcentrations(String speciesId) {
        int i = hashSpeciesNames.get(speciesId);
        double[] retval = new double[t.length];
        System.arraycopy(p, i * t.length, retval, 0, t.length);

        return retval;
    }

    double[] getAllFluxes(String reactionId) {
        try {
            int i = hashReactionNames.get(reactionId);
            double[] retval = new double[t.length];
            System.arraycopy(v, i * t.length, retval, 0, t.length);

            return retval;
        } catch (java.lang.NullPointerException ex) {
            return null;
        }
    }
}
