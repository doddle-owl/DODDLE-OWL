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

    private static JCheckBox showPrefixCheckBox;

    private JButton saveOptionButton;
    private JButton cancelButton;
    private DirectoryPanel directoryPanel;

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
        complexWordOptionPanel.setLayout(new GridLayout(2, 1, 5, 5));
        complexWordOptionPanel.add(complexWordSetSameConceptButton);
        complexWordOptionPanel.add(complexWordSetSubConceptButton);

        showPrefixCheckBox = new JCheckBox(Translator.getString("Component.Tool.Option.View.ShowPrefix"));
        showPrefixCheckBox.addActionListener(this);
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        viewPanel.add(showPrefixCheckBox, BorderLayout.CENTER);

        directoryPanel = new DirectoryPanel();

        JTabbedPane optionTab = new JTabbedPane();
        optionTab.add(automaticDisambiguationOptionPanel, Translator.getString("Component.Tool.Option.Disambiguation"));
        optionTab.add(complexWordOptionPanel, Translator.getString("Component.Tool.Option.ComplexWord"));
        optionTab.add(viewPanel, Translator.getString("Component.Tool.Option.View"));
        optionTab.add(directoryPanel, Translator.getString("Component.Tool.Option.Directory"));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(optionTab, BorderLayout.CENTER);

        saveOptionButton = new JButton(Translator.getString("Component.Tool.Option.SaveOption"));
        saveOptionButton.addActionListener(this);
        cancelButton = new JButton(Translator.getString("Close"));
        cancelButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveOptionButton);
        buttonPanel.add(cancelButton);
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BorderLayout());
        eastPanel.add(buttonPanel, BorderLayout.EAST);
        Container contentPane = getContentPane();
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(eastPanel, BorderLayout.SOUTH);

        loadOption(new File(ProjectFileNames.CONFIG_FILE));

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

    public static boolean isComplexWordSetSameConcept() {
        return complexWordSetSameConceptButton.isSelected();
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

    public static boolean isShowPrefix() {
        return showPrefixCheckBox.isSelected();
    }

    public void saveOption(File file) {
        BufferedWriter writer = null;
        try {
            Properties properties = new Properties();
            properties.setProperty("SEN_HOME", directoryPanel.getSenDicDir());
            properties.setProperty("DODDLE_DIC", directoryPanel.getEDRDicDir());
            properties.setProperty("DODDLE_EDRT_DIC", directoryPanel.getEDRTDicDir());
            properties.setProperty("PERL_EXE", directoryPanel.getPerlDir());
            properties.setProperty("CHASEN_EXE", directoryPanel.getChasenDir());
            if (DocumentSelectionPanel.Japanese_Morphological_Analyzer != null) {
                properties.setProperty("Japanese_Morphological_Analyzer",
                        DocumentSelectionPanel.Japanese_Morphological_Analyzer);
            } else {
                properties.setProperty("Japanese_Morphological_Analyzer","C:/Program Files/Mecab/bin/mecab.exe -Ochasen");
            }
            properties.setProperty("SSTAGGER_HOME", directoryPanel.getSSTaggerDir());
            properties.setProperty("XDOC2TXT_EXE", directoryPanel.getXdoc2txtDir());
            properties.setProperty("WORDNET_PATH", directoryPanel.getWNDicDir());

            properties.setProperty("AutomaticDisambiguation.useSiblingNodeCount", String
                    .valueOf(siblingDisambiguationCheckBox.isSelected()));
            properties.setProperty("AutomaticDisambiguation.useChildNodeCount", String
                    .valueOf(subDisambiguationCheckBox.isSelected()));
            properties.setProperty("AutomaticDisambiguation.usePathToRootNodeCount", String
                    .valueOf(supDisambiguationCheckBox.isSelected()));

            properties.setProperty("AutomaticDisambiguation.isUsingSpreadActivationAlgorithm", String
                    .valueOf(isUsingSpreadActivatingAlgorithm()));
            properties.setProperty("AutomaticDisambiguation.isCheckShortestSpreadActivation", String
                    .valueOf(isCheckShortestSpreadActivation()));
            properties.setProperty("AutomaticDisambiguation.isCheckLongestSpreadActivation", String
                    .valueOf(isCheckLongestSpreadActivation()));
            properties.setProperty("AutomaticDisambiguation.isCheckAverageSpreadActivation", String
                    .valueOf(isCheckAverageSpreadActivation()));

            String isSameConceptOrSubConcept = "";
            if (complexWordSetSameConceptButton.isSelected()) {
                isSameConceptOrSubConcept = "SAME";
            } else {
                isSameConceptOrSubConcept = "SUB";
            }
            properties.setProperty("MakeConceptTreeWithComplexWord.isSameConceptOrSubConcept",
                    isSameConceptOrSubConcept);

            properties.setProperty("DisplayPrefix", String.valueOf(showPrefixCheckBox.isSelected()));

            // 以下はオプションダイアログでの設定は未実装
            properties.setProperty("BASE_URI", DODDLE.BASE_URI);
            properties.setProperty("BASE_PREFIX", DODDLE.BASE_PREFIX);
            properties.setProperty("PROJECT_DIR", DODDLE.PROJECT_DIR);
            properties.setProperty("UPPER_CONCEPT_LIST", UpperConceptManager.UPPER_CONCEPT_LIST);
            properties.setProperty("USING_DB", String.valueOf(DODDLE.IS_USING_DB));
            properties.setProperty("LANG", DODDLE.LANG);

            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            properties.store(writer, "DODDLE-OWL Option");
        } catch (IOException ioe) {
            ioe.printStackTrace();
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

    public void loadOption(File file) {
        if (!file.exists()) { return; }
        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            Properties properties = new Properties();
            properties.load(reader);

            boolean t = new Boolean(properties.getProperty("AutomaticDisambiguation.useSiblingNodeCount"));
            siblingDisambiguationCheckBox.setSelected(t);
            t = new Boolean(properties.getProperty("AutomaticDisambiguation.useChildNodeCount"));
            subDisambiguationCheckBox.setSelected(t);
            t = new Boolean(properties.getProperty("AutomaticDisambiguation.usePathToRootNodeCount"));
            supDisambiguationCheckBox.setSelected(t);

            t = new Boolean(properties.getProperty("AutomaticDisambiguation.isUsingSpreadActivationAlgorithm"));
            isUsingSpreadActivatingAlgorithmForDisambiguationBox.setSelected(t);
            t = new Boolean(properties.getProperty("AutomaticDisambiguation.isCheckShortestSpreadActivation"));
            shortestSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);
            t = new Boolean(properties.getProperty("AutomaticDisambiguation.isCheckLongestSpreadActivation"));
            longestSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);
            t = new Boolean(properties.getProperty("AutomaticDisambiguation.isCheckAverageSpreadActivation"));
            averageSpreadActivatingAlgorithmForDisambiguationButton.setSelected(t);

            String isSameConceptOrSubConcept = properties
                    .getProperty("MakeConceptTreeWithComplexWord.isSameConceptOrSubConcept");
            if (isSameConceptOrSubConcept != null) {
                complexWordSetSameConceptButton.setSelected(isSameConceptOrSubConcept.equals("SAME"));
                complexWordSetSubConceptButton.setSelected(isSameConceptOrSubConcept.equals("SUB"));
            }

            t = new Boolean(properties.getProperty("DisplayPrefix"));
            showPrefixCheckBox.setSelected(t);
        } catch (IOException ioe) {
            ioe.printStackTrace();
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

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveOptionButton) {
            saveOption(new File(ProjectFileNames.CONFIG_FILE));
        } else if (e.getSource() == cancelButton) {
            setVisible(false);
        } else if (e.getSource() == isUsingSpreadActivatingAlgorithmForDisambiguationBox) {
            boolean t = isUsingSpreadActivatingAlgorithmForDisambiguationBox.isSelected();
            shortestSpreadActivatingAlgorithmForDisambiguationButton.setEnabled(t);
            longestSpreadActivatingAlgorithmForDisambiguationButton.setEnabled(t);
            averageSpreadActivatingAlgorithmForDisambiguationButton.setEnabled(t);
        } else if (e.getSource() == showPrefixCheckBox) {
            DODDLE.getCurrentProject().getConstructClassPanel().getConceptTree().updateUI();
            DODDLE.getCurrentProject().getConstructPropertyPanel().getConceptTree().updateUI();
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

        public String getChasenDir() {
            return chasenDirField.getText();
        }

        public String getSenDicDir() {
            return senDicDirField.getText();
        }

        public String getPerlDir() {
            return perlDirField.getText();
        }

        public String getXdoc2txtDir() {
            return xdoc2txtDirField.getText();
        }

        public String getSSTaggerDir() {
            return ssTaggerDirField.getText();
        }

        public String getEDRDicDir() {
            return edrDicDirField.getText();
        }

        public String getEDRTDicDir() {
            return edrtDicDirField.getText();
        }

        public String getWNDicDir() {
            return wnDicDirField.getText();
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
