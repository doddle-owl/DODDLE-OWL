package jp.ac.keio.ae.comp.yamaguti.doddle.data;

/*
 * Created on 2003/09/08
 * 
 */

import java.util.*;

import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

/**
 * 
 * @author mioki
 * 
 * modified by takeshi morita
 */
public class ConceptTreeNode extends DefaultMutableTreeNode implements Comparable {

    private String uri;
    private boolean isInputConcept;
    private boolean isUserConcept;
    private boolean isMultipleInheritance;

    private List<List<Concept>> trimmedConceptList;

    private DODDLEProject project;

    public ConceptTreeNode(Concept c, DODDLEProject p) {
        super();
        project = p;
        uri = c.getURI();
        if (project.getConcept(uri) == null) {
            project.putConcept(uri, c);
        }
        initTrimmedConceptList();
    }

    public void initTrimmedConceptList() {
        trimmedConceptList = new ArrayList<List<Concept>>();
        trimmedConceptList.add(new ArrayList<Concept>());
    }

    /**
     * コピー用のコンストラクタ このコンストラクタ内では，初期化処理はしないこと
     */
    public ConceptTreeNode(ConceptTreeNode ctn, DODDLEProject p) {
        super();
        project = p;
        Concept c = ctn.getConcept();
        uri = c.getURI();
        if (project.getConcept(uri) == null) {
            project.putConcept(uri, c);
        }
        isInputConcept = ctn.isInputConcept();
        isUserConcept = ctn.isUserConcept();
        isMultipleInheritance = ctn.isMultipleInheritance();
        trimmedConceptList = ctn.getTrimmedConceptList();
    }

    public void addTrimmedConcept(Concept c) {
        trimmedConceptList.get(0).add(c);
    }

    public void addAllTrimmedConcept(List<Concept> list) {
        trimmedConceptList.get(0).addAll(list);
    }

    public void addTrimmedConceptList(List<Concept> list) {
        trimmedConceptList.add(list);
    }

    public void setTrimmedConceptList(List<List<Concept>> list) {
        trimmedConceptList = list;
    }

    public List<List<Concept>> getTrimmedConceptList() {
        return trimmedConceptList;
    }

    public Concept getConcept() {
        return project.getConcept(uri);
    }

    public void setConcept(Concept c) {
        project.putConcept(c.getURI(), c);
    }

    public void removeRelation() {
        this.removeAllChildren();
        this.removeFromParent();
    }

    public Map<String, List<DODDLELiteral>> getLangLabelLiteralListMap() {
        return getConcept().getLangLabelListMap();
    }

    public Map<String, List<DODDLELiteral>> getLangDescriptionLiteralListMap() {
        return getConcept().getLangDescriptionListMap();
    }

    public List<Integer> getTrimmedCountList() {
        List<Integer> trimmedCountList = new ArrayList<Integer>();
        for (List<Concept> tcList : trimmedConceptList) {
            trimmedCountList.add(tcList.size());
        }
        return trimmedCountList;
    }

    public String getURI() {
        return getConcept().getURI();
    }

    public String getInputWord() {
        if (getConcept().getInputLabel() != null) { return getConcept().getInputLabel().getString(); }
        return "";
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
        if (OptionDialog.isShowQName()) { return getConcept().toString(); }
        if (getConcept() == null) { return ""; }
        return getConcept().getWord();
    }

    public int compareTo(Object o) {
        ConceptTreeNode node = (ConceptTreeNode) o;
        return getConcept().getWord().compareTo(node.getConcept().getWord());
    }
}