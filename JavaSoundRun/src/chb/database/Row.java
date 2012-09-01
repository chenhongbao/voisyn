/**
 * 
 */
package chb.database;

/**
 * Abstraction for a row inside a table.
 * @author Hongbao Chen
 *
 */
public interface Row {
	/**
	 * Get the field of the specified column inside a row.
	 * @param colname the column name, the field of the first pait is used as the key.
	 * @return the field value.
	 */
	public String GetField(String colname); 
	
	/**
	 * Set the fields' content into a row.
	 * @param args variable length parameters.
	 */
	public void SetFileds(String...args);
}
