/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.beans.*;
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
public class OpenProjectAction extends AbstractAction {

    private String title;
    private File openFile;
    private DODDLE doddle;
    private DODDLEProject newProject;

    public OpenProjectAction(String title, DODDLE ddl) {
        super(title, Utils.getImageIcon("open.gif"));
        this.title = title;
        doddle = ddl;
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
    }

    public String getTitle() {
        return title;
    }

    protected static final int EOF = -1;

    public static void getEntry(ZipFile zipFile, ZipEntry target) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            File file = new File(target.getName());
            if (target.isDirectory()) {
                file.mkdirs();
            } else {
                bis = new BufferedInputStream(zipFile.getInputStream(target));
                String parentName;
                if ((parentName = file.getParent()) != null) {
                    File dir = new File(parentName);
                    dir.mkdirs();
                }
                bos = new BufferedOutputStream(new FileOutputStream(file));
                int c;
                while ((c = bis.read()) != EOF) {
                    bos.write((byte) c);
                }
                bis.close();
                bos.close();
            }
        } catch (ZipException ze) {
            ze.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private File unzipProjectDir(File openFile) {
        BufferedInputStream bis = null;
        File openDir = null;
        try {
            ZipFile projectFile = new ZipFile(openFile);
            ZipEntry entry = null;
            for (Enumeration enumeration = projectFile.entries(); enumeration.hasMoreElements();) {
                entry = (ZipEntry) enumeration.nextElement();
                getEntry(projectFile, entry);
            }
            openDir = new File(entry.getName()).getParentFile();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return openDir;
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

    class OpenProjectWorker extends SwingWorker implements PropertyChangeListener {

        private int taskCnt;
        private int currentTaskCnt;
        
        public OpenProjectWorker(int taskCnt) {
            this.taskCnt = taskCnt;
            currentTaskCnt = 1;
            addPropertyChangeListener(this);                
        }
        
        public String doInBackground() {
            while (!newProject.isInitialized()) {
                try {
                    Thread.sleep(1000);
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            newProject.setVisible(false);
            DODDLE.STATUS_BAR.setLastMessage(title);
            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(taskCnt);
            DODDLE.STATUS_BAR.lock();
            
            setProgress(currentTaskCnt++);
            try {
                DODDLEProject currentProject = newProject;
                                                
                OntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
                DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
                DocumentSelectionPanel docSelectionPanelI = currentProject.getDocumentSelectionPanel();
                InputWordSelectionPanel inputWordSelectionPanel = currentProject.getInputWordSelectionPanel();
                ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();
                File openDir = null;
                if (openFile.isDirectory()) {
                    openDir = openFile;
                } else {
                    openDir = unzipProjectDir(openFile);
                }
                currentProject.loadLog(new File(openDir, ProjectFileNames.LOG_FILE));
                currentProject.addLog("OpenProjectAction");
                setProgress(currentTaskCnt++);
                doddle.loadBaseURI(new File(openDir, ProjectFileNames.PROJECT_INFO_FILE));
                setProgress(currentTaskCnt++);
                docSelectionPanelI.loadDocuments(openDir);
                setProgress(currentTaskCnt++);
                ontSelectionPanel.loadGeneralOntologyInfo(new File(openDir, ProjectFileNames.GENERAL_ONTOLOGY_INFO_FILE));
                setProgress(currentTaskCnt++);
                ontSelectionPanel.loadOWLMetaDataSet(new File(openDir, ProjectFileNames.OWL_META_DATA_SET_DIR));
                setProgress(currentTaskCnt++);
                inputWordSelectionPanel.loadWordInfoTable(new File(openDir, ProjectFileNames.WORD_INFO_TABLE_FILE),
                        new File(openDir, ProjectFileNames.REMOVED_WORD_INFO_TABLE_FILE));
                setProgress(currentTaskCnt++);
                File inputWordSetFile = new File(openDir, ProjectFileNames.INPUT_WORD_SET_FILE);
                disambiguationPanel.loadInputWordSet(inputWordSetFile);
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadWordEvalConceptSet(new File(openDir, ProjectFileNames.WORD_EVAL_CONCEPT_SET_FILE));
                setProgress(currentTaskCnt++);
                if (inputWordSetFile.exists()) {
                    disambiguationPanel.loadWordCorrespondConceptSetMap(new File(openDir,
                            ProjectFileNames.INPUT_WORD_CONCEPT_MAP_FILE));                    
                }
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadConstructTreeOption(new File(openDir, ProjectFileNames.CONSTRUCT_TREE_OPTION_FILE));
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadInputWordConstructTreeOptionSet(new File(openDir,
                        ProjectFileNames.INPUT_WORD_CONSTRUCT_TREE_OPTION_SET_FILE));
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadInputConceptSet(new File(openDir, ProjectFileNames.INPUT_CONCEPT_SET_FILE));
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadUndefinedWordSet(new File(openDir, ProjectFileNames.UNDEFINED_WORD_SET_FILE));
                setProgress(currentTaskCnt++);
                doddle.loadOntology(currentProject, new File(openDir, ProjectFileNames.ONTOLOGY_FILE));
                setProgress(currentTaskCnt++);

                ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
                ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
                constructClassPanel.getConceptDriftManagementPanel().loadTrimmedResultAnalysis(new File(openDir,
                        ProjectFileNames.CLASS_TRIMMED_RESULT_ANALYSIS_FILE));
                constructPropertyPanel.getConceptDriftManagementPanel().loadTrimmedResultAnalysis(new File(openDir,
                        ProjectFileNames.PROPERTY_TRIMMED_RESULT_ANALYSIS_FILE));

                conceptDefinitionPanel.setInputDocList();
                conceptDefinitionPanel.loadConceptDefinitionParameters(new File(openDir,
                        ProjectFileNames.CONCEPT_DEFINITION_PARAMETERS_FILE));
                setProgress(currentTaskCnt++);
                File conceptDefinitionResultDir = new File(openDir, ProjectFileNames.WORDSPACE_RESULTS_DIR);
                conceptDefinitionResultDir.mkdir();
                conceptDefinitionPanel.loadWordSpaceResult(conceptDefinitionResultDir);
                setProgress(currentTaskCnt++);
                conceptDefinitionResultDir = new File(openDir, ProjectFileNames.APRIORI_RESULTS_DIR);
                conceptDefinitionResultDir.mkdir();
                conceptDefinitionPanel.loadAprioriResult(conceptDefinitionResultDir);
                setProgress(currentTaskCnt++);

                conceptDefinitionPanel.loadConceptDefinition(new File(openDir, ProjectFileNames.CONCEPT_DEFINITION_FILE));
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.loadWrongPairSet(new File(openDir, ProjectFileNames.WRONG_PAIR_SET_FILE));
                setProgress(currentTaskCnt++);

                disambiguationPanel.selectTopList();
                constructClassPanel.expandIsaTree();
                constructClassPanel.expandHasaTree();
                constructPropertyPanel.expandIsaTree();

                if (!openFile.isDirectory()) {
                    List<File> allFile = new ArrayList<File>();
                    getAllProjectFile(openDir, allFile);
                    for (File file : allFile) {
                        file.delete();
                    }
                    File[] dirs = openDir.listFiles();
                    for (int i = 0; i < dirs.length; i++) {
                        dirs[i].delete();
                    }
                    openDir.delete();
                }
                setProgress(currentTaskCnt++);                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {                
                newProject.setVisible(true);
                try {
                    newProject.setMaximum(true);
                } catch(PropertyVetoException pve) {
                    pve.printStackTrace();
                }
                DODDLE.STATUS_BAR.unLock();
                DODDLE.STATUS_BAR.hideProgressBar();
            }            
            return "done";
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof Integer) {
                DODDLE.STATUS_BAR.setValue(currentTaskCnt);
            }            
        }
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return; }
        openFile = chooser.getSelectedFile();        
        newProject = new DODDLEProject(openFile.getAbsolutePath());
        OpenProjectWorker worker = new OpenProjectWorker(22);
        DODDLE.STATUS_BAR.setSwingWorker(worker);        
        worker.execute();
    }
}
