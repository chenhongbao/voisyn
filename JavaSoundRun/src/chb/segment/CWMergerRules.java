package chb.segment;

import chb.Utility;
import chb.database.Corpus;
import chb.database.DataSource;
import chb.text.AsciiChineseNumber;

import java.util.LinkedList;
import java.util.List;

/**
 * CWMergerRules implements the CWMerger interface and provides the
 * functionalities to merge split words for producing the persodes.
 *
 * @author Hongbao Chen
 */
public class CWMergerRules extends CWMerger {

    public DataSource Conn = null;

    public int CountMax = 4;

    public CWMergerRules(DataSource conn) {
        this.Conn = conn;
    }

    /*
     * (non-Javadoc)
     *
     * @see chb.segment.CWMerger#Merge(java.util.List, int, int)
     */
    @Override
    int Merge(List<CWWord> words, int start, int end, List<CWWord> merge) {
        /**
         * Inside this method, it will process all the thress case: [0]=[1],
         * [0]<[1] and [0]>[1]
         */
        List<CWWord> tmp = new LinkedList<CWWord>();
        int beg = start;

        CWWord word = null;
        try {
            if (start < end) {
                while (beg <= end) {
                    word = new CWWord();
                    beg = MergeStep(words, beg, end, word);
                    if (beg == -1)
                        break;

                    tmp.add(word);
                }

            } else if (start == end) {
                word = new CWWord();
                MergeStep(words, start, end, word);
                tmp.add(word);
            } else {
                word = new CWWord();
                end = start;
                MergeStep(words, start, end, word);
                tmp.add(word);
            }

            merge.addAll(tmp);

            return end + 1;

        } catch (Exception e) {
            chb.Utility.Log(e.getStackTrace());
            return end + 1;
        }
    }

    /**
     * Merge the next word according to the [start, end] range and the words
     * list given.
     *
     * @param words word list.
     * @param start start index.
     * @param end   end index.
     * @return the next index to start merging.
     * @throws Exception
     */
    public int MergeStep(List<CWWord> words, int start, int end, CWWord word)
            throws Exception {

        if (words == null || words.size() == 0)
            throw new Exception(
                    "MergerRules: words null pointer or size() zero.");
        if (word == null)
            throw new Exception("MergerRules: word null pointer.");
        if (end < start)
            throw new Exception("MergerRules: start index error.");

        String tmp = words.get(start).Word;
        if (IsDigit(tmp)) {
            return MergeNumberStep(words, start, end, word);
        } else {
            return MergeLiteralStep(words, start, end, word);
        }
    }

    /**
     * Merge the split words which are numbers.
     *
     * @param words the original word list
     * @param start start index
     * @param end   end index, inclusive.
     * @param word  the final output, it is the total caoncatenation of the words
     * @return the next index to search on.
     * @throws Exception input argument error will cause exceptions.
     */
    public int MergeNumberStep(List<CWWord> words, int start, int end,
                               CWWord word) throws Exception {

        if (words == null || words.size() == 0)
            throw new Exception(
                    "MergerRules: words null pointer or size() zero.");
        if (word == null)
            throw new Exception("MergerRules: word null pointer.");
        if (end < start)
            throw new Exception("MergerRules: start index error.");

        List<Character> chs = new LinkedList<Character>();
        boolean chinesedigit = false;
        boolean asciidigit = false;

        for (int i = start; i <= end; ++i) {
            String tmp = words.get(i).Word;
            for (int j = 0; j < tmp.length(); ++j) {
                if (Utility.IsChineseDigit(tmp.charAt(j)))
                    chinesedigit = true;
                else
                    asciidigit = true;

                if (chinesedigit && Utility.IsAsciiDigit(tmp.charAt(j)))
                    throw new Exception(
                            "MergerRules: Number format error, chinese and ascii digit.");

                chs.add(tmp.charAt(j));
            }
        }

        if (chinesedigit && asciidigit == false) {
            String tmp = "";
            for (int index = start; index <= end; ++index)
                tmp += words.get(index).Word;

            word.Word = tmp;
            word.Begin = (long) start;
            word.End = (long) end;

        } else {
            String tmp = "";
            int i = 0;
            for (; i < chs.size(); ++i) {
                if (Utility.IsAsciiDigit(chs.get(i)) == false) {
                    break;
                }

                tmp += (char) chs.get(i);
            }

            String chnnum = GetChineseNumber(tmp);
            if (i < chs.size()) {
                for (int i2 = i; i2 < chs.size(); ++i2) {
                    chnnum += (char) chs.get(i2);
                }
            }

            word.Word = chnnum;
            word.Begin = (long) start;
            word.End = (long) end;

        }

        word.Word = "|" + word.Word;

        return end + 1;
    }

    /**
     * Translate the alphabetic number to Chinese number.
     *
     * @param tmp the alphabetic number.
     * @return the Chinese number.
     */
    public static String GetChineseNumber(String tmp) {

        String s = AsciiChineseNumber.cleanZero(AsciiChineseNumber
                .splitNum(tmp));
        return Utility.ChineseNumberBig2Small(s);
    }

