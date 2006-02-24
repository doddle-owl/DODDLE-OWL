package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

/**
 * @author takeshi morita
 */
public class OptionDialog extends JDialog implements ActionListener {

    private static JCheckBox ambiguitySiblingCheckBox;
    private static JCheckBox ambiguitySupCheckBox;
    private static JCheckBox ambiguitySubCheckBox;

    private static JRadioButton complexWordSetSameConceptButton;
    private static JRadioButton complexWordSetSubConceptButton;

    private static JCheckBox trimInternalComplexWordConceptBox;
    private static JCheckBox addAbstractInternalComplexWordConceptBox;
    private static JRadioButton nounConceptHierarchyButton;
    private static JRadioButton nounAndVerbConceptHierarchyButton;

    private JButton cancelButton;

    public OptionDialog(Frame owner) {
        super(owner);
        ambiguitySupCheckBox = new JCheckBox("���[�g�܂ł̃p�X�Ɋ܂܂��T�O");
        ambiguitySupCheckBox.setSelected(true);
        ambiguitySubCheckBox = new JCheckBox("���ʊT�O");
        ambiguitySubCheckBox.setSelected(true);
        ambiguitySiblingCheckBox = new JCheckBox("�Z��T�O");
        ambiguitySiblingCheckBox.setSelected(true);
        JPanel ambiguityOptionPanel = new JPanel();
        ambiguityOptionPanel.setBorder(BorderFactory.createTitledBorder("���`�������I�v�V����"));
        ambiguityOptionPanel.setLayout(new GridLayout(3, 1, 5, 5));
        ambiguityOptionPanel.add(ambiguitySupCheckBox);
        ambiguityOptionPanel.add(ambiguitySubCheckBox);
        ambiguityOptionPanel.add(ambiguitySiblingCheckBox);

        complexWordSetSameConceptButton = new JRadioButton("����T�O");
        complexWordSetSubConceptButton = new JRadioButton("���ʊT�O");
        complexWordSetSubConceptButton.setSelected(true);
        ButtonGroup complexWordButtonGroup = new ButtonGroup();
        complexWordButtonGroup.add(complexWordSetSameConceptButton);
        complexWordButtonGroup.add(complexWordSetSubConceptButton);
        JPanel complexWordOptionPanel = new JPanel();
        complexWordOptionPanel.setBorder(BorderFactory.createTitledBorder("������I�v�V����"));
        complexWordOptionPanel.setLayout(new GridLayout(2, 1, 5, 5));
        complexWordOptionPanel.add(complexWordSetSameConceptButton);
        complexWordOptionPanel.add(complexWordSetSubConceptButton);

        trimInternalComplexWordConceptBox = new JCheckBox("��������K�w������ۂɒ��ԃm�[�h���폜", true);
        addAbstractInternalComplexWordConceptBox = new JCheckBox("��������K�w������ۂɒ��ے��ԊT�O��ǉ�", true);
        nounConceptHierarchyButton = new JRadioButton("�����I�T�O�K�w�݂̂��\�z");
        nounConceptHierarchyButton.setSelected(true);
        nounAndVerbConceptHierarchyButton = new JRadioButton("�����I�T�O�{�����I�T�O�K�w���\�z");
        ButtonGroup group = new ButtonGroup();
        group.add(nounConceptHierarchyButton);
        group.add(nounAndVerbConceptHierarchyButton);
        JPanel hierarchyOptionPanel = new JPanel();
        hierarchyOptionPanel.setBorder(BorderFactory.createTitledBorder("�T�O�K�w�\�z�I�v�V����"));
        hierarchyOptionPanel.setLayout(new GridLayout(2, 1));
        hierarchyOptionPanel.add(trimInternalComplexWordConceptBox);
        hierarchyOptionPanel.add(addAbstractInternalComplexWordConceptBox);
        //hierarchyOptionPanel.add(nounConceptHierarchyButton);
        //hierarchyOptionPanel.add(nounAndVerbConceptHierarchyButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 1, 5, 5));
        mainPanel.add(ambiguityOptionPanel);
        mainPanel.add(complexWordOptionPanel);
        mainPanel.add(hierarchyOptionPanel);

        cancelButton = new JButton("����");
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(cancelButton, BorderLayout.EAST);
        Container contentPane = getContentPane();
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(owner);
        setTitle("�I�v�V����");
        pack();
    }

    public static boolean isTrimNodeWithComplexWordConceptConstruction() {
        return trimInternalComplexWordConceptBox.isSelected();
    }
    
    public static boolean isAddAbstractInternalComplexWordConcept() {
        return addAbstractInternalComplexWordConceptBox.isSelected();
    }

    public static boolean isComplexWordSetSameConcept() {
        return complexWordSetSameConceptButton.isSelected();
    }

    public static boolean isNounAndVerbConceptHierarchyConstructionMode() {
        return nounAndVerbConceptHierarchyButton.isSelected();
    }

    public static boolean isCheckSupConcepts() {
        return ambiguitySupCheckBox.isSelected();
    }

    public static boolean isCheckSubConcepts() {
        return ambiguitySubCheckBox.isSelected();
    }

    public static boolean isCheckSiblingConcepts() {
        return ambiguitySiblingCheckBox.isSelected();
    }

    public static void setNounAndVerbConceptHiearchy() {
        nounAndVerbConceptHierarchyButton.setSelected(true);
    }

    public static void setNounConceptHiearchy() {
        nounConceptHierarchyButton.setSelected(true);
    }

    public void saveOption(File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));
            StringBuffer buf = new StringBuffer();
            buf.append(ambiguitySiblingCheckBox.isSelected() + ",");
            buf.append(ambiguitySubCheckBox.isSelected() + ",");
            buf.append(ambiguitySupCheckBox.isSelected() + "\n");
            if (complexWordSetSameConceptButton.isSelected()) {
                buf.append("SAME\n");
            } else {
                buf.append("SUB\n");
            }
            if (nounConceptHierarchyButton.isSelected()) {
                buf.append("NOUN\n");
            } else {
                buf.append("NOUN+VERB\n");
            }
            if (trimInternalComplexWordConceptBox.isSelected()) {
                buf.append("Trim Internal Complex Word Concept Node\n");
            } else {
                buf.append("Keep Internal Complex Word Concept Node\n");
            }
            if (addAbstractInternalComplexWordConceptBox.isSelected()) {
                buf.append("Add Abstract Internal Complex Word Concept\n");
            } 
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadOption(File file) {
        if (!file.exists()) { return; }
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "SJIS"));
            String option = reader.readLine();
            String[] ambiguityOptions = option.split(",");
            ambiguitySiblingCheckBox.setSelected(ambiguityOptions[0].equals("true"));
            ambiguitySubCheckBox.setSelected(ambiguityOptions[1].equals("true"));
            ambiguitySupCheckBox.setSelected(ambiguityOptions[2].equals("true"));
            option = reader.readLine();
            complexWordSetSameConceptButton.setSelected(option.equals("SAME"));
            complexWordSetSubConceptButton.setSelected(option.equals("SUB"));
            option = reader.readLine();
            nounConceptHierarchyButton.setSelected(option.equals("NOUN"));
            nounAndVerbConceptHierarchyButton.setSelected(option.equals("NOUN+VERB"));
            option = reader.readLine();
            if (option != null) {
                trimInternalComplexWordConceptBox.setSelected(option.equals("Trim Internal Complex Word Concept Node"));
            }
            option = reader.readLine();
            if (option != null) {
                addAbstractInternalComplexWordConceptBox.setSelected(option.equals("Add Abstract Internal Complex Word Concept"));
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        setVisible(false);
    }
}
