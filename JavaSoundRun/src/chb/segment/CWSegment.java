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
public class CWSegment {

	/**
	 * 
	 */
    public List<CWCharacter> Characters;
    public Long Begin;
    public Long End;

    public CWSegment()
    {
        this.Characters = new LinkedList<CWCharacter>();
        this.Begin = 0L;
        this.End = 0L;
        
    }

    public static CWSegment CreateSegment(String s, Long b) throws Exception
    {
        CWSegment seg = new CWSegment();
        if(s==null)
            throw new Exception("Null reference String in CWSegment.CreateSegment");
        if(s.length() == 0)
            throw new Exception("Zero-length String in CWSegment.CreateSegment");
        
        for (int i = 0; i < s.length(); ++i)
        {
            seg.Characters.add(CWCharacter.CreateCWChar(s.charAt(i), b+i));
        }

        seg.Begin = b;
        seg.End = b + s.length() - 1;

        return seg;
    }


    public String GetString()
    {
        String tmp = "";

        for (int i = 0; i < this.Characters.size(); ++i)
            tmp += this.Characters.get(i).Content;

        return tmp;
    }

}
