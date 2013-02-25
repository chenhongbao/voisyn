package chb.text;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TextUtility class provides a set of static methods for the basic functions.
 *
 * @author Hongbao Chen
 */
public class TextUtility {

    public static Map<String, String> CHN_NUMBER_MAP = new HashMap<String, String>();
    private static String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";

    private static String MONTH_LITERALS = "年月日时分秒";

    private static String NUMBER_EN = "0123456789.";

    private static String NUMBER_LITERALS = "零一二三四五六七八九十百千万亿壹贰叁肆伍陸陆柒捌玖拾佰仟萬";

    private static String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    static {
        CHN_NUMBER_MAP.put("壹", "一");
        CHN_NUMBER_MAP.put("贰", "二");
        CHN_NUMBER_MAP.put("叁", "三");
        CHN_NUMBER_MAP.put("肆", "四");
        CHN_NUMBER_MAP.put("伍", "五");
        CHN_NUMBER_MAP.put("陆", "六");
        CHN_NUMBER_MAP.put("陸", "六");
        CHN_NUMBER_MAP.put("柒", "七");
        CHN_NUMBER_MAP.put("捌", "八");
        CHN_NUMBER_MAP.put("玖", "九");
        CHN_NUMBER_MAP.put("拾", "十");
        CHN_NUMBER_MAP.put("佰", "百");
        CHN_NUMBER_MAP.put("仟", "千");
        CHN_NUMBER_MAP.put("萬", "万");
    }

    public static String CleanText(String text) {
        String res = CleanTextPunctuation(text);
        res = CleanTextUnit(res);


        return res;
    }

    /**
     * Change the big chinese number to small chinese number.
     *
     * @param number the number to be translated.
     * @return the right small chinese number.
     */
    public static String ChineseNumberBig2Small(String number) {
        Set<String> set = CHN_NUMBER_MAP.keySet();
        String res = number;
        for (String s : set) {
            String rep = CHN_NUMBER_MAP.get(s);
            res = res.replaceAll(s, rep);
        }

        return res;
    }

    /**
     * Translate the ASCII punctuation to Chinese punctuation.
     *
     * @param text the text to be processed.
     * @return the new text that has been cleaned.
     */
    public static String CleanTextPunctuation(String text) {
        if (text == null)
            return "";

        String res = text.replace(',', '\uFF0C');
        res = res.replace('.', '\u3002');
        res = res.replace(':', '\uFF1A');
        res = res.replace(';', '\uFF1B');
        res = res.replace('?', '\uFF1F');
        res = res.replace('(', '\uFF08');
        res = res.replace(')', '\uFF09');
        res = res.replace('[', '\u3010');
        res = res.replace(']', '\u3011');
        res = res.replace('!', '\uFF01');

        return res;
    }

    /**
     * Change the unit like 'km' to chinese.
     *
     * @param text text to be processed.
     * @return the clean text.
     */
    public static String CleanTextUnit(String text) {
        String res = text.replace("km", "千米");
        res = res.replace("KM", "千米");
        res = res.replace("Km", "千米");


        return res;

    }

    public static boolean IsAsciiDigit(Character _c) {
        return IsDigit(String.valueOf(_c));
    }

    /**
     * Test whethre a character is a punctuation of English.
     *
     * @param _c Character to be tested.
     * @return true if the character is English punctuation and false if it is not.
     */
    public static boolean IsAsciiPunctuation(char _c) {
        switch (_c) {
            case '\u0021':
            case '\u0022':
            case '\u0023':
            case '\u0024':
            case '\u0025':
            case '\u0026':
            case '\'':		/*\u0027*/
            case '\u0028':
            case '\u0029':
            case '\u002A':
            case '\u002B':
            case '\u002C':
            case '\u002D':
            case '\u002E':
            case '\u002F':
            case '\u003A':
            case '\u003B':
            case '\u003C':
            case '\u003D':
            case '\u003E':
            case '\u003F':
            case '\u0040':
            case '\u005B':
            case '\\': 	/* \u005C */
            case '\u005D':
            case '\u005E':
            case '\u005F':
            case '\u0060':
            case '\u007B':
            case '\u007C':
            case '\u007D':
            case '\u007E':
                return true;
            default:
                return false;
        }
    }

