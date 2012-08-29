package chb.synthesis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import chb.math.Numerics;

/**
 * VoiceSynthesizer class provides all the necessary interfaces to process the
 * voice samples.
 * @author Hongbao Chen
 *
 */
public class VoiceSynthesizer {
	
	public int AVERAGE_WINDOW_HALF = 100;
	public int DUPLICATE_WINDOW = 2;
	
	/**
	 * Take one frame out of the samples x, from index start to index end. This method
	 * will supress the overflow of the sample value.
	 * @param x the samples of input.
	 * @param start the starting index.
	 * @param endi the ending index.
	 * @return a long array containing frame point values.
	 */
	public short[] GetEpoch(short[] x, int start, int endi) {
		if(x == null)
			return null;
		
		if(endi < 0  || start < 0)
			return null;
		
		if(endi < start) {
			int temp = endi;
			endi = start;
			start = temp;
		}
		
		double[] H = Numerics.GaussWin(endi-start+1, 3);
		short[] res = new short[endi-start+1];
		int count = 0;
		
		for(int i = start; i<=start+endi; ++i) {
			res[count] = Numerics.MultiplySupress(x[i], H[count]);
			++count;
		}
		
		return res;
	}
	
	/**
	 * Take one frame out of the samples x, from index start to index end. This method
	 * will <b>not</b> supress the overflow of the sample value.
	 * @param x the samples of input.
	 * @param start the starting index.
	 * @param endi the ending index.
	 * @return a long array containing frame point values.
	 */
	public long[] GetEpochL(short[] x, int start, int endi) {
		if(x == null)
			return null;
		
		if(endi < 0  || start < 0)
			return null;
		
		if(endi < start) {
			int temp = endi;
			endi = start;
			start = temp;
		}
		
		double[] H = Numerics.GaussWin(endi-start+1, 3);
		long[] res = new long[endi-start+1];
		int count = 0;
		
		for(int i = start; i<=start+endi; ++i) {
			res[count] = Numerics.Multiply(x[i], H[i]);
			++count;
		}
		
		return res;
	}
	
	/**
	 * Connect some samples into one.
	 * @param samples the variable-length samples.
	 * @return the final connected sample.
	 */
	public short[] GetConnect(short[]...samples) {
		if(samples.length == 0)
			return null;
		
		int count = 0;
		for(short[] s: samples) {
			if(s != null)
				count += s.length;
		}
		
		short[] res = new short[count];
		int index = 0;
		for(int i=0; i<samples.length; ++i) {
			for(int j=0; j<samples[i].length; ++j) {
				res[index] = samples[i][j];
				++index;
			}
		}
		
		return res;
	}
	
	/**
	 * Get the indexes of all peaks in the sample and return it as an integer array.
	 * @param y the sample.
	 * @return an integer array holding all the indexes of peaks.
	 */
	public int[] GetPeak(short[] y) {
		
		if(y == null)
			return null;
		
		/*
		 * Filter the fluctuation using moving average.
		 */
		double[] v = new double[y.length];
		for(int i=0; i < y.length; ++i) {
			v[i] = Numerics.AverageN(y, i, AVERAGE_WINDOW_HALF);
		}
		
		double[] w = new double[v.length];
		for(int i=0; i< v.length; ++i) {
			w[i] = Numerics.AverageN(v, i, AVERAGE_WINDOW_HALF);
		}
		
		double[] z = Numerics.ZerosD(y.length);
		for(int i =0; i<w.length-1; ++i) {
			if(w[i]*w[i+1] < 0.0D) {
				z[i] = 0.3D; //Any number other than zero is OK.
			}
		}
		
		/*
		 * Check the cross-zero points.
		 */
		List<Integer> px = new ArrayList<Integer>();
		px.add(0);
		for(int i=0; i<  z.length; ++i) {
			if(z[i] > 0.0D) 
				px.add(i);
		}
		px.add(z.length-1);
		
		/*
		 * Check the peak inside each period.
		 */
		List<Integer> peakx =  new ArrayList<Integer>();
		if(px.size()>3) {
			int i = 2;
			while(i <= px.size()-1) {
				
				short[] tmp = Numerics.Pick(y, px.get(i-2), px.get(i));
				Object[] max_idx = Numerics.Max(tmp);
				int L = (Integer)max_idx[1];
				
				if(L-px.get(i-1) < (px.get(i)-px.get(i-2))/5) {
					peakx.add(px.get(i-2)+L);
				}
				
				if(peakx.size() == 0)
					i = i+1;
				else 
					i = i+2;				
			}
			
		} else {
			peakx.add((int)Math.floor(z.length/2.0D));
		}
		
		/*
		 * Change peakx from List to Array.
		 */
		Integer[] ints = (Integer[]) peakx.toArray();
		int[] res = new int[ints.length];
		for(int i=0; i<ints.length; ++i)
			res[i] = (int)ints[i];
		
		return res;
		
	}
	/**
	 * Get the nearest peak index to the maximum value of the sample x.
	 * @param x the sample.
	 * @param peakx peak indexes.
	 * @return the index of the peak.
	 */
	public int GetPeakIndex(short[] x, int[] peakx) {
		Object[] max_idx = Numerics.Max(x);
		int L = (Integer)max_idx[1];
		
		int idx = peakx.length-1;
		Arrays.sort(peakx);
		
		for(int i=0; i<peakx.length; ++i) {
			if( peakx[i] > L) {
				idx = i;
				break;
			}
		}
		
		int y = 0;
		if(idx == peakx.length-1) {
			y = idx;
		}
		else if(idx == 1) {
			y = 1;
		}
		else {
			if(L-peakx[idx-1] > peakx[idx]-L) 
				y = idx;
			else
				y = idx-1;
		}
		
		return y;	
	}
	
