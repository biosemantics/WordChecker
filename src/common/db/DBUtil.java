/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common.db;

import java.io.IOException;
import java.sql.Connection;

/**
 *
 * @author iychoi
 */
public class DBUtil {
    
    private enum ConnectionMode {
        MODE_MYSQL,
        MODE_SQLITE
    }
    
    private static ConnectionMode CONNECTION_MODE = ConnectionMode.MODE_SQLITE;
    
    public static void setConnectionMode(ConnectionMode connectionMode) {
        CONNECTION_MODE = connectionMode;
    }
    
    public static ConnectionMode getConnectionMode() {
        return CONNECTION_MODE;
    }
    
    public static String getAutoIncrementString() {
        if(CONNECTION_MODE == ConnectionMode.MODE_SQLITE) {
            return "autoincrement";
        } else if(CONNECTION_MODE == ConnectionMode.MODE_MYSQL) {
            return "auto_increment";
        }
        return null;
    }
    
    public static String getDefaultCharsetString() {
        if(CONNECTION_MODE == ConnectionMode.MODE_SQLITE) {
            //return "DEFAULT CHARSET=utf8";
            return "";
        } else if(CONNECTION_MODE == ConnectionMode.MODE_MYSQL) {
            return "DEFAULT CHARACTER SET = 'utf8'";
        }
        return null;
    }
    
    public static Connection getConnection() throws IOException {
        if(CONNECTION_MODE == ConnectionMode.MODE_SQLITE) {
            return getConnectionToSQLite("wordchecker.db");
        } else if(CONNECTION_MODE == ConnectionMode.MODE_MYSQL) {
            return getConnectionToMySQL("wordchecker", "wordchecker.db");
        }
        return null;
    }
    
    private static Connection getConnectionToMySQL(String id, String pwd) throws IOException {
        try {
            Connection conn = MySQLConnector.getConnection(id, pwd);
            MySQLConnector.setCharacterSet(conn);
            return conn;
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
    private static Connection getConnectionToSQLite(String dbFilename) throws IOException {
        try {
            Connection conn = SQLiteConnector.getConnection(dbFilename);
            SQLiteConnector.setCharacterSet(conn);
            return conn;
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
    }
}
