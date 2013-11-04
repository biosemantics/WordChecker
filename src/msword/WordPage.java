/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package msword;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iychoi
 */
public class WordPage {
    private int pageNo;
    private List<WordParagraph> paragraphs;
    
    public WordPage(int pageNo) {
        this.pageNo = pageNo;
        this.paragraphs = new ArrayList<WordParagraph>();
    }
    
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
    
    public int getPageNo() {
        return this.pageNo;
    }
    
    public void addParagraph(WordParagraph paragraph) {
        this.paragraphs.add(paragraph);
    }
    
    public List<WordParagraph> getParagraphs() {
        return this.paragraphs;
    }
}
