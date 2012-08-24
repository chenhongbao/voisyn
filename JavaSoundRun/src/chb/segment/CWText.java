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
public class CWText {

    public List<CWCharacter> Text;
    public Integer Index;

    public CWText()
    {
        this.Text = new LinkedList<CWCharacter>();
        this.Index = -1;
    }

    public CWText(String text) throws Exception
    {
        this.Text = new LinkedList<CWCharacter>();
        this.Index = -1;
        InitText(text);
    }

    public void InitText(String text) throws Exception
    {
        if (this.Text == null)
            throw new Exception("Null property CWText::Text or CWText::InvalidText");
        
        for (int i = 0; i < text.length(); ++i)
        {
            Character c = text.charAt(i);
            if (!CWText.IsValidChar(c))
                continue;
            else
                Text.add(CWCharacter.CreateCWChar(c, (long)i));
        }

    }

    public String Peek(int i) throws Exception
    {
        String tmp = "";
        if (this.Index < 0)
            throw new Exception("CWText.Index < 0");
        if (this.Index + i >= this.Text.size())
            return null;

        for (int k = this.Index; k <= this.Index + i; ++k)
            tmp += this.Text.get(k).Content;

        return tmp;
    }

    public CWCharacter Next() throws Exception
    {
        this.Index++;
        if (this.Index >= this.Text.size())
            throw new Exception("CWText.Next reaches the end and overflows");

        return this.Text.get(this.Index);
    }

    public boolean HasNext()
    {
        return this.Index < this.Text.size() - 1;
    }

    public static boolean IsValidChar(Character c)
    {
    	if (Character.isSpaceChar(c) ==true)
    		return false;
    	if ((int)c == 0xFEFF)
    		return false;
        return true;
    }

}
