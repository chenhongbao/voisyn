package chb.template;

import java.util.HashMap;
import java.util.Map;

/**
 * Store the word anf its relative information about
 * rythme and pitch after it is processsed by template
 * engine.
 */
public class TInfo {

    final public static int PUNCTUATION = 1000;
    final public static int WORD = 1001;

    // Info is kept in pairs.
    public Map<String, String> info = null;
    // Literal value of this word.
    public String content = null;
    public int type = -1;
    // The index of the current word.
    public int index = -1;

    protected TInfo(String _c) {
        this.content = _c;
        this.info = new HashMap<String, String>();
    }

    /**
     * Query information by key-pair.
     *
     * @param k key in String
     * @return value in String
     */
    public String get(String k) {
        return info == null ? null : info.get(k);
    }

    /**
     * Set the key-value into the TInfo.
     *
     * @param k key in String
     * @param v value in String
     */
    public void set(String k, String v) {
        if (info == null) {
            info = new HashMap<String, String>();
        }

        info.put(k, v);
    }

    /**
     * Factory method to create an instance of TInfo.
     *
     * @param var_list Variable-length argument list.
     * @return An newly created instance of TInfo.
     */
    public static TInfo createTInfo(String... var_list) {
        if (var_list.length < 1) {
            return null;
        }

        TInfo ti = new TInfo(var_list[0]);
        for (int i = 1; i < var_list.length; i += 2) {
            String k = var_list[i];
            String v = null;
            if (i + 1 < var_list.length) {
                v = var_list[i + 1];
            }
            ti.set(k, v);
        }

        return ti;
    }
}
