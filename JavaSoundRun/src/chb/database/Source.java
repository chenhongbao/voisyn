package chb.database;

import java.sql.SQLException;

/**
 * Interface for the holder of corpus data.
 */
public interface Source {
    /**
     * Return the instance of the table according to its name.
     * @param name the name of the table.
     * @return  the instance of the table
     */
    Table getTable(String name);

    /**
     * Open the database and set up all the corpus data.
     */
    void Open();

    /**
     * Close the database and release resources.
     * @throws SQLException
     */
    void Close() throws SQLException;

    /**
     * Authorize the access.
     * @param user user name of the current session.
     * @param passwd  password for the current user.
     * @param db  the name of the database to be used.
     * @return  true if the user with password is granted the access to
     *   the database.
     */
    boolean IsUserValid(String user, String passwd, String db);
}
