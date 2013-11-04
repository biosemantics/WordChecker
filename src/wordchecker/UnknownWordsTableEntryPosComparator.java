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
public class UnknownWordsTableEntryPosComparator implements Comparator<UnknownWordsTableEntry> {

    public UnknownWordsTableEntryPosComparator() {
        
    }

    @Override
    public int compare(UnknownWordsTableEntry t, UnknownWordsTableEntry t1) {
        return t.getItemIndex() - t1.getItemIndex();
    }
    
}
