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
public class MySQLConnector {
    public static Connection getConnection(String id, String pwd) throws SQLException, ClassNotFoundException {
        // This will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        return DriverManager.getConnection("jdbc:mysql://localhost/wordchecker?useUnicode=true&characterEncoding=utf-8", id, pwd);
    }
    
    public static void setCharacterSet(Connection conn) throws IOException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute("SET NAMES 'utf8'");
            stmt.execute("set character_set_server='utf8';");
            // stmt.execute("set character_set_dabatase='utf8';");
            // stmt.execute("set character_set_system = 'utf8';");
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
