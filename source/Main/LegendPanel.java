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

import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.Rectangle2D.Double;
import javax.swing.*;

public class LegendPanel extends JPanel {

    Path2D p1 = new Path2D.Double(), p2 = new Path2D.Double();
    static final Dimension dim = new Dimension(300, 280);
    private Rectangle2D.Double bounds = new Rectangle2D.Double();
    private double x, y;
    private final AffineTransform at = new AffineTransform();
    
    private final GradientPaint upRegPaint;
    private final GradientPaint downRegPaint;
    
    private final Dimension regRectDim = new Dimension(80, 40);
    private final Point downRegPos = new Point(10, 10);
    private final Point upRegPos = new Point(regRectDim.width+downRegPos.x, downRegPos.y);
    
    JLabel jlDownArrow = new JLabel();
    JLabel jlUpArrow = new JLabel();
    JLabel jlDownReg = new JLabel();
    JLabel jlUpReg = new JLabel();
    
    Font font;

    public LegendPanel() {
        super();
        this.upRegPaint = new GradientPaint(upRegPos.x, upRegPos.y, Color.WHITE, upRegPos.x+regRectDim.width, upRegPos.y, Color.RED);;
        this.downRegPaint = new GradientPaint(downRegPos.x, downRegPos.y, Color.BLUE, downRegPos.x+regRectDim.width, downRegPos.y, Color.WHITE);
        setPreferredSize(dim);
        font = new Font("Helvetica", Font.PLAIN, 12);
        setFont(font);
        
        jlDownReg.setText("Down-regulation");
        jlUpReg.setText("Up-regulation");
        jlDownArrow.setText("<----------");
        jlUpArrow.setText("---------->");

        javax.swing.GroupLayout legendPanel2Layout = new javax.swing.GroupLayout(this);
        this.setLayout(legendPanel2Layout);
        legendPanel2Layout.setHorizontalGroup(
            legendPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, legendPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jlDownArrow)
                .addGap(28, 28, 28)
                .addComponent(jlUpArrow)
                .addGap(27, 27, 27))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, legendPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlDownReg, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(jlUpReg)
                .addContainerGap())
        );
        legendPanel2Layout.setVerticalGroup(
            legendPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(legendPanel2Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(legendPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlUpArrow)
                    .addComponent(jlDownArrow))
                .addGap(18, 18, 18)
                .addGroup(legendPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlUpReg)
                    .addComponent(jlDownReg))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(downRegPaint);
        g2.fill(new Rectangle2D.Double(downRegPos.x, downRegPos.y, regRectDim.width, regRectDim.height));
        
        g2.setPaint(upRegPaint);
        g2.fill(new Rectangle2D.Double(upRegPos.x, upRegPos.y, regRectDim.width, regRectDim.height));
        
        
    }
}
