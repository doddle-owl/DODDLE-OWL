package jp.ac.keio.ae.comp.yamaguti.doddle;

import gnu.getopt.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.actions.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.apache.log4j.*;

import com.jgoodies.looks.*;

/**
 * 
 * @author takeshi morita
 */
public class DODDLE extends JFrame {

    public static boolean IS_USING_DB;
    public static boolean IS_USING_JGOODIES_LOOKS;
    public static String LANG = "ja"; // DB構築時に必要
    public static DODDLEPlugin doddlePlugin;
    public static JDesktopPane desktop;

    public static JMenu projectMenu;

    private OptionDialog optionDialog;
    private LogConsole logConsole;

    public static StatusBarPanel STATUS_BAR;
    public static int DIVIDER_SIZE = 10;

    public static final String VERSION = "2007-02-21";

    public static final int DOCUMENT_SELECTION_PANEL = 1;
    public static final int INPUT_WORD_SELECTION_PANEL = 2;
    public static final int INPUT_MODULE = 3;
    public static final int TAXONOMIC_PANEL = 4;
    public static String BASE_URI = "http://www.yamaguti.comp.ae.keio.ac.jp/doddle#";
    public static String BASE_PREFIX = "keio";
    public static String EDR_URI = "http://www2.nict.go.jp/kk/e416/EDR#";
    public static String OLD_EDR_URI = "http://www2.nict.go.jp/kk/e416/EDR/";
    public static String EDRT_URI = "http://www2.nict.go.jp/kk/e416/EDRT#";
    public static String WN_URI = "http://wordnet.princeton.edu/wn/2.0#";
    public static Set<String> GENERAL_ONTOLOGY_NAMESPACE_SET;

    public static String EDR_HOME = "C:/EDR_DIC/";
    public static String EDRT_HOME = "C:/EDRT_DIC/";
    public static String SEN_HOME = "C:/SEN_DIC/sen-1.2.1/";
    public static String PROJECT_HOME = "./project/";
    private static final String RESOURCES = "jp/ac/keio/ae/comp/yamaguti/doddle/resources/";
    public static String JWNL_PROPERTIES_FILE = RESOURCES + "file_properties.xml";
    public static String WORDNET_HOME = "C:/program files/wordnet/2.0/dict";

    public static JRootPane rootPane;

    private NewProjectAction newProjectAction;
    private OpenProjectAction openProjectAction;
    private SaveProjectAction saveProjectAction;
    private SaveProjectAsAction saveProjectAsAction;
    private LoadConceptTypicalWordAction loadConceptTypicalWordAction;
    private SaveConceptTypicalWordAction saveConceptTypicalWordAction;
    private LoadOntologyAction loadOWLOntologyAction;
    private LoadOntologyAction loadFreeMindOntologyAction;
    private SaveOntologyAction saveOWLOntologyAction;
    private SaveOntologyAction saveFreeMindOntologyAction;
    private SaveConfigAction saveConfigAction;
    private LoadConfigAction loadConfigAction;
    private ShowLogConsoleAction showLogConsoleAction;
    private LayoutDockingWindowAction xgaLayoutDockingWindowAction;
    private LayoutDockingWindowAction uxgaLayoutDockingWindowAction;

    public DODDLE() {
        rootPane = getRootPane();
        desktop = new JDesktopPane();
        optionDialog = new OptionDialog(this);
        logConsole = new LogConsole(this, "Log Console", null);
        STATUS_BAR = new StatusBarPanel();
        GENERAL_ONTOLOGY_NAMESPACE_SET = new HashSet<String>();
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(EDR_URI);
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(EDRT_URI);
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(WN_URI);

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
        setSize(1024, 768);
        setLocation(10, 10);
        setTitle(Translator.getString("Title") + " - " + Translator.getString("Component.Help.Version") + ": "
                + VERSION);
        setIconImage(Utils.getImageIcon("doddle_splash.png").getImage());
        setVisible(true);
    }

    public static DODDLEPlugin getDODDLEPlugin() {
        return doddlePlugin;
    }

    public OptionDialog getOptionDialog() {
        return optionDialog;
    }

