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
 * 
 * @author Hongbao Chen
 * 
 */
public class VoiceSynthesizer {

	public int AVERAGE_WINDOW_HALF = 100;
	public int DUPLICATE_WINDOW = 2;
	public double INTERVAL_RATIO = 0.95D;
	public double TRIM_PORTION = 0.05D;

	/**
	 * Take one frame out of the samples x, from index start to index end. This
	 * method will supress the overflow of the sample value.
	 * 
	 * @param x
	 *            the samples of input.
	 * @param start
	 *            the starting index.
	 * @param endi
	 *            the ending index.
	 * @return a long array containing frame point values.
	 */
	public short[] GetEpoch(short[] x, int start, int endi) {
		if (x == null)
			return null;

		if (endi < 0 || start < 0)
			return null;

		if (endi < start) {
			int temp = endi;
			endi = start;
			start = temp;
		}

		double[] H = Numerics.GaussWin(endi - start + 1, 3);
		short[] res = new short[endi - start + 1];
		int count = 0;

		for (int i = start; i <= start + endi; ++i) {
			res[count] = Numerics.MultiplySupress(x[i], H[count]);
			++count;
		}

		return res;
	}

	/**
	 * Take one frame out of the samples x, from index start to index end. This
	 * method will <b>not</b> supress the overflow of the sample value.
	 * 
	 * @param x
	 *            the samples of input.
	 * @param start
	 *            the starting index.
	 * @param endi
	 *            the ending index.
	 * @return a long array containing frame point values.
	 */
	public long[] GetEpochL(short[] x, int start, int endi) {
		if (x == null)
			return null;

		if (endi < 0 || start < 0)
			return null;

		if (endi < start) {
			int temp = endi;
			endi = start;
			start = temp;
		}

		double[] H = Numerics.GaussWin(endi - start + 1, 3);
		long[] res = new long[endi - start + 1];
		int count = 0;

		for (int i = start; i <= start + endi; ++i) {
			res[count] = Numerics.Multiply(x[i], H[i]);
			++count;
		}

		return res;
	}

	/**
	 * Connect some samples into one.
	 * 
	 * @param samples
	 *            the variable-length samples.
	 * @return the final connected sample.
	 */
	public short[] GetConnect(short[]... samples) {
		if (samples.length == 0)
			return null;

		int count = 0;
		for (short[] s : samples) {
			if (s != null)
				count += s.length;
		}

		short[] res = new short[count];
		int index = 0;
		for (int i = 0; i < samples.length; ++i) {
			for (int j = 0; j < samples[i].length; ++j) {
				res[index] = samples[i][j];
				++index;
			}
		}

		return res;
	}

	/**
	 * Get the indexes of all peaks in the sample and return it as an integer
	 * array.
	 * 
	 * @param y
	 *            the sample.
	 * @return an integer array holding all the indexes of peaks.
	 */
	public int[] GetPeak(short[] y) {

		if (y == null)
			return null;

		/*
		 * Filter the fluctuation using moving average.
		 */
		double[] v = new double[y.length];
		for (int i = 0; i < y.length; ++i) {
			v[i] = Numerics.AverageN(y, i, AVERAGE_WINDOW_HALF);
		}

		double[] w = new double[v.length];
		for (int i = 0; i < v.length; ++i) {
			w[i] = Numerics.AverageN(v, i, AVERAGE_WINDOW_HALF);
		}

		double[] z = Numerics.ZerosD(y.length);
		for (int i = 0; i < w.length - 1; ++i) {
			if (w[i] * w[i + 1] < 0.0D) {
				z[i] = 0.3D; // Any number other than zero is OK.
			}
		}

		/*
		 * Check the cross-zero points.
		 */
		List<Integer> px = new ArrayList<Integer>();
		px.add(0);
		for (int i = 0; i < z.length; ++i) {
			if (z[i] > 0.0D)
				px.add(i);
		}
		px.add(z.length - 1);

		/*
		 * Check the peak inside each period.
		 */
		List<Integer> peakx = new ArrayList<Integer>();
		if (px.size() > 3) {
			int i = 2;
			while (i <= px.size() - 1) {

				short[] tmp = Numerics.Pick(y, px.get(i - 2), px.get(i));
				Object[] max_idx = Numerics.Max(tmp);
				int L = (Integer) max_idx[1];

				if (L - px.get(i - 1) < (px.get(i) - px.get(i - 2)) / 5) {
					peakx.add(px.get(i - 2) + L);
				}

				if (peakx.size() == 0)
					i = i + 1;
				else
					i = i + 2;
			}

		} else {
			peakx.add((int) Math.floor(z.length / 2.0D));
		}

		/*
		 * Change peakx from List to Array.
		 */
		Integer[] ints = (Integer[]) peakx.toArray();
		int[] res = new int[ints.length];
		for (int i = 0; i < ints.length; ++i)
			res[i] = (int) ints[i];

		return res;

	}

