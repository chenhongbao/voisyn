package chb.database;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import chb.Utility;


/**
 * Corpus provides interfaces to access the corpus, especially for word 
 * segmentation, including reading words from base with certain conditions, 
 * testing the existence of a word, find out a best matching word and so forth.
 * @author Hongbao Chen
 *
 */
public class Corpus {

	/**
	 * Given a connection, test whether tht word exists in the database. This
	 * method is used at the developing phase of the system. After the system is 
	 * finished, the method will not be used any more.
	 * @param s The word to be tested.
	 * @param conn Connection to databse.
	 * @return True if the word exists in the database and vice versa.ss
	 */
	public static boolean IsPhraseWithConn(String s,
			DataSource conn) {

        if (conn == null || conn.ConnectionString.length() == 0)
        	return false;
        
        /** 
         * Example : <br>
         *     prepareCall("{call demoSp(?, ?)}") <br>
         * Procedure:<br>
         *     create procedure IsPhrase( <br>
         *         in phrase varchar(20) CHARACTER SET utf8 COLLATE utf8_bin, <br>
         *         out exist int)<br>
         */
        int out = 0;
		try {
			CallableStatement prestate = conn.Connection.prepareCall("{call IsPhrase(?, ?)}");
			prestate.setString(1, s);
			prestate.registerOutParameter(2, Types.INTEGER);
			
			prestate.execute();
			out = prestate.getInt(2);
		} catch (SQLException e) {
			Utility.Log(e.getMessage());
			return false;
		}
        
		return out == 1;
	}
	
	/**
	 * Given a connection, test whether the word is an auxiliary word. This
	 * method is used at the developing phase of the system. After the system is 
	 * finished, the method will not be used any more.
	 * @param s The word to be tested.
	 * @param conn Connection to databse.
	 * @return True if the word is an auxiliary word and vice versa.ss
	 */
	public static boolean IsAuxWithConn(String s,
			DataSource conn) {

        if (conn == null || conn.ConnectionString.length() == 0)
        	return false;
        
        int out = 0;
		try {
			CallableStatement prestate = conn.Connection.prepareCall("{call IsAux(?, ?)}");
			prestate.setString(1, s);
			prestate.registerOutParameter(2, Types.INTEGER);
			
			prestate.execute();
			out = prestate.getInt(2);
		} catch (SQLException e) {
			Utility.Log(e.getMessage());
			return false;
		}
        
		return out == 1;

	}

	

}
