/*
 * @(#)  2007/01/30
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class GeneralOntologySelectionPanel extends JPanel implements ActionListener {
    private JCheckBox edrCheckBox;
    private JCheckBox edrtCheckBox;
    private JCheckBox wnCheckBox;

    private static final String wnTestID = "5498421"; // concept

    public GeneralOntologySelectionPanel() {
        edrCheckBox = new JCheckBox(Translator.getTerm("GenericEDRCheckBox"), false);
        edrCheckBox.addActionListener(this);
        edrtCheckBox = new JCheckBox(Translator.getTerm("TechnicalEDRCheckBox"), false);
        edrtCheckBox.addActionListener(this);
        wnCheckBox = new JCheckBox(Translator.getTerm("WordNetCheckBox"), false);
        wnCheckBox.addActionListener(this);
        JPanel checkPanel = new JPanel();
        checkPanel.setLayout(new GridLayout(3, 1));
        checkPanel.add(edrCheckBox);
        checkPanel.add(edrtCheckBox);
        checkPanel.add(wnCheckBox);
        setLayout(new BorderLayout());
        add(checkPanel, BorderLayout.NORTH);
    }

    public void saveGeneralOntologyInfo(File saveFile) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile), "UTF-8"));
            Properties properties = new Properties();
            properties.setProperty("EDR(general)", String.valueOf(isEDREnable()));
            properties.setProperty("EDR(technical)", String.valueOf(isEDRTEnable()));
            properties.setProperty("WordNet", String.valueOf(isWordNetEnable()));
            properties.store(writer, "Ontology Info");
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void loadGeneralOntologyInfo(File loadFile) {
        if (!loadFile.exists()) { return; }
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(loadFile);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            Properties properties = new Properties();
            properties.load(reader);
            boolean t = new Boolean(properties.getProperty("EDR(general)"));
            edrCheckBox.setSelected(t);
            enableEDRDic(t);
            t = new Boolean(properties.getProperty("EDR(technical)"));
            edrtCheckBox.setSelected(t);
            enableEDRTDic(t);
            t = new Boolean(properties.getProperty("WordNet"));
            wnCheckBox.setSelected(t);
            enableWordNetDic(t);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
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

    public boolean isEDREnable() {
        return edrCheckBox.isEnabled() && edrCheckBox.isSelected();
    }

    public boolean isEDRTEnable() {
        return edrtCheckBox.isEnabled() && edrtCheckBox.isSelected();
    }

    public boolean isWordNetEnable() {
        return wnCheckBox.isEnabled() && wnCheckBox.isSelected();
    }

    private void enableEDRDic(boolean t) {
        if (t) {
            boolean isInitEDRDic = EDRDic.initEDRDic();
            boolean isInitEDRConceptDescriptionDic =ConceptDefinition.getInstance().initConceptDescriptionDic();
            edrCheckBox.setEnabled(isInitEDRDic && isInitEDRConceptDescriptionDic);
            if (!edrCheckBox.isEnabled()) {
                edrCheckBox.setSelected(false);
            }
            DODDLE.STATUS_BAR.addValue();
        }
    }

    private void enableEDRTDic(boolean t) {
        if (t) {
            edrtCheckBox.setEnabled(EDRDic.initEDRTDic());
            if (!edrtCheckBox.isEnabled()) {
                edrtCheckBox.setSelected(false);
            }
        }
    }

    private void enableWordNetDic(boolean t) {
        if (t) {
            WordNetDic wnDic = WordNetDic.getInstance();
            Concept c = null;
            if (wnDic != null) {
                c = WordNetDic.getWNConcept(wnTestID);
            }
            wnCheckBox.setEnabled(wnDic != null && c != null);
            if (!wnCheckBox.isEnabled()) {
                wnCheckBox.setSelected(false);
                WordNetDic.resetWordNet();
            }
        }
    }

    /**
     * オプションダイアログでパスを変更した場合は，再度，チェックできるようにする．
     */
    public void resetCheckBoxes() {
        edrCheckBox.setEnabled(true);
        edrtCheckBox.setEnabled(true);
        wnCheckBox.setEnabled(true);
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEProject project = DODDLE.getCurrentProject();
        if (e.getSource() == edrCheckBox) {
            enableEDRDic(edrCheckBox.isSelected());
            project.addLog("GenericEDRCheckBox", edrCheckBox.isSelected());
        } else if (e.getSource() == edrtCheckBox) {
            enableEDRTDic(edrtCheckBox.isSelected());
            project.addLog("TechnicalEDRCheckBox", edrtCheckBox.isSelected());
        } else if (e.getSource() == wnCheckBox) {
            enableWordNetDic(wnCheckBox.isSelected());
            project.addLog("WordNetCheckBox", wnCheckBox.isSelected());
        }
    }
}
