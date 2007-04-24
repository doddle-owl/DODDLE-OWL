/*
 * @(#)  2007/03/17
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class LabelPanel extends LiteralPanel implements ActionListener {

    private JLabel typicalWordLabel;
    private JLabel typicalWordValueLabel;
    private JTextField langField;
    private JTextField labelField;
    private JButton addLabelButton;
    private JButton deleteLabelButton;
    private JButton editLabelButton;
    private JButton setTypcialLabelButton;

    private ConceptInformationPanel conceptInfoPanel;

    public LabelPanel(String type, ConceptInformationPanel ciPanel) {
        super(Translator.getTerm("LanguageLabel"), Translator.getTerm("LabelList"), type);
        conceptInfoPanel = ciPanel;

        typicalWordLabel = new JLabel(Translator.getTerm("DisplayWordLabel") + ": ");
        typicalWordValueLabel = new JLabel("");
        JPanel typicalWordPanel = new JPanel();
        typicalWordPanel.setLayout(new GridLayout(1, 2));
        typicalWordPanel.add(typicalWordLabel);
        typicalWordPanel.add(typicalWordValueLabel);

        langField = new JTextField(5);
        langField.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("LangTextField")));
        labelField = new JTextField(15);
        labelField.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("LabelTextField")));

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BorderLayout());
        fieldPanel.add(langField, BorderLayout.WEST);
        fieldPanel.add(labelField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));
        addLabelButton = new JButton(Translator.getTerm("AddButton"));
        addLabelButton.addActionListener(this);
        deleteLabelButton = new JButton(Translator.getTerm("RemoveButton"));
        deleteLabelButton.addActionListener(this);
        editLabelButton = new JButton(Translator.getTerm("EditButton"));
        editLabelButton.addActionListener(this);
        setTypcialLabelButton = new JButton(Translator.getTerm("SetDisplayWordButton"));
        setTypcialLabelButton.addActionListener(this);
        buttonPanel.add(setTypcialLabelButton);
        buttonPanel.add(addLabelButton);
        buttonPanel.add(editLabelButton);
        buttonPanel.add(deleteLabelButton);

        JPanel editPanel = new JPanel();
        editPanel.setLayout(new BorderLayout());
        editPanel.add(fieldPanel, BorderLayout.NORTH);
        editPanel.add(buttonPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createTitledBorder(Translator.getTerm("LabelBorder")));
        add(typicalWordPanel, BorderLayout.NORTH);
        add(editPanel, BorderLayout.SOUTH);
    }

    public void setTypicalWord(String word) {
        typicalWordValueLabel.setText(word);
    }

    public void clearTypicalWordValue() {
        typicalWordValueLabel.setText("");
    }

    public void clearLabelField() {
        langField.setText("");
        labelField.setText("");
    }

    public void setLabelList() {
        super.setLabelList();
        if (langJList.getSelectedValues().length == 0) {
            langField.setText("");
        } else if (langJList.getSelectedValues().length == 1) {
            langField.setText(langJList.getSelectedValue().toString());
        }
    }

    public void setField() {
        if (literalJList.getSelectedValues().length == 0) {
            labelField.setText("");
        } else if (literalJList.getSelectedValues().length == 1) {
            labelField.setText(literalJList.getSelectedValue().toString());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (selectedConcept == null) { return; }
        if (e.getSource() == addLabelButton) {
            selectedConcept.addLabel(new DODDLELiteral(langField.getText(), labelField.getText()));
            setLabelLangList();
            clearLabelField();
        } else if (e.getSource() == deleteLabelButton) {
            Object[] labelList = literalJList.getSelectedValues();
            for (int i = 0; i < labelList.length; i++) {
                DODDLELiteral label = (DODDLELiteral) labelList[i];
                if (label.getString().equals(typicalWordValueLabel.getText())) {
                    conceptInfoPanel.setTypicalWord(selectedConcept, new DODDLELiteral("", ""));
                }
                selectedConcept.removeLabel(label);
            }
            setLabelLangList();
            clearLabelField();
        } else if (e.getSource() == editLabelButton) {
            if (literalJList.getSelectedIndices().length == 1 && 0 < labelField.getText().length()) {
                DODDLELiteral label = (DODDLELiteral) literalJList.getSelectedValue();
                String labelText = label.getString();
                if (label.getLang().equals(langField.getText())) {
                    label.setString(labelField.getText());
                } else {
                    selectedConcept.removeLabel(label);
                    label.setLang(langField.getText());
                    label.setString(labelField.getText());
                    selectedConcept.addLabel(label);
                }
                if (labelText.equals(typicalWordValueLabel.getText())) {
                    conceptInfoPanel.setTypicalWord(selectedConcept, label);
                }
                setLabelLangList();
                clearLabelField();
                conceptInfoPanel.reloadConceptTreeNode(selectedConcept);
                conceptInfoPanel.reloadHasaTreeNode(selectedConcept);
            }
        } else if (e.getSource() == setTypcialLabelButton) {
            DODDLELiteral label = (DODDLELiteral) literalJList.getSelectedValue();
            conceptInfoPanel.setTypicalWord(selectedConcept, label);
        }
    }

}