    public void exit() {
        int messageType = JOptionPane.showConfirmDialog(rootPane, Translator.getString("Component.File.Exit.DODDLE"),
                Translator.getString("Component.File.Exit"), JOptionPane.YES_NO_CANCEL_OPTION);
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
        DODDLEProject project = new DODDLEProject(Translator.getString("Component.File.NewProject.Text"), projectMenu);
        try {
            desktop.add(project);
            project.toFront();
            desktop.setSelectedFrame(project);            
            project.setVisible(true);        
            project.setMaximum(true); // setVisibleより前にしてしまうと，初期サイズ(800x600)で最大化されてしまう
        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }

    private void makeActions() {
        newProjectAction = new NewProjectAction(Translator.getString("Component.File.NewProject.Text"));
        openProjectAction = new OpenProjectAction(Translator.getString("Component.File.OpenProject.Text"), this);
        saveProjectAction = new SaveProjectAction(Translator.getString("Component.File.SaveProject.Text"), this);
        saveProjectAsAction = new SaveProjectAsAction(Translator.getString("Component.File.SaveAsProject.Text"), this);
        loadConceptTypicalWordAction = new LoadConceptTypicalWordAction(Translator
                .getString("Component.File.Open.ConceptTypicalWordMap"));
        saveConceptTypicalWordAction = new SaveConceptTypicalWordAction(Translator
                .getString("Component.File.Save.ConceptTypicalWordMap"));
        saveOWLOntologyAction = new SaveOntologyAction(Translator.getString("Component.File.Save.Ontology")+" (OWL)", SaveOntologyAction.OWL_ONTOLOGY);
        saveFreeMindOntologyAction = new SaveOntologyAction(Translator.getString("Component.File.Save.Ontology")+" (FreeMind)", SaveOntologyAction.FREEMIND_ONTOLOGY);
        loadOWLOntologyAction = new LoadOntologyAction(Translator.getString("Component.File.Open.Ontology")+" (OWL)", LoadOntologyAction.OWL_ONTOLOGY);
        loadFreeMindOntologyAction = new LoadOntologyAction(Translator.getString("Component.File.Open.Ontology")+" (FreeMind)", LoadOntologyAction.FREEMIND_ONTOLOGY);
        saveConfigAction = new SaveConfigAction(Translator.getString("Component.File.Save.Config"), optionDialog);
        loadConfigAction = new LoadConfigAction(Translator.getString("Component.File.Open.Config"), optionDialog);
        showLogConsoleAction = new ShowLogConsoleAction("Log Console", logConsole);
        xgaLayoutDockingWindowAction = new LayoutDockingWindowAction(LayoutDockingWindowAction.XGA_LAYOUT, "XGA Layout");
        uxgaLayoutDockingWindowAction = new LayoutDockingWindowAction(LayoutDockingWindowAction.UXGA_LAYOUT, "UXGA Layout");
    }

    private void makeMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Translator.getString("Component.File.Text"));
        fileMenu.add(newProjectAction);
        fileMenu.addSeparator();
        fileMenu.add(openProjectAction);
        JMenu loadMenu = new JMenu(Translator.getString("Component.File.Open"));
        loadMenu.add(new LoadInputWordSetAction(Translator.getString("Component.Tool.OpenInputWordList")));
        loadMenu.add(new LoadWordInfoTableAction(Translator.getString("Component.File.Open.InputWordTable")));
        loadMenu
                .add(new LoadWordEvalConceptSetAction(Translator.getString("Component.File.Open.DisambiguationResult")));
        loadMenu.add(new LoadWordConceptMapAction(Translator.getString("Component.File.Open.InputWordConceptMap")));
        loadMenu.add(loadOWLOntologyAction);
        loadMenu.add(loadFreeMindOntologyAction);
        loadMenu.add(loadConceptTypicalWordAction);
        loadMenu.add(loadConfigAction);
        fileMenu.add(loadMenu);
        fileMenu.addSeparator();
        fileMenu.add(saveProjectAction);
        fileMenu.add(saveProjectAsAction);
        JMenu saveMenu = new JMenu(Translator.getString("Component.File.Save"));
        // saveMenu.add(new SaveMatchedWordList("辞書に載っていた入力語彙を保存"));
        saveMenu.add(new SaveWordInfoTableAction(Translator.getString("Component.File.Save.InputWordTable")));
        saveMenu
                .add(new SaveWordEvalConceptSetAction(Translator.getString("Component.File.Save.DisambiguationResult")));
        saveMenu.add(new SaveWordConceptMapAction(Translator.getString("Component.File.Save.InputWordConceptMap")));
        saveMenu.add(new SaveCompleteMatchWordAction(Translator.getString("Component.File.Save.CompleteMatchWord")));
        saveMenu.add(new SaveCompleteMatchWordWithComplexWordAction(Translator
                .getString("Component.File.Save.CompleteMatchWordComplexWordMap")));
        saveMenu.add(saveOWLOntologyAction);
        saveMenu.add(saveFreeMindOntologyAction);
        saveMenu.add(saveConceptTypicalWordAction);
        saveMenu.add(saveConfigAction);
        fileMenu.add(saveMenu);
        fileMenu.addSeparator();
        fileMenu.add(new ExitAction(Translator.getString("Component.File.Exit"), this));
        menuBar.add(fileMenu);

