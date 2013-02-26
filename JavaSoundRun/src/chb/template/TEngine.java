package chb.template;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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

    /**
     * Save XML to the specified path.
     *
     * @param path Path where it stores the XML.
     * @return Returns true at success and false at failure.
     */
    public boolean saveTemplate(String path) {

        File file = new File(path);
        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                chb.Utility.Log(e.getStackTrace());
            }
        }

        return saveTemplate(file);
    }

    /**
     * Save XML to the specified file.
     *
     * @param file File where it stores the XML.
     * @return Returns true at success and false at failure.
     */
    public boolean saveTemplate(File file) {
        if (file == null) {
            return false;
        }
        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                chb.Utility.Log(e.getStackTrace());
            }
        }

        try {
            FileOutputStream os = new FileOutputStream(file);
            return saveTemplate(os);
        } catch (FileNotFoundException e) {
            chb.Utility.Log(e.getStackTrace());
            return false;
        }

    }

    /**
     * Save XML to the specified output stream.
     *
     * @param os OutputStream where it stores the XML.
     * @return Returns true at success and false at failure.
     */
    public boolean saveTemplate(OutputStream os) {
        if (os == null) {
            return false;
        }

        xml.normalize();

        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // Encode the XML.
            DOMSource source = new DOMSource(xml);
            PrintWriter pw = new PrintWriter(os);
            StreamResult result = new StreamResult(pw);
            transformer.transform(source, result);

            return true;
        } catch (TransformerConfigurationException e) {
            chb.Utility.Log(e.getStackTrace());
        } catch (TransformerException e) {
            chb.Utility.Log(e.getStackTrace());
        }

        return false;
    }

}
