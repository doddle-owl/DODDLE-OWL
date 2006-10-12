package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.DisambiguationPanel.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class OptionDialog extends JDialog implements ActionListener {

    private static JCheckBox siblingDisambiguationCheckBox;
    private static JCheckBox supDisambiguationCheckBox;
    private static JCheckBox subDisambiguationCheckBox;

    private static JCheckBox isUsingSpreadActivatingAlgorithmForDisambiguationBox;
    private static JRadioButton shortestSpreadActivatingAlgorithmForDisambiguationButton;
    private static JRadioButton longestSpreadActivatingAlgorithmForDisambiguationButton;
    private static JRadioButton averageSpreadActivatingAlgorithmForDisambiguationButton;

    private static JRadioButton complexWordSetSameConceptButton;
    private static JRadioButton complexWordSetSubConceptButton;

    private static JCheckBox constructComplexWordTreeBox;
    private static JCheckBox trimInternalComplexWordConceptBox;
    private static JCheckBox addAbstractInternalComplexWordConceptBox;
    private static JRadioButton nounConceptHierarchyButton;
    private static JRadioButton nounAndVerbConceptHierarchyButton;

    private static JCheckBox showPrefixCheckBox;

    private JButton cancelButton;

   
    public OptionDialog(Frame owner) {
        super(owner);
        isUsingSpreadActivatingAlgorithmForDisambiguationBox = new JCheckBox("isUsingSpreadActivatingAlgorithm", true);
        isUsingSpreadActivatingAlgorithmForDisambiguationBox.addActionListener(this);
        shortestSpreadActivatingAlgorithmForDisambiguationButton = new JRadioButton("Shortest", true);
        longestSpreadActivatingAlgorithmForDisambiguationButton = new JRadioButton("Longest");
        averageSpreadActivatingAlgorithmForDisambiguationButton = new JRadioButton("Average");
        ButtonGroup spreadActivatingAlgorithmGroup = new ButtonGroup();
        spreadActivatingAlgorithmGroup.add(shortestSpreadActivatingAlgorithmForDisambiguationButton);
        spreadActivatingAlgorithmGroup.add(longestSpreadActivatingAlgorithmForDisambiguationButton);
        spreadActivatingAlgorithmGroup.add(averageSpreadActivatingAlgorithmForDisambiguationButton);
        JPanel spreadActivatingAlgorithmOptionPanel = new JPanel();
        spreadActivatingAlgorithmOptionPanel.add(isUsingSpreadActivatingAlgorithmForDisambiguationBox);
        spreadActivatingAlgorithmOptionPanel.add(shortestSpreadActivatingAlgorithmForDisambiguationButton);
        spreadActivatingAlgorithmOptionPanel.add(longestSpreadActivatingAlgorithmForDisambiguationButton);
        spreadActivatingAlgorithmOptionPanel.add(averageSpreadActivatingAlgorithmForDisambiguationButton);

        supDisambiguationCheckBox = new JCheckBox(Translator
                .getString("Component.Tool.Option.Disambiguation.ConceptsInPathToRoot"));
        supDisambiguationCheckBox.setSelected(true);
        subDisambiguationCheckBox = new JCheckBox(Translator
                .getString("Component.Tool.Option.Disambiguation.SubConcept"));
        subDisambiguationCheckBox.setSelected(true);
        siblingDisambiguationCheckBox = new JCheckBox(Translator
                .getString("Component.Tool.Option.Disambiguation.SiblingConcept"));
        siblingDisambiguationCheckBox.setSelected(true);
        JPanel automaticDisambiguationOptionPanel = new JPanel();
        automaticDisambiguationOptionPanel.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("Component.Tool.Option.Disambiguation")));
        automaticDisambiguationOptionPanel.setLayout(new GridLayout(4, 1, 5, 5));
        automaticDisambiguationOptionPanel.add(spreadActivatingAlgorithmOptionPanel);
        automaticDisambiguationOptionPanel.add(supDisambiguationCheckBox);
        automaticDisambiguationOptionPanel.add(subDisambiguationCheckBox);
        automaticDisambiguationOptionPanel.add(siblingDisambiguationCheckBox);

        complexWordSetSameConceptButton = new JRadioButton(Translator
                .getString("Component.Tool.Option.ComplexWord.SameConcept"));
        complexWordSetSubConceptButton = new JRadioButton(Translator
                .getString("Component.Tool.Option.ComplexWord.SubConcept"));
        complexWordSetSubConceptButton.setSelected(true);
        ButtonGroup complexWordButtonGroup = new ButtonGroup();
        complexWordButtonGroup.add(complexWordSetSameConceptButton);
        complexWordButtonGroup.add(complexWordSetSubConceptButton);
        JPanel complexWordOptionPanel = new JPanel();
        complexWordOptionPanel.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("Component.Tool.Option.ComplexWord")));
        complexWordOptionPanel.setLayout(new GridLayout(2, 1, 5, 5));
        complexWordOptionPanel.add(complexWordSetSameConceptButton);
        complexWordOptionPanel.add(complexWordSetSubConceptButton);

        constructComplexWordTreeBox = new JCheckBox(Translator
                .getString("Component.Tool.Option.ConstructTree.ComplexWordTreeConstruction"), true);
        constructComplexWordTreeBox.addActionListener(this);
        trimInternalComplexWordConceptBox = new JCheckBox(Translator
                .getString("Component.Tool.Option.ConstructTree.TrimInternalNode"), true);
        addAbstractInternalComplexWordConceptBox = new JCheckBox(Translator
                .getString("Component.Tool.Option.ConstructTree.AddAbstractInternalNode"), true);
        nounConceptHierarchyButton = new JRadioButton("");
        nounConceptHierarchyButton.setSelected(true);
        nounAndVerbConceptHierarchyButton = new JRadioButton("");
        ButtonGroup group = new ButtonGroup();
        group.add(nounConceptHierarchyButton);
        group.add(nounAndVerbConceptHierarchyButton);
        JPanel hierarchyOptionPanel = new JPanel();
        hierarchyOptionPanel.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("Component.Tool.Option.ConstructTree")));
        hierarchyOptionPanel.setLayout(new GridLayout(3, 1));
        hierarchyOptionPanel.add(constructComplexWordTreeBox);
        hierarchyOptionPanel.add(trimInternalComplexWordConceptBox);
        hierarchyOptionPanel.add(addAbstractInternalComplexWordConceptBox);
        // hierarchyOptionPanel.add(nounConceptHierarchyButton);
        // hierarchyOptionPanel.add(nounAndVerbConceptHierarchyButton);

        showPrefixCheckBox = new JCheckBox(Translator.getString("Component.Tool.Option.View.ShowPrefix"));
        JPanel viewPanel = new JPanel();
        viewPanel.setBorder(BorderFactory.createTitledBorder(Translator.getString("Component.Tool.Option.View")));
        viewPanel.setLayout(new BorderLayout());
        viewPanel.add(showPrefixCheckBox, BorderLayout.CENTER);

        JPanel op1Panel = new JPanel();
        op1Panel.setLayout(new GridLayout(2, 2));
        op1Panel.add(automaticDisambiguationOptionPanel);
        op1Panel.add(complexWordOptionPanel);
        op1Panel.add(hierarchyOptionPanel);
        op1Panel.add(viewPanel);

        JPanel directoryPanel = new DirectoryPanel();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(op1Panel, BorderLayout.CENTER);
        mainPanel.add(directoryPanel, BorderLayout.SOUTH);

        cancelButton = new JButton(Translator.getString("Close"));
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(cancelButton, BorderLayout.EAST);
        Container contentPane = getContentPane();
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(owner);
        setTitle(Translator.getString("Component.Tool.Option"));
        pack();
    }

    public static void setSiblingDisambiguation(boolean t) {
        siblingDisambiguationCheckBox.setSelected(t);
    }

    public static void setSupDisambiguation(boolean t) {
        supDisambiguationCheckBox.setSelected(t);
    }

    public static void setSubDisambiguation(boolean t) {
        subDisambiguationCheckBox.setSelected(t);
    }

    public static void setUsingSpreadActivationAlgorithmForDisambiguation(boolean t) {
        isUsingSpreadActivatingAlgorithmForDisambiguationBox.setSelected(t);
    }

    public static void setShortestSpreadActivatingAlgorithmforDisambiguation(boolean t) {
        shortestSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);
    }

    public static void setLongestSpreadActivatingAlgorithmforDisambiguation(boolean t) {
        longestSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);
    }

    public static void setAverageSpreadActivatingAlgorithmforDisambiguation(boolean t) {
        averageSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);
    }
    
    public static boolean isConstructComplexWordTree() {
        return constructComplexWordTreeBox.isSelected();
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

    public static boolean isUsingSpreadActivatingAlgorithm() {
        return isUsingSpreadActivatingAlgorithmForDisambiguationBox.isSelected();
    }

    public static boolean isCheckShortestSpreadActivation() {
        return shortestSpreadActivatingAlgorithmForDisambiguationButton.isSelected();
    }

    public static boolean isCheckLongestSpreadActivation() {
        return longestSpreadActivatingAlgorithmForDisambiguationButton.isSelected();
    }

    public static boolean isCheckAverageSpreadActivation() {
        return averageSpreadActivatingAlgorithmForDisambiguationButton.isSelected();
    }

    public static boolean isCheckSupConcepts() {
        return supDisambiguationCheckBox.isSelected();
    }

    public static boolean isCheckSubConcepts() {
        return subDisambiguationCheckBox.isSelected();
    }

    public static boolean isCheckSiblingConcepts() {
        return siblingDisambiguationCheckBox.isSelected();
    }

    public static void setNounAndVerbConceptHiearchy() {
        nounAndVerbConceptHierarchyButton.setSelected(true);
    }

    public static void setNounConceptHiearchy() {
        nounConceptHierarchyButton.setSelected(true);
    }

    public static boolean isShowPrefix() {
        return showPrefixCheckBox.isSelected();
    }

    public void saveOption(File file, DisambiguationPanel disambiguationPanel) {
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));
            StringBuffer buf = new StringBuffer();
            buf.append(siblingDisambiguationCheckBox.isSelected() + ",");
            buf.append(subDisambiguationCheckBox.isSelected() + ",");
            buf.append(supDisambiguationCheckBox.isSelected() + "\n");
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
            PerfectMatchedOptionPanel perfectMatchedOptionPanel = disambiguationPanel.getPerfectMatchedOptionPanel();
            buf.append(perfectMatchedOptionPanel.isConstruction() + ",");
            buf.append(perfectMatchedOptionPanel.isTrimming() + "\n");

            PartialMatchedOptionPanel partialMatchedOptionPanel = disambiguationPanel.getPartialMatchedOptionPanel();
            buf.append(partialMatchedOptionPanel.isConstruction() + ",");
            buf.append(partialMatchedOptionPanel.isTrimming() + ",");
            buf.append(partialMatchedOptionPanel.isAddAbstractConcept() + "\n");

            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadOption(File file, DisambiguationPanel disambiguationPanel) {
        if (!file.exists()) { return; }
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "SJIS"));
            String option = reader.readLine();
            String[] automaticDisambiguationOptions = option.split(",");
            siblingDisambiguationCheckBox.setSelected(automaticDisambiguationOptions[0].equals("true"));
            subDisambiguationCheckBox.setSelected(automaticDisambiguationOptions[1].equals("true"));
            supDisambiguationCheckBox.setSelected(automaticDisambiguationOptions[2].equals("true"));
            option = reader.readLine();
            complexWordSetSameConceptButton.setSelected(option.equals("SAME"));
            complexWordSetSubConceptButton.setSelected(option.equals("SUB"));
            option = reader.readLine();
            nounConceptHierarchyButton.setSelected(option.equals("NOUN"));
            nounAndVerbConceptHierarchyButton.setSelected(option.equals("NOUN+VERB"));
            option = reader.readLine();
            if (option != null) {
                String[] perfectMatchedConstructionOption = option.split(",");
                if (perfectMatchedConstructionOption.length == 2) {
                    PerfectMatchedOptionPanel perfectMatchedOptionPanel = disambiguationPanel
                            .getPerfectMatchedOptionPanel();
                    perfectMatchedOptionPanel.setConstruction(perfectMatchedConstructionOption[0].equals("true"));
                    perfectMatchedOptionPanel.setTrimming(perfectMatchedConstructionOption[1].equals("true"));
                }
            }
            option = reader.readLine();
            if (option != null) {
                String[] partialMatchedConstructionOption = option.split(",");
                if (partialMatchedConstructionOption.length == 3) {
                    PartialMatchedOptionPanel partialMatchedOptionPanel = disambiguationPanel
                            .getPartialMatchedOptionPanel();
                    partialMatchedOptionPanel.setConstruction(partialMatchedConstructionOption[0].equals("true"));
                    partialMatchedOptionPanel.setTrimming(partialMatchedConstructionOption[1].equals("true"));
                    partialMatchedOptionPanel.setAddAbstractConcept(partialMatchedConstructionOption[2].equals("true"));
                }
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == constructComplexWordTreeBox) {
            boolean isEnable = constructComplexWordTreeBox.isSelected();
            trimInternalComplexWordConceptBox.setEnabled(isEnable);
            addAbstractInternalComplexWordConceptBox.setEnabled(isEnable);
        } else if (e.getSource() == cancelButton) {
            setVisible(false);
        } else if (e.getSource() == isUsingSpreadActivatingAlgorithmForDisambiguationBox) {
            boolean t = isUsingSpreadActivatingAlgorithmForDisambiguationBox.isSelected();
            shortestSpreadActivatingAlgorithmForDisambiguationButton.setEnabled(t);
            longestSpreadActivatingAlgorithmForDisambiguationButton.setEnabled(t);
            averageSpreadActivatingAlgorithmForDisambiguationButton.setEnabled(t);
        }
    }

    class DirectoryPanel extends JPanel {
        private JTextField chasenDirField;
        private JTextField ssTaggerDirField;
        private JTextField perlDirField;
        private JTextField xdoc2txtDirField;
        private JTextField senDicDirField;
        private JTextField edrDicDirField;
        private JTextField edrtDicDirField;
        private JTextField wnDicDirField;

        private JButton browseChasenDirButton;
        private JButton browseSSTaggerDirButton;
        private JButton browsePerlDirButton;
        private JButton browseXdoc2txtDirButton;
        private JButton browseSenDicDirButton;
        private JButton browseEDRDicDirButton;
        private JButton browseEDRTDicDirButton;
        private JButton browseWNDicDirButton;

        public DirectoryPanel() {
            initChasenDirField();
            initSSTaggerDirField();
            initPerlDirField();
            initXdoc2txtDirField();
            initSenDicDirField();
            initEDRDicDirField();
            initEDRTDirField();
            initWNDicDirField();

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(4, 2, 10, 5));
            panel.add(getChasenDirPanel());
            panel.add(getSSTaggerDirPanel());
            panel.add(getPerlDirPanel());
            panel.add(getXdoc2txtDirPanel());
            panel.add(getSenDicDirPanel());
            panel.add(getEDRDicDirPanel());
            panel.add(getEDRTDicDirPanel());
            panel.add(getWNDicDirPanel());
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEtchedBorder());
            add(panel, BorderLayout.CENTER);
        }

        private void initChasenDirField() {
            chasenDirField = new JTextField(15);
            chasenDirField.setText(DocumentSelectionPanel.CHASEN_EXE);
            chasenDirField.setEditable(false);
            browseChasenDirButton = new JButton(Translator.getString("OptionDialog.DirectoryTab.Browse"));
            // browseChasenDirButton.setMnemonic('r');
            browseChasenDirButton.addActionListener(new BrowseDirectory(chasenDirField));
        }

        private void initSSTaggerDirField() {
            ssTaggerDirField = new JTextField(15);
            ssTaggerDirField.setText(DocumentSelectionPanel.SS_TAGGER_HOME);
            ssTaggerDirField.setEditable(false);
            browseSSTaggerDirButton = new JButton(Translator.getString("OptionDialog.DirectoryTab.Browse"));
            // browseChasenDirButton.setMnemonic('r');
            browseSSTaggerDirButton.addActionListener(new BrowseDirectory(ssTaggerDirField));
        }

        private void initPerlDirField() {
            perlDirField = new JTextField(15);
            perlDirField.setText(DocumentSelectionPanel.PERL_EXE);
            perlDirField.setEditable(false);
            browsePerlDirButton = new JButton(Translator.getString("OptionDialog.DirectoryTab.Browse"));
            // browseChasenDirButton.setMnemonic('r');
            browsePerlDirButton.addActionListener(new BrowseDirectory(perlDirField));
        }

        private void initXdoc2txtDirField() {
            xdoc2txtDirField = new JTextField(15);
            xdoc2txtDirField.setText(DocumentSelectionPanel.XDOC2TXT_EXE);
            xdoc2txtDirField.setEditable(false);
            browseXdoc2txtDirButton = new JButton(Translator.getString("OptionDialog.DirectoryTab.Browse"));
            // browseChasenDirButton.setMnemonic('r');
            browseXdoc2txtDirButton.addActionListener(new BrowseDirectory(xdoc2txtDirField));
        }

        private void initSenDicDirField() {
            senDicDirField = new JTextField(15);
            senDicDirField.setText(DODDLE.SEN_HOME);
            senDicDirField.setEditable(false);
            browseSenDicDirButton = new JButton(Translator.getString("OptionDialog.DirectoryTab.Browse"));
            // browseChasenDirButton.setMnemonic('r');
            browseSenDicDirButton.addActionListener(new BrowseDirectory(senDicDirField));
        }

        private void initEDRDicDirField() {
            edrDicDirField = new JTextField(15);
            edrDicDirField.setText(DODDLE.DODDLE_DIC);
            edrDicDirField.setEditable(false);
            browseEDRDicDirButton = new JButton(Translator.getString("OptionDialog.DirectoryTab.Browse"));
            // browseChasenDirButton.setMnemonic('r');
            browseEDRDicDirButton.addActionListener(new BrowseDirectory(edrDicDirField));
        }
        private void initEDRTDirField() {
            edrtDicDirField = new JTextField(15);
            edrtDicDirField.setText(DODDLE.DODDLE_EDRT_DIC);
            edrtDicDirField.setEditable(false);
            browseEDRTDicDirButton = new JButton(Translator.getString("OptionDialog.DirectoryTab.Browse"));
            // browseChasenDirButton.setMnemonic('r');
            browseEDRTDicDirButton.addActionListener(new BrowseDirectory(edrtDicDirField));
        }
        private void initWNDicDirField() {
            wnDicDirField = new JTextField(15);
            wnDicDirField.setText(DODDLE.WORDNET_PATH);
            wnDicDirField.setEditable(false);
            browseWNDicDirButton = new JButton(Translator.getString("OptionDialog.DirectoryTab.Browse"));
            // browseChasenDirButton.setMnemonic('r');
            browseWNDicDirButton.addActionListener(new BrowseDirectory(wnDicDirField));
        }

        class BrowseDirectory extends AbstractAction {
            private JTextField directoryField;

            BrowseDirectory(JTextField field) {
                directoryField = field;
            }

            private String getDirectoryName() {
                File currentDirectory = new File(directoryField.getText());
                JFileChooser jfc = new JFileChooser(currentDirectory);
                jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                jfc.setDialogTitle("Select Directory");
                int fd = jfc.showOpenDialog(DODDLE.getCurrentProject().getRootPane());
                if (fd == JFileChooser.APPROVE_OPTION) { return jfc.getSelectedFile().toString(); }
                return null;
            }

            public void actionPerformed(ActionEvent e) {
                String directoryName = getDirectoryName();
                if (directoryName != null) {
                    directoryField.setText(directoryName);
                    directoryField.setToolTipText(directoryName);
                    if (directoryField == chasenDirField) {
                        DocumentSelectionPanel.CHASEN_EXE = directoryName;
                    } else if (directoryField == ssTaggerDirField) {
                        DocumentSelectionPanel.SS_TAGGER_HOME = directoryName;
                    } else if (directoryField == perlDirField) {
                        DocumentSelectionPanel.PERL_EXE = directoryName;
                    } else if (directoryField == senDicDirField) {
                        DODDLE.SEN_HOME = directoryName;
                    } else if (directoryField == edrDicDirField) {
                        DODDLE.DODDLE_DIC = directoryName;
                    } else if (directoryField == edrtDicDirField) {
                        DODDLE.DODDLE_EDRT_DIC = directoryName;
                    } else if (directoryField == wnDicDirField) {
                        DODDLE.WORDNET_PATH = directoryName;
                    }
                }
            }
        }

        private JPanel getChasenDirPanel() {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("OptionDialog.DirectoryTab.ChasenDirectory")));
            workDirectoryPanel.add(chasenDirField);
            workDirectoryPanel.add(browseChasenDirButton);

            return workDirectoryPanel;
        }

        private JPanel getSSTaggerDirPanel() {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("OptionDialog.DirectoryTab.SSTaggerDirectory")));
            workDirectoryPanel.add(ssTaggerDirField);
            workDirectoryPanel.add(browseSSTaggerDirButton);

            return workDirectoryPanel;
        }

        private JPanel getPerlDirPanel() {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("OptionDialog.DirectoryTab.PerlDirectory")));
            workDirectoryPanel.add(perlDirField);
            workDirectoryPanel.add(browsePerlDirButton);

            return workDirectoryPanel;
        }

        private JPanel getXdoc2txtDirPanel() {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("OptionDialog.DirectoryTab.Xdoc2txtDirectory")));
            workDirectoryPanel.add(xdoc2txtDirField);
            workDirectoryPanel.add(browseXdoc2txtDirButton);

            return workDirectoryPanel;
        }

        private JPanel getSenDicDirPanel() {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("OptionDialog.DirectoryTab.SenDicDirectory")));
            workDirectoryPanel.add(senDicDirField);
            workDirectoryPanel.add(browseSenDicDirButton);

            return workDirectoryPanel;
        }

        private JPanel getEDRDicDirPanel() {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("OptionDialog.DirectoryTab.EDRDicDirectory")));
            workDirectoryPanel.add(edrDicDirField);
            workDirectoryPanel.add(browseEDRDicDirButton);

            return workDirectoryPanel;
        }

        private JPanel getEDRTDicDirPanel() {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("OptionDialog.DirectoryTab.EDRTDicDirectory")));
            workDirectoryPanel.add(edrtDicDirField);
            workDirectoryPanel.add(browseEDRTDicDirButton);

            return workDirectoryPanel;
        }

        private JPanel getWNDicDirPanel() {
            JPanel workDirectoryPanel = new JPanel();
            workDirectoryPanel.setLayout(new BoxLayout(workDirectoryPanel, BoxLayout.X_AXIS));
            workDirectoryPanel.setBorder(BorderFactory.createTitledBorder(Translator
                    .getString("OptionDialog.DirectoryTab.WNDicDirectory")));
            workDirectoryPanel.add(wnDicDirField);
            workDirectoryPanel.add(browseWNDicDirButton);

            return workDirectoryPanel;
        }
    }
}
