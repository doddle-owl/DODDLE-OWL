package jp.ac.keio.ae.comp.yamaguti.doddle;

import gnu.getopt.*;

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.actions.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.didion.jwnl.dictionary.*;
import net.didion.jwnl.dictionary.file_manager.*;
import net.didion.jwnl.princeton.data.*;

import org.apache.log4j.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class DODDLE extends JFrame {

    public static boolean IS_USING_DB;
    public static DODDLEPlugin doddlePlugin;
    public static JDesktopPane desktop;

    public static JMenu projectMenu;

    private OptionDialog optionDialog;

    public static StatusBarPanel STATUS_BAR;
    public static int DIVIDER_SIZE = 10;

    public static final String VERSION = "2006-02-24";

    public static final int INPUT_WORD_SELECTION_PANEL = 1;
    public static final int INPUT_MODULE = 2;
    public static final int TAXONOMIC_PANEL = 3;
    public static String BASE_URI = "http://www.yamaguti.comp.ae.keio.ac.jp/doddle-j#";
    public static String EDR_URI = "http://www2.nict.go.jp/kk/e416/EDR#";
    public static String OLD_EDR_URI = "http://www2.nict.go.jp/kk/e416/EDR/";
    public static String WN_URI = "http://wordnet.princeton.edu/wn/2.0#";

    public static String DODDLE_DIC = "./DODDLE_DIR/";
    public static String PROJECT_DIR = "./project/";
    private static final String RESOURCES = "jp/ac/keio/ae/comp/yamaguti/doddle/resources/";
    public static String JWNL_PROPERTIES_FILE = RESOURCES + "file_properties.xml";
    public static String WORDNET_PATH = "C:/program files/wordnet/2.0/dict";

    public static JRootPane rootPane;

    private NewProjectAction newProjectAction;
    private OpenProjectAction openProjectAction;
    private SaveProjectAction saveProjectAction;
    private SaveProjectAsAction saveProjectAsAction;

    public DODDLE() {
        EDRDic.init();
        rootPane = getRootPane();
        desktop = new JDesktopPane();
        optionDialog = new OptionDialog(this);
        STATUS_BAR = new StatusBarPanel();

        Container contentPane = getContentPane();
        makeActions();
        makeMenuBar();
        contentPane.add(getToolBar(), BorderLayout.NORTH);
        contentPane.add(desktop, BorderLayout.CENTER);
        contentPane.add(STATUS_BAR, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocation(50, 50);
        setTitle("DODDLE - �o�[�W����: " + VERSION);
        setVisible(true);
    }

    public DODDLEPlugin getDODDLEPlugin() {
        return doddlePlugin;
    }

    public OptionDialog getOptionDialog() {
        return optionDialog;
    }

    public void exit() {
        int messageType = JOptionPane.showConfirmDialog(rootPane, "DODDLE���I�����܂����H");
        if (messageType == JOptionPane.YES_OPTION) {
            EDRDic.closeDB();
            if (doddlePlugin == null) {
                System.exit(0);
            } else {
                dispose();
            }
        }
    }

    public static DODDLEProject newProject() {
        DODDLEProject project = new DODDLEProject("�V�K�v���W�F�N�g", projectMenu);
        try {
            desktop.add(project);
            project.toFront();
            desktop.setSelectedFrame(project);
            project.setVisible(true);
            project.setMaximum(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }

    private void makeActions() {
        newProjectAction = new NewProjectAction("�V�K�v���W�F�N�g");
        openProjectAction = new OpenProjectAction("�v���W�F�N�g���J��", this);
        saveProjectAction = new SaveProjectAction("�v���W�F�N�g���㏑���ۑ�", this);
        saveProjectAsAction = new SaveProjectAsAction("�v���W�F�N�g�𖼑O�����ĕۑ�", this);
    }

    private void makeMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("�t�@�C��");
        fileMenu.add(newProjectAction);
        fileMenu.addSeparator();
        fileMenu.add(openProjectAction);
        JMenu loadMenu = new JMenu("����");
        loadMenu.add(new LoadWordInfoTableAction("���͒P��e�[�u���𕜌�"));
        loadMenu.add(new LoadWordEvalConceptSetAction("���`���������ʂ𕜌�"));
        loadMenu.add(new LoadWordIDMapAction("�P���ID�̑Ή��𕜌�"));
        loadMenu.add(new LoadOntologyAction("�I���g���W�[�𕜌�", this));
        loadMenu.add(new LoadIDTypicalWordAction("ID�Ƒ�\���o���̑Ή��𕜌�", this));
        fileMenu.add(loadMenu);
        fileMenu.addSeparator();
        fileMenu.add(saveProjectAction);
        fileMenu.add(saveProjectAsAction);
        JMenu saveMenu = new JMenu("�ۑ�");
        // saveMenu.add(new SaveMatchedWordList("�����ɍڂ��Ă������͌�b��ۑ�"));
        saveMenu.add(new SaveWordInfoTableAction("���͒P��e�[�u����ۑ�"));
        saveMenu.add(new SaveWordEvalConceptSetAction("���`���������ʂ�ۑ�"));
        saveMenu.add(new SaveWordIDMapAction("�P���ID�̑Ή���ۑ�"));
        saveMenu.add(new SaveCompleteMatchWordAction("���S�ƍ��P�ꃊ�X�g��ۑ�"));
        saveMenu.add(new SaveCompleteMatchWordWithComplexWordAction("���S�ƍ��P�ꃊ�X�g�ƑΉ����镡�����ۑ�"));
        saveMenu.add(new SaveOntologyAction("�I���g���W�[��ۑ�", this));
        saveMenu.add(new SaveIDTypicalWordAction("ID�Ƒ�\���o���̑Ή���ۑ�", this));
        fileMenu.add(saveMenu);
        fileMenu.addSeparator();
        fileMenu.add(new ExitAction("�I��", this));
        menuBar.add(fileMenu);

        JMenu toolMenu = new JMenu("�c�[��");
        toolMenu.add(new OpenWordListAction("�P�ꃊ�X�g���J��"));
        toolMenu.addSeparator();
        toolMenu.add(new ShowAllWordsAction("���ׂĂ̒P���\��"));
        toolMenu.addSeparator();
        toolMenu.add(new AutomaticDisAmbiguationAction("���`������"));
        toolMenu.addSeparator();
        toolMenu.add(new ConstructNounTreeAction("�T�O�K�w�\�z�i�����j"));
        toolMenu.add(new ConstructNounAndVerbTreeAction("�T�O�K�w�\�z�i��������ѓ����j"));
        toolMenu.addSeparator();
        if (doddlePlugin != null) {
            toolMenu.add(new VisualizeAction("���o��", doddlePlugin));
            toolMenu.addSeparator();
        }
        toolMenu.add(new OptionAction("�I�v�V����", this));
        menuBar.add(toolMenu);

        projectMenu = new JMenu("�v���W�F�N�g");
        menuBar.add(projectMenu);

        JMenu helpMenu = new JMenu("�w���v");
        JMenuItem versionItem = new JMenuItem("�o�[�W����");
        versionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SplashWindow();
            }
        });
        helpMenu.add(versionItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JToolBar getToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.add(newProjectAction).setToolTipText(newProjectAction.getTitle());
        toolBar.add(openProjectAction).setToolTipText(openProjectAction.getTitle());
        toolBar.add(saveProjectAction).setToolTipText(saveProjectAction.getTitle());
        toolBar.add(saveProjectAsAction).setToolTipText(saveProjectAsAction.getTitle());
        return toolBar;
    }

    public static DODDLEProject getCurrentProject() {
        DODDLEProject currentProject = (DODDLEProject) desktop.getSelectedFrame();
        if (currentProject == null) {
            currentProject = newProject();
        }
        return currentProject;
    }

    public void saveProject(File saveDir, DODDLEProject currentProject) {
        InputModuleUI inputModuleUI = currentProject.getInputModuleUI();
        DocumentSelectionPanel docSelectionPanel = currentProject.getDocumentSelectionPanel();
        InputWordSelectionPanel inputWordSelectionPanel = currentProject.getInputWordSelectionPanel();
        currentProject.setTitle(saveDir.getAbsolutePath());
        currentProject.setProjectName(saveDir.getAbsolutePath());
        saveDir.mkdir();
        optionDialog.saveOption(new File(saveDir, "option.txt"));
        docSelectionPanel.saveDocuments(saveDir);
        inputWordSelectionPanel.saveWordInfoTable(new File(saveDir, "WordInfoTable.txt"));
        inputModuleUI.saveInputWordSet(new File(saveDir, "InputWordSet.txt"));
        inputModuleUI.saveWordEvalConceptSet(new File(saveDir, "wordEvalConceptSet.txt"));
        inputModuleUI.saveWordConceptMap(new File(saveDir, "InputWord_ID.txt"));
        inputModuleUI.saveConstructTreeOptionSet(new File(saveDir, "InputWord_ConstructTreeOption.txt"));
        inputModuleUI.saveInputConceptSet(new File(saveDir, "InputIDSet.txt"));
        inputModuleUI.saveUndefinedWordSet(new File(saveDir, "UndefinedWordSet.txt"));
        saveOntology(currentProject, new File(saveDir, "Ontology.owl"));
        saveIDTypicalWord(currentProject, new File(saveDir, "ID_TypicalWord.txt"));
        saveProjectInfo(currentProject, new File(saveDir, "projectInfo.txt"));
        STATUS_BAR
                .setText(java.util.Calendar.getInstance().getTime() + ": " + currentProject.getTitle() + "�v���W�F�N�g��ۑ��D");
    }

    public void loadIDTypicalWord(DODDLEProject currentProject, File file) {
        ConstructConceptTreePanel constructConceptTreePanel = currentProject.getConstructConceptTreePanel();
        ConstructPropertyTreePanel constructConceptDefinitionPanel = currentProject.getConstructPropertyTreePanel();

        if (!file.exists()) { return; }
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            String line = "";
            Map<String, String> idTypicalWordMap = new HashMap<String, String>();
            while ((line = reader.readLine()) != null) {
                String[] idInputWord = line.replaceAll("\n", "").split("\t");
                if (idInputWord.length == 2) {
                    idTypicalWordMap.put(idInputWord[0], idInputWord[1]);
                }
            }
            constructConceptTreePanel.loadIDTypicalWord(idTypicalWordMap);
            if (OptionDialog.isNounAndVerbConceptHierarchyConstructionMode()) {
                constructConceptDefinitionPanel.loadIDTypicalWord(idTypicalWordMap);
            }
            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveProjectInfo(DODDLEProject currentProject, File file) {
        InputModuleUI inputModuleUI = currentProject.getInputModuleUI();
        ConstructConceptTreePanel constructConceptTreePanel = currentProject.getConstructConceptTreePanel();
        ConstructPropertyTreePanel constructConceptDefinitionPanel = currentProject.getConstructPropertyTreePanel();

        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            StringBuffer buf = new StringBuffer();
            buf.append(BASE_URI + "\n");
            if (inputModuleUI.getInputWordModelSet() != null) {
                buf.append("���͒P�ꐔ: " + inputModuleUI.getInputWordModelSet().size() + "\n");
            }
            if (inputModuleUI.getInputConceptSet() != null) {
                buf.append("���͊T�O��: " + inputModuleUI.getInputConceptSet().size() + "\n");
            }
            buf.append("�N���X��: " + constructConceptTreePanel.getAllConceptID().size() + "\n");
            buf.append("�v���p�e�B��: " + constructConceptDefinitionPanel.getAllConceptID().size() + "\n");
            writer.write(buf.toString());
            writer.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveIDTypicalWord(DODDLEProject currentProject, File file) {
        ConstructConceptTreePanel constructConceptTreePanel = currentProject.getConstructConceptTreePanel();
        ConstructPropertyTreePanel constructConceptDefinitionPanel = currentProject.getConstructPropertyTreePanel();

        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            Map idTypicalWordMap = constructConceptTreePanel.getIDTypicalWordMap();
            idTypicalWordMap.putAll(constructConceptDefinitionPanel.getIDTypicalWordMap());
            StringBuffer buf = new StringBuffer();
            for (Iterator i = idTypicalWordMap.keySet().iterator(); i.hasNext();) {
                String id = (String) i.next();
                String typicalWord = (String) idTypicalWordMap.get(id);
                buf.append(id + "\t" + typicalWord + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void setSelectedIndex(int index) {
        DODDLEProject currentProject = (DODDLEProject) desktop.getSelectedFrame();
        currentProject.setSelectedIndex(index);
    }

    public static Model getOntology(DODDLEProject currentProject) {
        ConstructConceptTreePanel constructConceptTreePanel = currentProject.getConstructConceptTreePanel();
        ConstructPropertyTreePanel constructPropertyTreePanel = currentProject.getConstructPropertyTreePanel();
        TextConceptDefinitionPanel conceptDefinitionWithTextPanel = currentProject.getTextConceptDefinitionPanel();

        Model ontology = JenaModelMaker.makeClassModel(constructConceptTreePanel.getTreeModelRoot(), ModelFactory
                .createDefaultModel());
        JenaModelMaker.makePropertyModel(constructPropertyTreePanel.getTreeModelRoot(), ontology);
        conceptDefinitionWithTextPanel.addConceptDefinition(ontology);
        return ontology;
    }

    public void saveOntology(DODDLEProject project, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF8"));
            Model ontModel = getOntology(project);
            RDFWriter rdfWriter = ontModel.getWriter("RDF/XML-ABBREV");
            rdfWriter.setProperty("xmlbase", BASE_URI);
            rdfWriter.setProperty("showXmlDeclaration", Boolean.TRUE);
            rdfWriter.write(ontModel, writer, BASE_URI);
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadBaseURI(File file) {
        if (!file.exists()) { return; }
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
            BASE_URI = reader.readLine();
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadOntology(DODDLEProject currentProject, File file) {
        InputModuleUI inputModuleUI = currentProject.getInputModuleUI();
        ConstructConceptTreePanel constructConceptTreePanel = currentProject.getConstructConceptTreePanel();
        ConstructPropertyTreePanel constructPropertyTreePanel = currentProject.getConstructPropertyTreePanel();

        if (!file.exists()) { return; }
        constructConceptTreePanel.init();
        constructPropertyTreePanel.init();
        currentProject.resetIDConceptMap();
        ConceptTreeMaker.getInstance().setInputConceptSet(inputModuleUI.getInputConceptSet());
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
            Model model = ModelFactory.createDefaultModel();
            model.read(reader, BASE_URI, "RDF/XML");
            currentProject.initUserIDCount();
            TreeNode rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject, model,
                    ResourceFactory.createResource(EDR_URI + "ID" + ConceptTreeMaker.EDR_CLASS_ROOT_ID));
            DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
            constructConceptTreePanel.setTreeModel(treeModel);
            constructConceptTreePanel.setVisibleConceptTree(true);
            constructConceptTreePanel.checkMultipleInheritance(treeModel);
            currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
            treeModel.reload();

            currentProject.setUserIDCount(currentProject.getUserIDCount());
            rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
                    ResourceFactory.createResource(EDR_URI + "ID" + ConceptTreeMaker.EDR_PROPERTY_ROOT_ID));
            treeModel = new DefaultTreeModel(rootNode);
            constructPropertyTreePanel.setTreeModel(treeModel);
            constructPropertyTreePanel.setVisibleConceptTree(true);
            constructPropertyTreePanel.checkMultipleInheritance(treeModel);
            treeModel.reload();
            currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);

            constructConceptTreePanel.expandTree();
            constructPropertyTreePanel.expandTree();
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void setPath(String type) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("./config.txt"));
            DODDLE_DIC = properties.getProperty("DODDLE_DIC");
            DocumentSelectionPanel.PERL_EXE = properties.getProperty("PERL_EXE");
            DocumentSelectionPanel.CHASEN_EXE = properties.getProperty("CHASEN_EXE");
            DocumentSelectionPanel.SS_TAGGER_HOME = properties.getProperty("SSTAGGER_HOME");
            BASE_URI = properties.getProperty("BASE_URI");
            PROJECT_DIR = properties.getProperty("PROJECT_DIR");
            UpperConceptManager.UPPER_CONCEPT_LIST = properties.getProperty("UPPER_CONCEPT_LIST");
            WORDNET_PATH = properties.getProperty("WORDNET_PATH");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (type.equals("normal")) {
            EDRDic.ID_DEFINITION_MAP = DODDLE_DIC + "idDefinitionMapforEDR.txt";
            EDRDic.WORD_IDs_MAP = DODDLE_DIC + "wordIDsMapforEDR.txt";
            EDRTree.CONCEPT_SUBCONCEPTSET_MAP = DODDLE_DIC + "conceptSubConceptSetMapforEDR.txt";
            ConceptDefinition.CONCEPT_DEFINITION = DODDLE_DIC + "conceptDefinitionforEDR.txt";
        } else if (type.equals("special")) {
            EDRDic.ID_DEFINITION_MAP = DODDLE_DIC + "T_IDDefinitionMapforEDR.txt";
            EDRDic.WORD_IDs_MAP = DODDLE_DIC + "T_WordIDsMapforEDR.txt";
            EDRTree.CONCEPT_SUBCONCEPTSET_MAP = DODDLE_DIC + "T_ConceptSubConceptSetMapforEDR.txt";
            ConceptDefinition.CONCEPT_DEFINITION = DODDLE_DIC + "T_ConceptDefinitionforEDR.txt";
        }
    }

    public static void setRootID(String type) {
        if (type.equals("normal")) {
            ConceptTreeMaker.EDR_CLASS_ROOT_ID = "3aa966";
        } else if (type.equals("special")) {
            ConceptTreeMaker.EDR_CLASS_ROOT_ID = "2f3526";
        }
    }

    public static void setProgressValue(String type) {
        if (type.equals("normal")) {
            InputModule.INIT_PROGRESS_VALUE = 887253;
        } else if (type.equals("special")) {
            InputModule.INIT_PROGRESS_VALUE = 283517;
        }
    }

    public static Logger getLogger() {
        return Logger.getLogger(DODDLE.class);
    }

    private static void setDefaultLoggerFormat() {
        for (Enumeration enumeration = Logger.getRootLogger().getAllAppenders(); enumeration.hasMoreElements();) {
            Appender appender = (Appender) enumeration.nextElement();
            if (appender.getName().equals("stdout")) {
                appender.setLayout(new PatternLayout("[%5p][%c{1}][%d{yyyy-MMM-dd HH:mm:ss}]: %m\n"));
            }
        }
    }

    public static void initOptions(String[] args) {
        DODDLE.IS_USING_DB = false;
        setDefaultLoggerFormat();

        LongOpt[] longopts = new LongOpt[4];
        longopts[0] = new LongOpt("DB", LongOpt.NO_ARGUMENT, null, 'd');
        longopts[1] = new LongOpt("normal", LongOpt.NO_ARGUMENT, null, 'n');
        longopts[2] = new LongOpt("special", LongOpt.NO_ARGUMENT, null, 's');
        longopts[3] = new LongOpt("DEBUG", LongOpt.NO_ARGUMENT, null, 'g');
        Getopt g = new Getopt("DODDLE", args, "", longopts);
        g.setOpterr(false);

        int c;
        String type = "normal";
        setPath(type);
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'g':
                getLogger().setLevel(Level.DEBUG);
                break;
            case 'd':
                setPath(type);
                DODDLE.IS_USING_DB = true;
                break;
            case 'n':
            case 's':
                if (c == 'n') {
                    type = "normal";
                } else if (c == 's') {
                    type = "special";
                }
                setPath(type);
                setRootID(type);
                setProgressValue(type);
                break;
            default:
                break;
            }
        }
        if (DODDLE.IS_USING_DB) {
            getLogger().log(Level.INFO, "Read EDR DIC using Berkeley DB");
        } else {
            getLogger().log(Level.INFO, "Read EDR DIC on Memory");
        }
        if (type.equals("normal")) {
            getLogger().log(Level.INFO, "EDR ��ʎ���");
        } else {
            getLogger().log(Level.INFO, "EDR ��厫��");
        }
    }

    public static void main(String[] args) {
        SplashWindow splashWindow = new SplashWindow();
        DODDLE.initOptions(args);
        try {
            ToolTipManager.sharedInstance().setEnabled(true);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new DODDLE();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            splashWindow.setVisible(false);
        }
    }
}