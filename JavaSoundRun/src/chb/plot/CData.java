/**
 * 
 */
package chb.plot;

import java.util.Arrays;

/**
 * CData wraps the data, both the 2-D and 3-D.
 * @author Hongbao Chen
 *
 */
public class CData {
	/*
	 * TODO Rewrite setter or getter to cut down the number of samples used to plot.
	 * We must cut down the number of samples used to draw a figure
	 *because jmathplot is really slow in processsing large dataset.
	 *Probably, we can inspect the getter or setter to have this job completed. 
	 */
	private double[] X = null;
	private double[] Y = null;
	private double[][] Z = null;
	
	public CData(double[] _x, double[] _y, double[][] _z) {
		this.setX(_x);
		this.setY(_y);
		this.setZ(_z);
	}
	/**
	 * Generate the double range between start and end, with the interval 
	 * as step-length.
	 * @param start start of the range.
	 * @param interval end of the range.
	 * @param end step length of the range.
	 * @return the range generated.
	 */
	public static double[] GetRange(double start, double interval, double end) {
		
		if(start<end && interval <0)
			return null;
		if(start > end && interval >0)
			return null;
		
		int num = (int) (Math.abs(
				Math.ceil((end-start)/interval))+1);
		
		double[] res = new double[num];
		
		double tmp = start;
		
		for(int i = 0; i<res.length; ++i) {
			res[i] = tmp;
			tmp += interval;
		}
			
		return res;
	}

	/**
	 * Generate the integer range between start and end, with the interval 
	 * as step-length.
	 * @param start start of the range.
	 * @param interval end of the range.
	 * @param end step length of the range.
	 * @return the range generated.
	 */
	public static int[] GetRange(int start, int interval, int end) {
		
		if(start<end && interval <0)
			return null;
		if(start > end && interval >0)
			return null;
		
		int num = (int) (Math.abs(
				Math.ceil((end-start)/interval))+1);
		
		int[] res = new int[num];
		
		int tmp = start;
		
		for(int i = 0; i<res.length; ++i) {
			res[i] = tmp;
			tmp += interval;
		}
			
		return res;
		
	}
	/**
	 * Deep copy of a 2-D array.
	 * @param input the array to be copied
	 * @return the new instance of the same array.
	 */
	public static double[][] DeepCopy2DArray(double[][] input) {
		double[][] target = new double[input.length][];
	      for (int i=0; i <input.length; i++) {
	        target[i] = Arrays.copyOfRange(input[i], 0,  input[i].length);
	      }
	      return target;
	}

	/**
	 * Get the maximum and minimum values of array.
	 * @param array the double array for examining.
	 * @return the double[2] containing maximum value at index 1 
	 * and minimum value at index 0.
	 */
	public static double[] GetMaxMin(double[] array) {
		
		if(array == null)
			return null;
		
		double[] res = new double[]{0.0D, 0.0D};
		
		for(double d: array) {
			if(d<res[0])
				res[0] = d;
			if(d>res[1])
				res[1] = d;
		}
		
		return res;
	}

	/**
	 * Normalize the double array in spot.
	 * @param data array to be normalized.
	 * @return the reference to data.
	 */
	public static double[] Normalize(double[] data) {

		if(data == null)
			return null;
		
		double[] range = GetMaxMin(data);		
		for(double d: data) 
			d = d/range[1];
		
		return data;
	}

	/**
	 * Get the number of dimensions for the current data.
	 * @return 2 for 2-D, 3 for 3-D and 0 for error.
	 */
	public int GetDimension() {
		if(Validate() == false)
			return 0;
		
		if(this.Z == null)
			return 2;
		else
			return 3;
	}

	/**
	 * Get the normalized Y.
	 * @return the reference to the new instance of double array, containing 
	 * the normalize values of Y.
	 */
	public double[] GetNormY() {
		if(this.Y == null)
			return null;
		else {
			double[] cpy = Arrays.copyOfRange(this.Y, 0, this.Y.length);
			return Normalize(cpy);
		}
	}

	/**
	 * Get the normalized Z.
	 * @return the reference to the new instance of double array, containing 
	 * the normalize values of Z.
	 */
	public double[][] GetNormZ() {
		if(this.Z == null)
			return null;
		
		double[][] cpy = DeepCopy2DArray(this.Z);
		
		for(int i =0; i<this.Z.length; ++i) {
			cpy[i] = Normalize(cpy[i]);
		}
			
		return cpy;
	}
	
	/**
	 * Set a copy of X.
	 * @return the x
	 */
	public double[] getX() {
		if (this.X == null) 
			return null;
		
		return Arrays.copyOfRange(X, 0, X.length);
	}

	/**
	 * Get the range of X.
	 * @return the double[2] containing maximum value at index 1 
	 * and minimum value at index 0.
	 */
	public double[] GetXRange() {
		
		return GetMaxMin(this.X);
	}
	
	/**
	 * Get a copy of Y.
	 * @return the y
	 */
	public double[] getY() {
		if (this.Y == null) 
			return null;
		
		return Arrays.copyOfRange(Y, 0, Y.length);
	}
	
	/**
	 * Get the range of Y.
	 * @return the double[2] containing maximum value at index 1 
	 * and minimum value at index 0.
	 */
	public double[] GetYRange() {
		
		return GetMaxMin(this.Y);
	}
	
	/**
	 * Get a copy of Z.
	 * @return the z
	 */
	public double[][] getZ() {
		if (this.Z == null) 
			return null;
		
		return CData.DeepCopy2DArray(this.Z);
	}
	
	/**
	 * Get the range of Z.
	 * @return the double[2] containing maximum value at index 1 
	 * and minimum value at index 0.
	 */
	public double[] GetZRange() {
		
		double[]  res = null;
		
		for(double[] d: this.Z) {
			double[] tmp = GetMaxMin(d);
			
			if(res == null) {
				res = tmp;
			}
			else {
				if(tmp[0]<res[0])
					res[0] = tmp[0];
				if(tmp[1]>res[1])
					res[1] = tmp[1];
			}
		}
		
		return res;
	}
	
	/**
	 * Set the X to the given samples, copied by value.
	 * @param x the x to set
	 */
	public void setX(double[] x) {
		if(x == null)
			return;
		
		this.X = Arrays.copyOfRange(x, 0, x.length);
	}
	
	/**
	 * Set the Y by value.
	 * @param y the y to set
	 */
	public void setY(double[] y) {
		if(y == null)
			return;
		
		this.Y = Arrays.copyOfRange(y, 0, y.length);
	}
	
	/**
	 * Set Z by value.
	 * @param z the z to set
	 */
	public void setZ(double[][] z) {
		if(z == null)
			return;
		
		this.Z = CData.DeepCopy2DArray(z);
	}
	
	/**
	 * Test whether the CData is valid.
	 * @return true if data is valid, or flase if it is not.
	 */
	public boolean Validate() {
		
		if(this.X == null || this.Y == null)
			return false;
		
		if(this.Z == null) {
			return this.X.length ==  this.Y.length;
		} else {
			if(Z[0] == null)
				return false;
			
			return Z.length == Y.length && Z[0].length == X.length;
		}
	}
	
}
