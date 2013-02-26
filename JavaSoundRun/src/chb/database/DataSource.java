/**
 *
 */
package chb.database;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 */
public class DataSource implements Source {

    public Integer State;

    public String ConnectionString;
    public String DbAddress;
    public String DataBase;
    public String User;
    public String Password;
    public String Encoding;

    private Document XmlDoc = null;
    private XPath XPath = null;

    public static String initdoc = "user_info.xml";

    private Map<String, Table> Tables = null;

    private boolean ReOpened = false;

    /**
     * Get table by name.
     *
     * @param name the table name.
     * @return the Table instance.
     */
    @Override
    public Table getTable(String name) {
        if (name == null || name.length() == 0)
            return null;

        return this.Tables.get(name);
    }

    public static class ConnectionState {

        public static final Integer Closed = 0x0001;
        public static final Integer Opened = 0x0002;

    }

    public DataSource() {
        this.Tables = new HashMap<String, Table>();
    }

    /**
     * Factory method to get a new instance of DataSource.
     *
     * @param _addr     the database address (relative path to the root of application).
     * @param _db       the name of the database.
     * @param _user     the user name of the current user.
     * @param _passwd   the password of the user.
     * @param _encoding the encoding of the files.
     * @return a newly instantiated DataSource object.
     */
    public static DataSource CreateConnection(String _addr, String _db,
                                              String _user, String _passwd, String _encoding) {

        DataSource conn = new DataSource();
        conn.DbAddress = _addr;
        conn.DataBase = _db;
        conn.User = _user;
        conn.Password = _passwd;
        conn.Encoding = _encoding;
        conn.Tables = new HashMap<String, Table>();

        return conn;

    }

    @Override
    public void Open() {
        // If the database has been opened, but closed later, just change its state
        // and leave it.
        if (this.ReOpened) {
            this.State = DataSource.ConnectionState.Opened;
            return;
        }

        //Read the user_info.xml, and decide whether user exists in the XML file.
        //Read in all the databases related to that user, and the tables in the
        //databases.
        if (!IsUserValid(this.User, this.Password, this.DataBase))
            return;
        String path = "/datasource/databases/database[@name='"
                + this.DataBase + "']/@address";
        NodeList list = (NodeList) ExecXpath(path, XPathConstants.NODESET);
        if (list.getLength() != 1)
            return;

        this.DbAddress = list.item(0).getNodeValue();
        if (!this.DbAddress.endsWith("/"))
            this.DbAddress += "/";

        path = "/datasource/databases/database[@name='"
                + this.DataBase + "']/table";

        list = (NodeList) ExecXpath(path, XPathConstants.NODESET);
        for (int i = 0; i < list.getLength(); ++i) {
            Element elem = (Element) list.item(i);
            String tablename = elem.getAttribute("name");
            String tablelocation = elem.getAttribute("location");
            String tablepath = this.DbAddress + tablelocation;

            Table table = new DataTable(tablename, tablepath, this.Encoding);
            this.Tables.put(tablename, table);
        }

        for (String s : this.Tables.keySet()) {
            Table t = this.Tables.get(s);
            t.SetUp();
        }

        this.State = DataSource.ConnectionState.Opened;

    }

    @Override
    public void Close() throws SQLException {
        //Clear all the resources related to that user.
        this.ReOpened = true;
        this.State = DataSource.ConnectionState.Closed;

    }

    /**
     * Test whether the user is a valid user.
     *
     * @param user   the user name.
     * @param passwd user's password.
     * @param db     the database name.
     * @return true if the user is valid and false if he is not.
     */
    @Override
    public boolean IsUserValid(String user, String passwd, String db) {
        try {
            SetUpContext();
        } catch (ParserConfigurationException e) {
            chb.Utility.Log(e.getStackTrace());
        } catch (SAXException e) {
            chb.Utility.Log(e.getStackTrace());
        } catch (IOException e) {
            chb.Utility.Log(e.getStackTrace());
        }

        String path = "/datasource/users/user[@name='"
                + user + "' and @password='"
                + passwd + "']/db[@name='"
                + db + "']";

        Object res = ExecXpath(path, XPathConstants.NODESET);
        NodeList nodes = (NodeList) res;

        return 0 != nodes.getLength();

    }

    /**
     * Set up the XML Document and the XPath objects.
     *
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private void SetUpContext()
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.XmlDoc = builder.parse(DataSource.initdoc);


        XPathFactory xpathfactory = XPathFactory.newInstance();
        this.XPath = xpathfactory.newXPath();
    }

    /**
     * Execute XPath expression with the Document and XPath provided.
     *
     * @param path the xpath expression.
     * @return the object that represent the returned result.
     */
    private Object ExecXpath(String path, QName type) {
        if (path == null || path.length() == 0)
            return null;

        if (this.XPath == null || this.XmlDoc == null)
            return null;

        try {
            return this.XPath.evaluate(path, this.XmlDoc, type);
        } catch (XPathExpressionException e) {
            chb.Utility.Log(e.getStackTrace());
            return null;
        }
    }

}
