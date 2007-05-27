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

    private void removeFiles(File dir) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }

    public void saveProject(File saveFile, DODDLEProject currentProject) {
        File saveDir = null;
        if (saveFile.isDirectory()) {
            saveDir = saveFile;
        } else {
            saveDir = new File(saveFile.getAbsolutePath() + ".dir");
            saveDir.mkdir();
        }
        OntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
        DocumentSelectionPanel docSelectionPanel = currentProject.getDocumentSelectionPanel();
        InputWordSelectionPanel inputWordSelectionPanel = currentProject.getInputWordSelectionPanel();
        ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();
        currentProject.setTitle(saveFile.getAbsolutePath());
        currentProject.setProjectName(saveFile.getAbsolutePath());
        ontSelectionPanel.saveGeneralOntologyInfo(new File(saveDir, ProjectFileNames.GENERAL_ONTOLOGY_INFO_FILE));
        File owlMetaDataSetDir = new File(saveDir, ProjectFileNames.OWL_META_DATA_SET_DIR);
        owlMetaDataSetDir.mkdir();
        removeFiles(owlMetaDataSetDir);
        ontSelectionPanel.saveOWLMetaDataSet(owlMetaDataSetDir);
        // docSelectionPanel.saveDocuments(saveDir); // ファイルの内容のコピーはしないようにした
        docSelectionPanel.saveDocumentInfo(saveDir);
        inputWordSelectionPanel.saveWordInfoTable(new File(saveDir, ProjectFileNames.WORD_INFO_TABLE_FILE), new File(
                saveDir, ProjectFileNames.REMOVED_WORD_INFO_TABLE_FILE));
        disambiguationPanel.saveInputWordSet(new File(saveDir, ProjectFileNames.INPUT_WORD_SET_FILE));
        disambiguationPanel.saveWordEvalConceptSet(new File(saveDir, ProjectFileNames.WORD_EVAL_CONCEPT_SET_FILE));
        disambiguationPanel.saveWordCorrespondConceptSetMap(new File(saveDir,
                ProjectFileNames.INPUT_WORD_CONCEPT_MAP_FILE));
        disambiguationPanel.saveConstructTreeOption(new File(saveDir, ProjectFileNames.CONSTRUCT_TREE_OPTION_FILE));
        disambiguationPanel.saveInputWordConstructTreeOptionSet(new File(saveDir,
                ProjectFileNames.INPUT_WORD_CONSTRUCT_TREE_OPTION_SET_FILE));
        disambiguationPanel.saveInputConceptSet(new File(saveDir, ProjectFileNames.INPUT_CONCEPT_SET_FILE));
        disambiguationPanel.saveUndefinedWordSet(new File(saveDir, ProjectFileNames.UNDEFINED_WORD_SET_FILE));
        doddle.saveOntology(currentProject, new File(saveDir, ProjectFileNames.ONTOLOGY_FILE));

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
        removeFiles(conceptDefinitionResultDir);
        conceptDefinitionPanel.saveWordSpaceResult(conceptDefinitionResultDir);
        conceptDefinitionResultDir = new File(saveDir, ProjectFileNames.APRIORI_RESULTS_DIR);
        conceptDefinitionResultDir.mkdir();
        removeFiles(conceptDefinitionResultDir);
        conceptDefinitionPanel.saveAprioriResult(conceptDefinitionResultDir);
        currentProject.addLog("SaveProjectAction");
        currentProject.saveLog(new File(saveDir, ProjectFileNames.LOG_FILE));
        if (!saveFile.isDirectory()) {
            zipProjectDir(saveFile, saveDir);
        }
        DODDLE.STATUS_BAR.setText(Translator.getTerm("SaveProjectDoneMessage") + " ----- "
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
                        if (tc == null) {
                            continue;
                        }
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
            buf.append(DODDLEConstants.BASE_URI + "\n");
            buf.append("利用可能な汎用辞書: " + ontSelectionPanel.getEnableDicList() + "\n");
            if (disambiguationPanel.getInputWordModelSet() != null) {
                buf.append("入力単語数: " + disambiguationPanel.getInputWordCnt() + "\n");
            }
            buf.append("完全照合単語数: " + disambiguationPanel.getPerfectlyMatchedWordCnt() + "\n");
            buf.append("システムが追加した完全照合単語数: " + disambiguationPanel.getSystemAddedPerfectlyMatchedWordCnt() + "\n");
            buf.append("部分照合単語数: " + disambiguationPanel.getPartiallyMatchedWordCnt() + "\n");
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

    private static final int EOF = -1;

    private void zipProjectDir(File saveFile, File saveDir) {
        ZipOutputStream zos = null;
        BufferedInputStream bis = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(saveFile));
            List<File> allFile = new ArrayList<File>();
            getAllProjectFile(saveDir, allFile);
            for (File file : allFile) {
                ZipEntry entry = new ZipEntry(file.getPath());
                zos.putNextEntry(entry);
                bis = new BufferedInputStream(new FileInputStream(file));
                int count;
                byte buf[] = new byte[1024];
                while ((count = bis.read(buf, 0, 104)) != EOF) {
                    zos.write(buf, 0, count);
                }
                bis.close();
                zos.closeEntry();
                file.delete();
            }
            File[] dirs = saveDir.listFiles();
            for (int i = 0; i < dirs.length; i++) {
                dirs[i].delete();
            }
            saveDir.delete();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (zos != null) {
                    zos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getAllProjectFile(File dir, List<File> allFile) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                getAllProjectFile(files[i], allFile);
            } else {
                allFile.add(files[i]);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEProject currentProject = DODDLE.getCurrentProject();
        if (currentProject == null) { return; }
        File saveFile = null;
        if (!currentProject.getTitle().equals(Translator.getTerm("NewProjectAction"))) {
            saveFile = new File(currentProject.getTitle());
        } else {
            JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int retval = chooser.showSaveDialog(DODDLE.rootPane);
            if (retval != JFileChooser.APPROVE_OPTION) { return; }
            saveFile = chooser.getSelectedFile();
        }
        saveProject(saveFile, currentProject);
    }
}
