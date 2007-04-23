/*
 * @(#)  2007/03/17
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class DescriptionPanel extends LiteralPanel implements ActionListener {

    private JButton addDescriptionButton;
    private JButton deleteDescriptionButton;
    private JButton editDescriptionButton;

    public DescriptionPanel(String type) {
        super(Translator.getTerm("LanguageLabel"), Translator.getTerm("TextLabel"), type);

        JPanel editPanel = new JPanel();
        editPanel.setLayout(new GridLayout(1, 3));
        addDescriptionButton = new JButton(Translator.getTerm("AddButton"));
        addDescriptionButton.addActionListener(this);
        deleteDescriptionButton = new JButton(Translator.getTerm("RemoveButton"));
        deleteDescriptionButton.addActionListener(this);
        editDescriptionButton = new JButton(Translator.getTerm("EditButton"));
        editDescriptionButton.addActionListener(this);
        editPanel.add(addDescriptionButton);
        editPanel.add(editDescriptionButton);
        editPanel.add(deleteDescriptionButton);

        setBorder(BorderFactory.createTitledBorder(Translator.getTerm("DescriptionBorder")));
        add(editPanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        if (selectedConcept == null) { return; }
        if (e.getSource() == addDescriptionButton) {
            EditDescriptionDialog editDescriptionDialog = new EditDescriptionDialog(DODDLE.rootFrame);
            editDescriptionDialog.setVisible(true);
            selectedConcept.addDescription(editDescriptionDialog.getDescription());
            setDescriptionLangList();
        } else if (e.getSource() == deleteDescriptionButton) {
            Object[] descritionList = literalJList.getSelectedValues();
            for (int i = 0; i < descritionList.length; i++) {
                selectedConcept.removeDescription((DODDLELiteral) descritionList[i]);
            }
            setDescriptionLangList();
        } else if (e.getSource() == editDescriptionButton) {
            if (literalJList.getSelectedValues().length == 1) {
                EditDescriptionDialog editDescriptionDialog = new EditDescriptionDialog(DODDLE.rootFrame);
                DODDLELiteral description = (DODDLELiteral) literalJList.getSelectedValue();
                editDescriptionDialog.setDescription(description);
                editDescriptionDialog.setVisible(true);
                DODDLELiteral editDescription = editDescriptionDialog.getDescription();
                if (0 < editDescription.getString().length()) {
                    if (editDescription.getLang().equals(description.getLang())) {
                        description.setString(editDescription.getString());
                    } else {
                        selectedConcept.removeDescription(description);
                        description.setLang(editDescription.getLang());
                        description.setString(editDescription.getString());
                        selectedConcept.addDescription(description);
                    }
                    setDescriptionLangList();
                }
            }
        }
    }

}
