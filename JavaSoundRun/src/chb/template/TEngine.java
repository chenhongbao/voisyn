package chb.template;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

public class TEngine {
    // XML document instance.
    protected Document xml = null;

    /**
     * Load template by given path.
     *
     * @param path Path to the template files.
     * @return If the method successfully loads template, it
     *         will return true, otherwise returns false.
     */
    public boolean loadTemplate(String path) {
        File f = new File(path);
        if (f.exists() == false || f.isDirectory()) {
            return false;
        }
        return loadTemplate(f);
    }

    /**
     * Load template by given File.
     *
     * @param file File instance of the template files.
     * @return If the method successfully loads template, it
     *         will return true, otherwise returns false.
     */
    public boolean loadTemplate(File file) {
        if (file.exists() == false || file.isDirectory()) {
            return false;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            chb.Utility.Log(e.getStackTrace());
            return false;
        }

        if (fis == null) {
            return false;
        }
        return loadTemplate(fis);
    }

    /**
     * Load template from given InputStream.
     *
     * @param ins InputStream of the template files.
     * @return If the method successfully loads template, it
     *         will return true, otherwise returns false.
     */
    public boolean loadTemplate(InputStream ins) {
        try {
            this.xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ins);
        } catch (SAXException e) {
            chb.Utility.Log(e.getStackTrace());
            return false;
        } catch (IOException e) {
            chb.Utility.Log(e.getStackTrace());
            return false;
        } catch (ParserConfigurationException e) {
            chb.Utility.Log(e.getStackTrace());
            return false;
        }
        return true;
    }

}
