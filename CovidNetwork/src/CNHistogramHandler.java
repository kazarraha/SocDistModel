import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class CNHistogramHandler extends JPanel {

	public JFrame window;
	public double[] boundaries;
	public double[] bins;
	public String label;
	
	public double scaling = 1000; //value required to hit the max
	
	
	public CNHistogramHandler(double[] bound, double[] bin, String lab){	
		window = new JFrame("sub window");
        window.add(this);
        window.setSize(1400, 1000);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		boundaries = bound;
		bins = bin;
		label = lab;
		repaint();
	}
	
	public void rescale(double scale) {
		scaling = scale;
		repaint();
	}
	
	
	
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		int top = 100;
		int left = 100;
		int height = 800;
		int length = 1200;
		 
		//int numberOfBins = 12;
		int numberOfBins = bins.length;
		 int xTic = length/numberOfBins;
		 double yTic = (0.0+height)/scaling;
		 	 
		 g2d.setColor(Color.black);
		 
		 DecimalFormat df = new DecimalFormat("#.0");
		 g2d.drawString(label, left+length/2,top/2);
		 
		 for(int i = 0; i < bins.length; i++){
			 int x = left + xTic*i;
			 int y = (int)(top + height - yTic * bins[i]);
			 g2d.draw(new Rectangle2D.Double(x, y,xTic, yTic * bins[i]));
			 
			 String count = df.format(bins[i]);
			 g2d.drawString(count,x+xTic/2,y-3);
			 if(i<boundaries.length) g2d.drawString(df.format(boundaries[i]), x+xTic - 5, top+height+12);
			 
		 }
		 	
	 }
	
	
	
	
}



