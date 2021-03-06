/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wordchecker;

import common.db.DBUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import worddb.Document;
import worddb.Errata;
import worddb.ErrataTable;

/**
 *
 * @author iychoi
 */
public class ErrataAdditionPopup extends javax.swing.JDialog {

    private Document document;
    private List<Errata> errataList;
    
    private String errata;
    
    /**
     * Creates new form ErrataAdditionPopup
     */
    public ErrataAdditionPopup(Document document, java.awt.Frame parent, boolean modal) throws IOException {
        super(parent, modal);
        
        this.document = document;
        
        initComponents();
        
        this.errataList = loadErrataList(this.document);
        showErrataTable();
        
        if(this.errata != null) {
            this.txtErrata.setText(this.errata);
        }
    }
    
    private List<Errata> loadErrataList(Document document) throws IOException {
        Connection conn = DBUtil.getConnection();
        List<Errata> errataList = ErrataTable.getErrataList(conn, document.getDocumentID());

        try {
            conn.close();
        } catch (SQLException ex) {
            throw new IOException(ex.getMessage());
        }
        return errataList;
    }
    
    private void showErrataTable() {
        DefaultTableModel model = (DefaultTableModel) this.tblErrata.getModel();

        model.setRowCount(0);
        for (Errata errata : this.errataList) {
            Object[] rowData = new Object[2];
            rowData[0] = errata.getErrata();
            rowData[1] = errata.getFixed();

            model.addRow(rowData);
        }
    }
    
    private void addNewErrata(int documentID, String errata, String fixed) throws IOException {
        Connection conn = DBUtil.getConnection();

        Errata errata_new = new Errata();
        errata_new.setDocumentID(documentID);
        errata_new.setErrata(errata);
        errata_new.setFixed(fixed);
        
        ErrataTable.insertErrata(conn, errata_new);
        
        try {
            conn.close();
        } catch (SQLException ex) {
            throw new IOException(ex.getMessage());
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

        jScrollPane3 = new javax.swing.JScrollPane();
        tblErrata = new javax.swing.JTable();
        lblErrata = new javax.swing.JLabel();
        lblFixTo = new javax.swing.JLabel();
        txtFixTo = new javax.swing.JTextField();
        txtErrata = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblErrata.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "errata", "fixto"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblErrata.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(tblErrata);

        lblErrata.setText("Errata");

        lblFixTo.setText("Fix to");

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblErrata)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtErrata, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblFixTo)
                        .addGap(18, 18, 18)
                        .addComponent(txtFixTo))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnSave)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblErrata)
                    .addComponent(txtErrata, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFixTo)
                    .addComponent(txtFixTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSave)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            String errata = this.txtErrata.getText().trim();
            String fixed = this.txtFixTo.getText().trim();

            addNewErrata(this.document.getDocumentID(), errata, fixed);

            this.dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_btnSaveActionPerformed

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
            java.util.logging.Logger.getLogger(ErrataAdditionPopup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ErrataAdditionPopup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ErrataAdditionPopup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ErrataAdditionPopup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        /*
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ErrataAdditionPopup dialog = new ErrataAdditionPopup(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
        */
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSave;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblErrata;
    private javax.swing.JLabel lblFixTo;
    private javax.swing.JTable tblErrata;
    private javax.swing.JTextField txtErrata;
    private javax.swing.JTextField txtFixTo;
    // End of variables declaration//GEN-END:variables

    public void setErrata(String word) {
        this.errata = word;
        if(this.txtErrata != null) {
            this.txtErrata.setText(word);
        }
    }
}
