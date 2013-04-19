/**
 *
 */
package chb.synthesis;

import chb.database.DataSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;

/**
 * Get wave files via WaveLoader and sentence information from template.*, and
 * then combine them together. It is the final producer of the speech synthesis.
 *
 * @author Hongbao Chen
 */
public final class VoiceOrchestra implements Runnable {

    protected String txt_path = "";
    protected String exec_tmpl_path = "";
    protected String wav_dir = "";
    protected Document xmldoc = null;
    protected XPath xpath = null;

    /**
     * Constructor, initializing the path parameter.
     */
    public VoiceOrchestra(String p) {
        this.txt_path = p;
        File f = new File(this.txt_path);
        this.exec_tmpl_path = "./JavaSoundRun/tmpl/" + f.getName() + ".exec.xml";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.xmldoc = builder.parse(DataSource.initdoc);


            XPathFactory xpathfactory = XPathFactory.newInstance();
            this.xpath = xpathfactory.newXPath();

            XPathExpression expr = this.xpath.compile("/datasource/waves/wave[@name='basic']/@location");
            NodeList nodes = (NodeList) expr.evaluate(this.xmldoc, XPathConstants.NODESET);
            if (nodes.getLength() < 1) {
                JOptionPane.showMessageDialog(null, "未配置波形文件路径。");
                return;
            }

            org.w3c.dom.Node n = nodes.item(0);
            this.wav_dir = n.getNodeValue();

        } catch (ParserConfigurationException e1) {
            JOptionPane.showMessageDialog(null, "XML配置错误。\n" + e1.getMessage());
            return;
        } catch (SAXException e2) {
            JOptionPane.showMessageDialog(null, "配置文件错误。\n" + e2.getMessage());
            return;
        } catch (IOException e3) {
            JOptionPane.showMessageDialog(null, "配置文件读取错误。\n" + e3.getMessage());
            return;
        } catch (XPathExpressionException e4) {
            JOptionPane.showMessageDialog(null, "配置文件解析错误。\n" + e4.getMessage());
            return;
        }

    }

    @Override
    public void run() {
        // TODO Implement the synthesizing method.
        // This method is under intensive testing and bug-fixing. Please wait until
        // it is completely available.
    }
}
