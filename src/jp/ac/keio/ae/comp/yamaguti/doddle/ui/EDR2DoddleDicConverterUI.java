/*
 * Project Name: DODDLE (a Domain Ontology rapiD DeveLopment Environment)
 * Project Website: http://doddle-owl.sourceforge.net/
 * 
 * Copyright (C) 2004-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
 * 
 * This file is part of DODDLE-OWL.
 * 
 * DODDLE-OWL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DODDLE-OWL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DODDLE-OWL.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.EDR2DoddleDicConverter.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class EDR2DoddleDicConverterUI extends JDialog implements ActionListener {

    private JRadioButton edrRadioButton;
    private JRadioButton edrtRadioButton;
    private JRadioButton txtBox;
    // private JCheckBox dbBox;
    private JRadioButton owlBox;
    private JTextField edrPathField;
    private JButton refEDRPathButton;
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
        radioButtonPanel.setBorder(BorderFactory.createTitledBorder("Dictionary Type"));
        radioButtonPanel.add(edrRadioButton);
        radioButtonPanel.add(edrtRadioButton);

        txtBox = new JRadioButton("Text", true);
        // dbBox = new JCheckBox("Berkely DB", false);
        owlBox = new JRadioButton("OWL", false);
        group = new ButtonGroup();
        group.add(txtBox);
        group.add(owlBox);
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new GridLayout(1, 2));
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder("Conversion Type"));
        checkBoxPanel.add(txtBox);
        // checkBoxPanel.add(dbBox);
        checkBoxPanel.add(owlBox);

        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new GridLayout(1, 2));
        optionPanel.add(radioButtonPanel);
        optionPanel.add(checkBoxPanel);

        edrPathField = new JTextField(40);
        edrPathField.setEditable(false);
        refEDRPathButton = new JButton("Browse");
        refEDRPathButton.addActionListener(this);
        JPanel edrPathPanel = new JPanel();
        edrPathPanel.setLayout(new BorderLayout());
        edrPathPanel.add(edrPathField, BorderLayout.CENTER);
        edrPathPanel.add(refEDRPathButton, BorderLayout.EAST);
        edrPathPanel.setBorder(BorderFactory.createTitledBorder("Input: EDR(T)_ORG_HOME"));

        doddleDicPathField = new JTextField(40);
        doddleDicPathField.setEditable(false);
        refDoddleDicPathButton = new JButton("Browse");
        refDoddleDicPathButton.addActionListener(this);
        JPanel doddleDicPanel = new JPanel();
        doddleDicPanel.setLayout(new BorderLayout());
        doddleDicPanel.add(doddleDicPathField, BorderLayout.CENTER);
        doddleDicPanel.add(refDoddleDicPathButton, BorderLayout.EAST);
        doddleDicPanel.setBorder(BorderFactory.createTitledBorder("Output: EDR(T)_HOME"));

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

        progressLabel.setBorder(BorderFactory.createTitledBorder("Message"));

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
        setTitle("EDR2DODDLEDic Converter");
        setLocationRelativeTo(null);
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
        int fd = jfc.showOpenDialog(this);
        if (fd == JFileChooser.APPROVE_OPTION) {
            textField.setText(jfc.getSelectedFile().toString() + File.separator);
            System.out.println(textField.getText());
        }
    }

    private DictionaryType getDicType() {
        if (edrRadioButton.isSelected()) {
            return DictionaryType.EDR;
        } else if (edrtRadioButton.isSelected()) { return DictionaryType.EDRT; }
        return DictionaryType.EDR;
    }

    private void convertEDR2DoddleDic() {
        new Thread() {

            private void makeRelationData(DictionaryType dicType) {
                if (dicType == DictionaryType.EDR) {
                    progressLabel.setText("Make Relation Data");
                    System.out.println("Make Relation Data");
                    EDR2DoddleDicConverter.readRelationData();
                    EDR2DoddleDicConverter.writeRelationData();
                    EDR2DoddleDicConverter.clearRelationMaps();
                    addProgressValue();
                }
            }

            private void makeRelationIndex(DictionaryType dicType) {
                if (dicType == DictionaryType.EDR) {
                    progressLabel.setText("Make Relation Index");
                    System.out.println("Make Relation Index");
                    EDR2DoddleDicConverter.writeRelationIndex();
                    addProgressValue();
                }
            }

            private void makeTreeData(DictionaryType dicType) {
                progressLabel.setText("Make ID SubIDSet Map");
                if (dicType == DictionaryType.EDR) {
                    EDR2DoddleDicConverter.readTreeData(ConceptTreeMaker.EDR_CLASS_ROOT_ID);
                } else {
                    EDR2DoddleDicConverter.readTreeData(ConceptTreeMaker.EDRT_CLASS_ROOT_ID);
                }
                EDR2DoddleDicConverter.writeTreeData();
                EDR2DoddleDicConverter.clearTreeData();
                addProgressValue();
            }

            private void makeTreeIndex() {
                progressLabel.setText("Make Tree Index");
                EDR2DoddleDicConverter.writeTreeIndex();
                addProgressValue();
            }

            private void makeConceptData() {
                progressLabel.setText("Make Concept Data");
                EDR2DoddleDicConverter.readConceptData();
                EDR2DoddleDicConverter.writeConceptData();
                addProgressValue();
            }

            private void makeConceptIndex() {
                progressLabel.setText("Make Concept Index");
                EDR2DoddleDicConverter.readConceptIndex();
                EDR2DoddleDicConverter.writeConceptIndex();
                EDR2DoddleDicConverter.clearDataFilePointerList();
                addProgressValue();
            }

            private void makeWordData() {
                progressLabel.setText("Make Word Data");
                EDR2DoddleDicConverter.readWordData();
                EDR2DoddleDicConverter.writeWordData();
                clearMap();
                addProgressValue();
            }

            private void makeWordIndex() {
                progressLabel.setText("Make Word Index");
                EDR2DoddleDicConverter.writeWordIndex();
                addProgressValue();
            }

            private void clearMap() {
                progressLabel.setText("Clear Maps");
                EDR2DoddleDicConverter.clearIDDefinitionMap();
                EDR2DoddleDicConverter.clearWordIDSetMap();
                EDR2DoddleDicConverter.clearIDFilePointerMap();
                EDR2DoddleDicConverter.clearWordFilePointerSetMap();
            }

            private void makeTextDataAndIndex(DictionaryType dicType) {
                makeRelationData(dicType);
                makeRelationIndex(dicType);
                makeTreeData(dicType);
                makeTreeIndex();
                makeConceptData();
                makeConceptIndex();
                makeWordData();
                makeWordIndex();
            }

            public void convertEDR2OWL(DictionaryType dicType) {
                String ns = "";
                if (dicType == DictionaryType.EDR) {
                    ns = DODDLEConstants.EDR_URI;
                } else if (dicType == DictionaryType.EDRT) {
                    ns = DODDLEConstants.EDRT_URI;
                }
                Model jaOntModel = ModelFactory.createDefaultModel();
                Model enOntModel = ModelFactory.createDefaultModel();
                EDR2DoddleDicConverter.writeOWLConceptData(jaOntModel, enOntModel, ns);
                EDR2DoddleDicConverter.saveOntology(jaOntModel, dicType + "_ja.owl");
                EDR2DoddleDicConverter.saveOntology(enOntModel, dicType + "_en.owl");
                Model treeOntModel = ModelFactory.createDefaultModel();
                EDR2DoddleDicConverter.writeOWLTreeData(treeOntModel, ns);
                EDR2DoddleDicConverter.saveOntology(treeOntModel, dicType + "_tree.owl");

                if (dicType == DictionaryType.EDR) {
                    Model regionOntModel = ModelFactory.createDefaultModel();
                    EDR2DoddleDicConverter.writeOWLRegionData(regionOntModel, ns);
                    EDR2DoddleDicConverter.saveOntology(regionOntModel, dicType + "_region.owl");
                }
            }

            private void setTXTProgressValue(DictionaryType dicType) {
                progressBar.setValue(0);
                progressLabel.setText("");
                int value = 0;
                if (dicType == DictionaryType.EDR) {
                    value = 11;
                } else if (dicType == DictionaryType.EDRT) {
                    value = 9;
                }
                progressBar.setMaximum(value);
            }

            private void setOWLProgressValue(DictionaryType dicType) {
                progressBar.setValue(0);
                progressLabel.setText("");
                int value = 0;
                if (dicType == DictionaryType.EDR) {
                    value = 9;
                } else if (dicType == DictionaryType.EDRT) {
                    value = 7;
                }
                progressBar.setMaximum(value);
            }

            public void run() {
                DictionaryType dicType = getDicType();
                boolean isEnable = EDR2DoddleDicConverter.setEDRDicPath(edrPathField.getText(), dicType);
                if (!isEnable) {
                    initProgressBar("Error: FileNotFound");
                    return;
                }
                EDR2DoddleDicConverter.setDODDLEDicPath(doddleDicPathField.getText());
                try {
                    if (owlBox.isSelected()) {
                        setOWLProgressValue(dicType);
                        convertEDR2OWL(dicType);
                    } else if (txtBox.isSelected()) {
                        setTXTProgressValue(dicType);
                        makeTextDataAndIndex(dicType);
                    }
                    progressLabel.setText("Done");
                } catch (Exception e) {
                    e.printStackTrace();
                    initProgressBar("Error");
                }
            }
        }.start();
    }

    public static void initProgressBar(String msg) {
        progressLabel.setText(msg);
        progressBar.setValue(0);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exitButton) {
            System.exit(0);
        } else if (e.getSource() == refEDRPathButton) {
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
