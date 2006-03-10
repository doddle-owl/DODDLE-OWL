/*
 * @(#)  2006/03/01
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
public class OntologySelectionPanel extends JPanel implements ActionListener {

    private JCheckBox edrCheckBox;
    private JCheckBox edrtCheckBox;
    private JCheckBox wnCheckBox;

    private JButton nextTabButton;

    private static final String edrTestID = "3aa966"; // 概念
    private static final String edrtTestID = "2f3526"; // ルートノード
    private static final String wnTestID = "5498421"; // concept

    public OntologySelectionPanel() {
        edrCheckBox = new JCheckBox(Translator.getString("OntologySelectionPanel.EDR"), true);
        Concept c = EDRDic.getEDRConcept(edrTestID);
        edrCheckBox.setEnabled(c != null);
        edrtCheckBox = new JCheckBox(Translator.getString("OntologySelectionPanel.EDRT"), true);
        c = EDRDic.getEDRTConcept(edrtTestID);
        edrtCheckBox.setEnabled(c != null);
        wnCheckBox = new JCheckBox(Translator.getString("OntologySelectionPanel.WordNet"), true);
        WordNetDic wnDic = WordNetDic.getInstance();
        if (wnDic != null) {
            c = WordNetDic.getWNConcept(wnTestID);
        }
        wnCheckBox.setEnabled(wnDic != null && c != null);

        JPanel generalOntologyPanel = new JPanel();
        generalOntologyPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("OntologySelectionPanel.RefGenericOntologySelection")));
        generalOntologyPanel.setLayout(new GridLayout(3, 1));
        generalOntologyPanel.add(edrCheckBox);
        generalOntologyPanel.add(edrtCheckBox);
        generalOntologyPanel.add(wnCheckBox);

        nextTabButton = new JButton("文書選択へ");
        nextTabButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(nextTabButton, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(generalOntologyPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean isEDREnable() {
        return edrCheckBox.isEnabled() && edrCheckBox.isSelected();
    }

    public boolean isEDRTEnable() {
        return edrtCheckBox.isEnabled() && edrtCheckBox.isSelected();
    }

    public boolean isWordNetEnable() {
        return wnCheckBox.isEnabled() && wnCheckBox.isSelected();
    }

    public void actionPerformed(ActionEvent e) {
        DODDLE.setSelectedIndex(DODDLE.DOCUMENT_SELECTION_PANEL);
    }

    public String getEnableDicList() {
        StringBuilder builder = new StringBuilder();
        if (isEDREnable()) {
            builder.append("EDR一般辞書 ");
        }
        if (isEDRTEnable()) {
            builder.append("EDR専門辞書 ");
        }
        if (isWordNetEnable()) {
            builder.append("WordNet ");
        }
        return builder.toString();
    }
}
