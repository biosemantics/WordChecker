/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author iychoi
 */
public class SQLiteConnector {
    public static Connection getConnection(String dbFilename) throws SQLException, ClassNotFoundException {
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");

        // Setup the connection with the DB
        return DriverManager.getConnection("jdbc:sqlite:" + dbFilename);
    }
    
    public static void setCharacterSet(Connection conn) throws IOException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute( "PRAGMA encoding = \"UTF-8\"" );
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
