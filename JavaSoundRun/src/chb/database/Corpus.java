package chb.database;


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

        /** 
         * Example : <br>
         *     prepareCall("{call demoSp(?, ?)}") <br>
         * Procedure:<br>
         *     create procedure IsPhrase( <br>
         *         in phrase varchar(20) CHARACTER SET utf8 COLLATE utf8_bin, <br>
         *         out exist int)<br>
         */
        int out = 0;
        // TODO Implement IsPhraseWithConn
        
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
        
        int out = 0;
        // TODO Implement IsAuxWithConn
        
		return out == 1;

	}

	

}
