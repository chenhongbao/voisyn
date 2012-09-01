package chb.database;

/**
 * Provide the interfaces for table of a database.
 * @author Hongbao Chen
 *
 */
public interface Table {
	/**
	 * Test whether the key exists in the current table.
	 * @param key the key to inspect.
	 * @return true if it exists, and false if it does not.
	 */
	public boolean Exists(String key);
	
	/**
	 * Get the value with the key as key at the column with the colname.
	 * @param key the key.
	 * @param colname the column name.
	 * @return the string of the field.
	 */
	public String GetValue(String key, String colname);
	
	/**
	 * Get the whole row which mathes the key.
	 * @param key the key.
	 * @return the Column object.
	 */
	public Row GetRow(String key);
	
	/**
	 * Set up the data table with real data.
	 */
	public void SetUp();
}
