/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wordchecker;

import common.db.DBUtil;
import common.utils.StringUtil;
import gui.common.DjvuView;
import gui.common.ScreenUtil;
import java.awt.Color;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import msword.WordPage;
import msword.WordParagraph;
import msword.WordReader;
import worddb.BioTermTable;
import worddb.CityTable;
import worddb.DBFacade;
import worddb.Document;
import worddb.Errata;
import worddb.ProperNounTable;
import worddb.WordTable;

/**
 *
 * @author iychoi
 */
public class WordCheckerGui extends javax.swing.JFrame {

    private Document document;
    private File workingParentDir;
    private File wordFile;
    private File djvuFile;
    private DjvuView djvuView;
    private List<WordPage> wordPages;
    private int currentDjvuPage;
    private int wordsInDB;
    private Hashtable wordsTableCaseSensitive;
    private Hashtable wordsTableCaseInsensitive;
    private Hashtable errataTable;
    private List<WordPagePair> unknownWords;
    private List<WordPagePair> errataWords;
    private Style gstyle;
    private int unknownWordsCount;
    private int unknownBytesCount;
    private int errataCount;
    private int errataBytesCount;
    private int totalWordsCount;
    private int totalBytesCount;

    public enum TableShowMode {
        MODE_POSITION,
        MODE_ALPHABETIC,
        MODE_LENGTH,
    };
    
    private TableShowMode mode = TableShowMode.MODE_POSITION;
    
