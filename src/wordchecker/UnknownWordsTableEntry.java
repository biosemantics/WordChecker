/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wordchecker;

/**
 *
 * @author iychoi
 */
public class UnknownWordsTableEntry {

    private boolean errata;
    private String word;
    private int pageNo;
    private int idx;
    
    public void setErrata(boolean b) {
        this.errata = b;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public void setItemIndex(int idx) {
        this.idx = idx;
    }
    
    public boolean getErrata() {
        return this.errata;
    }
    
    public String getWord() {
        return this.word;
    }
    
    public int getItemIndex() {
        return this.idx;
    }
    
    public int getPageNo() {
        return this.pageNo;
    }
    
}
