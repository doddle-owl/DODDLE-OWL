/*
 * @(#)  2007/09/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.actions.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

import com.hp.hpl.jena.db.*;

/**
 * @author takeshi morita
 */
public class DBManagerPanel extends JDialog implements ActionListener, ListSelectionListener {

    private int projectID;
    private int lastProjectID;
    private String author;
    private String projectName;
    private String projectComment;
    private DODDLEProject currentProject;

    private JButton connectButton;

    private Connection con;
    private IDBConnection icon;
    private Statement stmt;

    private JLabel projectNameLabel;
    private JTextField projectNameField;
    private JLabel authorLabel;
    private JTextField authorField;
    private JTextArea commentArea;
    private JButton editCommentButton;

    private JLabel serverHostLabel;
    private JTextField serverHostTextField;
    private JLabel userNameLabel;
    private JTextField userNameTextField;
    private JLabel passWordLabel;
    private JPasswordField passWordField;

    private JTable projectInfoTable;
    private TableRowSorter<TableModel> rowSorter;
    private ProjectInfoTableModel projectInfoTableModel;

    private JButton openProjectButton;
    private JButton updateProjectButton;
    private JButton newProjectButton;
    private JButton removeProjectButton;

    private SaveOntologyAction saveOntologyAction;
    private LoadOntologyAction loadOntologyAction;

