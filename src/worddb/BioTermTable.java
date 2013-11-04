/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worddb;

import common.db.DBUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

/**
 *
 * @author iychoi
 */
public class BioTermTable {
    public static final String BIOTERM_TABLE_NAME = "tblBioTerm";
    
    public static void createWordTable(Connection conn, boolean bClean) throws IOException {
        try {
            Statement stmt = conn.createStatement();
            if (bClean) {
                stmt.execute("drop table if exists " + BIOTERM_TABLE_NAME);
            }
            stmt.execute("create table if not exists " + BIOTERM_TABLE_NAME
                    + " (wordID integer not null primary key " + DBUtil.getAutoIncrementString() + ", "
                    + "word text(50) not null) " + DBUtil.getDefaultCharsetString() + ";");
            
            stmt.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static void insertWord(Connection conn, String word) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("insert into " + BIOTERM_TABLE_NAME 
                    + " (word) values (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, word);
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                // skip
            } else {
                throw new IOException("there's no generated id");
            }
            
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static int getRegisteredWordCount(Connection conn) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("select count(*) as cnt from " + BIOTERM_TABLE_NAME);
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt("cnt");
            }
            
            rs.close();
            pstmt.close();
            return count;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static Hashtable getHashTable(Connection conn) throws IOException {
        Hashtable table = new Hashtable<String, Integer>();
        try {
            PreparedStatement pstmt = conn.prepareStatement("select * from " + BIOTERM_TABLE_NAME);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("wordID");
                String word = rs.getString("word");
                if(!table.containsKey(word))
                    table.put(word, id);
            }
            
            rs.close();
            pstmt.close();
            return table;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static boolean checkWord(Connection conn, String word) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("select * from " + BIOTERM_TABLE_NAME 
                    + " where word = ?");
            pstmt.setString(1, word);
            ResultSet rs = pstmt.executeQuery();
            boolean bFound = false;
            if (rs.next()) {
                bFound = true;
            }
            
            rs.close();
            pstmt.close();
            return bFound;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static void deleteWord(Connection conn, String word) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("delete from " + BIOTERM_TABLE_NAME 
                    + " where word = ?");
            pstmt.setString(1, word);
            pstmt.executeUpdate();
            
            pstmt.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
