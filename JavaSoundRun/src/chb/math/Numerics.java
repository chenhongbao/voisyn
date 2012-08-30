package chb.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Hongbao Chen
 *
 */
public class Numerics {

	/**
	 * Gaussian window. It is implemented according to the document:<br>
	 * http://www.mathworks.cn/help/toolbox/signal/ref/gausswin.html
	 * @param N the total number of points in gaussian window.
	 * @param alpha alpha defined in gaussian window.
	 * @return the array of values in gaussian window.
	 */
	public static double[] GaussWin(int N, double alpha) {
		int start = (-1)*(int)Math.floor(N/2.0D);
		int end = (int)Math.floor(N/2.0D);
		
		double[] res = new double[N];
		int count = 0;
		for(int i = start; i <end; ++i) {
			res[count] = GaussWinPoint(i, N, alpha);
			++count;
		}
		
		return res;
	}
	
	/**
	 * Compute a single point in the gaussian window.
	 * @param n the n'th point.
	 * @param N the total number of points.
	 * @param alpha alpha defined in gaussian window.
	 * @return the value of the single point in gaussian window.
	 */
	public static double GaussWinPoint(int n, int N, double alpha) {
		
		double a = alpha*(n/(N/2.0D));
		double b = -0.5D*a*a;
		
		return Math.exp(b);
	}
	
	/**
	 * Multiply one short value and one double value. If the result is larger
	 * than Short.MAX_VALUE, it will supress all the values above the maximum
	 * short value to the maximum short value.
	 * @param d short value.
	 * @param alpha double value as amplifying factor.
	 * @return the short value, may have been supressed.
	 */
	public static short MultiplySupress(short d, double alpha) {
		double r = d*alpha;
		if(r>Short.MAX_VALUE)
			r = Short.MAX_VALUE;
		
		if(r<Short.MIN_VALUE)
			r = Short.MIN_VALUE;
		
		return (short)Math.floor(r);
	}
	
	/**
	 * Multiply one short value and one double value. No supressing.
	 * @param d short value.
	 * @param alpha double value as amplifying factor.
	 * @return the short value.
	 */
	public static long Multiply(short d, double alpha) {
		double r = d*alpha;	
		return (long)Math.floor(r);
	}
	
	/**
	 * Compute the moving average value at the point i, from i-n to i+n, inclusive.
	 * @param x the sample.
	 * @param i the center (position) of the moving averaging.
	 * @param n the bound (left, right) of the moving averaging.
	 * @return the average value of the samples.
	 */
	public static double AverageN(short[] x, int i, int n) {
		long avr = 0L;
		int count = 0;
		for(int k = i-n; k <= +n; ++k) {
			if(k>=0 && k<x.length) {
				avr += x[k];
				++count;
			}
		}
		
		return (double)(1.0D*avr/count);
	}
	
	public static double AverageN(double[] x, int i, int n) {
		double avr = 0.0D;
		int count = 0;
		for(int k = i-n; k <= +n; ++k) {
			if(k>=0 && k<x.length) {
				avr += x[k];
				++count;
			}
		}
		
		return (double)(1.0D*avr/count);
	}
	
	/**
	 * Create an array of n elements, with each equal to zero.
	 * @param n the total number of elements.
	 * @return the array of zero-elements.
	 */
	public static double[] ZerosD(int n) {
		double[] res = new double[n];
		for(int i=0; i< res.length; ++i)
			res[i] = 0.0D;
		
		return res;
	}
	
	public static int[] ZerosI(int n) {
		int[] res = new int[n];
		for(int i=0; i< res.length; ++i)
			res[i] = 0;
		
		return res;
	}
	
	public static long[] ZerosL(int n) {
		long[] res = new long[n];
		for(int i=0; i< res.length; ++i)
			res[i] = 0L;
		
		return res;
	}
	
	public static short[] ZerosS(int n) {
		short[] res = new short[n];
		for(int i=0; i< res.length; ++i)
			res[i] = 0;
		
		return res;
	}
	
	/**
	 * Find the maximum value in a double array and its index.
	 * @param x the double array.
	 * @return a pair of Objects, of types Double and Integer.
	 */
	public static Object[] Max(short[] x) {
		if(x == null)
			return null;
		
		int index = 0;
		short value = Short.MIN_VALUE;
		
		for(int i = 0; i<x.length; ++i) {
			if(value<x[i]) {
				value = x[i];
				index = i;
			}
		}
		
		return new Object[]{(Short)value, (Integer)index};
	}
	