    /**
     * Creates new form WordChecker
     */
    public WordCheckerGui() {
        
        DocumentSelectionPopup popup = new DocumentSelectionPopup(this, true);
        popup.setVisible(true);
        
        this.document = popup.getSelectedDocument();
        this.djvuFile = popup.getSelectedDjvuFile();
        this.wordFile = popup.getSelectedWordFile();
        
        initComponents();

        init();
        
        this.tblUnknownWords.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent lse) {
                fireTableSelected();
            }
        });
    }

    private void init() {
        
        loadDjvuFile(this.djvuFile);
        loadWordFile(this.wordFile);
        
        reloadDicWords();
    }

    private void movePage(int page) {
        this.currentDjvuPage = page;
        this.djvuView.movePage(page);

        int pageEnd = 0;
        if (this.wordPages != null && this.wordPages.size() > 0) {
            pageEnd = this.wordPages.get(this.wordPages.size() - 1).getPageNo();
        }
        this.lblPage.setText("Page " + this.currentDjvuPage + " / " + pageEnd);
    }

    private int getPage() {
        return this.currentDjvuPage;
    }

    private void loadView(File djvuFile) {
        if (djvuFile == null) {
            throw new IllegalArgumentException("file is null");
        }

        this.djvuView = new DjvuView(djvuFile.getAbsolutePath());
        this.djvuView.run();
        
        resizeDjvuView(1, 2);
    }
    
    private void resizeDjvuView(int ratioDjvu, int ratioControl) {
        int scrWidth = ScreenUtil.getScreenWorkingWidth();
        int scrHeight = ScreenUtil.getScreenWorkingHeight();

        double ratio = (double)ratioDjvu / (double)(ratioDjvu + ratioControl);
        Insets scrInsets = ScreenUtil.getScreenInsets();

        int wnd1Width = (int)(scrWidth * ratio);
        int wnd2Width = (int)(scrWidth * (1 - ratio));
        int wndHeight = scrHeight;
        
        int wnd1PosX = scrInsets.left;
        int wnd1PosY = scrInsets.top;
        int wnd2PosX = scrInsets.left + wnd1Width;
        int wnd2PosY = scrInsets.top;
        
        if (this.djvuView != null) {
            this.djvuView.setSize(wnd1Width, wndHeight);
            this.djvuView.setLocation(wnd1PosX, wnd1PosY);
        }

        this.setSize(wnd2Width, wndHeight);
        this.setLocation(wnd2PosX, wnd2PosY);
    }

    private void closeView() {
        if (this.djvuView != null) {
            this.djvuView.setVisible(false);
            this.djvuView.dispose();
        }

        this.djvuView = null;
    }

    private void loadDjvuFile(File file) {
        this.djvuFile = file;
        this.workingParentDir = file.getParentFile();

        loadView(file);

        this.lblDjvu.setText(file.getName());
        this.currentDjvuPage = 0;
    }

    private void loadWordFile(File file) {
        this.workingParentDir = file.getParentFile();
        this.wordFile = file;

        this.lblWord.setText(file.getName());
    }

    private void startParsing() throws Exception {
        if (this.wordFile == null) {
            throw new Exception("djvuXMLFile is null");
        }

        if (!this.wordFile.exists() || !this.wordFile.isFile()) {
            throw new Exception("wordFile is not exist");
        }
        
        // load database
        WordReader reader = new WordReader(this.wordFile);
        this.wordPages = reader.parse();

        processPages();
    }

    private String[] splitWords(String paragraph) {
        return StringUtil.splitWithNonAlphabet(paragraph);
    }
    
    private boolean filterWord(String word) {
        if (word.trim().equals("")) {
            return true;
        }
        if (word.matches("^\\d+$")) {
            return true;
        }
        if (word.matches("^[A-Z]\\d+$")) {
            return true;
        }
        if (word.matches("^[A-Z]$")) {
            return true;
        }
        return false;
    }

    private boolean matchDictionary(String word) {
        // case insensitive match
        if (this.wordsTableCaseInsensitive.containsKey(word.toLowerCase())) {
            return true;
        }
        // case sensitive match
        if (this.wordsTableCaseSensitive.containsKey(word)) {
            return true;
        }

        if (this.wordsTableCaseSensitive.contains(word.toUpperCase())) {
            return true;
        }

        if (this.wordsTableCaseSensitive.contains(word.toLowerCase())) {
            return true;
        }

        if (word.length() > 1) {
            String capWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            if (this.wordsTableCaseSensitive.contains(capWord)) {
                return true;
            }
        }

        return false;
    }
    
    private String getRatio(double x, double y) {
        double r = x / y * 100;
        return (Math.round(r * 100.0) / 100.0) + "%";
    }

    private void processPages() throws Exception {
        this.unknownWordsCount = 0;
        this.unknownBytesCount = 0;
        this.errataCount = 0;
        this.errataBytesCount = 0;
        this.totalWordsCount = 0;
        this.totalBytesCount = 0;
        
        
        this.unknownWords = new ArrayList<WordPagePair>();
        this.errataWords = new ArrayList<WordPagePair>();
        this.txtPara.setText("");
        int firstPageNo = -1;
        for(WordPage page : this.wordPages) {
            if(firstPageNo == -1) {
                firstPageNo = page.getPageNo();
            }
            
            List<WordParagraph> paragraphs = page.getParagraphs();
            for (WordParagraph paragraph : paragraphs) {
                String paragraphString = paragraph.getParagraph().trim();
                if (!paragraphString.equals("")) {
                     this.totalBytesCount += paragraphString.trim().length();
                     String[] words = splitWords(paragraphString);
                     for (String word : words) {
                         if(word.trim().equals("")) {
                             continue;
                         }
                         
                         this.totalWordsCount++;
                         if(!filterWord(word)) {
                             if (!matchDictionary(word.trim())) {
                                 if (this.errataTable.containsKey(word)) {
                                     this.errataCount++;
                                     this.errataBytesCount += word.length();
                                     this.errataWords.add(new WordPagePair(word, page));
                                 } else {
                                     this.unknownWordsCount++;
                                     this.unknownBytesCount += word.length();
                                     this.unknownWords.add(new WordPagePair(word, page));
                                 }
                             }
                         }
                     }
                }
            }
        }
        
        String unknownWordsStr = "unknown : " + this.unknownWordsCount + " words " + " (" + getRatio(this.unknownWordsCount, this.totalWordsCount) + "), "
                + this.unknownBytesCount + " chars " + " (" + getRatio(this.unknownBytesCount, this.totalBytesCount) + ")";
        String errataWordsStr = "errata : " + this.errataCount + " words " + " (" + getRatio(this.errataCount, this.totalWordsCount) + "), "
                + this.errataBytesCount + " chars " + " (" + getRatio(this.errataBytesCount, this.totalBytesCount) + ")";
        String totalWordsStr = "total : " + this.totalWordsCount + " words, "
                + this.totalBytesCount + " chars";
        this.lblTotalWords.setText(totalWordsStr);
        this.lblUnknownWords.setText(unknownWordsStr);
        this.lblErrata.setText(errataWordsStr);
        showUnknownWordsToTable();
        
        if(firstPageNo == -1) {
            this.currentDjvuPage = 0;
        } else {
            this.currentDjvuPage = firstPageNo;
        }
        this.movePage(this.currentDjvuPage);
    }

    private void reloadDicWords() {
        try {
            Connection conn = DBUtil.getConnection();
            this.wordsTableCaseSensitive = DBFacade.getHashTableCaseSensitive(conn);
            this.wordsTableCaseInsensitive = DBFacade.getHashTableCaseInsensitive(conn);
            this.errataTable = DBFacade.getHashTableErrata(conn, this.document.getDocumentID());
            this.wordsInDB = DBFacade.getRegisteredWordCount(conn);
            conn.close();
            this.lblWordsInDB.setText(this.wordsInDB + "words in DB");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void showUnknownWordsToTable() {
        DefaultTableModel model = (DefaultTableModel) this.tblUnknownWords.getModel();
        model.setRowCount(0);
        
        List<UnknownWordsTableEntry> arr = new ArrayList<UnknownWordsTableEntry>();
        if (this.errataWords != null) {
            for(int idx=0;idx<this.errataWords.size();idx++) {
                WordPagePair pair = this.errataWords.get(idx);
                WordPage page = pair.getPage();
                
                UnknownWordsTableEntry entry = new UnknownWordsTableEntry();
                entry.setItemIndex(idx);
                entry.setPageNo(page.getPageNo());
                entry.setWord(pair.getWord());
                entry.setErrata(true);
                arr.add(entry);
            }
        }
        
        if (this.unknownWords != null) {
            for(int idx=0;idx<this.unknownWords.size();idx++) {
                WordPagePair pair = this.unknownWords.get(idx);
                WordPage page = pair.getPage();
                Object[] rowData = new Object[4];
                
                UnknownWordsTableEntry entry = new UnknownWordsTableEntry();
                entry.setItemIndex(idx);
                entry.setPageNo(page.getPageNo());
                entry.setWord(pair.getWord());
                entry.setErrata(false);
                arr.add(entry);
            }
        }
        
        if(this.mode == TableShowMode.MODE_POSITION) {
            Collections.sort(arr, new UnknownWordsTableEntryPosComparator());
        } else if(this.mode == TableShowMode.MODE_ALPHABETIC) {
            Collections.sort(arr, new UnknownWordsTableEntryAlphabeticComparator());
        } else if(this.mode == TableShowMode.MODE_LENGTH) {
            Collections.sort(arr, new UnknownWordsTableEntryLengthComparator());
        }
        
        for(UnknownWordsTableEntry entry : arr) {
            Object[] rowData = new Object[4];
            rowData[0] = entry.getItemIndex();
            rowData[1] = entry.getPageNo();
            rowData[2] = entry.getWord();
            rowData[3] = entry.getErrata();
            
            model.addRow(rowData);
        }
    }

    private void addNewWordsToWordTable(List<String> words) {
        try {
            Connection conn = DBUtil.getConnection();
            for (String word : words) {
                WordTable.insertWord(conn, word.toLowerCase().trim());
            }
            conn.close();
            reloadDicWords();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void addNewWordsToBioTermTable(List<String> words) {
        try {
            Connection conn = DBUtil.getConnection();
            for (String word : words) {
                BioTermTable.insertWord(conn, word.toLowerCase().trim());
            }
            conn.close();
            reloadDicWords();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void addNewWordsToCityTable(List<String> words) {
        try {
            Connection conn = DBUtil.getConnection();
            for (String word : words) {
                CityTable.insertWord(conn, word.trim());
            }
            conn.close();
            reloadDicWords();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void addNewWordsToProperNounTable(List<String> words) {
        try {
            Connection conn = DBUtil.getConnection();
            for (String word : words) {
                ProperNounTable.insertWord(conn, word.trim());
            }
            conn.close();
            reloadDicWords();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
    
    private void fireTableSelected() {
        try {
            int[] selectedRows = this.tblUnknownWords.getSelectedRows();
            if(selectedRows.length > 0) {
                showParagraphs(selectedRows);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void showParagraphs(int[] indices) {
        this.txtPara.setText("");
        DefaultTableModel model = (DefaultTableModel) this.tblUnknownWords.getModel();
        for (int index : indices) {
            int entryIdx = (int) model.getValueAt(index, 0);
            boolean bErrata = (boolean) model.getValueAt(index, 3);
            
            WordPagePair pair;
            if(bErrata) {
                // errata words
                pair = this.errataWords.get(entryIdx);
            } else {
                // unknown words
                pair = this.unknownWords.get(entryIdx);
            }

            String word = pair.getWord();
            WordPage page = pair.getPage();

            StyledDocument doc = this.txtPara.getStyledDocument();
            //Style style = this.txtPara.addStyle("style-" + index, null);
            if (this.gstyle == null) {
                this.gstyle = this.txtPara.addStyle("global_style", null);
            }

            StyleConstants.setForeground(this.gstyle, Color.red);
            StyleConstants.setBold(this.gstyle, true);
            try {
                doc.insertString(doc.getLength(), word, this.gstyle);
            } catch (BadLocationException e) {
            }
            
            StyleConstants.setForeground(this.gstyle, Color.black);
            StyleConstants.setBold(this.gstyle, false);
            try {
                doc.insertString(doc.getLength(), " : \n\n", this.gstyle);
            } catch (BadLocationException e) {
            }
            
            String stringData = "";
            for(WordParagraph paragraph : page.getParagraphs()) {
                stringData += paragraph.getParagraph() + "\n\n";
            }
            
            stringData = stringData.trim();
            
            String regex = "\\b" + word + "\\b";
            Pattern p = Pattern.compile(regex);
            Matcher matcher = p.matcher(stringData);
            
            int lastPos = 0;
            while(matcher.find()) {
                int idx = matcher.start();
                
                StyleConstants.setForeground(this.gstyle, Color.black);
                try {
                    doc.insertString(doc.getLength(), stringData.substring(lastPos, idx), this.gstyle);
                } catch (BadLocationException e) {
                }
                
                StyleConstants.setForeground(this.gstyle, Color.red);
                StyleConstants.setBackground(this.gstyle, Color.yellow);
                try {
                    doc.insertString(doc.getLength(), stringData.substring(idx, idx + word.length()), this.gstyle);
                } catch (BadLocationException e) {
                }
                
                StyleConstants.setBackground(this.gstyle, Color.white);
                
                lastPos = idx + word.length();
            }

            if(lastPos < stringData.length()) {
                StyleConstants.setForeground(this.gstyle, Color.black);
                try {
                    doc.insertString(doc.getLength(), stringData.substring(lastPos), this.gstyle);
                } catch (BadLocationException e) {
                }
            }
            
            StyleConstants.setForeground(this.gstyle, Color.black);
            try {
                doc.insertString(doc.getLength(), "\n\n", this.gstyle);
            } catch (BadLocationException e) {
            }
            
            this.txtPara.setCaretPosition(0);
            movePage(page.getPageNo());
            break; // temp
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblDjvu = new javax.swing.JLabel();
        lblWord = new javax.swing.JLabel();
        btnStart = new javax.swing.JButton();
        lblPage = new javax.swing.JLabel();
        lblWordsInDB = new javax.swing.JLabel();
        btnImportWords = new javax.swing.JButton();
        btnRegNewWordToWordTable = new javax.swing.JButton();
        lblTotalWords = new javax.swing.JLabel();
        btnRegNewWordToCityTable = new javax.swing.JButton();
        btnRegNewWordToBioTermTable = new javax.swing.JButton();
        btnRegNewWordToProperNounTable = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtPara = new javax.swing.JTextPane();
        btnDisplay50 = new javax.swing.JButton();
        btnDisplay66 = new javax.swing.JButton();
        btnAddNewErrata = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUnknownWords = new javax.swing.JTable();
        lblUnknownWords = new javax.swing.JLabel();
        lblErrata = new javax.swing.JLabel();
        btnExportUnknownWords = new javax.swing.JButton();
        btnExportErrata = new javax.swing.JButton();
        btnAlphabeticOrder = new javax.swing.JButton();
        btnPositionOrder = new javax.swing.JButton();
        btnWordLenOrder = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblDjvu.setText("Not Loaded");

        lblWord.setText("Not Loaded");
        lblWord.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblWordMouseClicked(evt);
            }
        });

        btnStart.setText("Start!");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        lblPage.setText("Page");

        lblWordsInDB.setText("Words in DB");

        btnImportWords.setText("Import Words");
        btnImportWords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportWordsActionPerformed(evt);
            }
        });

        btnRegNewWordToWordTable.setText("Add General Words");
        btnRegNewWordToWordTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegNewWordToWordTableActionPerformed(evt);
            }
        });

        lblTotalWords.setText("0");

        btnRegNewWordToCityTable.setText("Add City/Region Name");
        btnRegNewWordToCityTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegNewWordToCityTableActionPerformed(evt);
            }
        });

        btnRegNewWordToBioTermTable.setText("Add BioTerm");
        btnRegNewWordToBioTermTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegNewWordToBioTermTableActionPerformed(evt);
            }
        });

        btnRegNewWordToProperNounTable.setText("Add ProperNoun");
        btnRegNewWordToProperNounTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegNewWordToProperNounTableActionPerformed(evt);
            }
        });

        jScrollPane3.setViewportView(txtPara);

        btnDisplay50.setText("1:1");
        btnDisplay50.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisplay50ActionPerformed(evt);
            }
        });

        btnDisplay66.setText("1:2");
        btnDisplay66.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisplay66ActionPerformed(evt);
            }
        });

        btnAddNewErrata.setText("Add Errata");
        btnAddNewErrata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddNewErrataActionPerformed(evt);
            }
        });

        tblUnknownWords.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "idx", "page", "word", "errata"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUnknownWords.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblUnknownWords.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tblUnknownWords);

        lblUnknownWords.setText("0");

        lblErrata.setText("0");

        btnExportUnknownWords.setText("Export Words Not In Dic");
        btnExportUnknownWords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportUnknownWordsActionPerformed(evt);
            }
        });

        btnExportErrata.setText("Export Errata");
        btnExportErrata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportErrataActionPerformed(evt);
            }
        });

        btnAlphabeticOrder.setText("Alphabetic");
        btnAlphabeticOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlphabeticOrderActionPerformed(evt);
            }
        });

        btnPositionOrder.setText("Positional");
        btnPositionOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPositionOrderActionPerformed(evt);
            }
        });

        btnWordLenOrder.setText("Word Len");
        btnWordLenOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWordLenOrderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblPage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnImportWords)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExportErrata)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExportUnknownWords))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblWord)
                            .addComponent(lblDjvu)
                            .addComponent(lblWordsInDB)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnPositionOrder)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAlphabeticOrder)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnWordLenOrder)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblTotalWords)
                                            .addComponent(lblUnknownWords))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 164, Short.MAX_VALUE)
                                        .addComponent(btnDisplay50)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnDisplay66))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblErrata)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnRegNewWordToWordTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnRegNewWordToCityTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnRegNewWordToBioTermTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnRegNewWordToProperNounTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnAddNewErrata, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane3)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblDjvu)
                            .addComponent(lblTotalWords))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblWord)
                            .addComponent(lblUnknownWords))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblWordsInDB)
                            .addComponent(lblErrata)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnDisplay66)
                        .addComponent(btnDisplay50)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRegNewWordToWordTable)
                            .addComponent(btnAlphabeticOrder)
                            .addComponent(btnPositionOrder)
                            .addComponent(btnWordLenOrder))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnRegNewWordToCityTable)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRegNewWordToBioTermTable)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRegNewWordToProperNounTable)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
                                .addComponent(btnAddNewErrata))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addComponent(jScrollPane3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPage)
                    .addComponent(btnImportWords)
                    .addComponent(btnExportUnknownWords)
                    .addComponent(btnExportErrata))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblWordMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblWordMouseClicked
        if (this.wordFile != null && this.wordFile.exists() && this.wordFile.isFile()) {
            try {
                Runtime.getRuntime().exec(new String[]{"gedit", this.wordFile.getAbsolutePath()});
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }//GEN-LAST:event_lblWordMouseClicked

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        try {
            startParsing();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnImportWordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportWordsActionPerformed
        DictionaryImportPopup popup = new DictionaryImportPopup(this, true);
        popup.setVisible(true);
        reloadDicWords();
    }//GEN-LAST:event_btnImportWordsActionPerformed

    private void btnRegNewWordToWordTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegNewWordToWordTableActionPerformed
        try {
            int[] selectedRows = this.tblUnknownWords.getSelectedRows();
            if(selectedRows.length > 0) {
                List<String> selectedWords = new ArrayList<String>();
                for(int row : selectedRows) {
                    String word = this.tblUnknownWords.getModel().getValueAt(row, 2).toString();
                    selectedWords.add(word);
                }
                addNewWordsToWordTable(selectedWords);
                processPages();
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_btnRegNewWordToWordTableActionPerformed

    private void btnRegNewWordToCityTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegNewWordToCityTableActionPerformed
        try {
            int[] selectedRows = this.tblUnknownWords.getSelectedRows();
            if(selectedRows.length > 0) {
                List<String> selectedWords = new ArrayList<String>();
                for(int row : selectedRows) {
                    String word = this.tblUnknownWords.getModel().getValueAt(row, 2).toString();
                    selectedWords.add(word);
                }
                addNewWordsToCityTable(selectedWords);
                processPages();
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_btnRegNewWordToCityTableActionPerformed

    private void btnRegNewWordToBioTermTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegNewWordToBioTermTableActionPerformed
        try {
            int[] selectedRows = this.tblUnknownWords.getSelectedRows();
            if(selectedRows.length > 0) {
                List<String> selectedWords = new ArrayList<String>();
                for(int row : selectedRows) {
                    String word = this.tblUnknownWords.getModel().getValueAt(row, 2).toString();
                    selectedWords.add(word);
                }
                addNewWordsToBioTermTable(selectedWords);
                processPages();
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_btnRegNewWordToBioTermTableActionPerformed

    private void btnRegNewWordToProperNounTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegNewWordToProperNounTableActionPerformed
        try {
            int[] selectedRows = this.tblUnknownWords.getSelectedRows();
            if(selectedRows.length > 0) {
                List<String> selectedWords = new ArrayList<String>();
                for(int row : selectedRows) {
                    String word = this.tblUnknownWords.getModel().getValueAt(row, 2).toString();
                    selectedWords.add(word);
                }
                addNewWordsToProperNounTable(selectedWords);
                processPages();
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_btnRegNewWordToProperNounTableActionPerformed

    private void btnDisplay50ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisplay50ActionPerformed
        resizeDjvuView(1, 1);
    }//GEN-LAST:event_btnDisplay50ActionPerformed

    private void btnDisplay66ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisplay66ActionPerformed
        resizeDjvuView(1, 2);
    }//GEN-LAST:event_btnDisplay66ActionPerformed

    private void btnAddNewErrataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddNewErrataActionPerformed
        try {
            int[] selectedRows = this.tblUnknownWords.getSelectedRows();
            if(selectedRows.length > 0) {
                for(int row : selectedRows) {
                    String word = this.tblUnknownWords.getModel().getValueAt(row, 2).toString();
                    ErrataAdditionPopup popup = new ErrataAdditionPopup(this.document, this, true);
                    popup.setErrata(word);
                    popup.setVisible(true);
                }
            }
            reloadDicWords();

            processPages();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_btnAddNewErrataActionPerformed

    private void btnExportUnknownWordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportUnknownWordsActionPerformed
        try {
            exportUnknownWords();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_btnExportUnknownWordsActionPerformed

    private void btnExportErrataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportErrataActionPerformed
        try {
            exportErrata();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_btnExportErrataActionPerformed

    private void btnPositionOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPositionOrderActionPerformed
        this.mode = TableShowMode.MODE_POSITION;
        showUnknownWordsToTable();
    }//GEN-LAST:event_btnPositionOrderActionPerformed

    private void btnAlphabeticOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlphabeticOrderActionPerformed
        this.mode = TableShowMode.MODE_ALPHABETIC;
        showUnknownWordsToTable();
    }//GEN-LAST:event_btnAlphabeticOrderActionPerformed

    private void btnWordLenOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWordLenOrderActionPerformed
        this.mode = TableShowMode.MODE_LENGTH;
        showUnknownWordsToTable();
    }//GEN-LAST:event_btnWordLenOrderActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(WordCheckerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(WordCheckerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(WordCheckerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WordCheckerGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new WordCheckerGui().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddNewErrata;
    private javax.swing.JButton btnAlphabeticOrder;
    private javax.swing.JButton btnDisplay50;
    private javax.swing.JButton btnDisplay66;
    private javax.swing.JButton btnExportErrata;
    private javax.swing.JButton btnExportUnknownWords;
    private javax.swing.JButton btnImportWords;
    private javax.swing.JButton btnPositionOrder;
    private javax.swing.JButton btnRegNewWordToBioTermTable;
    private javax.swing.JButton btnRegNewWordToCityTable;
    private javax.swing.JButton btnRegNewWordToProperNounTable;
    private javax.swing.JButton btnRegNewWordToWordTable;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnWordLenOrder;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblDjvu;
    private javax.swing.JLabel lblErrata;
    private javax.swing.JLabel lblPage;
    private javax.swing.JLabel lblTotalWords;
    private javax.swing.JLabel lblUnknownWords;
    private javax.swing.JLabel lblWord;
    private javax.swing.JLabel lblWordsInDB;
    private javax.swing.JTable tblUnknownWords;
    private javax.swing.JTextPane txtPara;
    // End of variables declaration//GEN-END:variables

    private void exportUnknownWords() throws Exception {
        final JFileChooser fc = new JFileChooser();
        if (this.workingParentDir != null) {
            fc.setCurrentDirectory(this.workingParentDir);
        }

        //In response to a button click:
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileWriter writer = null;
            try {
                File file = fc.getSelectedFile();
                writer = new FileWriter(file, false);

                writer.write("Page\tWord\n");
                for(WordPagePair pair : this.unknownWords) {
                    writer.write(pair.getPage().getPageNo() + "\t" + pair.getWord() + "\n");
                }
                
                writer.close();
            } catch (IOException ex) {
                throw new Exception(ex.getMessage());
            } finally {
                try {
                    writer.close();
                } catch (IOException ex) {
                    throw new Exception(ex.getMessage());
                }
            }
        }
    }

    private void exportErrata() throws Exception {
        final JFileChooser fc = new JFileChooser();
        if (this.workingParentDir != null) {
            fc.setCurrentDirectory(this.workingParentDir);
        }

        //In response to a button click:
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileWriter writer = null;
            try {
                File file = fc.getSelectedFile();
                writer = new FileWriter(file, false);

                writer.write("Page\tWord\tFixTo\n");
                for(WordPagePair pair : this.errataWords) {
                    String fixto = (String)this.errataTable.get(pair.getWord());
                    writer.write(pair.getPage().getPageNo() + "\t" + pair.getWord() + "\t" + fixto + "\n");
                }
                
                writer.close();
            } catch (IOException ex) {
                throw new Exception(ex.getMessage());
            } finally {
                try {
                    writer.close();
                } catch (IOException ex) {
                    throw new Exception(ex.getMessage());
                }
            }
        }
    }
}
