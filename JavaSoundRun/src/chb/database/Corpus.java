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
			Source conn) {

        Table word = conn.getTable("WORD_LIST_LEVEL_0");
        Table idiom = conn.getTable("IDIOM_LIST_LEVEL_0");
        Table proverbe = conn.getTable("PROVERBE_LIST_LEVEL_0");
        
        return word.Exists(s) || idiom.Exists(s) || proverbe.Exists(s);
        
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
			Source conn) {
        

		Table charac = conn.getTable("CHARACTER_LIST_LEVEL_5");
		return charac.Exists(s);

	}

	

}
