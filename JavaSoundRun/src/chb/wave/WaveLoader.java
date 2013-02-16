package chb.wave;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Load the path of each wave file, and get the wave files according to 
 * its hash string.
 * @author Hongbao Chen
 */
public class WaveLoader {

	public Map<String, String> WavePaths = null;
	
	private XPath XPath;
	private Document XmlDoc;
	private static String initdoc = "user_info.xml";
	
	/**
	 * Constructor. It will load the file paths into Map<String, String>.
	 */
	public WaveLoader() {
		this.WavePaths = new HashMap<String, String>();
		try {
			this.SetUpContext();
		} catch (ParserConfigurationException e) {
            e.printStackTrace();
            return;
        }
        catch(SAXException e) {
			e.printStackTrace();
			return;
		}  catch(IOException e) {
            e.printStackTrace();
            return;
        }
		
		String xpath = "/datasource/waves/wave";
		NodeList list = (NodeList)this.ExecXpath(xpath,  XPathConstants.NODESET);
		if(list.getLength() == 0)
			return;
		
		List<String> pathlist = new ArrayList<String>();
		for(int i = 0; i<list.getLength(); ++i) {
			Element elem = (Element)list.item(i);
			String tmp = elem.getAttribute("location");
			pathlist.add(tmp);
		}
		
		for(String s: pathlist) {
			File f = new File(s);
			if(f.isDirectory() == false)
				continue;
			
			this.LoadFiles(f);
		}
	}
	
	/**
	 * Load files paths into Map<String, String>.
	 * @param dir
	 */
	private void LoadFiles(File dir) {
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".wav");
		    }
		};
		
		String[] files = dir.list(filter);
		if(files != null && files.length > 0) {
			String[] segs = null;
			for(String s: files) {
				// Only process the file with the suffix ".wav", 
				// and directories are exclusive.
				File ft = new File(s);
				if(ft.isDirectory() == true)
					continue;
				
				segs = s.split(".");
				if(segs.length!=4)
					continue;
				
				String hash = segs[2];
				String path = dir.getAbsolutePath();
				if(path.endsWith("\\") == false)
					path += "\\";
				
				path += s;
				this.WavePaths.put(hash, path);
			}
		}
	}
	
	/**
	 * Get Wave object with the hash string given.
	 * @param hash the pinyin hashing string of the character.
	 * @return Wave object.
	 */
	public Wave GetWave(String hash) {
		if(hash == null || hash.length() == 0)
			return null;
		
		Wave wave = Wave.CreateWave(Wave.DefaultFormat);
		String path = this.WavePaths.get(hash);
		try {
			wave.ReadFrom(path);
			return wave;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Set up the XML Document and the XPath objects.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void SetUpContext() 
			throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); 
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.XmlDoc = builder.parse(WaveLoader.initdoc);
        

        XPathFactory xpathfactory = XPathFactory.newInstance();
        this.XPath = xpathfactory.newXPath();
	} 
	
	/**
	 * Execute XPath expression with the Document and XPath provided.
	 * @param path the xpath expression.
	 * @return the object that represent the returned result.
	 */
	private Object ExecXpath(String path, QName type) {
		if(path == null || path.length() == 0)
			return null;
		
		if(this.XPath == null || this.XmlDoc == null)
			return null;
		
		try {
			return this.XPath.evaluate(path, this.XmlDoc, type);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
	}

}
