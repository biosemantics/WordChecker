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
public class UnknownWordsTableEntryLengthComparator implements Comparator<UnknownWordsTableEntry> {

    public UnknownWordsTableEntryLengthComparator() {
        
    }

    @Override
    public int compare(UnknownWordsTableEntry t, UnknownWordsTableEntry t1) {
        return t.getWord().length() - t1.getWord().length();
    }
    
}
