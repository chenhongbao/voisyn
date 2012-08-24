/**
 * 
 */
package chb.segment;

import java.util.Comparator;


/**
 * @author Hongbao Chen
 *
 */
public class CWSegmentComparater implements Comparator<CWSegment> {

	@Override
	public int compare(CWSegment x, CWSegment y) {
		Long lres = x.Begin - y.Begin;
		 return lres.intValue();
	}
	
    private CWSegmentComparater() { }

    static public CWSegmentComparater CreateComparater()
    {
        return new CWSegmentComparater();
    }

}