	/**
	 * Get the nearest peak index to the maximum value of the sample x.
	 * 
	 * @param x
	 *            the sample.
	 * @param peakx
	 *            peak indexes.
	 * @return the index of the peak.
	 */
	public int GetPeakIndex(short[] x, int[] peakx) {
		Object[] max_idx = Numerics.Max(x);
		int L = (Integer) max_idx[1];

		int idx = peakx.length - 1;
		Arrays.sort(peakx);

		for (int i = 0; i < peakx.length; ++i) {
			if (peakx[i] > L) {
				idx = i;
				break;
			}
		}

		int y = 0;
		if (idx == peakx.length - 1) {
			y = idx;
		} else if (idx == 1) {
			y = 1;
		} else {
			if (L - peakx[idx - 1] > peakx[idx] - L)
				y = idx;
			else
				y = idx - 1;
		}

		return y;
	}

	/**
	 * Shorten or elongate the first half and second half of the samples, separated
	 * by the acme in the samples.
	 * @param allrate the total factor.
	 * @param before_peak_rate the factor before the acme.
	 * @param after_peak_rate the factor before the acme.
	 * @param x the samples.
	 * @param peakx the indexes of all peaks in the samples.
	 * @return the extended or shortened samples.
	 */
	public short[] GetNarrow(double allrate, double before_peak_rate,
			double after_peak_rate, short[] x, int[] peakx) {

		allrate = Math.abs(allrate);
		before_peak_rate = Math.abs(before_peak_rate);
		after_peak_rate = Math.abs(after_peak_rate);

		if (allrate == 0.0D)
			allrate = 1.0D;
		if (before_peak_rate == 0.0D)
			before_peak_rate = allrate;
		if (after_peak_rate == 0.0D)
			after_peak_rate = allrate;

		int pi = GetPeakIndex(x, peakx);
		Random rds = new Random();

		short[] y = null;

		if (1.0D - before_peak_rate > 0.0D) {
			// Shorten the audio

			int bp_not_num = (int) Math.floor(before_peak_rate * pi);
			List<Integer> bp_not_idx = Numerics.GetRangeL(0, 1, pi);

			while (bp_not_idx.size() > bp_not_num) {
				// Generate persudo number ranging from 0 to MAX_VALUE.
				int i = rds.nextInt(Integer.MAX_VALUE) % pi;
				bp_not_idx.remove(i);
			}

			Collections.sort(bp_not_idx);
			y = GetConnectIndex(bp_not_idx, x, peakx);

		} else {
			// Elongate the audio

			int bp_not_num = (int) Math.floor(before_peak_rate * pi);
			List<Integer> bp_not_idx = Numerics.GetRangeL(0, 1, pi);
			List<Integer> bp_not_idx_add = new ArrayList<Integer>();

			while (bp_not_idx.size() + bp_not_idx_add.size() < bp_not_num) {
				double ratio = rds.nextDouble();
				bp_not_idx_add.add(GetUpSlope(x, peakx, ratio));
			}

			bp_not_idx = GetMix(bp_not_idx, bp_not_idx_add);
			y = GetConnectIndex(bp_not_idx, x, peakx);
		}

		if (1.0D - after_peak_rate > 0) {
			// Shorten the audio

			int ap_not_num = (int) Math.floor(after_peak_rate
					* (peakx.length - 1 - pi));
			List<Integer> ap_not_idx = Numerics.GetRangeL(pi + 1, 1,
					peakx.length - 1);
			while (ap_not_idx.size() > ap_not_num) {
				int index = rds.nextInt(Integer.MAX_VALUE)
						% (peakx.length - 1 - pi);
				ap_not_idx.remove(index);
			}

			Collections.sort(ap_not_idx);

			y = GetConnectIndex(ap_not_idx, x, peakx);

		} else {
			// Elongate the audio

			int ap_not_num = (int) Math.floor(after_peak_rate
					* (peakx.length - 1 - pi));
			List<Integer> ap_not_idx = Numerics.GetRangeL(pi + 1, 1,
					peakx.length - 1);
			List<Integer> ap_not_idx_add = new ArrayList<Integer>();

			while (ap_not_idx.size() + ap_not_idx_add.size() < ap_not_num) {
				double ratio = rds.nextDouble();
				ap_not_idx_add.add(GetDownSlope(x, peakx, ratio));
			}

			ap_not_idx = GetMix(ap_not_idx, ap_not_idx_add);

			y = GetConnectIndex(ap_not_idx, x, peakx);

		}

		return y;
	}

