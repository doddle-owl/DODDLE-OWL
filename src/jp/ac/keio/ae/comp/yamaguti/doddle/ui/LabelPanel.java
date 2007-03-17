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

    private JTextField langField;
    private JTextField labelField;
    private JButton addLabelButton;
    private JButton deleteLabelButton;
    private JButton editLabelButton;
    private JButton setTypcialLabelButton;
    
    private ConceptInformationPanel conceptInfoPanel;

    public LabelPanel(String type, ConceptInformationPanel ciPanel) {
        super(Translator.getString("Lang"), Translator.getString("Text"), type);
        conceptInfoPanel = ciPanel;
        langField = new JTextField(5);
        langField.setBorder(BorderFactory.createTitledBorder(Translator.getString("Lang")));
        labelField = new JTextField(15);
        labelField.setBorder(BorderFactory.createTitledBorder(Translator.getString("Text")));

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BorderLayout());
        fieldPanel.add(langField, BorderLayout.WEST);
        fieldPanel.add(labelField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));
        addLabelButton = new JButton(Translator.getString("ConceptTreePanel.Add"));
        addLabelButton.addActionListener(this);
        deleteLabelButton = new JButton(Translator.getString("ConceptTreePanel.Remove"));
        deleteLabelButton.addActionListener(this);
        editLabelButton = new JButton(Translator.getString("ConceptTreePanel.Edit"));
        editLabelButton.addActionListener(this);
        setTypcialLabelButton = new JButton(Translator.getString("ConceptTreePanel.SetDisplayWord"));
        setTypcialLabelButton.addActionListener(this);
        buttonPanel.add(setTypcialLabelButton);
        buttonPanel.add(addLabelButton);
        buttonPanel.add(editLabelButton);
        buttonPanel.add(deleteLabelButton);

        JPanel editPanel = new JPanel();
        editPanel.setLayout(new BorderLayout());
        editPanel.add(fieldPanel, BorderLayout.NORTH);
        editPanel.add(buttonPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createTitledBorder(Translator.getString("Label")));
        add(editPanel, BorderLayout.SOUTH);
    }
    
    private void clearLabelField() {
        langField.setText("");
        labelField.setText("");
    }

    public void actionPerformed(ActionEvent e) {
        if (selectedConcept == null) {
            return;
        }
        if (e.getSource() == addLabelButton) {
            selectedConcept.addLabel(new DODDLELiteral(langField.getText(), labelField.getText()));
            setLabelLangList();
            clearLabelField();
        } else if (e.getSource() == deleteLabelButton) {
            Object[] labelList = literalJList.getSelectedValues();
            for (int i = 0; i < labelList.length; i++) {
                selectedConcept.removeLabel((DODDLELiteral)labelList[i]);
            }
            setLabelLangList();
            clearLabelField();
        } else if (e.getSource() == editLabelButton) {
            if (literalJList.getSelectedIndices().length == 1 && 0 < labelField.getText().length()) {
                DODDLELiteral label = (DODDLELiteral)literalJList.getSelectedValue();
                if (label.getLang().equals(langField.getText())) {
                    label.setString(labelField.getText());
                } else {
                    selectedConcept.removeLabel(label);
                    label.setLang(langField.getText());
                    label.setString(labelField.getText());
                    selectedConcept.addLabel(label);
                }
                setLabelLangList();
                clearLabelField();
                conceptInfoPanel.reloadConceptTreeNode(selectedConcept);
            }
        } else if (e.getSource() == setTypcialLabelButton) {
            DODDLELiteral label = (DODDLELiteral) literalJList.getSelectedValue();
            conceptInfoPanel.setTypicalWord(selectedConcept, label);
        }
    }

}
