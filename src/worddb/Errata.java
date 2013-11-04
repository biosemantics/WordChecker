/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worddb;

/**
 *
 * @author iychoi
 */
public class Errata {
    private int errataID;
    private int documentID;
    private String errata;
    private String fixed;
    
    public Errata() {
        
    }
    
    public void setDocumentID(int documentID) {
        this.documentID = documentID;
    }
    
    public int getDocumentID() {
        return this.documentID;
    }
    
    public void setErrataID(int errataID) {
        this.errataID = errataID;
    }
    
    public int getErrataID() {
        return this.errataID;
    }
    
    public void setErrata(String errata) {
        this.errata = errata;
    }
    
    public String getErrata() {
        return this.errata;
    }
    
    public void setFixed(String fixed) {
        this.fixed = fixed;
    }
    
    public String getFixed() {
        return this.fixed;
    }
}
