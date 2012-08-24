/**
 * 
 */
package chb.segment;

import java.util.Comparator;

/**
 * @author Administrator
 *
 */
public class CWWordComparater implements Comparator<CWWord> {

	@Override
	public int compare(CWWord x, CWWord y) {
		Long lres = x.Begin - y.Begin;
		return lres.intValue();
	}
	
    private CWWordComparater() { }
    static public CWWordComparater CreateComparater()
    {
        return new CWWordComparater();
    }

}