	/**
	 * Connect all the samples according to the indexes in bp_not_idx.
	 * 
	 * @param not_idx
	 *            indexes to be connected.
	 * @param x
	 *            samples.
	 * @param peakx
	 *            indexes of peaks.
	 * @return the connected samples.
	 */
	public short[] GetConnectIndex(List<Integer> not_idx, short[] x, int[] peakx) {
		short[] y = null;

		for (int i = 0; i < not_idx.size(); ++i) {
			int ii = not_idx.get(i);
			if (i == 0) {
				short[] tmp = Numerics.Pick(x, 0, peakx[ii]);
				y = GetConnect(y, tmp);
			} else {
				short[] tmp = null;
				if (ii == 0) {
					tmp = Numerics.Pick(x, 0, peakx[ii]);
				} else {
					tmp = Numerics.Pick(x, peakx[ii - 1] + 1, peakx[ii]);
				}
				y = GetConnect(y, tmp);
			}
		}

		return y;

	}

	/**
	 * Get the index of the point on upword slope, whose value is most
	 * approximate the 'ratio*peak_value'.
	 * 
	 * @param x
	 *            the samples.
	 * @param peakx
	 *            indexes of peaks.
	 * @param ratio
	 *            the ratio of the value/peak on the slope.
	 * @return the index of the point on the slope.
	 */
	public int GetUpSlope(short[] x, int[] peakx, double ratio) {
		// Return nagative one to indicate error.
		if (x == null || peakx == null)
			return -1;

		Arrays.sort(peakx);
		int pi = GetPeakIndex(x, peakx);
		Object[] res = Numerics.Max(x);
		int Y = (int) res[0];
		int z = 0;

		double Y_m = Y * ratio;
		double Y_d = Double.MAX_VALUE;

		for (int i = 0; i <= pi; ++i) {
			double tmp = Math.abs(x[peakx[i]] - Y_m);
			if (Y_d > tmp) {
				Y_d = tmp;
				z = i;
			}
		}

		return z;
	}