	/**
	 * Get some elements ranging from start to end, from x.
	 * @param x the original samples.
	 * @param start starting index.
	 * @param end ending index.
	 * @return the short array.
	 */
	public static short[] Pick(short[] x, int start, int end) {
		if(x == null)
			return null;
		
		if(start<0 || end<0)
			return null;
		
		short[] res = new short[end-start+1];
		int count = 0;
		for(int i=start; i<= end; ++i) {
			res[count] =  res[i];
			++count;
		}
		
		return res;
	}
	
	/**
	 * Get a range from from to to, with difference of interval.
	 * @param from starting index.
	 * @param interval difference between the two adjacent elements.
	 * @param to ending index.
	 * @return the list of the range.
	 */
	public static List<Integer> GetRangeL(int from, int interval, int to) {
		if(to > from && interval < 0)
			return null;
		if(to < from && interval > 0)
			return null;
		
		List<Integer> res = new ArrayList<Integer>();
		for(int i = from; i <= to; i += interval)
			res.add(i);
		
		return res;
	}
	
	public static int[] Unique(int[] x) {
		List<Integer> list = new ArrayList<Integer>();
		for(int i: x) {
			if(list.contains(i))
				continue;
			list.add(i);
		}
		
		Object[] tmp = list.toArray();
		int[] tmp2 = new int[tmp.length];
		for(int i=0; i<tmp2.length; ++i) 
			tmp2[i] = (Integer)tmp[i];
		
		return tmp2;
	}
	
	/**
	 * Get List<Object> from array.
	 * @param x array.
	 * @return List<T> reference.
	 */
	public static List<Integer> GetListFrom(int[] x) {
		if(x == null)
			return null;
		
		List<Integer> list = new ArrayList<Integer>();
		for(int i: x)
			list.add(i);
		
		return list;
	}
	
	public static List<Short> GetListFrom(short[] x) {
		if(x == null)
			return null;
		
		List<Short> list = new ArrayList<Short>();
		for(short s: x)
			list.add(s);
		
		return list;
	}
	
	/**
	 * Get array from the List.
	 * @param x List.
	 * @return Array.
	 */
	public static int[] GetArrayFromL(List<Integer> x) {
		if(x==null)
			return null;
		
		Object[] res = x.toArray();
		int[] res2 = new int[res.length];
		
		for(int i = 0; i< res2.length; ++i)
			res2[i] = (Integer)res[i];
		
		return res2;
	}
	
	public static short[] GetArrayFromS(List<Short> x) {
		if(x == null)
			return null;
		
		Object[] res = x.toArray();
		short[] res2 = new short[res.length];
		
		for(int i=0; i<res2.length; ++i)
			res2[i] = (Short)res[i];
		
		return res2;
	}
	
	/**
	 * Return the number of the samples whose values are equal to y.
	 * @param x samples.
	 * @param y value.
	 * @return the number of samples with the value of y.
	 */
	public static int GetSameCount(List<Integer> x, int y) {
		if(x == null)
			return 0;
		
		int count = 0;
		for(Integer i: x) {
			if(i == y)
				++count;
		}
		
		return count;
	}
	
	/**
	 * Pick out an element randomly from the sample x.
	 * @param x the samples.
	 * @return the randomly picked-out element.
	 */
	public static short PickRandom(short[] x) {
		Random rd = new Random();
		int t = rd.nextInt(Integer.MAX_VALUE);
		return x[t%x.length];
	}
	
	/**
	 * Supress all the samples with value above Short.MAX_VALUE or below
	 * Short.MIN_VALUE.
	 * @param x the samples.
	 * @return the supressed samples.
	 */
	public static short[] SupressS(long[] x) {
		if(x == null)
			return null;
		
		short[] res = new short[x.length];
		for(int i=0; i<x.length; ++i) {
			if(x[i] > Short.MAX_VALUE)
				res[i] = Short.MAX_VALUE;
			else if(x[i] < Short.MIN_VALUE)
				res[i] = Short.MIN_VALUE;
			else
				res[i] = (short)x[i];
		}
		return res;
	}
}
