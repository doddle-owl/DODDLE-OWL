package jp.ac.keio.ae.comp.yamaguti.doddle;

import gnu.getopt.*;

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.actions.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.apache.log4j.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * 
 * @author takeshi morita
 */
public class DODDLE extends JFrame {

    private OptionDialog optionDialog;
    private LogConsole logConsole;

    public static DODDLEPlugin doddlePlugin;

    public static Frame rootFrame;
    public static JRootPane rootPane;
    public static JDesktopPane desktop;
    public static JMenu projectMenu;
    public static JMenu recentProjectMenu;
    public static StatusBarPanel STATUS_BAR;
    public static Set<String> GENERAL_ONTOLOGY_NAMESPACE_SET;

    private NewProjectAction newProjectAction;
    private OpenProjectAction openProjectAction;
    private SaveProjectAction saveProjectAction;
    private SaveProjectAsAction saveProjectAsAction;
    private LoadDescriptionsAction loadDescriptionAction;
    private LoadConceptPreferentialTermAction loadConceptDisplayTermAction;
    private SaveConceptPreferentialTermAction saveConceptDisplayTermAction;
    private LoadOntologyAction loadOWLOntologyAction;
    private LoadOntologyAction loadFreeMindOntologyAction;
    private SaveOntologyAction saveOWLOntologyAction;
    private SaveOntologyAction saveFreeMindOntologyAction;
    private SaveConfigAction saveConfigAction;
    private LoadConfigAction loadConfigAction;
    private ShowLogConsoleAction showLogConsoleAction;
    private LayoutDockingWindowAction xgaLayoutDockingWindowAction;
    private LayoutDockingWindowAction uxgaLayoutDockingWindowAction;
    private DBManagerAction dbManagerAction;

    public static final Property HASA_PROPERTY = ResourceFactory.createProperty(DODDLEConstants.BASE_URI + "partOf");

