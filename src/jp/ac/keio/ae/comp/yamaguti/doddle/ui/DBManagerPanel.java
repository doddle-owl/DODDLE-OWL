/*
 * @(#)  2007/09/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.actions.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import com.hp.hpl.jena.db.*;

/**
 * @author takeshi morita
 */
public class DBManagerPanel extends JDialog implements ActionListener {

    private JButton openDBButton;
    private JButton saveDBButton;
    
    private SaveOntologyAction saveOntologyAction;

    public DBManagerPanel() {
        openDBButton = new JButton("OpenDB");
        openDBButton.addActionListener(this);
        saveDBButton = new JButton("SaveDB");
        saveDBButton.addActionListener(this);
        
        saveOntologyAction = new SaveOntologyAction("", SaveOntologyAction.OWL_ONTOLOGY);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openDBButton);
        buttonPanel.add(saveDBButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setSize(new Dimension(800, 600));
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
    
    public void saveProjectInfo(int projectID, String author, String projectName, Statement stmt, DODDLEProject currentProject) {
        OntologySelectionPanel ontSelectionPanel = currentProject.getOntologySelectionPanel();
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        try {
            StringBuffer sqlbuf = new StringBuffer();
            sqlbuf.append("INSERT INTO project_info (Project_ID,Project_Name,Author,Creation_Date," +
            		"Modification_Date,Available_General_Ontologies,Input_Term_Count,Perfectly_Matched_Term_Count," +
            		"System_Added_Perfectly_Matched_Term_Count,Partially_Matched_Term_Count," +
            		"Matched_Term_Count,Undefined_Term_Count,Input_Concept_Count,Input_Noun_Concept_Count," +
            		"Input_Verb_Concept_Count,Class_SIN_Count,Before_Trimming_Class_Count," +
            		"Trimmed_Class_Count,After_Trimming_Class_Count,Property_SIN_Count," +
            		"Before_Trimming_Property_Count,Trimmed_Property_Count,After_Trimming_Property_Count," +
            		"Abstract_Internal_Class_Count,Average_Abstract_Sibling_Concept_Count_In_Classes," +
            		"Abstract_Internal_Property_Count_Message,Average_Abstract_Sibling_Concept_Count_In_Properties," +
            		"Class_From_Compound_Word_Count,Property_From_Compound_Word_Count,Total_Class_Count," +
            		"Total_Property_Count,Average_Sibling_Classes,Average_Sibling_Properties,Base_URI) ");
            sqlbuf.append("VALUES(");

            sqlbuf.append(projectID);
            sqlbuf.append(",'");
            sqlbuf.append(projectName);
            sqlbuf.append("','");
            sqlbuf.append(author);
            sqlbuf.append("','");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sqlbuf.append(dateFormat.format(Calendar.getInstance().getTime()));
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
            sqlbuf.append("')");
            System.out.println(sqlbuf);
            stmt.executeUpdate(sqlbuf.toString());
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void saveDB(int projectID) {
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
            String host = "zest.comp.ae.keio.ac.jp";
            String dbName = "doddle";
            String url = "jdbc:mysql://" + host + "/" + dbName + "?useUnicode=true&characterEncoding=UTF-8";
            String projectName = "sample project";
            String userName = "t_morita";
            String passWord = "t_morita.pass";
            Connection con = DriverManager.getConnection(url, userName, passWord);
            Statement stmt = con.createStatement();

            DODDLEProject project = DODDLE.getCurrentProject();
            OntologySelectionPanel ontSelectionPanel = project.getOntologySelectionPanel();
            DocumentSelectionPanel docSelectionPanel = project.getDocumentSelectionPanel();
            InputWordSelectionPanel inputWordSelectionPanel = project.getInputWordSelectionPanel();
            DisambiguationPanel disambiguationPanel = project.getDisambiguationPanel();
            ConceptDefinitionPanel conceptDefinitionPanel = project.getConceptDefinitionPanel();

            ontSelectionPanel.saveGeneralOntologyInfoToDB(projectID, stmt);
            docSelectionPanel.saveDocumentInfo(projectID, stmt);
            inputWordSelectionPanel.saveWordInfoTable(projectID, stmt);
            disambiguationPanel.saveInputWordSetToDB(projectID, stmt);
            disambiguationPanel.saveWordEvalConceptSet(projectID, stmt);
            disambiguationPanel.saveWordCorrespondConceptSetMapToDB(projectID, stmt);
            disambiguationPanel.saveInputWordConstructTreeOptionSetToDB(projectID, stmt);
            disambiguationPanel.saveInputConceptSetToDB(projectID, stmt);
            disambiguationPanel.saveConstructTreeOptionToDB(projectID, stmt);
            disambiguationPanel.saveUndefinedWordSet(projectID, stmt);
            IDBConnection icon = new DBConnection(url, userName, passWord, "MySQL");
            saveOntologyAction.saveOWLOntology(projectID, icon, project);
            
            project.getConstructClassPanel().getConceptDriftManagementPanel().saveTrimmedResultAnalysis(
                    projectID, stmt, "class_trimmed_result_analysis", "trimmed_class_list");
            project.getConstructPropertyPanel().getConceptDriftManagementPanel().saveTrimmedResultAnalysis(
                     projectID, stmt, "property_trimmed_result_analysis", "trimmed_property_list");
            
            conceptDefinitionPanel.saveConeptDefinitionParameters(projectID, stmt);
            conceptDefinitionPanel.saveConceptDefinition(projectID, stmt);
            conceptDefinitionPanel.saveWrongPairSet(projectID, stmt);
            conceptDefinitionPanel.saveWordSpaceResult(projectID, stmt);
            conceptDefinitionPanel.saveAprioriResult(projectID, stmt);
            
            saveProjectInfo(projectID, userName, projectName, stmt, project);
            
            String sql = "SELECT * from doc_info";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("Project_ID");
                String path = rs.getString("Doc_Path");
                System.out.println(id + " " + URLDecoder.decode(path, "UTF8"));
            }
            stmt.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openDB(int projectID) {
        System.out.println("open project");
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openDBButton) {
            openDB(1);
        } else if (e.getSource() == saveDBButton) {
            saveDB(1);
        }
    }

}
