/**
 * 
 */
package chb.segment;

import java.util.List;

/**
 * @author Hongbao Chen
 *
 */
public abstract class CWSolver {

	/**
	 * Solve the conflict of the points, from index start on inclusing index start.
	 * @param points the reference to the points list.
	 * @param start the index to start to solve conflicts.
	 * @return true if conflict is solved and false if it is not.
	 */
    abstract public boolean Solve(List<CWPoint> points, int start);

}