    /**
     * Merge the split words which are not numbers.
     *
     * @param words the original word list
     * @param start start index
     * @param end   end index, inclusive.
     * @param word  the final output, it is the total caoncatenation of the words
     * @return the next index to search on.
     * @throws Exception input argument error will cause exceptions.
     */
    public int MergeLiteralStep(List<CWWord> words, int start, int end,
                                CWWord word) throws Exception {

        if (words == null || words.size() == 0)
            throw new Exception(
                    "MergerRules: words null pointer or size() zero.");
        if (word == null)
            throw new Exception("MergerRules: word null pointer.");
        if (end < start)
            throw new Exception("MergerRules: start index error.");

        int index = start;
        String tmp = "";
        do {
            if (words.get(index).Word.length() > 1) {
                tmp += "|" + words.get(index).Word;
            } else {
                tmp += words.get(index).Word;
            }
            index += 1;

            if (index >= words.size())
                break;
            if (index > end)
                break;
        } while (tmp.length() <= this.CountMax);

        word.Word = tmp;
        word.Begin = (long) start;
        if (tmp.length() > this.CountMax)
            word.End = (long) index - 1;
        else
            word.End = (long) index - 1;

        return (int) ((long) word.End + 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see chb.segment.CWMerger#GetMergeRange(java.util.List, int)
     */
    @Override
    int[] GetMergeRange(List<CWWord> words, int start) {
        if (words == null || words.size() == 0)
            return null;

        if (start >= words.size())
            return null;

        int index = start;
        String tmp = words.get(index).Word;
        if (IsDigit(tmp) == false)
            return GetLiteralRange(words, start);
        else
            return GetNumberRange(words, start);

    }

    /**
     * Set up the data source for processing.
     *
     * @param dsrc Connection string.
     * @throws Exception when data source cannot be set up, it throws an exception.
     */
    public void SetDataSource(String dsrc) throws Exception {
        this.Conn.ConnectionString = dsrc;

        if (this.Conn.State != DataSource.ConnectionState.Opened)
            this.Conn.Open();
    }

    /**
     * Get the range of a chinese word.
     *
     * @param words words list.
     * @param start the index to start to search.
     * @return int[2], if array[0] > array[1], no word starts from index start (
     *         at index start, it is a punctuation), if array[0] = array[1],
     *         there is only one word and it is at index start, if array[0] <
     *         array[1], the word starts from the index start to the index
     *         array[1], including the array[1].
     */
    private int[] GetLiteralRange(List<CWWord> words, int start) {

        if (words == null || words.size() == 0)
            return null;

        if (start >= words.size())
            return null;

        int index = start;
        while (index < words.size()) {
            String stmp = words.get(index).Word;
            boolean res1 = IsPause(stmp);
            boolean res2 = IsDigit(stmp);

            boolean res = res1 || res2;

            if (res == false) {
                index += 1;
                continue;
            } else {
                break;
            }
        }

        return new int[]{start, index - 1};
    }

    /**
     * Test whether the current character should be split.
     *
     * @param word word to be tested.
     * @return true if the sentence should be split at the current position and
     *         vice versa.
     */
    private boolean IsPause(String word) {
        if (word.length() > 1)
            return false;

        if (word.length() == 1) {
            if (IsAux(word) == true)
                return true;

            if (Utility.IsChinesePunctuation(word.charAt(0)) == true)
                return true;
        }

        return false;
    }

    private boolean IsAux(String _c) {
        return Corpus.IsAuxWithConn(_c, this.Conn);
    }

    /**
     * Get the range of a chinese number.
     *
     * @param words words list.
     * @param start the index to start to search.
     * @return int[2], if array[0] > array[1], no number starts from index
     *         start, if array[0] = array[1], there is only one number and it is
     *         at index start, if array[0] < array[1], the number starts from
     *         the index start to the index array[1], including the array[1].
     */
    private int[] GetNumberRange(List<CWWord> words, int start) {

        if (words == null || words.size() == 0)
            return null;

        if (start >= words.size())
            return null;

        int index = start;

        while (index < words.size()) {
            String tmp = words.get(index).Word;
            if (IsDigit(tmp) == false)
                break;
            index += 1;
        }

        return new int[]{start, index - 1};

    }

    private static boolean IsDigit(String _s) {
        if (_s == null || _s.length() == 0)
            return false;

        char[] chs = _s.toCharArray();
        for (int i = 0; i < chs.length; ++i) {
            if (CWMergerRules.IsDigit(chs[i]) == false)
                return false;
        }

        return true;
    }

    /**
     * Test whether the character is a digit, both in Chinese and English.
     *
     * @param _c Character to be tested on.
     * @return true if it is a digit, and false if it is not.
     */
    private static boolean IsDigit(Character _c) {
        return Utility.IsAsciiDigit(_c) || Utility.IsChineseDigit(_c);
    }

}
