/*
 * @(#)  2006/08/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class EDR2DoddleDicConverterUI extends JDialog implements ActionListener {

    private JRadioButton edrRadioButton;
    private JRadioButton edrtRadioButton;
    private JCheckBox txtBox;
    private JCheckBox dbBox;
    private JTextField edrPathField;
    private JButton refEdrPathButton;
    private JTextField doddleDicPathField;
    private JButton refDoddleDicPathButton;
    private JButton convertButton;
    private JButton exitButton;
    private static JLabel progressLabel = new JLabel();
    private static JProgressBar progressBar = new JProgressBar();

    public EDR2DoddleDicConverterUI() {
        edrRadioButton = new JRadioButton("EDR");
        edrRadioButton.setSelected(true);
        edrtRadioButton = new JRadioButton("EDRT");
        ButtonGroup group = new ButtonGroup();
        group.add(edrRadioButton);
        group.add(edrtRadioButton);
        JPanel radioButtonPanel = new JPanel();
        radioButtonPanel.setLayout(new GridLayout(1, 2));
        radioButtonPanel.setBorder(BorderFactory.createTitledBorder("Dic Type"));
        radioButtonPanel.add(edrRadioButton);
        radioButtonPanel.add(edrtRadioButton);

        txtBox = new JCheckBox("TXT", true);
        dbBox = new JCheckBox("DB", true);
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new GridLayout(1, 2));
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder("Convertion Type"));
        checkBoxPanel.add(txtBox);
        checkBoxPanel.add(dbBox);

        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new GridLayout(1, 2));
        optionPanel.add(radioButtonPanel);
        optionPanel.add(checkBoxPanel);

        edrPathField = new JTextField(40);
        edrPathField.setEditable(false);
        refEdrPathButton = new JButton("Browse");
        refEdrPathButton.addActionListener(this);
        JPanel edrPathPanel = new JPanel();
        edrPathPanel.setLayout(new BorderLayout());
        edrPathPanel.add(edrPathField, BorderLayout.CENTER);
        edrPathPanel.add(refEdrPathButton, BorderLayout.EAST);
        edrPathPanel.setBorder(BorderFactory.createTitledBorder("EDR DIC Path"));

        doddleDicPathField = new JTextField(40);
        doddleDicPathField.setEditable(false);
        refDoddleDicPathButton = new JButton("Browse");
        refDoddleDicPathButton.addActionListener(this);
        JPanel doddleDicPanel = new JPanel();
        doddleDicPanel.setLayout(new BorderLayout());
        doddleDicPanel.add(doddleDicPathField, BorderLayout.CENTER);
        doddleDicPanel.add(refDoddleDicPathButton, BorderLayout.EAST);
        doddleDicPanel.setBorder(BorderFactory.createTitledBorder("DODDLE DIC Path"));

        convertButton = new JButton("Convert");
        convertButton.addActionListener(this);
        JPanel progressBarPanel = new JPanel();
        progressBarPanel.setLayout(new BorderLayout());
        progressBarPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
        progressBarPanel.add(progressBar, BorderLayout.CENTER);
        progressBarPanel.add(convertButton, BorderLayout.EAST);

        exitButton = new JButton("Exit");
        exitButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(exitButton, BorderLayout.EAST);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(5, 1));
        mainPanel.add(optionPanel);
        mainPanel.add(edrPathPanel);
        mainPanel.add(doddleDicPanel);
        mainPanel.add(progressBarPanel);
        mainPanel.add(progressLabel);

        Container contentPane = getContentPane();
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        pack();
        setTitle("EDR2DODDLE_Dic");
    }

    public static void addProgressValue() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    public static void setProgressText(String text) {
        progressLabel.setText(text);
    }

    private void setDicPath(JTextField textField) {
        File currentDirectory = new File(textField.getText());
        JFileChooser jfc = new JFileChooser(currentDirectory);
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setDialogTitle("Select Directory");
        // int fd =
        // jfc.showOpenDialog(DODDLE.getCurrentProject().getRootPane());
        int fd = jfc.showOpenDialog(this);
        if (fd == JFileChooser.APPROVE_OPTION) {
            textField.setText(jfc.getSelectedFile().toString() + File.separator);
            System.out.println(textField.getText());
        }
    }

    private String getDicType() {
        if (edrRadioButton.isSelected()) {
            return "EDR";
        } else if (edrtRadioButton.isSelected()) { return "EDRT"; }
        return "EDR";
    }

    private void convertEDR2DoddleDic() {
        new Thread() {
            private void convertEDR2DoddleTextDic(String dicType) {
                EDR2DoddleDicConverter.setEDRDicPath(edrPathField.getText(), dicType);
                EDR2DoddleDicConverter.setDODDLEDicPath(doddleDicPathField.getText());
                progressLabel.setText("make idDefinitionMap");
                EDR2DoddleDicConverter.makeIDDefinitionMap();
                progressLabel.setText("make wordIDSetMap");
                EDR2DoddleDicConverter.makeWordIDSetMap();
                addProgressValue();
                progressLabel.setText("clear Map");
                EDR2DoddleDicConverter.clearIDDefinitionMap();
                EDR2DoddleDicConverter.clearWordIDSetMap();
                addProgressValue();
                progressLabel.setText("make idSubIDSetMap");
                EDR2DoddleDicConverter.makeIDSubIDSetMap();
                addProgressValue();

                if (dicType.equals("EDR")) {
                    progressLabel.setText("clear Map");
                    EDR2DoddleDicConverter.clearIDSubIDSetMap();
                    addProgressValue();
                    progressLabel.setText("make conceptDefinitionMap");
                    EDR2DoddleDicConverter.makeConceptDefinitionMap();
                    addProgressValue();
                }
            }

            private void convertEDR2DoddleDBDic(String dicType) {
                DBManager edrDBManager = null;
                DBManager edrtDBManager = null;
                try {
                    if (dicType.equals("EDR")) {
                        edrDBManager = new DBManager(false, doddleDicPathField.getText());
                        edrDBManager.makeDB("edr", doddleDicPathField.getText(), false);
                    } else {
                        edrtDBManager = new DBManager(false, doddleDicPathField.getText());
                        edrtDBManager.makeDB("edrt", doddleDicPathField.getText(), true);
                    }
                } catch (Exception e) {
                    // If an exception reaches this point, the last transaction
                    // did not
                    // complete. If the exception is RunRecoveryException,
                    // follow
                    // the Berkeley DB recovery procedures before running again.
                    e.printStackTrace();
                } finally {
                    if (edrDBManager != null) {
                        try {
                            // Always attempt to close the database cleanly.
                            edrDBManager.close();
                            System.out.println("Close DB");
                        } catch (Exception e) {
                            System.err.println("Exception during database close:");
                            e.printStackTrace();
                        }
                    }
                    if (edrtDBManager != null) {
                        try {
                            // Always attempt to close the database cleanly.
                            edrtDBManager.close();
                            System.out.println("Close DB");
                        } catch (Exception e) {
                            System.err.println("Exception during database close:");
                            e.printStackTrace();
                        }
                    }
                }
            }

            public void run() {
                String dicType = getDicType();
                progressBar.setValue(0);
                progressLabel.setText("");
                int value = 0;
                if (dbBox.isSelected()) {
                    value += 3;
                }
                if (dicType.equals("EDR")) {                    
                    if (txtBox.isSelected()) {
                        value += 9;
                    }
                    progressBar.setMaximum(value);
                } else {
                    if (txtBox.isSelected()) {
                        value += 7;
                    }
                    progressBar.setMaximum(value);
                }
                try {
                    if (txtBox.isSelected()) {
                        convertEDR2DoddleTextDic(dicType);
                    }
                    if (dbBox.isSelected()) {
                        convertEDR2DoddleDBDic(dicType);
                    }
                    progressLabel.setText("Done");
                } catch (Exception e) {
                    e.printStackTrace();
                    progressBar.setValue(0);
                    progressLabel.setText("Error");
                }
            }
        }.start();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exitButton) {
            System.exit(0);
        } else if (e.getSource() == refEdrPathButton) {
            setDicPath(edrPathField);
        } else if (e.getSource() == refDoddleDicPathButton) {
            setDicPath(doddleDicPathField);
        } else if (e.getSource() == convertButton) {
            convertEDR2DoddleDic();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            EDR2DoddleDicConverterUI converter = new EDR2DoddleDicConverterUI();
            converter.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
