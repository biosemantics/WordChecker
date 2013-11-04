/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package msword;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 *
 * @author iychoi
 */
public class WordReader {
    private File file;

    public WordReader(File wordFile) {
        if(wordFile == null || !wordFile.exists() || !wordFile.isFile())
            throw new IllegalArgumentException("Cannot find word file");
        
        this.file = wordFile;
    }
    
    private static boolean isFootnote(XWPFParagraph para) {
        boolean isFootnote = false;
        if (para.getCTP() != null) {
            if (para.getCTP().getPPr() != null) {
                if (para.getCTP().getPPr().getPStyle() != null) {
                    if (para.getCTP().getPPr().getPStyle().getVal()
                            .contains("Footnote")) {
                        return true;
                    }
                }
            }
        }
        return isFootnote;
    }
    
    public int parsePageInfo(String paragraph) {
        Pattern p1 = Pattern.compile("^\\[(page|Page|PAGE)\\s+(\\d+)\\s+(from|From|FROM)\\s+(pdf|Pdf|PDF)\\]$");
        Matcher mt1 = p1.matcher(paragraph);
        if(mt1.find()) {
            int page = Integer.parseInt(mt1.group(2));
            return page;
        }
        
        return -1;
    }
    
    public String getCleanParagraph(String paragraph) {
        Pattern p1 = Pattern.compile("^\\[(page|Page|PAGE)\\s+(\\d+)\\s+(from|From|FROM)\\s+(pdf|Pdf|PDF)\\]$");
        Matcher mt1 = p1.matcher(paragraph);
        if(mt1.find()) {
            //int page = Integer.parseInt(mt1.group(2));
            int offset = mt1.end();
            return paragraph.substring(offset);
        }
        
        return paragraph;
    }

    public List<WordPage> parse() throws IOException {
        FileInputStream fileIS = new FileInputStream(this.file);
        XWPFDocument wordDoc = new XWPFDocument(fileIS);
        
        List<WordPage> wordPages = new ArrayList<WordPage>();
        
        WordPage currentPage = null;
        List<XWPFParagraph> paragraphs = wordDoc.getParagraphs();
        for (XWPFParagraph para : paragraphs) {
            if (isFootnote(para)) {
                continue;
            }

            if (para.getRuns().size() < 1) {
                continue;
            }

            if (para.getText() != null
                    && (para.getText().trim().equals(""))) {
                continue;
            }
            
            String paragraphString = para.getText().trim();
            String cleanParagraph = getCleanParagraph(paragraphString);
            int page = parsePageInfo(paragraphString);
            if(page > 0) {
                // found
                currentPage = new WordPage(page);
                wordPages.add(currentPage);
                
                WordParagraph paragraph = new WordParagraph();
                paragraph.setParagraph(cleanParagraph);
                currentPage.addParagraph(paragraph);
            } else {
                // not found
                if(currentPage != null) {
                    WordParagraph paragraph = new WordParagraph();
                    paragraph.setParagraph(cleanParagraph);
                    currentPage.addParagraph(paragraph);
                } else {
                    currentPage = new WordPage(0);
                    wordPages.add(currentPage);

                    WordParagraph paragraph = new WordParagraph();
                    paragraph.setParagraph(cleanParagraph);
                    currentPage.addParagraph(paragraph);
                }
            }
        }
        
        return wordPages;
    }
}
