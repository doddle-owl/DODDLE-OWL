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

    private static final String edrTestID = "3aa966"; // �T�O
    private static final String edrtTestID = "2f3526"; // ���[�g�m�[�h
    private static final String wnTestID = "5498421"; // concept

    public OntologySelectionPanel() {
        edrCheckBox = new JCheckBox("EDR ��ʎ���", true);
        Concept c = EDRDic.getEDRConcept(edrTestID);
        edrCheckBox.setEnabled(c != null);
        edrtCheckBox = new JCheckBox("EDR ��厫��", true);
        c = EDRDic.getEDRTConcept(edrtTestID);
        edrtCheckBox.setEnabled(c != null);
        wnCheckBox = new JCheckBox("WordNet", true);
        WordNetDic wnDic = WordNetDic.getInstance();
        if (wnDic != null) {
            c = WordNetDic.getWNConcept(wnTestID);
        }
        wnCheckBox.setEnabled(wnDic != null && c != null);

        JPanel generalOntologyPanel = new JPanel();
        generalOntologyPanel.setBorder(BorderFactory.createTitledBorder("�Q�Ƃ����ʃI���g���W�[�̑I��"));
        generalOntologyPanel.setLayout(new GridLayout(3, 1));
        generalOntologyPanel.add(edrCheckBox);
        generalOntologyPanel.add(edrtCheckBox);
        generalOntologyPanel.add(wnCheckBox);

        nextTabButton = new JButton("�����I����");
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
        return edrtCheckBox.isEnabled() &&edrtCheckBox.isSelected();
    }

    public boolean isWordNetEnable() {
        return wnCheckBox.isEnabled() &&wnCheckBox.isSelected();
    }

    public void actionPerformed(ActionEvent e) {
        DODDLE.setSelectedIndex(DODDLE.DOCUMENT_SELECTION_PANEL);
    }
}