	/**
	 * Get the index of the point on downword slope, whose value is most
	 * approximate the 'ratio*peak_value'.
	 * 
	 * @param x
	 *            the samples.
	 * @param peakx
	 *            indexes of peaks.
	 * @param ratio
	 *            the ratio of the value/peak on the slope.
	 * @return the index of the point on the slope.
	 */
	public int GetDownSlope(short[] x, int[] peakx, double ratio) {
		// Return nagative one to indicate error.
		if (x == null || peakx == null)
			return -1;

		Arrays.sort(peakx);
		int pi = GetPeakIndex(x, peakx);
		Object[] res = Numerics.Max(x);
		int Y = (Integer) res[0];
		int z = 0;

		double Y_m = Y * ratio;
		double Y_d = Double.MAX_VALUE;

		for (int i = peakx.length - 1; i >= pi + 1; --i) {
			double tmp = Math.abs(x[peakx[i]] - Y_m);
			if (Y_d > tmp) {
				Y_d = tmp;
				z = i;
			}
		}

		return z;
	}

	/**
	 * Mix the indexes in y into x, all the indexes will be adjusted to make
	 * sure the samples generated from this indexes look natural.
	 * 
	 * @param x
	 *            the destinated indexes.
	 * @param y
	 *            the source indexes.
	 * @return the mixes indexes.
	 */
	public int[] GetMix(int[] x, int[] y) {
		if (x == null || y == null)
			return null;

		int totalnum = x.length + y.length;
		Random rd = new Random();

		y = Numerics.Unique(y);
		List<Integer> xolist = Numerics.GetListFrom(x);
		List<Integer> xlist = Numerics.GetListFrom(x);
		List<Integer> ylist = Numerics.GetListFrom(y);

		Collections.sort(xlist);
		Collections.sort(ylist);

		while (xlist.size() < totalnum) {
			// The index of y, get the value with this index.
			int index = rd.nextInt(Integer.MAX_VALUE) % ylist.size();
			// The value is stored in pk.
			int pk = ylist.get(index);
			// Get the index of x, whose value is equal to pk, and the ending
			// index
			// of the generated duplication of peaks.
			int index_x = xolist.lastIndexOf(pk);
			int index_x_end = Math.min(index_x + DUPLICATE_WINDOW,
					xolist.size() - 1);

			// Generate the indexes to add to the x.
			List<Integer> tmp = Numerics.GetRangeL(index_x, 1, index_x_end);
			// Find the index of xlist to insert.
			int index_insert = Math.max(xlist.lastIndexOf(pk) - 1, 0);
			// Add indexes to x.
			xlist.addAll(index_insert, tmp);

			// Because the index in y has been added, delete it from y.
			ylist.remove(index);
		}

		return Numerics.GetArrayFromL(xlist);
	}

	public List<Integer> GetMix(List<Integer> x, List<Integer> y) {

		if (x == null || y == null)
			return null;

		int[] x1 = Numerics.GetArrayFromL(x);
		int[] y1 = Numerics.GetArrayFromL(y);

		int[] res = GetMix(x1, y1);

		return Numerics.GetListFrom(res);
	}

	/**
	 * Get all the frames, including the adjoining two periods, from the samples
	 * y.
	 * 
	 * @param y
	 *            the samples.
	 * @param peakx
	 *            the indexes of all the peaks.
	 * @return the List<short[]> of all te frames.
	 */
	public List<short[]> GetSeg(short[] y, int[] peakx) {

		if (y == null || peakx == null)
			return null;

		List<short[]> res = new ArrayList<short[]>();

		if (peakx.length == 1) {
			res.add(Arrays.copyOfRange(y, 0, y.length));
		} else {

			for (int i = 0; i < peakx.length; ++i) {
				if (i == 0) {
					short[] H = GetEpoch(y, 0, peakx[2]);
					res.add(H);
				} else if (i == peakx.length - 1) {
					short[] H = GetEpoch(y, peakx[i - 1], y.length - 1);
					res.add(H);
				} else {
					short[] H = GetEpoch(y, peakx[i - 1], peakx[i + 1]);
					res.add(H);
				}
			}
		}

		return res;
	}

