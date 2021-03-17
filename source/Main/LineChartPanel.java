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

public class LineChartPanel extends JPanel {

    Path2D p1 = new Path2D.Double(), p2 = new Path2D.Double();
    static final Dimension dim = new Dimension(300, 280);
    private Rectangle2D.Double bounds = new Rectangle2D.Double();
    private double x, y;
    private final AffineTransform at = new AffineTransform();
    private final Stroke stroke = new BasicStroke(2);
    
    //graph style vars
    private static final int BORDER_GAP = 15;
    private static final int Y_HATCH_CNT = 10;
    private static final int GRAPH_POINT_WIDTH = 6;
    private static final String xLabel = "Time";
    private static final String yLabel = "Concentration";
    
    Font font;
    private double simTime = 0;
    private double maxSimTime;
    private int type = 0;

    public LineChartPanel() {
        super();
        setPreferredSize(dim);
        font = new Font("Helvetica", Font.PLAIN, 14);
        setFont(font);
    }

    public void setData(double[] x1s, double[] y1s, double[] x2s, double[] y2s, int type) {
        this.type = type;

        p1.reset();
        p2.reset();

        for (int i = 0; i < x1s.length; i++) {
            x = x1s[i];
            y = y1s[i];
            if (i == 0) {
                p1.moveTo(x, y);
            } else {
                p1.lineTo(x, y);
            }
        }

        for (int i = 0; i < x2s.length; i++) {
            x = x2s[i];
            y = y2s[i];
            if (i == 0) {
                p2.moveTo(x, y);
            } else {
                p2.lineTo(x, y);
            }
        }
        bounds = (Double) p2.getBounds2D().createUnion(p1.getBounds2D());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int h = getHeight(), w = getWidth();
        g2.setColor(SystemColor.control);
        g2.fillRect(0, 0, w, h);
        g2.setStroke(stroke);

        at.setToIdentity();
        at.translate(BORDER_GAP, h - BORDER_GAP);
        at.scale((w-BORDER_GAP) / bounds.getMaxX(), (-h+BORDER_GAP) / bounds.getMaxY());

        // experiment graph
        g2.setPaint(new Color(0,0.6f, 0));
        g2.draw(p2.createTransformedShape(at));
        // control graph
        g2.setPaint(Color.black);
        g2.draw(p1.createTransformedShape(at));
        
        // draw axis lines
        // create x and y axes 
         g2.setPaint(Color.black);
         g2.setStroke(new BasicStroke(1));
         
         g2.drawLine(BORDER_GAP, getHeight() - BORDER_GAP, BORDER_GAP, 0);
         g2.drawLine(BORDER_GAP, getHeight() - BORDER_GAP, getWidth() - BORDER_GAP, getHeight() - BORDER_GAP);
         
         //draw timepoint line
         g2.setPaint(Color.MAGENTA);
         int timeX = (int) (((getWidth()-(BORDER_GAP*2))*simTime)/maxSimTime);
        // System.out.println("SimTime: " + simTime);
        // System.out.println("Time coords: " + timeX);
         g2.drawLine(timeX+BORDER_GAP , getHeight() - BORDER_GAP, timeX+BORDER_GAP, 0);

         g2.setPaint(Color.black);
          // create hatch marks for y axis. 
          for (int i = 0; i < Y_HATCH_CNT; i++) {
             int x0 = BORDER_GAP;
             int x1 = GRAPH_POINT_WIDTH + BORDER_GAP;
             int y0 = getHeight() - (((i + 1) * (getHeight() - BORDER_GAP * 2)) / Y_HATCH_CNT + BORDER_GAP);
             int y1 = y0;
             g2.drawLine(x0, y0, x1, y1);
          }

          // and for x axis
          for (int i = 0; i < Y_HATCH_CNT; i++) {
             int x0 = (i + 1) * (getWidth() - BORDER_GAP * 2) / Y_HATCH_CNT + BORDER_GAP;
             int x1 = x0;
             int y0 = getHeight() - BORDER_GAP;
             int y1 = y0 - GRAPH_POINT_WIDTH;
             g2.drawLine(x0, y0, x1, y1);
          }
          
          // get width of text for proper alignment
          FontMetrics fm = getFontMetrics( getFont() );
          int width = fm.stringWidth(xLabel);
          
          // add labels to axis
          g2.drawString("Time", (getWidth()/2)-(width/2), getHeight());
          width = fm.stringWidth(yLabel);
          g2.rotate(Math.toRadians(-90.0));
          
          String verticalLabel = "";
          switch(this.type){
              case ContextMenu.TYPE_NODE: verticalLabel = "Concentration"; break;
              case ContextMenu.TYPE_EDGE: verticalLabel = "Reaction Rate"; break;
              default: verticalLabel = "Concentration";
          }
          g2.drawString(verticalLabel, (-getWidth()/2)-(width/2)+BORDER_GAP, 10);
          
          

    }

    void setTime(double time, double maxTime) {
        this.simTime = time;
        this.maxSimTime = maxTime;
    }
}
