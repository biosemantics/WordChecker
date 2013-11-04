/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wordchecker;

import msword.WordPage;

/**
 *
 * @author iychoi
 */
public class WordPagePair {
    private String word;
    private WordPage page;
    
    public WordPagePair() {
    }
    
    public WordPagePair(String word, WordPage page) {
        this.word = word;
        this.page = page;
    }
    
    public String getWord() {
        return this.word;
    }
    
    public void setWord(String word) {
        this.word = word;
    }
    
    public WordPage getPage() {
        return this.page;
    }
    
    public void setPage(WordPage page) {
        this.page = page;
    }
}
