package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

/**
 * @author takeshi morita
 */
public class ConstructTreeOption {

    private Concept c;
    private String option;
    private boolean isReplaceSubConcepts;

    public ConstructTreeOption(Concept c) {
        this.c = c;
        if (OptionDialog.isCompoundWordSetSameConcept()) {
            option = "SAME";
        } else {
            option = "SUB";
        }
    }

    public ConstructTreeOption(Concept c, String opt) {
        this.c = c;
        option = opt;
    }

    public void setConcept(Concept c) {
        this.c = c;
    }

    public Concept getConcept() {
        return c;
    }

    public void setOption(String opt) {
        option = opt;
    }

    public String getOption() {
        return option;
    }

    public boolean isReplaceSubConcepts() {
        return isReplaceSubConcepts;
    }

    public void setIsReplaceSubConcepts(boolean t) {
        isReplaceSubConcepts = t;
    }
    
    public String toString() {
        return "option: "+option+" concept: "+c+" is Replace Sub Concept: "+isReplaceSubConcepts;
    }
}
