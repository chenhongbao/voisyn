/**
 * 
 */
package chb.database;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 *
 */
public class DataRow implements Row {

	private Map<String, String> Fields = null;
	public DataRow() {
		this.Fields = new HashMap<String, String>();
	}

	@Override
	public void SetFileds(String...args) {
		if(args.length%2 != 0)
			return;
		
		for(int i=0; i<args.length-1; i+=2) {
			this.Fields.put(args[i], args[i+1]);
		}
	}

	/* (non-Javadoc)
	 * @see chb.database.Row#GetField(java.lang.String)
	 */
	@Override
	public String GetField(String colname) {
		if(this.Fields == null)
			return null;
		return this.Fields.get(colname);
	}

}
