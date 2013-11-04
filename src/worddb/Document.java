/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worddb;

import java.io.File;

/**
 *
 * @author iychoi
 */
public class Document {
    private int documentID;
    private String filename;
    
    public Document() {
        
    }
    
    public void setDocumentID(int documentID) {
        this.documentID = documentID;
    }
    
    public int getDocumentID() {
        return this.documentID;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public void setFilename(File file) {
        this.filename = file.getName();
    }
    
    public String getFilename() {
        return this.filename;
    }
}