        JMenu toolMenu = new JMenu(Translator.getString("Component.Tool.Text"));
        toolMenu.add(new ShowAllWordsAction(Translator.getString("Component.Tool.OpenAllWordList")));
        toolMenu.addSeparator();
        toolMenu.add(new AutomaticDisAmbiguationAction(Translator.getString("Component.Tool.Disambiguation")));
        // toolMenu.add(new AutomaticDisambiguationTestAction("自動多義性解消テスト"));
        toolMenu.addSeparator();
        toolMenu.add(new ConstructNounTreeAction());
        toolMenu.add(new ConstructNounAndVerbTreeAction());
        toolMenu.addSeparator();
        toolMenu.add(showLogConsoleAction);
        toolMenu.addSeparator();
        toolMenu.add(xgaLayoutDockingWindowAction);
        toolMenu.add(uxgaLayoutDockingWindowAction);
        toolMenu.addSeparator();
        toolMenu.add(new OptionAction(Translator.getString("Component.Tool.Option"), this));
        menuBar.add(toolMenu);

        projectMenu = new JMenu(Translator.getString("Component.Project.Text"));
        menuBar.add(projectMenu);
        JMenu helpMenu = new JMenu(Translator.getString("Component.Help.Text"));
        JMenuItem versionItem = new JMenuItem(Translator.getString("Component.Help.Version"));
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
        if (desktop == null) { return null; }
        DODDLEProject currentProject = (DODDLEProject) desktop.getSelectedFrame();
        if (currentProject == null) {
            currentProject = newProject();
        } 
        return currentProject;
    }

    public void loadConceptTypicalWord(DODDLEProject currentProject, File file) {
        loadConceptTypicalWordAction.loadIDTypicalWord(currentProject, file);
    }

    public void saveConceptTypicalWord(DODDLEProject currentProject, File file) {
        saveConceptTypicalWordAction.saveIDTypicalWord(currentProject, file);
    }

    public void saveOntology(DODDLEProject currentProject, File file) {
        saveOWLOntologyAction.saveOWLOntology(currentProject, file);
    }

    public void loadOntology(DODDLEProject currentProject, File file) {
        loadOWLOntologyAction.loadOWLOntology(currentProject, file);
    }

    public static void setSelectedIndex(int index) {
        DODDLEProject currentProject = (DODDLEProject) desktop.getSelectedFrame();
        currentProject.setSelectedIndex(index);
    }

    public void loadBaseURI(File file) {
        if (!file.exists()) { return; }
        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            BASE_URI = reader.readLine();
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

    public static void setPath() {
        String configPath = getExecPath();
        File configFile = new File(configPath + ProjectFileNames.CONFIG_FILE);
        if (configFile.exists()) {
            BufferedReader reader = null;
            try {
                Properties properties = new Properties();
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(configPath
                        + ProjectFileNames.CONFIG_FILE), "UTF-8"));
                properties.load(reader);
                SEN_HOME = properties.getProperty("SEN_HOME");
                EDR_HOME = properties.getProperty("EDR_HOME");
                EDRT_HOME = properties.getProperty("EDRT_HOME");
                DocumentSelectionPanel.PERL_EXE = properties.getProperty("PERL_EXE");
                DocumentSelectionPanel.CHASEN_EXE = properties.getProperty("CHASEN_EXE");
                DocumentSelectionPanel.Japanese_Morphological_Analyzer = properties
                        .getProperty("Japanese_Morphological_Analyzer");
                DocumentSelectionPanel.SS_TAGGER_HOME = properties.getProperty("SSTAGGER_HOME");
                DocumentSelectionPanel.XDOC2TXT_EXE = properties.getProperty("XDOC2TXT_EXE");
                BASE_URI = properties.getProperty("BASE_URI");
                BASE_PREFIX = properties.getProperty("BASE_PREFIX");
                PROJECT_HOME = properties.getProperty("PROJECT_DIR");
                UpperConceptManager.UPPER_CONCEPT_LIST = properties.getProperty("UPPER_CONCEPT_LIST");
                WORDNET_HOME = properties.getProperty("WORDNET_HOME");
                if (properties.getProperty("USING_DB").equals("true")) {
                    IS_USING_DB = true;
                }
                DODDLE.LANG = properties.getProperty("LANG");
            } catch (Exception e) {
                e.printStackTrace();
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
        EDRDic.ID_DEFINITION_MAP = EDR_HOME + "idDefinitionMapforEDR.txt";
        EDRDic.WORD_IDSET_MAP = EDR_HOME + "wordIDSetMapforEDR.txt";
        EDRTree.ID_SUBIDSET_MAP = EDR_HOME + "idSubIDSetMapforEDR.txt";
        ConceptDefinition.CONCEPT_DEFINITION = EDR_HOME + "conceptDefinitionforEDR.txt";

        EDRDic.EDRT_ID_DEFINITION_MAP = EDRT_HOME + "idDefinitionMapforEDR.txt";
        EDRDic.EDRT_WORD_IDSET_MAP = EDRT_HOME + "wordIDSetMapforEDR.txt";
        EDRTree.EDRT_ID_SUBIDSET_MAP = EDRT_HOME + "idSubIDSetMapforEDR.txt";
    }

    public static void setProgressValue() {
        InputModule.INIT_PROGRESS_VALUE = 887253;
        // InputModule.INIT_PROGRESS_VALUE = 283517;
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
    
    private static void setFileLogger() {
        try {
            FileAppender appender = new FileAppender(new PatternLayout("[%5p][%c{1}][%d{yyyy-MMM-dd HH:mm:ss}]: %m\n"),
                    "./doddle_log.txt");
            appender.setName("LOG File");
            appender.setAppend(true);
            Logger.getRootLogger().addAppender(appender);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public static void initOptions(String[] args) {
        DODDLE.IS_USING_DB = false;
        setDefaultLoggerFormat();
        setFileLogger();

        LongOpt[] longopts = new LongOpt[4];
        longopts[0] = new LongOpt("DB", LongOpt.NO_ARGUMENT, null, 'd');
        longopts[1] = new LongOpt("DEBUG", LongOpt.NO_ARGUMENT, null, 'g');
        longopts[2] = new LongOpt("SKIN", LongOpt.NO_ARGUMENT, null, 's');
        longopts[3] = new LongOpt("LANG", LongOpt.REQUIRED_ARGUMENT, null, 'l');
        Getopt g = new Getopt("DODDLE", args, "", longopts);
        g.setOpterr(false);

        setPath();
        setProgressValue();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'g':
                getLogger().setLevel(Level.DEBUG);
                break;
            case 'd':
                DODDLE.IS_USING_DB = true;
                break;
            case 's':
                DODDLE.IS_USING_JGOODIES_LOOKS = true;
                break;
            case 'l':
                DODDLE.LANG = g.getOptarg();
                break;
            default:
                break;
            }
        }
        if (DODDLE.LANG == null) {
            DODDLE.LANG = "ja";
        }
        Translator.loadResourceBundle(DODDLE.LANG);
    }

    public static String getExecPath() {
        if (doddlePlugin == null) { return "." + File.separator; }
        String jarPath = DODDLE.class.getClassLoader().getResource("").getFile();
        File file = new File(jarPath);
        String configPath = file.getAbsolutePath() + File.separator;
        return configPath;
    }

    public static void main(String[] args) {
        SplashWindow splashWindow = new SplashWindow();
        DODDLE.initOptions(args);
        try {
            ToolTipManager.sharedInstance().setEnabled(true);
            if (DODDLE.IS_USING_JGOODIES_LOOKS) {
                UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
                Options.setDefaultIconSize(new Dimension(18, 18));
                String lafName = LookUtils.IS_OS_WINDOWS_XP ? Options.getCrossPlatformLookAndFeelClassName() : Options
                        .getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel(lafName);                
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            new DODDLE();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            splashWindow.setVisible(false);
        }
    }
}