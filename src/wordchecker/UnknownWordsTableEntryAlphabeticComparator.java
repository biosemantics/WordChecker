/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wordchecker;

import java.util.Comparator;

/**
 *
 * @author iychoi
 */
public class UnknownWordsTableEntryAlphabeticComparator implements Comparator<UnknownWordsTableEntry> {

    public UnknownWordsTableEntryAlphabeticComparator() {
        
    }

    @Override
    public int compare(UnknownWordsTableEntry t, UnknownWordsTableEntry t1) {
        return t.getWord().compareTo(t1.getWord());
    }
    
}
