/*
 * @(#)  2006/03/01
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

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
    private NameSpaceTable nsTable;

    private static final String edrTestID = "ID3aa966"; // 概念
    private static final String edrtTestID = "ID2f3526"; // ルートノード
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

        JPanel generalOntologyCheckPanel = new JPanel();
        generalOntologyCheckPanel.setLayout(new GridLayout(3, 1));
        generalOntologyCheckPanel.add(edrCheckBox);
        generalOntologyCheckPanel.add(edrtCheckBox);
        generalOntologyCheckPanel.add(wnCheckBox);

        nextTabButton = new JButton(Translator.getString("OntologySelectionPanel.DocumentSelection"));
        nextTabButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(nextTabButton, BorderLayout.EAST);

        JPanel generalOntologyPanel = new JPanel();
        generalOntologyPanel.setLayout(new BorderLayout());
        generalOntologyPanel.add(generalOntologyCheckPanel, BorderLayout.NORTH);
        generalOntologyPanel.add(buttonPanel, BorderLayout.SOUTH);

        nsTable = new NameSpaceTable();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane
                .add(generalOntologyPanel, Translator.getString("OntologySelectionPanel.RefGenericOntologySelection"));
        tabbedPane.add(new OWLOntologySelectionPanel(), Translator.getString("OntologySelectionPanel.OWLOntologySelection"));
        tabbedPane.add(nsTable, Translator.getString("OntologySelectionPanel.NameSpaceTable"));

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    public String getPrefix(String ns) {
        return nsTable.getPrefix(ns);
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

    public void saveOntologyInfo(File saveFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
            writer.write("EDR(general)," + isEDREnable() + "\n");
            writer.write("EDR(technical)," + isEDRTEnable() + "\n");
            writer.write("WordNet," + isWordNetEnable() + "\n");
            writer.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    public void loadOntologyInfo(File loadFile) {
        if (!loadFile.exists()) { return; }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(loadFile));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] ontInfo = line.split(",");
                String ontology = ontInfo[0];
                boolean isEnable = ontInfo[1].equals("true");
                if (ontology.equals("EDR(general)")) {
                    edrCheckBox.setSelected(isEnable);
                } else if (ontology.equals("EDR(technical)")) {
                    edrtCheckBox.setSelected(isEnable);
                } else if (ontology.equals("WordNet")) {
                    wnCheckBox.setSelected(isEnable);
                }
            }
            reader.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
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
