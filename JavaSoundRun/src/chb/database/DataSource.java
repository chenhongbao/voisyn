/**
 * 
 */
package chb.database;

import java.sql.*;

/**
 * @author Administrator
 * 
 */
public class DataSource {

	public Integer State;

	public Connection Connection;

	public String ConnectionString;
	public String DbAddress;
	public String DataBase;
	public String User;
	public String Password;
	public String Encoding;

	public static class ConnectionState {

		public static final Integer Closed = 0x0001;
		public static final Integer Opened = 0x0002;

	}

	public DataSource() {
	}

	public DataSource CreateConnection(String _addr, String _db,
			String _user, String _passwd, String _encoding) {

		DataSource conn = new DataSource();
		conn.DbAddress = _addr;
		conn.DataBase = _db;
		conn.User = _user;
		conn.Password = _passwd;
		conn.Encoding = _encoding;

		return conn;

	}

	public void Open() throws SQLException {
		if (this.Connection != null) {
			if (this.Connection.isValid(500) == true)
				return;
		}
		
		if (this.ConnectionString == null
				|| this.ConnectionString.length() == 0) {

			this.ConnectionString = this.BuildConnectionString(this.DbAddress,
					this.DataBase, this.User, this.Password, this.Encoding);
		}

		this.Connection = DriverManager.getConnection(this.ConnectionString);
		this.State = DataSource.ConnectionState.Opened;

	}

	public void Close() throws SQLException {
		if (this.Connection.isClosed() == false)
			this.Connection.close();

		this.State = DataSource.ConnectionState.Closed;

	}

	private String BuildConnectionString(String _addr, String _db,
			String _user, String _passwd, String _encoding) {

		String conn = "jdbc:mysql://" + this.DbAddress + "/" + this.DataBase
				+ "?" + "user=" + this.User + "&password=" + this.Password
				+ "&characterEncoding=" + this.Encoding;

		return conn;
	}

}
