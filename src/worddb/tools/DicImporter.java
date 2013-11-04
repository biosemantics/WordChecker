/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package worddb.tools;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import worddb.BioTermTable;
import worddb.CityTable;
import worddb.ProperNounTable;
import worddb.WordTable;

/**
 *
 * @author iychoi
 */
public class DicImporter {

    private File file;

    public DicImporter(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Cannot find the file");
        }

        this.file = file;
    }
    
    public int countDicEntries() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.file));

        int count = 0;
        String line = br.readLine();
        while (line != null) {
            if (!line.trim().equals("")) {
                count++;
            }
            line = br.readLine();
        }
        br.close();
        return count;
    }

    public void loadToWordTable(Connection conn, Component parentComponent) throws IOException {
        int entries = countDicEntries();
        int curEntries = 0;
        BufferedReader br = new BufferedReader(new FileReader(this.file));

        String line = br.readLine();

        WordTable.createWordTable(conn, false);
        while (line != null) {
            if (!line.trim().equals("")) {
                WordTable.insertWord(conn, line.trim());
                curEntries++;
            }
            
            line = br.readLine();
        }
        br.close();
    }

    public void loadToCityTable(Connection conn, Component parentComponent) throws IOException {
        int entries = countDicEntries();
        int curEntries = 0;
        BufferedReader br = new BufferedReader(new FileReader(this.file));

        String line = br.readLine();

        CityTable.createWordTable(conn, false);
        while (line != null) {
            if (!line.trim().equals("")) {
                CityTable.insertWord(conn, line.trim());
                curEntries++;
            }
            
            line = br.readLine();
        }
        br.close();
    }

    public void loadToBioTermTable(Connection conn, Component parentComponent) throws IOException {
        int entries = countDicEntries();
        int curEntries = 0;
        BufferedReader br = new BufferedReader(new FileReader(this.file));

        String line = br.readLine();

        BioTermTable.createWordTable(conn, false);
        while (line != null) {
            if (!line.trim().equals("")) {
                BioTermTable.insertWord(conn, line.trim());
                curEntries++;
            }

            line = br.readLine();
        }
        br.close();
    }

    public void loadToProperNounTable(Connection conn, Component parentComponent) throws IOException {
        int entries = countDicEntries();
        int curEntries = 0;
        BufferedReader br = new BufferedReader(new FileReader(this.file));

        String line = br.readLine();

        ProperNounTable.createWordTable(conn, false);
        while (line != null) {
            if (!line.trim().equals("")) {
                ProperNounTable.insertWord(conn, line.trim());
                curEntries++;
            }

            line = br.readLine();
        }
        br.close();
    }
}