	public short[] GetNarrow(double allrate, double before_peak_rate, 
			double after_peak_rate, short[] x, int[] peakx) {
		
		allrate = Math.abs(allrate);
		before_peak_rate = Math.abs(before_peak_rate);
		after_peak_rate = Math.abs(after_peak_rate);
		
		if(allrate == 0.0D)
			allrate = 1.0D;
		if(before_peak_rate == 0.0D) 
			before_peak_rate = allrate;
		if(after_peak_rate == 0.0D)
			after_peak_rate = allrate;
		
		int pi = GetPeakIndex(x, peakx);
		Random rds = new Random();
		
		short[] y = null;
		
		if(1.0D - before_peak_rate > 0.0D) {
			// Shorten the audio
			
			int bp_not_num = (int)Math.floor(before_peak_rate*pi);
			List<Integer> bp_not_idx = Numerics.GetRangeL(0, 1, pi);
			
			while(bp_not_idx.size() > bp_not_num) {
				// Generate persudo number ranging from 0 to MAX_VALUE.
				int i = rds.nextInt(Integer.MAX_VALUE)%pi;
				bp_not_idx.remove(i);
			}
			
			Collections.sort(bp_not_idx);
			y = GetConnectIndex(bp_not_idx, x, peakx);
			
		} else {
			// Elongate the audio
			
			int bp_not_num = (int)Math.floor(before_peak_rate * pi);
			List<Integer>bp_not_idx = Numerics.GetRangeL(0, 1, pi);
			List<Integer>bp_not_idx_add = new ArrayList<Integer>();
			
			while(bp_not_idx.size() + bp_not_idx_add.size() < bp_not_num) {
				double ratio = rds.nextDouble();
				bp_not_idx_add.add(GetUpSlope(x, peakx, ratio));
			}
			
			bp_not_idx = GetMix(bp_not_idx, bp_not_idx_add);
			y = GetConnectIndex(bp_not_idx, x, peakx);
		}
		
		//TODO To be continued
		
		
		return y;
	}
	
	/**
	 * Connect all the samples according to the indexes in bp_not_idx.
	 * @param bp_not_idx indexes to be connected.
	 * @param x samples.
	 * @param peakx indexes of peaks.
	 * @return the connected samples.
	 */
	public short[] GetConnectIndex(List<Integer> bp_not_idx, short[] x, int[] peakx) {
		short[] y =null;
		
		for(int i=0; i< bp_not_idx.size(); ++i) {
			int ii = bp_not_idx.get(i);
			if(i == 0) {
				short[] tmp = Numerics.Pick(x, 0, peakx[ii]);
				y = GetConnect(y, tmp);
			} else {
				short[] tmp = null;
				if(ii == 0) {
					tmp = Numerics.Pick(x, 0, peakx[ii]);
				} else {
					tmp = Numerics.Pick(x, peakx[ii-1] + 1, peakx[ii]);
				}
				y = GetConnect(y, tmp);
			}
		}
		
		return y;
		
	}
	