    public DODDLE() {
        rootPane = getRootPane();
        rootFrame = this;
        desktop = new JDesktopPane();
        optionDialog = new OptionDialog(this);
        logConsole = new LogConsole(this, Translator.getTerm("LogConsoleDialog"), null);
        STATUS_BAR = new StatusBarPanel();
        GENERAL_ONTOLOGY_NAMESPACE_SET = new HashSet<String>();
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.EDR_URI);
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.EDRT_URI);
        GENERAL_ONTOLOGY_NAMESPACE_SET.add(DODDLEConstants.WN_URI);

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
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setIconImage(Utils.getImageIcon("application.png").getImage());
        setTitle(Translator.getTerm("ApplicationName") + " - " + Translator.getTerm("VersionMenu") + ": "
                + DODDLEConstants.VERSION);
        setVisible(true);
    }

    public static DODDLEPlugin getDODDLEPlugin() {
        return doddlePlugin;
    }

    public OptionDialog getOptionDialog() {
        return optionDialog;
    }

    public void exit() {
        int messageType = JOptionPane.showConfirmDialog(rootPane, Translator.getDescription("ExitAction"), Translator
                .getTerm("ExitAction"), JOptionPane.YES_NO_OPTION);
        if (messageType == JOptionPane.YES_OPTION) {
            EDRDic.closeDB();
            dbManagerAction.closeDB();
            if (isExistingCurrentProject()) {
                getCurrentProject().getDocumentSelectionPanel().destroyProcesses();
            }
            if (doddlePlugin == null) {
                System.exit(0);
            } else {
                dispose();
            }
        }
    }

    public List<String> loadRecentProject() {
        BufferedReader reader = null;
        List<String> recentProjects = new ArrayList<String>();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(
                    DODDLEConstants.PROJECT_HOME, ProjectFileNames.RECENT_PROJECTS_FILE)), "UTF-8"));
            while (reader.ready()) {
                recentProjects.add(reader.readLine());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
        return recentProjects;
    }

    public void saveRecentProject(List<String> recentProjects) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
                    DODDLEConstants.PROJECT_HOME, ProjectFileNames.RECENT_PROJECTS_FILE)), "UTF-8"));
            int cnt = 0;
            DODDLE.recentProjectMenu.removeAll();
            for (String project : recentProjects) {
                if (cnt == 10) {
                    break;
                }
                writer.write(project);
                writer.write("\n");
                JMenuItem item = new JMenuItem(project);
                item.addActionListener(new OpenRecentProjectAction(project, this));
                DODDLE.recentProjectMenu.add(item);
                cnt++;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void finishNewProject(DODDLEProject project) {
        try {
            desktop.add(project);
            project.toFront();
            desktop.setSelectedFrame(project);
            project.setVisible(true);
            project.setMaximum(true); // setVisibleより前にしてしまうと，初期サイズ(800x600)で最大化されてしまう
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeActions() {
        newProjectAction = new NewProjectAction(Translator.getTerm("NewProjectAction"));
        openProjectAction = new OpenProjectAction(Translator.getTerm("OpenProjectAction"), this);
        saveProjectAction = new SaveProjectAction(Translator.getTerm("SaveProjectAction"), this);
        saveProjectAsAction = new SaveProjectAsAction(Translator.getTerm("SaveAsProjectAction"), this);
        loadDescriptionAction = new LoadDescriptionsAction(Translator.getTerm("OpenDescriptionsAction"));
        loadConceptDisplayTermAction = new LoadConceptPreferentialTermAction(Translator
                .getTerm("OpenConceptPreferentialTermMapAction"));
        saveConceptDisplayTermAction = new SaveConceptPreferentialTermAction(Translator
                .getTerm("SaveConceptPreferentialTermMapAction"));
        saveOWLOntologyAction = new SaveOntologyAction(Translator.getTerm("SaveOWLOntologyAction"),
                SaveOntologyAction.OWL_ONTOLOGY);
        saveFreeMindOntologyAction = new SaveOntologyAction(Translator.getTerm("SaveFreeMindOntologyAction"),
                SaveOntologyAction.FREEMIND_ONTOLOGY);
        loadOWLOntologyAction = new LoadOntologyAction(Translator.getTerm("OpenOWLOntologyAction"),
                LoadOntologyAction.OWL_ONTOLOGY);
        loadFreeMindOntologyAction = new LoadOntologyAction(Translator.getTerm("OpenFreeMindOntologyAction"),
                LoadOntologyAction.FREEMIND_ONTOLOGY);
        saveConfigAction = new SaveConfigAction(Translator.getTerm("SaveConfigFileAction"), optionDialog);
        loadConfigAction = new LoadConfigAction(Translator.getTerm("OpenConfigFileAction"), optionDialog);
        showLogConsoleAction = new ShowLogConsoleAction(Translator.getTerm("ShowLogConsoleAction"), logConsole);
        xgaLayoutDockingWindowAction = new LayoutDockingWindowAction(LayoutDockingWindowAction.XGA_LAYOUT, Translator
                .getTerm("XGALayoutAction"));
        uxgaLayoutDockingWindowAction = new LayoutDockingWindowAction(LayoutDockingWindowAction.UXGA_LAYOUT, Translator
                .getTerm("UXGALayoutAction"));
        dbManagerAction = new DBManagerAction(Translator.getTerm("DBManagerDialog"));
    }

    private void makeMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Translator.getTerm("FileMenu"));
        fileMenu.add(newProjectAction);
        fileMenu.addSeparator();
        fileMenu.add(openProjectAction);
        recentProjectMenu = new JMenu(Translator.getTerm("OpenRecentProjectsMenu"));
        List<String> recentProjects = loadRecentProject();
        saveRecentProject(recentProjects);
        fileMenu.add(recentProjectMenu);
        JMenu loadMenu = new JMenu(Translator.getTerm("OpenMenu"));
        loadMenu.add(new LoadInputTermSetAction(Translator.getTerm("OpenInputTermListAction")));
        loadMenu.add(new LoadTermInfoTableAction(Translator.getTerm("OpenInputTermTableAction")));
        loadMenu.add(loadDescriptionAction);
        loadMenu.add(new LoadTermEvalConceptSetAction(Translator.getTerm("OpenDisambiguationResultAction")));
        loadMenu.add(new LoadTermConceptMapAction(Translator.getTerm("OpenInputTermConceptMapAction")));
        loadMenu.add(loadOWLOntologyAction);
        loadMenu.add(loadFreeMindOntologyAction);
        loadMenu.add(loadConceptDisplayTermAction);
        loadMenu.add(loadConfigAction);
        fileMenu.add(loadMenu);
        fileMenu.addSeparator();
        fileMenu.add(saveProjectAction);
        fileMenu.add(saveProjectAsAction);
        JMenu saveMenu = new JMenu(Translator.getTerm("SaveMenu"));
        // saveMenu.add(new SaveMatchedWordList("辞書に載っていた入力語彙を保存"));
        saveMenu.add(new SaveInputTermSetAction(Translator.getTerm("SaveInputTermListAction")));
        saveMenu.add(new SaveTermInfoTableAction(Translator.getTerm("SaveInputTermTableAction")));
        saveMenu.add(new SaveTermEvalConceptSetAction(Translator.getTerm("SaveDisambiguationResultAction")));
        saveMenu.add(new SaveTermConceptMapAction(Translator.getTerm("SaveInputTermConceptMapAction")));
        saveMenu.add(new SavePerfectlyMatchedTermAction(Translator.getTerm("SavePerfectlyMatchedTermListAction")));
        saveMenu.add(new SavePerfectlyMatchedTermWithCompoundWordAction(Translator
                .getTerm("SavePerfectlyMatchedTermCompoundWordMapAction")));
        saveMenu.add(saveOWLOntologyAction);
        saveMenu.add(saveFreeMindOntologyAction);
        saveMenu.add(saveConceptDisplayTermAction);
        saveMenu.add(saveConfigAction);
        fileMenu.add(saveMenu);
        fileMenu.addSeparator();
        fileMenu.add(dbManagerAction);
        fileMenu.addSeparator();
        fileMenu.add(new ExitAction(Translator.getTerm("ExitAction"), this));
        menuBar.add(fileMenu);

        JMenu toolMenu = new JMenu(Translator.getTerm("ToolMenu"));
        toolMenu.add(new ShowAllTermAction(Translator.getTerm("ShowAllTermAction")));
        toolMenu.addSeparator();
        toolMenu.add(new AutomaticDisAmbiguationAction(Translator.getTerm("AutomaticDisambiguationAction")));
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
        toolMenu.add(new OptionAction(Translator.getTerm("ShowOptionDialogAction"), this));
        menuBar.add(toolMenu);

        projectMenu = new JMenu(Translator.getTerm("ProjectMenu"));
        menuBar.add(projectMenu);
        JMenu helpMenu = new JMenu(Translator.getTerm("HelpMenu"));
        JMenuItem versionItem = new JMenuItem(Translator.getTerm("VersionMenu"), Utils.getImageIcon("help.png"));
        versionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SplashWindow(rootFrame);
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
            currentProject = new DODDLEProject(Translator.getTerm("NewProjectAction"), 11);
        }
        return currentProject;
    }

    public static boolean isExistingCurrentProject() {
        if (desktop == null) { return false; }
        DODDLEProject currentProject = (DODDLEProject) desktop.getSelectedFrame();
        return currentProject != null;
    }

    public static void addProjectMenuItem(JMenuItem item) {
        projectMenu.add(item);
    }

    public static void removeProjectMenuItem(JMenuItem item) {
        projectMenu.remove(item);
    }

    public void loadConceptDisplayTerm(DODDLEProject currentProject, File file) {
        loadConceptDisplayTermAction.loadIDPreferentialTerm(currentProject, file);
    }

    public void saveConceptDisplayTerm(DODDLEProject currentProject, File file) {
        saveConceptDisplayTermAction.saveIDPreferentialTerm(currentProject, file);
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
            DODDLEConstants.BASE_URI = reader.readLine();
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

    public static void setPath(Properties properties) {
        DODDLEConstants.GOSEN_CONFIGURATION_FILE = properties.getProperty("GOSEN_CONFIGURATION_FILE");
        DODDLEConstants.EDR_HOME = properties.getProperty("EDR_HOME") + File.separator;
        DODDLEConstants.EDRT_HOME = properties.getProperty("EDRT_HOME") + File.separator;
        InputDocumentSelectionPanel.PERL_EXE = properties.getProperty("PERL_EXE");
        InputDocumentSelectionPanel.Japanese_Morphological_Analyzer = properties
                .getProperty("Japanese_Morphological_Analyzer");
        InputDocumentSelectionPanel.Japanese_Dependency_Structure_Analyzer = properties
                .getProperty("Japanese_Dependency_Structure_Analyzer");
        InputDocumentSelectionPanel.SS_TAGGER_HOME = properties.getProperty("SSTAGGER_HOME") + File.separator;
        InputDocumentSelectionPanel.TERM_EXTRACT_SCRIPTS_DIR = properties.getProperty("TERM_EXTRACT_SCRIPTS_DIR")
                + File.separator;
        InputDocumentSelectionPanel.XDOC2TXT_EXE = properties.getProperty("XDOC2TXT_EXE");
        DODDLEConstants.BASE_URI = properties.getProperty("BASE_URI");
        DODDLEConstants.BASE_PREFIX = properties.getProperty("BASE_PREFIX");
        DODDLEConstants.PROJECT_HOME = properties.getProperty("PROJECT_DIR");
        UpperConceptManager.UPPER_CONCEPT_LIST = properties.getProperty("UPPER_CONCEPT_LIST");
        DODDLEConstants.WORDNET_HOME = properties.getProperty("WORDNET_HOME");
        if (properties.getProperty("USING_DB").equals("true")) {
            DODDLEConstants.IS_USING_DB = true;
        }
        DODDLEConstants.LANG = properties.getProperty("LANG");
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
                setPath(properties);
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
        } else {
            try {
                Preferences userPrefs = Preferences.userNodeForPackage(DODDLE.class);
                String[] keys = userPrefs.keys();
                if (0 < keys.length) {
                    Properties properties = new Properties();
                    for (int i = 0; i < keys.length; i++) {
                        properties.put(keys[i], userPrefs.get(keys[i], ""));
                    }
                    setPath(properties);
                }
            } catch (BackingStoreException bse) {
                bse.printStackTrace();
            }
        }
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

    public static void setFileLogger() {
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
        DODDLEConstants.IS_USING_DB = false;
        getLogger().setLevel(Level.INFO);
        setDefaultLoggerFormat();
        setFileLogger();

        LongOpt[] longopts = new LongOpt[4];
        longopts[0] = new LongOpt("DB", LongOpt.NO_ARGUMENT, null, 'd');
        longopts[1] = new LongOpt("DEBUG", LongOpt.NO_ARGUMENT, null, 'g');
        longopts[2] = new LongOpt("LANG", LongOpt.REQUIRED_ARGUMENT, null, 'l');
        longopts[3] = new LongOpt("Swoogle", LongOpt.NO_ARGUMENT, null, 's');
        Getopt g = new Getopt("DODDLE", args, "", longopts);
        g.setOpterr(false);

        setPath();
        setProgressValue();
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'g':
                getLogger().setLevel(Level.DEBUG);
                DODDLEConstants.DEBUG = true;
                break;
            case 'd':
                DODDLEConstants.IS_USING_DB = true;
                break;
            case 'l':
                DODDLEConstants.LANG = g.getOptarg();
                break;
            case 's':
                DODDLEConstants.IS_INTEGRATING_SWOOGLE = true;
                break;
            default:
                break;
            }
        }
        if (DODDLEConstants.LANG == null) {
            DODDLEConstants.LANG = "ja";
        }
    }

    public static String getExecPath() {
        if (doddlePlugin == null) { return "." + File.separator; }
        String jarPath = DODDLE.class.getClassLoader().getResource("").getFile();
        File file = new File(jarPath);
        String configPath = file.getAbsolutePath() + File.separator;
        return configPath;
    }

    public static void main(String[] args) {
        SplashWindow splashWindow = new SplashWindow(null);
        DODDLE.initOptions(args);
        Translator.loadDODDLEComponentOntology(DODDLEConstants.LANG);
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