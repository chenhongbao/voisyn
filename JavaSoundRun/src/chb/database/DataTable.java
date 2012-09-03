/**
 * 
 */
package chb.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Implement the Table interface, providing detailed implementation. It holds
 * the real data. The first column in the dbx file is taken as the key for its row in
 * the table. The key is stored twice, including its being used as the key for
 * the map of  <string, row> and the field inside a row.
 * @author Hongbao Chen
 *
 */
public class DataTable implements Table {
	
	public String Name = "";
	public String Path ="";
	public String Encoding = "";
	
	private List<String> Columns = null; 
	public List<String> getColumns() {
		return Columns;
	}
	private Map<String, Row> Rows = null;

	public DataTable() {}
	public DataTable(String _name, String _path, String _encode) {
		this.Name = _name;
		this.Path = _path;
		this.Encoding = _encode;
		this.Rows = new HashMap<String, Row>();
		this.Columns = new ArrayList<String>();
	}
	

	/* (non-Javadoc)
	 * @see chb.database.Table#Exists(java.lang.String)
	 */
	@Override
	public boolean Exists(String key) {
		if(this.Rows == null)
			return false;
		
		return this.Rows.keySet().contains(key);
	}

	/* (non-Javadoc)
	 * @see chb.database.Table#GetValue(java.lang.String, java.lang.String)
	 */
	@Override
	public String GetValue(String key, String colname) {
		if(this.Rows == null)
			return null;
		Row row = this.Rows.get(key);
		if(row == null)
			return null;
		
		return row.GetField(colname);
	}
	
	@Override
	public Row GetRow(String key) {
		if(this.Rows == null)
			return null;
		return this.Rows.get(key);
	}

	@Override
	public void SetUp() {
        File file = new File(this.Path);
        if(file.canRead() == false)
        	file.setReadable(true);
        
        Scanner  intext = null;
        try {
			intext = new Scanner(
					new FileInputStream(file), this.Encoding);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} 
        
        String tmp = null;
        do {
			tmp = intext.nextLine();
        	String[] fields = tmp.split(",");
        	if(fields.length == 0)
        		continue;
        	
        	if(this.Columns.size()==0) {
        		for(int i =0; i<fields.length; ++i)
        			this.Columns.add(fields[i]);
        	} else {
        		Row row = new DataRow();
        		int num = 0;
        		if(fields.length > Columns.size()) {
        			num = Columns.size();
        		}
        		else { 
        			num = fields.length;
        		}       		
        		for(int i =0; i<num; ++i) {
        			row.SetFileds(Columns.get(i), fields[i]);
        		}
        		this.Rows.put(fields[0], row);
        	}
        	
        } while(intext.hasNextLine());
        
        if(intext!=null)
        	intext.close();
        
	}

}