	/**
	 * Get interval between two samples.
	 * @param fs sampling / frequency rate of the samples.
	 * @param seconds the time of the interval.
	 * @param before the first sample.
	 * @param after the second sample.
	 * @return the interval generated between the two samples.
	 */
	public short[] GetInterval(int fs, int seconds, short[] before,
			short[] after) {
		
		if(before == null)
			before = new short[]{0,0,0,0};
		if(after == null)
			after = new short[]{0,0,0,0};

		int total = (int) Math.floor(seconds * fs);
		List<Short> y = new ArrayList<Short>();

		short[] sample1 = Numerics.Pick(before, 
				(int)Math.floor(before.length*0.95D), before.length-1);
		short[] sample2 = Numerics.Pick(after, 
				0, (int)Math.floor(after.length*0.05D));
		
		Random rd = new Random();
		for(int i=0; i<total; ++i) {
			double ratio = rd.nextDouble();
			if(ratio > 1.0D*i/total) {
				y.add(Numerics.PickRandom(sample1));
			} else {
				y.add(Numerics.PickRandom(sample2));
			}
		}

		return Numerics.GetArrayFromS(y);
	}
	
	/**
	 * Connect two words into one.
	 * @param x the first samples.
	 * @param peakx the indexes of peaks for the first samples.
	 * @param y the second samples.
	 * @param peaky the indexes of the peaks for the second samples.
	 * @param ratio the portion of the value to the maximum value at the
	 * conjuction point.
	 * @return the combined value.
	 */
	public short[] GetFusion(short[] x, int[] peakx, 
			short[] y, int[] peaky, double ratio) {
		
		Object[] res = Numerics.Max(x);
		short M = (Short)res[0];
		
		int pi = 0;
		for(int i=peakx.length -1; i>=0; --i) {
			if(x[peakx[i]] > M) {
				pi = i;
				break;
			}
		}
		
		int pi2 = 0;
		for(int i=0; i<y.length; ++i) {
			if(y[peaky[i]] > M) {
				pi2 = i;
				break;
			}
		}
		
		short[] x1 = Numerics.Pick(x, 1, peakx[pi]);
		short[] y1 = Numerics.Pick(y, peaky[pi2], y.length-1);
		
		return GetConnect(x1, y1);
	}
	
	/**
	 * Create a window to process the samples, in order to provide a emotion
	 * effects to the voice.
	 * @param start amplifier for the samples from the beginning to the first acme.
	 * @param duration amplifier for the samples in the middle of the voice.
	 * @param end amplifier for the samples in the tail of the voice.
	 * @param x the total samples.
	 * @param peakx the indexes of the peaks in the samples.
	 * @return the windowed samples, should have emotion effects.
	 */
	public double[] GetEmotionWin(double start, double duration, 
			double end, short[] x, int[] peakx) {
		
		int pi = GetPeakIndex(x, peakx);
		/** 
		 * Adjust the half-length of the window in order to fit into the samples.
		 * If you the acme resides in the first half of the samples sequence, no
		 * adjustment is needed. However, if the acme resides in the second half
		 * of the samples sequence, the length from acme to the end of the 
		 * sequence will be assigned to pi so that array is not overflowed.
		 * Additionally, this will advance the acme to match the general pattern
		 * of human speech.
		 * */
		if(pi > (int)Math.floor(peakx.length/2.0D)) {
			pi = peakx.length-1-pi;
		}
		double[] z = Numerics.OnesD(x.length);
		double[] gwin =Numerics.GaussWin(2*peakx[pi]);
		Numerics.Multiply(gwin, start-1);
		
		// Generate window for the first half of the sequence.
		for(int i=0; i<gwin.length; ++i) {
			z[i] += gwin[i];
		}
		
		int nd = (int)Math.floor((peakx.length-pi)*2.0D/3.0D);
		int pi2 = nd + pi;
		int count = 0;
		double[] gwin2 = Numerics.GaussWin((peakx[pi2] - peakx[pi]));
		Numerics.Multiply(gwin2, (duration - 1.0D));
		// Generate window for the middle half of the sequence.
		for(int i=peakx[pi]+1; i<=peakx[pi2]; ++i) {
			z[i] += gwin2[count];
			++count;
		}
		
		count = 0;
		double[] gwin3 = Numerics.GaussWin(x.length - peakx[pi2]);
		Numerics.Multiply(gwin3, (end - 1.0D));
		// Generate window for the last half of the sequence.
		for(int i=peakx[pi2]+1; i<x.length; ++i) {
			z[i] += gwin2[count];
			++count;
		}
		return z;
	}
	