    public DBManagerPanel() {
        super(DODDLE.rootFrame, "DB Manager");
        projectNameLabel = new JLabel("Project Name");
        projectNameField = new JTextField(20);
        authorLabel = new JLabel("Author");
        authorField = new JTextField(20);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));
        panel.add(projectNameLabel);
        panel.add(projectNameField);
        panel.add(authorLabel);
        panel.add(authorField);
        commentArea = new JTextArea(4, 20);
        JScrollPane commentAreaScroll = new JScrollPane(commentArea);
        commentAreaScroll.setBorder(BorderFactory.createTitledBorder("Project Comment"));
        editCommentButton = new JButton("更新");
        editCommentButton.addActionListener(this);

        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BorderLayout());
        eastPanel.add(editCommentButton, BorderLayout.EAST);
        JPanel projectInfoPanel = new JPanel();
        projectInfoPanel.setBorder(BorderFactory.createTitledBorder("Project Info"));
        projectInfoPanel.setLayout(new BorderLayout());
        projectInfoPanel.add(panel, BorderLayout.NORTH);
        projectInfoPanel.add(commentAreaScroll, BorderLayout.CENTER);
        projectInfoPanel.add(eastPanel, BorderLayout.SOUTH);

        serverHostLabel = new JLabel("サーバホスト");
        serverHostTextField = new JTextField(20);
        serverHostTextField.setText("zest.comp.ae.keio.ac.jp");
        userNameLabel = new JLabel("ユーザ名");
        userNameTextField = new JTextField(20);
        userNameTextField.setText("doddle");
        passWordLabel = new JLabel("パスワード");
        passWordField = new JPasswordField(20);
        passWordField.setText("yamaguti");

        connectButton = new JButton("接続");
        connectButton.addActionListener(this);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        JPanel dbAccountPanel = new JPanel();
        dbAccountPanel.setBorder(BorderFactory.createTitledBorder("DBアカウント"));
        dbAccountPanel.setLayout(new GridLayout(4, 2));
        dbAccountPanel.add(serverHostLabel);
        dbAccountPanel.add(serverHostTextField);
        dbAccountPanel.add(userNameLabel);
        dbAccountPanel.add(userNameTextField);
        dbAccountPanel.add(passWordLabel);
        dbAccountPanel.add(passWordField);
        dbAccountPanel.add(new JLabel(""));
        dbAccountPanel.add(connectButton);
        JPanel dbAccountNorthPanel = new JPanel();
        dbAccountNorthPanel.setLayout(new BorderLayout());
        dbAccountNorthPanel.add(dbAccountPanel, BorderLayout.NORTH);

        northPanel.add(projectInfoPanel, BorderLayout.CENTER);
        northPanel.add(dbAccountNorthPanel, BorderLayout.WEST);

        projectInfoTable = new JTable();
        projectInfoTable.getSelectionModel().addListSelectionListener(this);
        projectInfoTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setProjectInfoTableModel();
        JScrollPane projectInfoTableScroll = new JScrollPane(projectInfoTable);
        
        newProjectButton = new JButton("New");
        newProjectButton.addActionListener(this);
        openProjectButton = new JButton("Open");
        openProjectButton.addActionListener(this);
        updateProjectButton = new JButton("Update");
        updateProjectButton.addActionListener(this);       
        removeProjectButton = new JButton("Remove");
        removeProjectButton.addActionListener(this);

        saveOntologyAction = new SaveOntologyAction("", SaveOntologyAction.OWL_ONTOLOGY);
        loadOntologyAction = new LoadOntologyAction("", LoadOntologyAction.OWL_ONTOLOGY);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(newProjectButton);
        buttonPanel.add(openProjectButton);
        buttonPanel.add(updateProjectButton);        
        buttonPanel.add(removeProjectButton);

        getContentPane().add(northPanel, BorderLayout.NORTH);
        getContentPane().add(projectInfoTableScroll, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setSize(new Dimension(800, 600));
        setLocationRelativeTo(DODDLE.rootFrame);
    }

    public void setProjectInfoTableModel() {
        // String WORD = Translator.getTerm("WordLabel");
        Object[] titles = new Object[] { "id", "プロジェクト名", "作成者", "作成日", "更新日"};

        projectInfoTableModel = new ProjectInfoTableModel(null, titles);
        projectInfoTableModel.getColumnClass(0);
        rowSorter = new TableRowSorter<TableModel>(projectInfoTableModel);
        rowSorter.setMaxSortKeys(5);

        projectInfoTable.setRowSorter(rowSorter);
        projectInfoTable.setModel(projectInfoTableModel);
        projectInfoTable.getTableHeader().setToolTipText("sorted by column");
    }

    class ProjectInfoTableModel extends DefaultTableModel {

        ProjectInfoTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        public Class< ? > getColumnClass(int columnIndex) {
            String columnName = getColumnName(columnIndex);
            if (columnName.equals("id")) { return Integer.class; }
            return String.class;
        }
    }

    public void connectDB() {
        try {
            setProjectInfoTableModel();
            Class.forName("org.gjt.mm.mysql.Driver");
            String host = serverHostTextField.getText();
            String dbName = "doddle";
            String url = "jdbc:mysql://" + host + "/" + dbName + "?useUnicode=true&characterEncoding=UTF-8";
            String userName = userNameTextField.getText();
            String passWord = String.valueOf(passWordField.getPassword());
            con = DriverManager.getConnection(url, userName, passWord);
            icon = new DBConnection(url, userName, passWord, "MySQL");
            stmt = con.createStatement();

            String sql = "SELECT * from project_info";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                lastProjectID = rs.getInt("Project_ID");
                String projectName = rs.getString("Project_Name");
                String author = rs.getString("Author");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creationDate = dateFormat.format(rs.getTimestamp("Creation_Date"));
                String modificationDate = dateFormat.format(rs.getTimestamp("Modification_Date"));
                projectInfoTableModel.addRow(new Object[] { lastProjectID, projectName, author, creationDate,
                        modificationDate});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "DB Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void closeDB() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                con.close();
            }
            if (icon != null) {
                icon.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteTableContents(int projectID, Statement stmt, String tableName) {
        try {
            String sql = "DELETE FROM " + tableName + " WHERE Project_ID=" + projectID;
            stmt.executeUpdate(sql);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public static int getMySQLBoolean(boolean t) {
        if (t) { return 1; }
        return 0;
    }

    public static boolean getMySQLBoolean(int t) {
        return t == 1;
    }

    public void saveProjectInfo(int projectID, DODDLEProject currentProject) {
        OntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        try {
            String sql = "SELECT * from project_info where Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            Date creationDate = null;
            while (rs.next()) {
                creationDate = rs.getTimestamp("Creation_Date");
                projectName = rs.getString("Project_Name");
            }
            if (creationDate == null) {
                creationDate = Calendar.getInstance().getTime();
            }
            DBManagerPanel.deleteTableContents(projectID, stmt, "project_info");

            StringBuffer sqlbuf = new StringBuffer();
            sqlbuf.append("INSERT INTO project_info (Project_ID,Project_Name,Author,Creation_Date,"
                    + "Modification_Date,Available_General_Ontologies,Input_Term_Count,Perfectly_Matched_Term_Count,"
                    + "System_Added_Perfectly_Matched_Term_Count,Partially_Matched_Term_Count,"
                    + "Matched_Term_Count,Undefined_Term_Count,Input_Concept_Count,Input_Noun_Concept_Count,"
                    + "Input_Verb_Concept_Count,Class_SIN_Count,Before_Trimming_Class_Count,"
                    + "Trimmed_Class_Count,After_Trimming_Class_Count,Property_SIN_Count,"
                    + "Before_Trimming_Property_Count,Trimmed_Property_Count,After_Trimming_Property_Count,"
                    + "Abstract_Internal_Class_Count,Average_Abstract_Sibling_Concept_Count_In_Classes,"
                    + "Abstract_Internal_Property_Count_Message,Average_Abstract_Sibling_Concept_Count_In_Properties,"
                    + "Class_From_Compound_Word_Count,Property_From_Compound_Word_Count,Total_Class_Count,"
                    + "Total_Property_Count,Average_Sibling_Classes,Average_Sibling_Properties,Base_URI,Comment) ");
            sqlbuf.append("VALUES(");

            sqlbuf.append(projectID);
            sqlbuf.append(",'");
            sqlbuf.append(projectName);
            sqlbuf.append("','");
            sqlbuf.append(author);
            sqlbuf.append("','");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sqlbuf.append(dateFormat.format(creationDate));
            sqlbuf.append("','");
            sqlbuf.append(dateFormat.format(Calendar.getInstance().getTime()));
            sqlbuf.append("','");
            sqlbuf.append(ontSelectionPanel.getEnableDicList());
            sqlbuf.append("',");
            if (disambiguationPanel.getInputWordModelSet() != null) {
                sqlbuf.append(disambiguationPanel.getInputWordCnt());
            } else {
                sqlbuf.append(0);
            }
            sqlbuf.append(",");
            sqlbuf.append(disambiguationPanel.getPerfectlyMatchedWordCnt());
            sqlbuf.append(",");
            sqlbuf.append(disambiguationPanel.getSystemAddedPerfectlyMatchedWordCnt());
            sqlbuf.append(",");
            sqlbuf.append(disambiguationPanel.getPartiallyMatchedWordCnt());
            sqlbuf.append(",");
            sqlbuf.append(disambiguationPanel.getMatchedWordCnt());
            sqlbuf.append(",");
            sqlbuf.append(disambiguationPanel.getUndefinedWordCnt());
            sqlbuf.append(",");
            if (disambiguationPanel.getInputConceptSet() != null) {
                sqlbuf.append(disambiguationPanel.getInputConceptSet().size());
            } else {
                sqlbuf.append(0);
            }
            sqlbuf.append(",");
            if (disambiguationPanel.getInputNounConceptSet() != null) {
                sqlbuf.append(disambiguationPanel.getInputNounConceptSet().size());
            } else {
                sqlbuf.append(0);
            }
            sqlbuf.append(",");
            if (disambiguationPanel.getInputVerbConceptSet() != null) {
                sqlbuf.append(disambiguationPanel.getInputVerbConceptSet().size());
            } else {
                sqlbuf.append(0);
            }
            sqlbuf.append(",");
            sqlbuf.append(constructClassPanel.getAddedSINNum());
            sqlbuf.append(",");
            sqlbuf.append(constructClassPanel.getBeforeTrimmingConceptNum());
            sqlbuf.append(",");
            sqlbuf.append(constructClassPanel.getTrimmedConceptNum());
            sqlbuf.append(",");
            int afterTrimmingConceptNum = constructClassPanel.getAfterTrimmingConceptNum();
            sqlbuf.append(afterTrimmingConceptNum);
            sqlbuf.append(",");
            sqlbuf.append(constructPropertyPanel.getAddedSINNum());
            sqlbuf.append(",");
            sqlbuf.append(constructPropertyPanel.getBeforeTrimmingConceptNum());
            sqlbuf.append(",");
            sqlbuf.append(constructPropertyPanel.getTrimmedConceptNum());
            sqlbuf.append(",");
            int afterTrimmingPropertyNum = constructPropertyPanel.getAfterTrimmingConceptNum();
            sqlbuf.append(afterTrimmingPropertyNum);
            sqlbuf.append(",");
            sqlbuf.append(constructClassPanel.getAddedAbstractComplexConceptCnt());
            sqlbuf.append(",");
            sqlbuf.append(constructClassPanel.getAverageAbstracComplexConceptGroupSiblingConceptCnt());
            sqlbuf.append(",");
            sqlbuf.append(constructPropertyPanel.getAddedAbstractComplexConceptCnt());
            sqlbuf.append(",");
            sqlbuf.append(constructPropertyPanel.getAverageAbstracComplexConceptGroupSiblingConceptCnt());
            sqlbuf.append(",");

            int lastClassNum = constructClassPanel.getAllConceptCnt();
            int lastPropertyNum = constructPropertyPanel.getAllConceptCnt();

            sqlbuf.append((lastClassNum - afterTrimmingConceptNum));
            sqlbuf.append(",");
            sqlbuf.append((lastPropertyNum - afterTrimmingPropertyNum));
            sqlbuf.append(",");
            sqlbuf.append(lastClassNum);
            sqlbuf.append(",");
            sqlbuf.append(lastPropertyNum);
            sqlbuf.append(",");
            sqlbuf.append(constructClassPanel.getChildCntAverage());
            sqlbuf.append(",");
            sqlbuf.append(constructPropertyPanel.getChildCntAverage());
            sqlbuf.append(",'");
            sqlbuf.append(DODDLEConstants.BASE_URI);
            sqlbuf.append("','");
            sqlbuf.append(projectComment);
            sqlbuf.append("')");
            stmt.executeUpdate(sqlbuf.toString());
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void removeProject() {
        int response = JOptionPane.showConfirmDialog(this, "Remove Project ?", "Remove Project",
                JOptionPane.OK_CANCEL_OPTION);
        if (response == JOptionPane.CANCEL_OPTION) { return; }
        DBManagerPanel.deleteTableContents(projectID, stmt, "general_ontology_info");
        DBManagerPanel.deleteTableContents(projectID, stmt, "doc_info");
        DBManagerPanel.deleteTableContents(projectID, stmt, "term_info");
        DBManagerPanel.deleteTableContents(projectID, stmt, "term_info_pos_list");
        DBManagerPanel.deleteTableContents(projectID, stmt, "term_info_doc_list");
        DBManagerPanel.deleteTableContents(projectID, stmt, "removed_term_info");
        DBManagerPanel.deleteTableContents(projectID, stmt, "removed_term_info_pos_list");
        DBManagerPanel.deleteTableContents(projectID, stmt, "removed_term_info_doc_list");
        DBManagerPanel.deleteTableContents(projectID, stmt, "input_term_set");
        DBManagerPanel.deleteTableContents(projectID, stmt, "term_eval_concept_set");
        DBManagerPanel.deleteTableContents(projectID, stmt, "eval_concept_set");
        DBManagerPanel.deleteTableContents(projectID, stmt, "input_term_concept_map");
        DBManagerPanel.deleteTableContents(projectID, stmt, "input_term_construct_tree_option");
        DBManagerPanel.deleteTableContents(projectID, stmt, "input_concept_set");
        DBManagerPanel.deleteTableContents(projectID, stmt, "construct_tree_option");
        DBManagerPanel.deleteTableContents(projectID, stmt, "undefined_term_set");
        saveOntologyAction.removeOWLOntology(projectID, icon);
        DBManagerPanel.deleteTableContents(projectID, stmt, "class_trimmed_result_analysis");
        DBManagerPanel.deleteTableContents(projectID, stmt, "trimmed_class_list");
        DBManagerPanel.deleteTableContents(projectID, stmt, "property_trimmed_result_analysis");
        DBManagerPanel.deleteTableContents(projectID, stmt, "trimmed_property_list");
        DBManagerPanel.deleteTableContents(projectID, stmt, "concept_definition_parameter");
        DBManagerPanel.deleteTableContents(projectID, stmt, "concept_definition");
        DBManagerPanel.deleteTableContents(projectID, stmt, "wrong_pair");
        DBManagerPanel.deleteTableContents(projectID, stmt, "wordspace_result");
        DBManagerPanel.deleteTableContents(projectID, stmt, "apriori_result");
        DBManagerPanel.deleteTableContents(projectID, stmt, "project_info");
        connectDB();
        System.out.println("remove project: " + projectID);
    }

    class UpdateProjectWorker extends SwingWorker implements java.beans.PropertyChangeListener {

        private int currentTaskCnt;

        public UpdateProjectWorker() {
            currentTaskCnt = 0;
            addPropertyChangeListener(this);
        }

        @Override
        protected Object doInBackground() throws Exception {
            DODDLE.STATUS_BAR.setLastMessage(projectName);
            try {
                DODDLEProject project = DODDLE.getCurrentProject();
                project.setTitle(projectName);
                OntologySelectionPanel ontSelectionPanel = project.getOntologySelectionPanel();
                DocumentSelectionPanel docSelectionPanel = project.getDocumentSelectionPanel();
                InputWordSelectionPanel inputWordSelectionPanel = project.getInputWordSelectionPanel();
                DisambiguationPanel disambiguationPanel = project.getDisambiguationPanel();
                ConceptDefinitionPanel conceptDefinitionPanel = project.getConceptDefinitionPanel();

                ontSelectionPanel.saveGeneralOntologyInfoToDB(projectID, stmt);
                setProgress(currentTaskCnt++);
                docSelectionPanel.saveDocumentInfo(projectID, stmt);
                setProgress(currentTaskCnt++);
                inputWordSelectionPanel.saveWordInfoTable(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.saveInputWordSetToDB(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.saveWordEvalConceptSet(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.saveWordCorrespondConceptSetMapToDB(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.saveInputWordConstructTreeOptionSetToDB(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.saveInputConceptSetToDB(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.saveConstructTreeOptionToDB(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.saveUndefinedWordSet(projectID, stmt);
                setProgress(currentTaskCnt++);
                saveOntologyAction.saveOWLOntology(projectID, icon, project);
                setProgress(currentTaskCnt++);

                project.getConstructClassPanel().getConceptDriftManagementPanel().saveTrimmedResultAnalysis(projectID,
                        stmt, "class_trimmed_result_analysis", "trimmed_class_list");
                setProgress(currentTaskCnt++);
                project.getConstructPropertyPanel().getConceptDriftManagementPanel().saveTrimmedResultAnalysis(
                        projectID, stmt, "property_trimmed_result_analysis", "trimmed_property_list");
                setProgress(currentTaskCnt++);

                conceptDefinitionPanel.saveConeptDefinitionParameters(projectID, stmt);
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.saveConceptDefinition(projectID, stmt);
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.saveWrongPairSet(projectID, stmt);
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.saveWordSpaceResult(projectID, stmt);
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.saveAprioriResult(projectID, stmt);
                setProgress(currentTaskCnt++);

                saveProjectInfo(projectID, project);
                setProgress(currentTaskCnt++);
                System.out.println("save db done");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DODDLE.STATUS_BAR.unLock();
                DODDLE.STATUS_BAR.hideProgressBar();
                connectDB();
            }

            return "done";
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof Integer) {
                DODDLE.STATUS_BAR.setValue(currentTaskCnt);
            }
        }

    }

    public void updateProject() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DODDLE.STATUS_BAR.setLastMessage("Update");
                DODDLE.STATUS_BAR.startTime();
                DODDLE.STATUS_BAR.initNormal(19);
                DODDLE.STATUS_BAR.lock();
                UpdateProjectWorker worker = new UpdateProjectWorker();
                DODDLE.STATUS_BAR.setSwingWorker(worker);
                worker.execute();
            }
        });
    }

    public void newProject() {
        NewProjectPanel newProjectPanel = new NewProjectPanel();
        if (newProjectPanel.isNewProject()) {
            System.out.println("last project id: " + lastProjectID);
            author = newProjectPanel.getAuthor();
            projectName = newProjectPanel.getProjectName();
            projectComment = newProjectPanel.getComment();
            projectID = lastProjectID + 1;
            updateProject();
        }
    }

    private void loadBaseURI(int projectID) {
        try {
            String sql = "SELECT * from project_info where Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String baseURI = rs.getString("Base_URI");
                DODDLEConstants.BASE_URI = baseURI;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    class OpenProjectWorker extends SwingWorker implements java.beans.PropertyChangeListener {

        private int currentTaskCnt;

        public OpenProjectWorker(int taskCnt) {
            currentTaskCnt = taskCnt;
            addPropertyChangeListener(this);
        }

        @Override
        protected Object doInBackground() throws Exception {
            while (!currentProject.isInitialized()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            DODDLE.STATUS_BAR.setLastMessage(projectName);
            setProgress(currentTaskCnt++);

            try {
                currentProject.setVisible(false);
                currentProject.setTitle(projectName);
                OntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
                DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
                DocumentSelectionPanel docSelectionPanel = currentProject.getDocumentSelectionPanel();
                InputWordSelectionPanel inputWordSelectionPanel = currentProject.getInputWordSelectionPanel();
                ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();

                loadBaseURI(projectID);
                setProgress(currentTaskCnt++);
                docSelectionPanel.loadDocuments(projectID, stmt);
                setProgress(currentTaskCnt++);
                ontSelectionPanel.loadGeneralOntologyInfo(projectID, stmt);
                setProgress(currentTaskCnt++);
                // ontSelectionPanel.loadOWLMetaDataSet(new File(openDir,
                // ProjectFileNames.OWL_META_DATA_SET_DIR));

                inputWordSelectionPanel.loadWordInfoTable(projectID, stmt, docSelectionPanel.getDocNum());
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadInputWordSet(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadWordEvalConceptSet(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadWordCorrespondConceptSetMap(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadConstructTreeOption(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadInputWordConstructTreeOptionSet(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadInputConceptSet(projectID, stmt);
                setProgress(currentTaskCnt++);
                disambiguationPanel.loadUndefinedWordSet(projectID, stmt);
                setProgress(currentTaskCnt++);
                loadOntologyAction.loadOWLOntology(projectID, icon, currentProject);
                setProgress(currentTaskCnt++);

                ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
                ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
                constructClassPanel.getConceptDriftManagementPanel().loadTrimmedResultAnalysis(projectID, stmt,
                        "class_trimmed_result_analysis", "trimmed_class_list");
                setProgress(currentTaskCnt++);
                constructPropertyPanel.getConceptDriftManagementPanel().loadTrimmedResultAnalysis(projectID, stmt,
                        "property_trimmed_result_analysis", "trimmed_property_list");
                setProgress(currentTaskCnt++);

                conceptDefinitionPanel.setInputDocList();
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.loadConceptDefinitionParameters(projectID, stmt);
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.loadWordSpaceResult(projectID, stmt);
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.loadAprioriResult(projectID, stmt);
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.loadConceptDefinition(projectID, stmt);
                setProgress(currentTaskCnt++);
                conceptDefinitionPanel.loadWrongPairSet(projectID, stmt);
                setProgress(currentTaskCnt++);

                disambiguationPanel.selectTopList();
                constructClassPanel.expandIsaTree();
                constructClassPanel.expandHasaTree();
                constructPropertyPanel.expandIsaTree();
                System.out.println("load db done");
            } finally {
                try {
                    currentProject.setXGALayout();
                    currentProject.setVisible(true);
                    currentProject.setMaximum(true);
                } catch (java.beans.PropertyVetoException pve) {
                    pve.printStackTrace();
                }
                setProgress(currentTaskCnt++);
                DODDLE.STATUS_BAR.unLock();
                DODDLE.STATUS_BAR.hideProgressBar();
            }

            return "done";
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof Integer) {
                DODDLE.STATUS_BAR.setValue(currentTaskCnt);
            }
        }

    }

    public void openProject() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                OpenProjectWorker worker = null;
                if (DODDLE.isExistingCurrentProject()) {
                    currentProject = DODDLE.getCurrentProject();
                    DODDLE.STATUS_BAR.setLastMessage("Open");
                    DODDLE.STATUS_BAR.startTime();
                    DODDLE.STATUS_BAR.initNormal(22);
                    DODDLE.STATUS_BAR.lock();
                    worker = new OpenProjectWorker(0);
                } else {
                    currentProject = new DODDLEProject("new", 33);
                    worker = new OpenProjectWorker(11);
                }
                DODDLE.STATUS_BAR.setSwingWorker(worker);
                worker.execute();
            }
        });
    }

    public void editProjectInfo() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String modificationDate = dateFormat.format(Calendar.getInstance().getTime());
            String sql = "UPDATE project_info SET Project_Name='" + projectNameField.getText() + "',Author='"
                    + authorField.getText() + "',Modification_Date='" + modificationDate + "',Comment='"
                    + commentArea.getText() + "' WHERE Project_ID=" + projectID;
            System.out.println(sql);
            stmt.executeUpdate(sql);
            connectDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        int selectedRow = projectInfoTable.getSelectedRow();
        if (selectedRow != -1) {
            projectID = (Integer) projectInfoTable.getValueAt(selectedRow, 0);
            projectName = (String) projectInfoTable.getValueAt(selectedRow, 1);
            author = (String) projectInfoTable.getValueAt(selectedRow, 2);
            projectComment = commentArea.getText();
        }
        if (e.getSource() == openProjectButton) {
            openProject();
        } else if (e.getSource() == updateProjectButton) {
            updateProject();
        } else if (e.getSource() == newProjectButton) {
            newProject();
        } else if (e.getSource() == removeProjectButton) {
            removeProject();
        } else if (e.getSource() == connectButton) {
            connectDB();
        } else if (e.getSource() == editCommentButton) {
            editProjectInfo();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int selectedRow = projectInfoTable.getSelectedRow();
        if (selectedRow != -1) {
            projectID = (Integer) projectInfoTable.getValueAt(selectedRow, 0);
        }
        try {
            String sql = "SELECT * from project_info WHERE Project_ID=" + projectID;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                projectNameField.setText(rs.getString("Project_Name"));
                authorField.setText(rs.getString("Author"));
                commentArea.setText(rs.getString("Comment"));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public class NewProjectPanel extends JDialog implements ActionListener {
        private JLabel projectNameLabel;
        private JTextField projectNameField;
        private JLabel authorLabel;
        private JTextField authorField;
        private JTextArea commentArea;

        private JButton okButton;
        private JButton cancelButton;

        private boolean isNewProject;

        public NewProjectPanel() {
            super(DODDLE.rootFrame, "New Project", true);
            projectNameLabel = new JLabel("Project Name");
            projectNameField = new JTextField(20);
            authorLabel = new JLabel("Author");
            authorField = new JTextField(20);
            commentArea = new JTextArea();
            JScrollPane commentAreaScroll = new JScrollPane(commentArea);
            commentAreaScroll.setBorder(BorderFactory.createTitledBorder("Project Comment"));

            JPanel northPanel = new JPanel();
            northPanel.setLayout(new GridLayout(2, 2));
            northPanel.add(projectNameLabel);
            northPanel.add(projectNameField);
            northPanel.add(authorLabel);
            northPanel.add(authorField);

            okButton = new JButton("OK");
            okButton.addActionListener(this);
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(this);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            JPanel southPanel = new JPanel();
            southPanel.setLayout(new BorderLayout());
            southPanel.add(buttonPanel, BorderLayout.EAST);

            getContentPane().add(northPanel, BorderLayout.NORTH);
            getContentPane().add(commentAreaScroll, BorderLayout.CENTER);
            getContentPane().add(southPanel, BorderLayout.SOUTH);
            setSize(400, 250);
            setLocationRelativeTo(DODDLE.rootFrame);
            setVisible(true);
        }

        public String getAuthor() {
            return authorField.getText();
        }

        public String getProjectName() {
            return projectNameField.getText();
        }

        public String getComment() {
            return commentArea.getText();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == okButton) {
                isNewProject = true;
                setVisible(false);
            } else if (e.getSource() == cancelButton) {
                isNewProject = false;
                setVisible(false);
            }
        }

        public boolean isNewProject() {
            return isNewProject;
        }
    }

}
