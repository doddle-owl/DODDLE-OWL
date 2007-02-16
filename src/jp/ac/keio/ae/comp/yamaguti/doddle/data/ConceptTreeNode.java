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

/**
 * 
 * @author mioki
 * 
 * modified by takeshi morita
 */
public class ConceptTreeNode extends DefaultMutableTreeNode {

    private String uri;
    private Concept concept;
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
            setInputWord(c);
            concept = c;
        }
        trimmedConceptList = new ArrayList<List<Concept>>();
        trimmedConceptList.add(new ArrayList<Concept>());
    }

    public ConceptTreeNode(ConceptTreeNode ctn, DODDLEProject p) {
        super();
        project = p;
        Concept c = ctn.getConcept();
        uri = c.getURI();
        if (project.getConcept(uri) == null) {
            project.putConcept(uri, c);
            setInputWord(c);
            concept = c;
            isInputConcept = ctn.isInputConcept();
            isUserConcept = ctn.isUserConcept();
        }
        trimmedConceptList = new ArrayList<List<Concept>>();
        trimmedConceptList.add(new ArrayList<Concept>());
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

    /**
     * 代表見出しをDBを使用している時に設定するためのメソッド． 少し，効率が悪いのと，複合語概念について適切に
     * 動作しているかどうかをテストする必要がある．
     */
    private void setInputWord(Concept c) {
        if (DODDLE.IS_USING_DB) {
            Map wordCorrespondConceptSetMap = project.getDisambiguationPanel().getWordCorrespondConceptSetMap();
            for (Iterator i = wordCorrespondConceptSetMap.entrySet().iterator(); i.hasNext();) {
                Entry entry = (Entry) i.next();
                String word = (String) entry.getKey();
                Set<Concept> conceptSet = (Set<Concept>) entry.getValue();
                if (conceptSet != null) {
                    // 辞書中に存在するかどうかをチェックしないと，複合語が見出しになる場合がある
                    for (Concept concept : conceptSet) {
                        if (concept != null && concept.equals(c) && DODDLEDic.getIDSet(word) != null) {
                            c.setInputWord(word);
                            break;
                        }
                    }
                }
            }
        }
    }

    public Concept getConcept() {
        return project.getConcept(uri);
    }

    public void setConcept(Concept c) {
        concept = c;
        project.putConcept(c.getURI(), c);
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
        return getConcept().getInputWord();
    }

    public String getURIStr() {
        String ns =getConcept().getNameSpace(); 
        if (ns.equals(DODDLE.EDR_URI) || ns.equals(DODDLE.EDRT_URI) || ns.equals(DODDLE.WN_URI)) {
            if (isUserConcept) { return getConcept().getNameSpace() + "UID" + getConcept().getLocalName(); }
            return getConcept().getNameSpace() + getConcept().getLocalName();
        }
        return getURI();
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
        if (OptionDialog.isShowPrefix()) { return getConcept().toString(); }
        if (getConcept() == null) { return ""; }
        return getConcept().getWord();
    }
}