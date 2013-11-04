/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worddb;

import common.db.DBUtil;
import java.io.IOException;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author iychoi
 */
public class DBFacade {
    public static void createTables(Connection conn) throws IOException {
        WordTable.createWordTable(conn, false);
        CityTable.createWordTable(conn, false);
        BioTermTable.createWordTable(conn, false);
        ProperNounTable.createWordTable(conn, false);
        
        DocumentTable.createDocumentTable(conn, false);
        ErrataTable.createErrataTable(conn, false);
    }
    
    public static int getRegisteredWordCount(Connection conn) throws IOException {
        int wordCnt = WordTable.getRegisteredWordCount(conn);
        int cityCnt = CityTable.getRegisteredWordCount(conn);
        int biotermCnt = BioTermTable.getRegisteredWordCount(conn);
        int propernounCnt = ProperNounTable.getRegisteredWordCount(conn);
        return wordCnt + cityCnt + biotermCnt + propernounCnt;
    }
    
    public static Hashtable getHashTableCaseSensitive(Connection conn) throws IOException {
        Hashtable ht = new Hashtable<String, Integer>();
        
        Hashtable cityHt = CityTable.getHashTable(conn);
        Hashtable propernounHt = ProperNounTable.getHashTable(conn);
        
        Enumeration<String> cityEnum = cityHt.keys();
        Enumeration<String> propernounEnum = propernounHt.keys();
        
        while(cityEnum.hasMoreElements()) {
            String word = cityEnum.nextElement();
            System.out.println("word : " + word);
            if(!ht.containsKey(word))
                ht.put(word, 1);
        }
        
        while(propernounEnum.hasMoreElements()) {
            String word = propernounEnum.nextElement();
            System.out.println("word : " + word);
            if(!ht.containsKey(word))
                ht.put(word, 3);
        }
        
        return ht;
    }
    
    public static Hashtable getHashTableCaseInsensitive(Connection conn) throws IOException {
        Hashtable ht = new Hashtable<String, Integer>();
        
        Hashtable wordHt = WordTable.getHashTable(conn);
        Hashtable biotermHt = BioTermTable.getHashTable(conn);
        
        Enumeration<String> wordEnum = wordHt.keys();
        Enumeration<String> biotermEnum = biotermHt.keys();
        
        while(wordEnum.hasMoreElements()) {
            String word = wordEnum.nextElement();
            
            if(!ht.containsKey(word))
                ht.put(word, 0);
        }
        
        while(biotermEnum.hasMoreElements()) {
            String word = biotermEnum.nextElement();
            if(!ht.containsKey(word))
                ht.put(word, 2);
        }
        
        return ht;
    }
    
    public static Hashtable getHashTableErrata(Connection conn, int documentID) throws IOException {
        Hashtable ht = new Hashtable<String, String>();
        
        return ErrataTable.getHashTable(conn, documentID);
    }
}
