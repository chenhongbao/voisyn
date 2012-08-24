
package chb;

import chb.hash.*;

/**
 * Utility class, which provides some useful static routines to deal with the common
 * problems.
 * @author Hongbao Chen
 *
 */
public class Utility extends chb.text.TextUtility {
	
	public static void Log(Object msg) {
		System.out.println(msg);
	}
	
	public static long Hash_01(byte[] bytes) {
		return HashUtility.__HashFnv__(bytes);
	}
	
	public static long Hash_02(byte[] bytes) {
		return HashUtility.__HashMurmur2__(bytes);
	}

}
