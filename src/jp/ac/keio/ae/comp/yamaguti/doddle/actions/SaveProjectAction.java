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
        
        currentProject.getConstructClassPanel().getConceptDriftManagementPanel().saveTrimmedResultAnalysis(new File(
                saveDir, ProjectFileNames.CLASS_TRIMMED_RESULT_ANALYSIS_FILE));
        currentProject.getConstructPropertyPanel().getConceptDriftManagementPanel().saveTrimmedResultAnalysis(new File(saveDir, ProjectFileNames.PROPERTY_TRIMMED_RESULT_ANALYSIS_FILE));
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

            buf.append(Translator.getTerm("AvailableGeneralOntologiesMessage")+": " + ontSelectionPanel.getEnableDicList() + "\n");
            if (disambiguationPanel.getInputWordModelSet() != null) {
                buf.append(Translator.getTerm("InputWordCountMessage")+": " + disambiguationPanel.getInputWordCnt() + "\n");
            }
            buf.append(Translator.getTerm("PerfectlyMatchedWordCountMessage")+": " + disambiguationPanel.getPerfectlyMatchedWordCnt() + "\n");
            buf.append(Translator.getTerm("SystemAddedPerfectlyMatchedWordCountMessage")+": " + disambiguationPanel.getSystemAddedPerfectlyMatchedWordCnt() + "\n");
            buf.append(Translator.getTerm("PartiallyMatchedWordCountMessage")+": " + disambiguationPanel.getPartiallyMatchedWordCnt() + "\n");
            buf.append(Translator.getTerm("MatchedWordCountMessage")+": " + disambiguationPanel.getMatchedWordCnt() + "\n");
            buf.append(Translator.getTerm("UndefinedWordCountMessage")+": " + disambiguationPanel.getUndefinedWordCnt() + "\n");

            if (disambiguationPanel.getInputConceptSet() != null) {
                buf.append(Translator.getTerm("InputConceptCountMessage")+": " + disambiguationPanel.getInputConceptSet().size() + "\n");
            }
            if (disambiguationPanel.getInputNounConceptSet() != null) {
                buf.append(Translator.getTerm("InputNounConceptCountMessage")+": " + disambiguationPanel.getInputNounConceptSet().size() + "\n");
            }
            if (disambiguationPanel.getInputVerbConceptSet() != null) {
                buf.append(Translator.getTerm("InputVerbConceptCountMessage")+": " + disambiguationPanel.getInputVerbConceptSet().size() + "\n");
            }

            buf.append(Translator.getTerm("ClassSINCountMessage")+": " + constructClassPanel.getAddedSINNum() + "\n");
            buf.append(Translator.getTerm("BeforeTrimmingClassCountMessage")+": " + constructClassPanel.getBeforeTrimmingConceptNum() + "\n");
            buf.append(Translator.getTerm("TrimmedClassCountMessage")+": " + constructClassPanel.getTrimmedConceptNum() + "\n");
            int afterTrimmingConceptNum = constructClassPanel.getAfterTrimmingConceptNum();
            buf.append(Translator.getTerm("AfterTrimmingClassCountMessage")+": " + afterTrimmingConceptNum + "\n");
            
            buf.append(Translator.getTerm("PropertySINCountMessage")+": " + constructPropertyPanel.getAddedSINNum() + "\n");
            buf.append(Translator.getTerm("BeforeTrimmingPropertyCountMessage")+": " + constructPropertyPanel.getBeforeTrimmingConceptNum() + "\n");
            buf.append(Translator.getTerm("TrimmedPropertyCountMessage")+": " + constructPropertyPanel.getTrimmedConceptNum() + "\n");
            int afterTrimmingPropertyNum = constructPropertyPanel.getAfterTrimmingConceptNum();
            buf.append(Translator.getTerm("AfterTrimmingPropertyCountMessage")+": " + afterTrimmingPropertyNum + "\n");

            buf.append(Translator.getTerm("AbstractInternalClassCountMessage")+": " + constructClassPanel.getAddedAbstractComplexConceptCnt() + "\n");
            buf.append(Translator.getTerm("AverageAbstractSiblingConceptCountInClassesMessage")+": "
                    + constructClassPanel.getAverageAbstracComplexConceptGroupSiblingConceptCnt() + "\n");

            buf.append(Translator.getTerm("AbstractInternalPropertyCountMessage")+": " + constructPropertyPanel.getAddedAbstractComplexConceptCnt() + "\n");
            buf.append(Translator.getTerm("AverageAbstractSiblingConceptCountInPropertiesMessage")+": "
                    + constructPropertyPanel.getAverageAbstracComplexConceptGroupSiblingConceptCnt() + "\n");

            int lastClassNum = constructClassPanel.getAllConceptCnt();
            int lastPropertyNum = constructPropertyPanel.getAllConceptCnt();

            buf.append(Translator.getTerm("ClassFromComplexWordCountMessage")+": " + (lastClassNum - afterTrimmingConceptNum) + "\n");
            buf.append(Translator.getTerm("PropertyFromComplexWordCountMessage")+": " + (lastPropertyNum - afterTrimmingPropertyNum) + "\n");

            buf.append(Translator.getTerm("TotalClassCountMessage")+": " + lastClassNum + "\n");
            buf.append(Translator.getTerm("TotalPropertyCountMessage")+": " + lastPropertyNum + "\n");

            buf.append(Translator.getTerm("AverageSiblingClassesMessage")+": " + constructClassPanel.getChildCntAverage() + "\n");
            buf.append(Translator.getTerm("AverageSiblingPropertiesMessage")+": " + constructPropertyPanel.getChildCntAverage() + "\n");

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
