package chb.segment;

import java.util.LinkedList;
import java.util.List;

public class CWPoint {

    public List<CWSegment> Segments;
    public Long Index;
    public CWSegment Content;
    public Boolean State;

    public CWPoint()
    {
        this.Segments = new LinkedList<CWSegment>();
        this.Index = 0L;
        this.Content = null;
        this.State = false;
    }

    //TODO: Implement Froze
    public void Froze(String content)
    {
        
    }

    public void AddSegment(CWSegment seg)
    {
        if (seg == null)
            return;

        this.Segments.add(seg);
    }

}
