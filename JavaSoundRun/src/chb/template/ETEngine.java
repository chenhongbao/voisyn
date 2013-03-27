package chb.template;

import chb.segment.CWWord;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

public class ETEngine extends TEngine {

    MTEngine meta = null;

    String exec_path = null;

    public ETEngine(String meta_path, String exec_path) {
        super();

        this.meta = new MTEngine();
        this.meta.loadTemplate(meta_path);
        this.exec_path = exec_path;

    }

    /**
     * Generate the execution template.
     *
     * @param lst List of the word segmentation result.
     * @return Returns true at success and false at failure.
     */
    public boolean generateExecTemplate(List<CWWord> lst) {
        if (lst == null || lst.size() < 1) {
            return false;
        }

        try {
            xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            chb.Utility.Log(e.getStackTrace());
        }

        xml.setXmlVersion("1.0");
        xml.setXmlStandalone(true);
        Element root = xml.createElement("article");

        int i = 0;
        for (CWWord w : lst) {
            root.appendChild(getExecWord(w, i));
            i += 1;
            root.appendChild(getExecBt(i));
            i += 1;
        }
        xml.appendChild(root);

        saveTemplate(this.exec_path);
        return true;
    }

    /**
     * Create a Element from the CWWord for further processing.
     *
     * @param word  CWWord to process.
     * @param index The index of the current word.
     * @return Element to be added to the XML document.
     */
    public Element getExecWord(CWWord word, int index) {
        Element node = null;
        TInfo inf = meta.query(word.Word);
        if (inf.type == TInfo.PUNCTUATION) {
            node = xml.createElement("pause");
            node.setAttribute("id", String.valueOf(index));
            node.setAttribute("msec", String.valueOf(inf.get("pause")));
        } else {
            node = xml.createElement("word");
            node.setAttribute("id", String.valueOf(index));
            node.setAttribute("msec", String.valueOf(inf.get("diaphone")));
            node.setAttribute("value", word.Word);
        }
        return node;
    }

    /**
     * Create element for the separator between words.
     *
     * @param index The index of the current word.
     * @return Instance of Element.
     */
    public Element getExecBt(int index) {

        TInfo inf = meta.queryBt();

        Element node = xml.createElement("pause");
        node.setAttribute("id", String.valueOf(index));
        node.setAttribute("msec", String.valueOf(inf.get("pause")));

        return node;
    }
}
