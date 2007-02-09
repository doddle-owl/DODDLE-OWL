/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class SaveProjectAction extends AbstractAction {

    private String title;
    private DODDLE doddle;

    public String getTitle() {
        return title;
    }

    public SaveProjectAction(String title, DODDLE ddl) {
        super(title, Utils.getImageIcon("save.gif"));
        this.title = title;
        doddle = ddl;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
    }

    public SaveProjectAction(String title, ImageIcon icon, DODDLE ddl) {
        super(title, icon);
        this.title = title;
        doddle = ddl;
    }

    public void saveProject(File saveDir, DODDLEProject currentProject) {
        OntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
        DocumentSelectionPanel docSelectionPanel = currentProject.getDocumentSelectionPanel();
        InputWordSelectionPanel inputWordSelectionPanel = currentProject.getInputWordSelectionPanel();
        ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();
        currentProject.setTitle(saveDir.getAbsolutePath());
        currentProject.setProjectName(saveDir.getAbsolutePath());
        saveDir.mkdir();
        ontSelectionPanel.saveGeneralOntologyInfo(new File(saveDir, ProjectFileNames.GENERAL_ONTOLOGY_INFO_FILE));
        ontSelectionPanel.saveOWLOntologySet(new File(saveDir, ProjectFileNames.OWL_ONTOLOGY_INFO_FILE));
        docSelectionPanel.saveDocuments(saveDir);
        inputWordSelectionPanel.saveWordInfoTable(new File(saveDir, ProjectFileNames.WORD_INFO_TABLE_FILE), new File(
                saveDir, ProjectFileNames.REMOVED_WORD_INFO_TABLE_FILE));
        disambiguationPanel.saveInputWordSet(new File(saveDir, ProjectFileNames.INPUT_WORD_SET_FILE));
        disambiguationPanel.saveWordEvalConceptSet(new File(saveDir, ProjectFileNames.WORD_EVAL_CONCEPT_SET_FILE));
        disambiguationPanel.saveWordCorrespondConceptSetMap(new File(saveDir, ProjectFileNames.INPUT_WORD_CONCEPT_MAP_FILE));
        disambiguationPanel.saveConstructTreeOption(new File(saveDir, ProjectFileNames.CONSTRUCT_TREE_OPTION_FILE));
        disambiguationPanel.saveInputWordConstructTreeOptionSet(new File(saveDir,
                ProjectFileNames.INPUT_WORD_CONSTRUCT_TREE_OPTION_SET_FILE));
        disambiguationPanel.saveInputConceptSet(new File(saveDir, ProjectFileNames.INPUT_CONCEPT_SET_FILE));
        disambiguationPanel.saveUndefinedWordSet(new File(saveDir, ProjectFileNames.UNDEFINED_WORD_SET_FILE));
        doddle.saveOntology(currentProject, new File(saveDir, ProjectFileNames.ONTOLOGY_FILE));
        doddle.saveConceptTypicalWord(currentProject, new File(saveDir, ProjectFileNames.CONCEPT_TYPICAL_WORD_MAP_FILE));

        saveTrimmedResultAnalysis(currentProject.getConstructClassPanel().getConceptDriftManagementPanel(), new File(
                saveDir, ProjectFileNames.CLASS_TRIMMED_RESULT_ANALYSIS_FILE));
        saveTrimmedResultAnalysis(currentProject.getConstructPropertyPanel().getConceptDriftManagementPanel(),
                new File(saveDir, ProjectFileNames.PROPERTY_TRIMMED_RESULT_ANALYSIS_FILE));
        saveProjectInfo(currentProject, new File(saveDir, ProjectFileNames.PROJECT_INFO_FILE));
        conceptDefinitionPanel.saveConeptDefinitionParameters(new File(saveDir,
                ProjectFileNames.CONCEPT_DEFINITION_PARAMETERS_FILE));
        conceptDefinitionPanel.saveConceptDefinition(new File(saveDir, ProjectFileNames.CONCEPT_DEFINITION_FILE));
        conceptDefinitionPanel.saveWrongPairSet(new File(saveDir, ProjectFileNames.WRONG_PAIR_SET_FILE));
        File conceptDefinitionResultDir = new File(saveDir, ProjectFileNames.WORDSPACE_RESULTS_DIR);
        conceptDefinitionResultDir.mkdir();
        conceptDefinitionPanel.saveWordSpaceResult(conceptDefinitionResultDir);
        conceptDefinitionResultDir = new File(saveDir, ProjectFileNames.APRIORI_RESULTS_DIR);
        conceptDefinitionResultDir.mkdir();
        conceptDefinitionPanel.saveAprioriResult(conceptDefinitionResultDir);
        // zipProjectDir(saveDir);
        DODDLE.STATUS_BAR.setText(Translator.getString("StatusBar.Message.SaveProjectDone") + " ----- "
                + java.util.Calendar.getInstance().getTime() + ": " + currentProject.getTitle());
    }

    public void saveTrimmedResultAnalysis(ConceptDriftManagementPanel conceptDriftManagementPanel, File file) {
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            StringBuilder builder = new StringBuilder();

            List<ConceptTreeNode> conceptTreeNodeList = conceptDriftManagementPanel.getTRAResult();
            for (ConceptTreeNode traNode : conceptTreeNodeList) {
                builder.append(traNode.getConcept().getURI());
                builder.append(",");
                ConceptTreeNode parentNode = (ConceptTreeNode) traNode.getParent();
                builder.append(parentNode.getConcept().getURI());
                builder.append(",");
                List<List<Concept>> trimmedConceptList = traNode.getTrimmedConceptList();
                for (List<Concept> list : trimmedConceptList) {
                    builder.append("|");
                    for (Concept tc : list) {
                        builder.append(tc.getURI());
                        builder.append(",");
                    }
                }
                builder.append("\n");
            }
            writer.write(builder.toString());
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void saveProjectInfo(DODDLEProject currentProject, File file) {
        OntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));

            StringBuffer buf = new StringBuffer();
            buf.append(DODDLE.BASE_URI + "\n");
            buf.append("利用可能な汎用辞書: " + ontSelectionPanel.getEnableDicList() + "\n");
            if (disambiguationPanel.getInputWordModelSet() != null) {
                buf.append("入力単語数: " + disambiguationPanel.getInputWordCnt() + "\n");
            }
            buf.append("完全照合単語数: " + disambiguationPanel.getPerfectMatchedWordCnt() + "\n");
            buf.append("システムが追加した完全照合単語数: " + disambiguationPanel.getSystemAddedPerfectMatchedWordCnt() + "\n");
            buf.append("部分照合単語数: " + disambiguationPanel.getPartialMatchedWordCnt() + "\n");
            buf.append("照合単語数: " + disambiguationPanel.getMatchedWordCnt() + "\n");
            buf.append("未照合単語数: " + disambiguationPanel.getUndefinedWordCnt() + "\n");

            if (disambiguationPanel.getInputConceptSet() != null) {
                buf.append("入力概念数: " + disambiguationPanel.getInputConceptSet().size() + "\n");
            }
            if (disambiguationPanel.getInputNounConceptSet() != null) {
                buf.append("入力名詞的概念数: " + disambiguationPanel.getInputNounConceptSet().size() + "\n");
            }
            if (disambiguationPanel.getInputVerbConceptSet() != null) {
                buf.append("入力動詞的概念数: " + disambiguationPanel.getInputVerbConceptSet().size() + "\n");
            }

            buf.append("クラス階層構築における追加SIN数: " + constructClassPanel.getAddedSINNum() + "\n");
            buf.append("剪定前クラス数: " + constructClassPanel.getBeforeTrimmingConceptNum() + "\n");
            buf.append("剪定クラス数: " + constructClassPanel.getTrimmedConceptNum() + "\n");
            int afterTrimmingConceptNum = constructClassPanel.getAfterTrimmingConceptNum();
            buf.append("剪定後クラス数: " + afterTrimmingConceptNum + "\n");

            buf.append("プロパティ階層構築における追加SIN数: " + constructPropertyPanel.getAddedSINNum() + "\n");
            buf.append("剪定前プロパティ数: " + constructPropertyPanel.getBeforeTrimmingConceptNum() + "\n");
            buf.append("剪定プロパティ数: " + constructPropertyPanel.getTrimmedConceptNum() + "\n");
            int afterTrimmingPropertyNum = constructPropertyPanel.getAfterTrimmingConceptNum();
            buf.append("剪定後プロパティ数: " + afterTrimmingPropertyNum + "\n");

            buf.append("追加抽象中間クラス数: " + constructClassPanel.getAddedAbstractComplexConceptCnt() + "\n");
            buf.append("抽象中間クラスの平均兄弟クラスグループ化数: "
                    + constructClassPanel.getAverageAbstracComplexConceptGroupSiblingConceptCnt() + "\n");

            buf.append("追加抽象中間プロパティ数: " + constructPropertyPanel.getAddedAbstractComplexConceptCnt() + "\n");
            buf.append("抽象中間プロパティの平均兄弟プロパティグループ化数: "
                    + constructPropertyPanel.getAverageAbstracComplexConceptGroupSiblingConceptCnt() + "\n");

            int lastClassNum = constructClassPanel.getAllConceptCnt();
            int lastPropertyNum = constructPropertyPanel.getAllConceptCnt();

            buf.append("複合語クラス数: " + (lastClassNum - afterTrimmingConceptNum) + "\n");
            buf.append("複合語プロパティ数: " + (lastPropertyNum - afterTrimmingPropertyNum) + "\n");

            buf.append("最終クラス数: " + lastClassNum + "\n");
            buf.append("最終プロパティ数: " + lastPropertyNum + "\n");

            buf.append("平均兄弟クラス数: " + constructClassPanel.getChildCntAverage() + "\n");
            buf.append("平均兄弟プロパティ数: " + constructPropertyPanel.getChildCntAverage() + "\n");

            writer.write(buf.toString());
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
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

    // projectフォルダを圧縮する処理
    // 意外と面倒なので，後回し
    private void zipProjectDir(File saveDir) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(saveDir.getAbsolutePath() + ".ddl"));
            File[] files = saveDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                ZipEntry entry = new ZipEntry(files[i].getName());
                zos.putNextEntry(entry);
            }
            zos.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEProject currentProject = DODDLE.getCurrentProject();
        if (currentProject == null) { return; }
        File saveDir = null;
        if (!currentProject.getTitle().equals(Translator.getString("Component.File.NewProject.Text"))) {
            saveDir = new File(currentProject.getTitle());
        } else {
            JFileChooser chooser = new JFileChooser(DODDLE.PROJECT_DIR);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int retval = chooser.showSaveDialog(DODDLE.rootPane);
            if (retval != JFileChooser.APPROVE_OPTION) { return; }
            saveDir = chooser.getSelectedFile();
        }
        saveProject(saveDir, currentProject);
    }
}