	/**
	 * Get the index of the point on upword slope, whose value is most approximate
	 * the 'ratio*peak_value'.
	 * @param x the samples.
	 * @param peakx indexes of peaks.
	 * @param ratio the ratio of the value/peak on the slope.
	 * @return the index of the point on the slope.
	 */
	public int GetUpSlope(short[] x, int[] peakx, double ratio) {
		// Return nagative one to indicate error.
		if(x == null || peakx == null)
			return -1;
		
		Arrays.sort(peakx);
		int pi = GetPeakIndex(x, peakx);
		Object[] res = Numerics.Max(x);
		int Y = (int)res[0];
		int z = 0;
		
		double Y_m =  Y * ratio;
		double Y_d = Double.MAX_VALUE;
		
		for(int i=0; i<=pi; ++i) {
			double tmp = Math.abs(x[peakx[i]] - Y_m);
			if(Y_d > tmp) {
				Y_d = tmp;
				z = i;
			}
		}
		
		return z;
	}
	
	/**
	 * Get the index of the point on downword slope, whose value is most approximate
	 * the 'ratio*peak_value'.
	 * @param x the samples.
	 * @param peakx indexes of peaks.
	 * @param ratio the ratio of the value/peak on the slope.
	 * @return the index of the point on the slope.
	 */
	public int GetDownSlope(short[] x, int[] peakx, double ratio) {
		// Return nagative one to indicate error.
		if(x == null || peakx == null)
			return -1;
		
		Arrays.sort(peakx);
		int pi = GetPeakIndex(x, peakx);
		Object[] res = Numerics.Max(x);
		int Y = (Integer)res[0];
		int z = 0;
		
		double Y_m = Y * ratio;
		double Y_d = Double.MAX_VALUE;
		
		for(int i=peakx.length -1; i >= pi+1; --i) {
			double tmp = Math.abs(x[peakx[i]] - Y_m);
			if(Y_d > tmp) {
				Y_d = tmp;
				z = i;
			}
		}
		
		return z;
	}
	
	/**
	 * Mix the indexes in y into x, all the indexes will be adjusted to make sure the 
	 * samples generated from this indexes look natural.
	 * @param x the destinated indexes.
	 * @param y the source indexes.
	 * @return the mixes indexes.
	 */
	public int[] GetMix(int[] x, int[] y) {
		if(x == null || y == null)
			return null;
		
		int totalnum = x.length + y.length;
		Random rd = new Random();
		
		y = Numerics.Unique(y);
		List<Integer> xolist = Numerics.GetListFrom(x);
		List<Integer> xlist = Numerics.GetListFrom(x);
		List<Integer> ylist = Numerics.GetListFrom(y);
		
		Collections.sort(xlist);
		Collections.sort(ylist);
		
		while(xlist.size() < totalnum) {
			// The index of y, get the value with this index.
			int index = rd.nextInt(Integer.MAX_VALUE)%ylist.size();
			// The value is stored in pk.
			int pk = ylist.get(index);
			// Get the index of x, whose value is equal to pk, and the ending index
			// of the generated duplication of peaks.
			int index_x = xolist.lastIndexOf(pk);		
			int index_x_end = Math.min(index_x+DUPLICATE_WINDOW, xolist.size()-1);
			
			// Generate the indexes to add to the x.
			List<Integer> tmp = Numerics.GetRangeL(index_x, 1, index_x_end);
			// Find the index of xlist to insert.
			int index_insert = Math.max(xlist.lastIndexOf(pk) - 1, 0);
			// Add indexes to x.
			xlist.addAll(index_insert, tmp);
			
			// Because the index in y has been added, delete it from y.
			ylist.remove(index);
		}
		
		return Numerics.GetArrayFrom(xlist);
	}
	
	public List<Integer> GetMix(List<Integer> x, List<Integer> y) {
		int[] x1 = Numerics.GetArrayFrom(x);
		int[] y1 = Numerics.GetArrayFrom(y);
		
		int[] res = GetMix(x1, y1);
		
		return Numerics.GetListFrom(res);
	}
	
}
