/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worddb;

import common.db.DBUtil;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iychoi
 */
public class DocumentTable {
    public static final String DOCUMENT_TABLE_NAME = "tblDocument";
    
    public static void createDocumentTable(Connection conn, boolean bClean) throws IOException {
        try {
            Statement stmt = conn.createStatement();
            if (bClean) {
                stmt.execute("drop table if exists " + DOCUMENT_TABLE_NAME);
            }
            stmt.execute("create table if not exists " + DOCUMENT_TABLE_NAME
                    + " (documentID integer not null primary key " + DBUtil.getAutoIncrementString() + ", "
                    + "filename text(1000) not null) " + DBUtil.getDefaultCharsetString() + ";");
            
            stmt.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static void insertDocument(Connection conn, Document document) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("insert into " + DOCUMENT_TABLE_NAME 
                    + " (filename) values (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, document.getFilename());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                document.setDocumentID(rs.getInt(1));
            } else {
                throw new IOException("there's no generated id");
            }
            
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static Document getDocument(Connection conn, int documentID) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("select * from " + DOCUMENT_TABLE_NAME 
                    + " where documentID = ?");
            pstmt.setInt(1, documentID);
            ResultSet rs = pstmt.executeQuery();
            Document document = null;
            if (rs.next()) {
                document = new Document();
                document.setDocumentID(rs.getInt("documentID"));
                document.setFilename(rs.getString("filename"));
            }
            
            rs.close();
            pstmt.close();
            return document;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static List<Document> getDocuments(Connection conn, File file) throws IOException {
        return getDocuments(conn, file.getName());
    }
    
    public static List<Document> getDocuments(Connection conn, String filename) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("select * from " + DOCUMENT_TABLE_NAME 
                    + " where filename = ?");
            pstmt.setString(1, filename);
            ResultSet rs = pstmt.executeQuery();
            
            List<Document> documents = new ArrayList<Document>();
            
            while (rs.next()) {
                Document document = new Document();
                document.setDocumentID(rs.getInt("documentID"));
                document.setFilename(rs.getString("filename"));
                
                documents.add(document);
            }
            
            rs.close();
            pstmt.close();
            return documents;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static void deleteDocument(Connection conn, int documentID) throws IOException {
        try {
            PreparedStatement pstmt = conn.prepareStatement("delete from " + DOCUMENT_TABLE_NAME 
                    + " where documentID = ?");
            pstmt.setInt(1, documentID);
            pstmt.executeUpdate();
            
            pstmt.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