	/**
	 * Apply EmotionWin to the samples.
	 * @param win the emotion window.
	 * @param x the samples.
	 * @return the unsupressed samples that have been windowed.
	 */
	public long[] GetEmotionAdd(double[] win, short[] x) {
		long[] z = new long[win.length];
		for(int i=0; i<z.length; ++i)
			z[i] = (int)Math.floor(win[i]*x[i]);
		
		return z;
	}
	
	/**
	 * Apply EmotionWin to the samples, supressing any value above the
	 * MAX_VALUE of the sample.
	 * @param win the emotion window.
	 * @param x the samples.
	 * @return the final windowed samples.
	 */
	public short[] GetEmotionAddSupress(double[] win, short[] x) {
		
		long[] res = GetEmotionAdd(win, x);
		return Numerics.SupressS(res);
	}
	
	/**
	 * Add emotion effects to the voice samples.
	 * @param start the factor to amplify the fisrt portion of the voice.
	 * @param duration the factor to amplify the middle portion of the voice.
	 * @param end the factor to amplify the last portion of the voice.
	 * @param x the samples.
	 * @param peakx the indexes of the peaks in the samples.
	 * @return the final emotional voice samples.
	 */
	public short[] GetEmotion(double start, double duration, 
			double end, short[] x, int[] peakx) {
		
		double[] W = GetEmotionWin(start, duration, end, x, peakx);
		return GetEmotionAddSupress(W, x);
	}
	
	/**
	 * Trim the voice according to the vocal and nasal part. It only trimes the 
	 * samples before the climax of all the samples.
	 * @param x the samples.
	 * @param peakx the indexes of peaks in x.
	 * @param pos1 the position between which and the up slope point
	 * the samples should be eliminated. 
	 * @param pos2 the position between up slope point and which 
	 * the samples should be eliminated. 
	 * @return the trimed samples.
	 */
	public short[] TrimBefore(short[] x, int[] peakx, double pos1, double pos2) {
		int pi = GetPeakIndex(x, peakx);
		int start = GetUpSlope(x, peakx, TRIM_PORTION);
		
		int idx1 = peakx[(int)Math.ceil(start * pos1)];
		int idx2 = peakx[start];
		
		short[] z1 = Numerics.Pick(x, idx1, idx2);
		
		idx1 = peakx[start + (int)Math.ceil((pi-start) * pos2)];
		idx2 = x.length-1;
		
		short[] z2 = Numerics.Pick(x, idx1, idx2);
		
		return GetConnect(z1, z2);
	}
	
	/**
	 * Trim the voice according to the vocal and nasal part. It only trimes the 
	 * samples after the climax of all the samples.
	 * @param x the samples.
	 * @param peakx the indexes of peaks in x.
	 * @param pos1 the position between which and the up slope point
	 * the samples should be eliminated. 
	 * @param pos2 the position between up slope point and which 
	 * the samples should be eliminated. 
	 * @return the trimed samples.
	 */
	public short[] TrimAfter(short[] x, int[] peakx, double pos1, double pos2) {
		
		int pi = GetPeakIndex(x, peakx);
		int start = GetDownSlope(x, peakx, TRIM_PORTION);
		
		int idx1 = peakx[start];
		int idx2 = peakx[(int)Math.floor((1-pos1) * (peakx.length-1))];
		short[] z1 = Numerics.Pick(x, idx1, idx2);
		
		idx1 = 1;
		idx2 = peakx[(int)Math.floor(start - (start-pi) * pos2)];
		short[] z2 = Numerics.Pick(x, idx1, idx2);
		
		return GetConnect(z1, z2);
	}

}
