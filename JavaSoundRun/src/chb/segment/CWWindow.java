/**
 * 
 */
package chb.segment;

import java.sql.SQLException;

import chb.database.Corpus;
import chb.database.DataSource;

/**
 * @author Administrator
 *
 */
public class CWWindow {
	
    public int Size = 4;
    public CWText Text;
    public DataSource DataSrc = null;

    public CWWindow(){}
    public CWWindow(DataSource ds)
    {
        this.Size = 4;
        this.DataSrc = ds;
    }

    public void SetDbConnection(String connstr) throws SQLException
    {
        if (connstr == null)
            return;
        if (connstr.length() == 0)
        	return;
        this.DataSrc.Open();
    }
    public void SetDb() throws Exception
    {   
        this.DataSrc.Open();

    }
    public CWPoint Move() throws Exception
    {

        if (this.Text == null)
        {
            throw new Exception("Null property CWWindow.Text");
        }
        if (this.HasNext())
        {
            Text.Next();
        }
        else
        {
            return null;
        }

        CWPoint pt = new CWPoint();
        pt.Index = (long)this.Text.Index;
        pt.State = false;
        pt.Content = null;

        pt.AddSegment(CWSegment.CreateSegment(Text.Peek(0), (long)this.Text.Index));

        int i;
        for (i = 1; i < this.Size; ++i)
        {
            String s = Text.Peek(i);
            if (s == null || s.length() == 0)
                break;
            if (IsValidPhrase(s))
            {
                pt.AddSegment(CWSegment.CreateSegment(s, (long)this.Text.Index));
            }
        }

        return pt;
    }

    public boolean HasNext() throws SQLException
    {
        boolean res = this.Text.HasNext();
        if (!res && this.DataSrc.State != DataSource.ConnectionState.Closed)
        {
            this.DataSrc.Close();
        }
        return res;
    }

    
    private boolean IsValidPhrase(String s)
    {
        return Corpus.IsPhraseWithConn(s, this.DataSrc);
    }

}
