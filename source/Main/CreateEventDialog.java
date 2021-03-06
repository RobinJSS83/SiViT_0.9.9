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

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import matlabcontrol.MatlabInvocationException;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import Utils.SortableComboBoxModel;

/**
 *
 * @authors Mark Shovman, Andrei Boiko, Paul Robertson
 */
public class CreateEventDialog extends javax.swing.JDialog {

    private double defaultTime = 0;

    /** Creates new form CreateEventDialog
     * @param parent
     * @param modal */
    public CreateEventDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        jcbSpeciesList.setModel(new SortableComboBoxModel());
        jcbSpeciesList.setEnabled(false);   //set enabled to false by default. To access the component the radio selectors have to be used.
        AutoCompleteDecorator.decorate(this.jcbDrugList);
        AutoCompleteDecorator.decorate(this.jcbSpeciesList);
        SpinnerModel sModel = new SpinnerNumberModel(0, 0, null, 1);
        jspTime.setModel(sModel);
    }

    /** This method is called from within the constructor to
     * initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jcbSpeciesList = new javax.swing.JComboBox();
        jspValue = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jbtnReset = new javax.swing.JButton();
        jbtnCreate = new javax.swing.JButton();
        jbtnCancel = new javax.swing.JButton();
        jspTime = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jcbDrugList = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jrbDrug = new javax.swing.JRadioButton();
        jrbModel = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jcbSpeciesList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbSpeciesListActionPerformed(evt);
            }
        });

        jspValue.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));

        jLabel3.setText("at time");

        jbtnReset.setText("Reset");
        jbtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnResetActionPerformed(evt);
            }
        });

        jbtnCreate.setText("Add");
        jbtnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCreateActionPerformed(evt);
            }
        });

        jbtnCancel.setText("Cancel");
        jbtnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCancelActionPerformed(evt);
            }
        });

        jspTime.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(1.0d)));

        jLabel2.setText("new value");

        jcbDrugList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcbDrugListActionPerformed(evt);
            }
        });

        jLabel4.setText("or");

        buttonGroup1.add(jrbDrug);
        jrbDrug.setText("Introduce Drug");
        jrbDrug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrbDrugActionPerformed(evt);
            }
        });

        buttonGroup1.add(jrbModel);
        jrbModel.setText("Adjust Model");
        jrbModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrbModelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jLabel4))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jrbDrug)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jcbDrugList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jrbModel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jcbSpeciesList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(130, 130, 130)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jspValue, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jspTime, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(198, 198, 198)
                        .addComponent(jbtnCreate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbtnReset)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jbtnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jrbDrug)
                    .addComponent(jcbDrugList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jrbModel)
                    .addComponent(jcbSpeciesList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jspValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jspTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbtnCancel)
                    .addComponent(jbtnReset)
                    .addComponent(jbtnCreate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jcbSpeciesListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbSpeciesListActionPerformed
        if (this.jcbSpeciesList.getSelectedIndex() >= 0) {
            //System.out.println(this.jcbSpeciesList.getSelectedIndex() + ": " + (String) this.jcbSpeciesList.getSelectedItem());
            this.jspValue.setValue(App.getInstance().getExperimentModelRun().getConcentration((String) this.jcbSpeciesList.getSelectedItem()));
        }
}//GEN-LAST:event_jcbSpeciesListActionPerformed

    private void jbtnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnResetActionPerformed
        this.jcbSpeciesList.setSelectedIndex(-1);
        this.jcbDrugList.setSelectedIndex(-1);
        this.jspValue.setValue(0);
        this.jspTime.setValue(this.defaultTime);

}//GEN-LAST:event_jbtnResetActionPerformed

    private void jbtnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnCancelActionPerformed
        this.setVisible(false);
}//GEN-LAST:event_jbtnCancelActionPerformed

    private void jbtnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnCreateActionPerformed
        String s;
        if (this.jrbDrug.isSelected()) {
            s = (String) jcbDrugList.getSelectedItem();
            
        } else {

            s = (String) jcbSpeciesList.getSelectedItem();
        }
        
        if(s == null)
        {
            return;
        }
        
        App.getInstance().addEvent(s,
                Double.parseDouble(jspValue.getValue().toString()),
                Double.parseDouble(jspTime.getValue().toString()));
        this.setVisible(false);
    }//GEN-LAST:event_jbtnCreateActionPerformed

    private void jrbDrugActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrbDrugActionPerformed
        jcbDrugList.setEditable(true);
        jcbDrugList.setEnabled(true);
        jcbSpeciesList.setSelectedIndex(-1);
        jcbSpeciesList.setEditable(false);
        jcbSpeciesList.setEnabled(false);
    }//GEN-LAST:event_jrbDrugActionPerformed

    private void jrbModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrbModelActionPerformed
        jcbSpeciesList.setEditable(true);
        jcbSpeciesList.setEnabled(true);
        jcbDrugList.setSelectedIndex(-1);
        jcbDrugList.setEditable(false);
        jcbDrugList.setEnabled(false);
    }//GEN-LAST:event_jrbModelActionPerformed

    private void jcbDrugListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbDrugListActionPerformed
        if (jcbDrugList.getSelectedIndex() >= 0) {
            jspValue.setValue(App.getInstance().getDrugSet().get((String) jcbDrugList.getSelectedItem()));
            //jspValue.setValue(App.getInstance().getExperimentModelRun().getConcentration((String) jcbDrugList.getSelectedItem()));
        }
    }//GEN-LAST:event_jcbDrugListActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton jbtnCancel;
    private javax.swing.JButton jbtnCreate;
    private javax.swing.JButton jbtnReset;
    private javax.swing.JComboBox jcbDrugList;
    private javax.swing.JComboBox jcbSpeciesList;
    private javax.swing.JRadioButton jrbDrug;
    private javax.swing.JRadioButton jrbModel;
    private javax.swing.JSpinner jspTime;
    private javax.swing.JSpinner jspValue;
    // End of variables declaration//GEN-END:variables

    void openForm(double time, Point parentLocation, Dimension parentSize) {
        this.defaultTime = time;
        jcbDrugList.setSelectedIndex(-1);
        jcbSpeciesList.setSelectedIndex(-1);
        jcbDrugList.setEditable(true);
        jcbDrugList.setEnabled(true);
        jcbSpeciesList.setEditable(false);
        jcbSpeciesList.setEnabled(false);
        this.jrbDrug.setSelected(true);
        this.jrbModel.setSelected(false);
        this.jspTime.setValue(defaultTime);
        this.jspValue.setValue(0);
        this.setVisible(true);
        // calculate and set position of popup in the middle of the parent window
        int posX = parentLocation.x + parentSize.width/2  - this.getSize().width/2;
        int posY = parentLocation.y + parentSize.height/2 - this.getSize().height/2;
        this.setLocation(posX, posY);
    }

    void resetModel() {
        jcbSpeciesList.removeAllItems();
        jcbDrugList.removeAllItems();

        HashMap drugs = App.getInstance().getDrugSet();
        try {
            String[] speciesNames = App.getInstance().getModelManager().getSpeciesNames();

            for (int i = 0; i < speciesNames.length; i++) {
                if (drugs.containsKey(speciesNames[i])) {
                    this.jcbDrugList.addItem(speciesNames[i]);
                } else {
                    this.jcbSpeciesList.addItem(speciesNames[i]);
                }
            }
        } catch (MatlabInvocationException ex) {
            Logger.getLogger(CreateEventDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            String[] parameterNames = App.getInstance().getModelManager().getParameterNames();

            for (int i = 0; i < parameterNames.length; i++) {
                if (drugs.containsKey(parameterNames[i])) {
                    this.jcbDrugList.addItem(parameterNames[i]);
                } else {
                    this.jcbSpeciesList.addItem(parameterNames[i]);
                }
            }
        } catch (MatlabInvocationException ex) {
            Logger.getLogger(CreateEventDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        AutoCompleteDecorator.decorate(this.jcbDrugList);
        AutoCompleteDecorator.decorate(this.jcbSpeciesList); 
    }
}