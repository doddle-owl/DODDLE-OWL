package jp.ac.keio.ae.comp.yamaguti.doddle.data;

/*
 * Created on 2003/09/08
 * 
 */

import java.util.*;
import java.util.Map.*;

import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * 
 * @author mioki
 * 
 * modified by takeshi morita
 */
public class ConceptTreeNode extends DefaultMutableTreeNode {

    private String conceptID;

    private boolean isInputConcept;
    private boolean isUserConcept;
    private boolean isMultipleInheritance;

    private List<Concept> trimmedConceptList;

    private DODDLEProject project;

    public ConceptTreeNode(Concept c, DODDLEProject p) {
        super();
        project = p;
        if (project.getConcept(c.getId()) == null) {
            setInputWord(c);
            project.putConcept(c.getId(), c);
        }
        conceptID = c.getId();
        trimmedConceptList = new ArrayList<Concept>();
    }

    public void addTrimmedConcept(Concept c) {
        trimmedConceptList.add(c);
    }

    public void addAllTrimmedConcept(List<Concept> list) {
        trimmedConceptList.addAll(list);
    }

    public List<Concept> getTrimmedConceptList() {
        return trimmedConceptList;
    }

    /**
     * 代表見出しをDBを使用している時に設定するためのメソッド． 少し，効率が悪いのと，複合語概念について適切に
     * 動作しているかどうかをテストする必要がある．
     */
    private void setInputWord(Concept c) {
        if (DODDLE.IS_USING_DB) {
            Map wordConceptMap = project.getInputModuleUI().getWordConceptMap();
            for (Iterator i = wordConceptMap.entrySet().iterator(); i.hasNext();) {
                Entry entry = (Entry) i.next();
                String word = (String) entry.getKey();
                Concept concept = (Concept) entry.getValue();
                // if (concept.equals(c) && EDRDic.getEDRIDSet(word) != null) {
                if (concept.equals(c)) {
                    c.setInputWord(word);
                    break;
                }
            }
        }
    }

    public Concept getConcept() {
        return project.getConcept(conceptID);
    }

    public void setConcept(Concept c) {
        conceptID = c.getId();
        project.putConcept(conceptID, c);
    }

    public void removeRelation() {
        this.removeAllChildren();
        this.removeFromParent();
    }

    public String getJpWord() {
        return getConcept().getJaWord();
    }

    public String[] getJpWords() {
        return getConcept().getJaWords();
    }

    public String getEnWord() {
        return getConcept().getEnWord();
    }

    public String[] getEnWords() {
        return getConcept().getEnWords();
    }

    public String getJpExplanation() {
        return getConcept().getJaExplanation();
    }

    public String getEnExplanation() {
        return getConcept().getEnExplanation();
    }

    /*
     * public void setTrimmedCount(int i) { trimmedNum = i; }
     */

    public int getTrimmedCount() {
        // return trimmedNum;
        return trimmedConceptList.size();
    }

    /*
     * public void setdepth(int d) { depth = d; }
     * 
     * public int getdepth() { return depth; }
     */
    public String getPrefix() {
        return getConcept().getPrefix();
    }

    public String getId() {
        return conceptID;
    }

    public String getIdentity() {
        return getConcept().getIdentity();
    }

    public String getInputWord() {
        return getConcept().getInputWord();
    }

    public String getIdStr() {
        if (isUserConcept) { return getConcept().getId(); }
        return "ID" + getConcept().getId();
    }

    public void setIsInputConcept(boolean t) {
        isInputConcept = t;
    }

    public boolean isInputConcept() {
        return isInputConcept;
    }

    public boolean isSINNode() {
        return !(isInputConcept || isUserConcept);
    }

    public void setIsMultipleInheritance(boolean t) {
        isMultipleInheritance = t;
    }

    public boolean isMultipleInheritance() {
        return isMultipleInheritance;
    }

    public boolean isUserConcept() {
        return isUserConcept;
    }

    public void setIsUserConcept(boolean t) {
        isUserConcept = t;
    }

    public String toString() {
        if (OptionDialog.isShowPrefix()) { return getConcept().getPrefix() + ":" + getConcept().getWord(); }
        return getConcept().getWord();
    }
}