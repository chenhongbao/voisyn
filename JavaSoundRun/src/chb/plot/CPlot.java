
package chb.plot;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import  java.lang.Math;

import org.math.plot.Plot2DPanel;
import org.math.plot.Plot3DPanel;
import org.math.plot.PlotPanel;

/**
 * CPlot class provides auxiliary methods to plot figures.
 * @author Hongbao Chen
 *
 */
public class CPlot {

	public static double PI = Math.PI;
	public static double E = Math.E;
	
	private PlotPanel Panel = null;
	private CData  Data;
	
	public String Legend = "X-Y Plot";
	public String PlotName = "Data Plot";
	
	public int XSize = 500;
	public int YSize = 600;
	
	
	public static String SOUTH = "SOUTH";
	public static String EAST = "EAST";
	public static String NORTH = "NORTH";
	public static String WEST = "WEST";
	
	/**
	 * Plot the figure with the data provided.
	 * @param name the name of the figure.
	 * @param legend the legend of the figure.
	 * @param data the data to plot.
	 * @param visible if true, it will display the figure, vice versa.
	 * @return the CPlot instance for the current plotting.
	 */
	public static CPlot Plot(String name, String legend, 
			CData data, boolean visible) {
		
		if(data.Validate() == false)
			return null;
		
		CPlot plot = new CPlot();
		plot.SetData(data);
		
		plot.PlotName = name;
		plot.Legend = legend;
		
		plot.__Plot__(visible);
		
		return plot;
	}
	
	/**
	 * Plot the data using the CPlot instance provided by plot.
	 * @param plot reference to the CPlot instance.
	 * @param name the name of the figure.
	 * @param data the data to be plotted.
	 * @param visible if true, it will display the figure, vice versa.
	 * @return the reference to the CPlot (the same as plot).
	 */
	public static CPlot Plot(CPlot plot, String name, CData data, boolean visible) {
		if(data.Validate() == false)
			return null;
		
		if(plot == null)
			return null;
		
		PlotPanel panel = plot.GetPanel();
		int dimen = plot.GetDimension();
		if(dimen != data.GetDimension())
			return plot;
		
		if(dimen == 0) {
			return plot;
		}
		else if(dimen == 2){
			Plot2DPanel pn2d = (Plot2DPanel)panel;
			pn2d.addLinePlot(name, data.getX(), data.getY());
		} else {
			Plot3DPanel pn3d = (Plot3DPanel)panel;
			pn3d.addGridPlot(name, data.getX(), data.getY(), data.getZ());
		}
		
		if(visible) 
			plot.SetVisible();
		
		return plot;
	}
	
	/**
	 * Plot the data simply.
	 * @param data the data to be plotted.
	 * @param visible if true, it will display the figure, vice versa. 
	 * @return the CPlot instance for the current plotting.
	 */
	public static CPlot Plot(CData data, boolean visible) {
		if(data.Validate() == false)
			return null;
		
		CPlot plot = new CPlot();
		plot.SetData(data);
		plot.__Plot__(visible);
		
		return plot;
		
	}

	/**
	 * Plot the data holding by this CPlot instance.
	 * @param visible  if true, it will display the figure, vice versa.
	 */
	private void __Plot__(boolean visible) {
		if(this.Data == null) {
			JOptionPane.showMessageDialog(null, "Data is null");
			return;
		}
		
		if(Data.GetDimension() == 0) {
			JOptionPane.showMessageDialog(null, "Data is not valid");
			return;
		} else if(Data.GetDimension() == 2) {
			Plot2DPanel plot = new Plot2DPanel();
			
			plot.addLinePlot(this.PlotName, this.Data.getX(), this.Data.getY());
			plot.addLegend(this.Legend);
			
			if(visible == true) {
				SetVisible(plot);
				this.Panel = plot;
			} else {
				this.Panel = plot;
			}
			
		} else {
			Plot3DPanel plot = new Plot3DPanel(this.PlotName);
			plot.addGridPlot(this.PlotName, this.Data.getX(), this.Data.getY(), this.Data.getZ());
			plot.addLegend(this.Legend);
			
			if(visible == true) {
				SetVisible(plot);			
				this.Panel = plot;
			} else {
				this.Panel = plot;
			}
		}
		
	}
	
	/**
	 * Display a frame to show the figure using the data of this instance.
	 */
	public void SetVisible() {
		this.SetVisible(this.Panel);
	}
	
	/**
	 * Display a frame to show the figure using the data of the plot.
	 * @param plot PlotPanel to be shown in the frame.
	 */
	public void SetVisible(PlotPanel plot) {
		
		if(plot == null)
			return;
		
		JFrame frame = new JFrame(this.PlotName);
		frame.setContentPane(plot);
		frame.setSize(this.XSize, this.YSize);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	
	/**
	 * Get the PlotPanel of this CPlot instance.
	 * @return the reference to the PlotPanel.
	 */
	public PlotPanel GetPanel() {
		return this.Panel;
	}
	
	/**
	 * Get the dimension of the CData.
	 * @return 0 for error, 2 for 2-D and 3 for 3-D.
	 */
	public int GetDimension() {
		if(this.Data == null)
			return 0;
		
		return this.Data.GetDimension();
	}

	/**
	 * Get the data of the CPlot.
	 * @return the data
	 */
	public CData GetData() {
		return Data;
	}

	/**
	 * Set the data of ths CPlot.
	 * @param data the data to set
	 */
	public void SetData(CData data) {
		Data = data;
	}
	
	

}
