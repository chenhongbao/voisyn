/**
 * 
 */
package chb.segment;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class CWSolverDP extends CWSolver {


    private List<CWSegment> Segments;

    private CWSolverDP() { }

    static public CWSolver CreateSolver()
    {
        CWSolverDP dp = new CWSolverDP();
        dp.Segments = new LinkedList<CWSegment>();

        return dp;
    }

	/* (non-Javadoc)
	 * @see chb.segment.CWSolver#Solve(java.util.List, int)
	 */
	@Override
    public boolean Solve(List<CWPoint> points, int start)
    {
        int[] range = FindConflictRange(points, start);

        MinNumberOfWords(points, range[0], range[1]);

        return true;
    }

    private int MinNumberOfWords(List<CWPoint> points, int start, int end)
    {
        if (start >= end)
            return 1;

        int num = Integer.MAX_VALUE;
        CWSegment minseg = null;
        CWPoint pt = points.get(start);
        for (CWSegment seg: pt.Segments)
        {
            if (minseg == null)
                minseg = seg;

            int numtmp = 1 + MinNumberOfWords(points, seg.End.intValue()+1, end);
            if (numtmp < num)
            {
                num = numtmp;
                minseg = seg;
            }
            
        }

        this.Segments.add(minseg);

        return num;
    }

    private int[] FindConflictRange(List<CWPoint> points, int start)
    {
        CWPoint pt = points.get(start);
        int end = 0;
        CWSegment segl = null;
        while (HasWord(pt))
        {
            segl = FindLongestSegment(pt);
            end = segl.End.intValue();

            for (int i = segl.Begin.intValue() + 1; i < segl.End.intValue(); ++i)
            {
                CWSegment seg2 = FindLongestSegment(points.get(i));
                if (end < seg2.End.intValue())
                {
                    end = seg2.End.intValue();
                }
            }

            pt = points.get(end);

        }

        return new int[] {start, end };
    }

    private CWSegment FindLongestSegment(CWPoint point)
    {
        CWSegment segl = null;
        for(CWSegment s: point.Segments)
        {
            if (segl == null)
                segl = s;
            if (segl.End < s.End)
                segl = s;
        }

        return segl;
    }

    private boolean HasWord(CWPoint point)
    {
        if (point.Segments.size() == 0)
            return false;

        for (CWSegment seg: point.Segments)
        {
            if (seg.Characters.size() > 1)
                return true;
        }
        return false;
    }

}
