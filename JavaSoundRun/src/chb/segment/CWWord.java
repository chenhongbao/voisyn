/**
 * 
 */
package chb.segment;

/**
 * @author Administrator
 *
 */
public class CWWord
{
    public String Word;
    public Long Begin;
    public Long End;

    public CWWord(){}
    static public CWWord CreateWord(CWSegment segment)
    {
        CWWord word = new CWWord();
        word.Word = segment.GetString();
        word.Begin = segment.Begin;
        word.End = segment.End;

        return word;
    }
}
