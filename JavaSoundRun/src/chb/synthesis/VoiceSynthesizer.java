package chb.synthesis;

import java.util.ArrayList;
import java.util.List;

import chb.math.Numerics;
import chb.wave.Wave;

/**
 * @author Hongbao Chen
 *
 */
public class VoiceSynthesizer {
	
	public static short[] GetEpoch(short[] x, int start, int endi) {
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
			double tmp = Math.floor(x[i]*H[count]);
			res[count] = (short)tmp;
			++count;
		}
		
		return res;
	}
}
