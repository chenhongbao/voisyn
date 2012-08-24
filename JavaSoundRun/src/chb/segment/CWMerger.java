/**
 * 
 */
package chb.segment;

import java.util.List;

/**
 * CWMerger provides a common interface for merging words and literals.
 * @author Hongbao Chen
 *
 */
public abstract class CWMerger {
	/**
	 * Merger the split words according to the rules previously defined.
	 * @param words the reference to the words list.
	 * @param start the index to start merging.
	 * @param end the index to stop merging.
	 * @return the next index to merge.
	 */
	abstract int Merge(List<CWWord> words, int start, int end, List<CWWord> merge);
	
	/**
	 * Get the range on the list of words that will be merged.
	 * @param words the reference to the words list.
	 * @param start the index to start to test to find the merging range.
	 * @return int[2] and the first element is the start of the merging range, and the
	 * second element is the end of the merging range.
	 */
	abstract int[] GetMergeRange(List<CWWord> words, int start);

}
