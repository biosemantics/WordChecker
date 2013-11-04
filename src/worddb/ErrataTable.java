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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author iychoi
 */
public class ErrataTable {
    public static final String ERRATA_TABLE_NAME = "tblErrata";
    
    public static void createErrataTable(Connection conn, boolean bClean) throws IOException {
        try {
            Statement stmt = conn.createStatement();
            if (bClean) {
                stmt.execute("drop table if exists " + ERRATA_TABLE_NAME);
            }
            stmt.execute("create table if not exists " + ERRATA_TABLE_NAME
                    + " (errataID integer not null primary key " + DBUtil.getAutoIncrementString() + ", "
                    + "errata text(50) not null, "
                    + "fixed text(50) not null, "
                    + "documentID integer not null) " + DBUtil.getDefaultCharsetString() + ";");
            
            stmt.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static void insertErrata(Connection conn, Errata errata) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("insert into " + ERRATA_TABLE_NAME 
                    + " (errata, fixed, documentID) values (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, errata.getErrata());
            pstmt.setString(2, errata.getFixed());
            pstmt.setInt(3, errata.getDocumentID());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                errata.setErrataID(rs.getInt(1));
            } else {
                throw new IOException("there's no generated id");
            }
            
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static int getRegisteredErrataCount(Connection conn) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("select count(*) as cnt from " + ERRATA_TABLE_NAME);
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
    
    public static Hashtable getHashTable(Connection conn, int documentID) throws IOException {
        Hashtable table = new Hashtable<String, String>();
        try {
            PreparedStatement pstmt = conn.prepareStatement("select * from " + ERRATA_TABLE_NAME
                    + " where documentID = ?");
            pstmt.setInt(1, documentID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String fixed = rs.getString("fixed");
                String errata = rs.getString("errata");
                if(!table.containsKey(errata))
                    table.put(errata, fixed);
            }
            
            rs.close();
            pstmt.close();
            return table;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static List<Errata> getErrataList(Connection conn, int documentID) throws IOException {
        List<Errata> errataList = new ArrayList<Errata>();
        try {
            PreparedStatement pstmt = conn.prepareStatement("select * from " + ERRATA_TABLE_NAME
                    + " where documentID = ?");
            pstmt.setInt(1, documentID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int errataID = rs.getInt("errataID");
                String fixed = rs.getString("fixed");
                String errata = rs.getString("errata");
                int n_documentID = rs.getInt("documentID");
                
                Errata err = new Errata();
                err.setDocumentID(n_documentID);
                err.setErrataID(errataID);
                err.setErrata(errata);
                err.setFixed(fixed);
                
                errataList.add(err);
            }
            
            rs.close();
            pstmt.close();
            return errataList;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static String fixErrata(Connection conn, int documentID, String word) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("select * from " + ERRATA_TABLE_NAME 
                    + " where documentID = ? and word = ?");
            
            pstmt.setInt(1, documentID);
            pstmt.setString(2, word);
            ResultSet rs = pstmt.executeQuery();
            String fixed = word;
            if (rs.next()) {
                fixed = rs.getString("fixed");
            }
            
            rs.close();
            pstmt.close();
            return fixed;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static void deleteErrata(Connection conn, int errataID) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("delete from " + ERRATA_TABLE_NAME 
                    + " where errataID = ?");
            pstmt.setInt(1, errataID);
            pstmt.executeUpdate();
            
            pstmt.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