    /**
     * Test whether the code point falls into the range of Chinese unicode.
     *
     * @param _c the character to be tested.
     * @return true if it is a chinese character and false if it is not.
     */
    public static boolean IsChinese(Character _c) {

        int codePoint = (int) _c;

        if (codePoint >= 0x3400 && codePoint <= 0x4DB5)
            return true;
        if (codePoint >= 0x4E00 && codePoint <= 0x9FA5)
            return true;
        if (codePoint >= 0x9FA6 && codePoint <= 0x9FBB)
            return true;
        if (codePoint >= 0xF900 && codePoint <= 0xFA2D)
            return true;
        if (codePoint >= 0xFA30 && codePoint <= 0xFA6A)
            return true;
        if (codePoint >= 0xFA70 && codePoint <= 0xFAD9)
            return true;

        return false;
    }

    /**
     * Test whether the character is chinese date.
     *
     * @param _c the character to be tested.
     * @return true if the character is chinese date and false if it is not.
     */
    public static boolean IsChineseDate(Character _c) {
        return IsChineseDate(String.valueOf(_c));
    }

    public static boolean IsChineseDate(String _c) {
        return MONTH_LITERALS.contains(_c);
    }

    /**
     * Test whether the character is chinese digit.
     *
     * @param _c the character to be tested.
     * @return true if the character is chinese digit and false if it is not.
     */
    public static boolean IsChineseDigit(Character _c) {
        return IsChineseDigit(String.valueOf(_c));
    }

    public static boolean IsChineseDigit(String _c) {
        return NUMBER_LITERALS.contains(_c);
    }

    /**
     * Test whethre a character is a punctuation of Chinese.
     *
     * @param _c Character to be tested.
     * @return true if the character is Chinese punctuation and false if it is not.
     */
    public static boolean IsChinesePunctuation(char _c) {
        switch (_c) {
            case '\u00B7': /* · */
            case '\u2018': /* ‘ */
            case '\u2019': /* ’ */
            case '\u201C': /* “ */
            case '\u201D': /* ” */
            case '\u2026': /* … */
            case '\u3001': /* 、 */
            case '\u3002': /* 。 */
            case '\u3010': /* 【 */
            case '\u3011': /* 】 */
            case '\u300A': /* 《 */
            case '\u300B': /* 》 */
            case '\uFF01': /* ！ */
            case '\uFF08': /* （ */
            case '\uFF09': /* ） */
            case '\uFF0C': /* ， */
            case '\uFF1A': /* ： */
            case '\uFF1B': /* ； */
            case '\uFF1F': /* ？ */
            case '\uFFE5': /* ￥ */
                return true;
            default:
                return false;
        }
    }

    /**
     * Test whether the character is a digit.
     *
     * @param _c The character to be tested.
     * @return true if it is a digit, and vice versa.
     */
    public static boolean IsDigit(String _c) {
        return NUMBER_EN.contains(_c);
    }

    /**
     * Test whether the character is a English letter.
     *
     * @param _c The character to be tested.
     * @return true if it is a English letter, and vice versa.
     */
    public static boolean IsLetter(char _c) {

        return IsUpperCase(_c) || IsLowerCase(_c);
    }

    public static boolean IsLowerCase(Character _c) {
        return IsLowerCase(String.valueOf(_c));
    }

    /**
     * Test whether a letter is a lower case letter.
     *
     * @param _c The character to be tested.
     * @return true if the character is a lower case letter, and vice versa.
     */
    public static boolean IsLowerCase(String _c) {
        return LOWER_CASE.contains(_c);
    }

    public static boolean IsUpperCase(Character _c) {
        return IsUpperCase(String.valueOf(_c));
    }

    /**
     * Test whether the character is a upper case letter.
     *
     * @param _c The character to be tested.
     * @return true if it is a upper case letter, and vice versa.
     */
    public static boolean IsUpperCase(String _c) {
        return UPPER_CASE.contains(_c);
    }

    /**
     * Read text content from the given file path.
     *
     * @param path Path to the text file.
     * @return String in the text, or null if error.
     */
    public static String readText(String path) {
        File file = new File(path);
        String txt = "";
        try {

            file.setReadable(true);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String tmp = null;
            while ((tmp = br.readLine()) != null) {
                txt = txt + tmp;
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

        return txt;
    }

}

