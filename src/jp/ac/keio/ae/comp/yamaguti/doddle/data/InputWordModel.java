package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

/*
 * @(#)  2005/03/16
 *
 */

/**
 * @author takeshi morita
 */
public class InputWordModel implements Comparable {

    private int matchedPoint;
    private int ambiguousCnt;
    private String inputWord;
    private String matchedInputWord;
    private List<String> basicWordList;
    private List<String> orgWordList;
    private String wordListStr;
    private boolean isSystemAdded;

    private DODDLEProject project;

    public InputWordModel(String w, List<String> orgWl, List<String> bsWl,  String miw, int ac, int mp, DODDLEProject p) {
        project = p;
        inputWord = w;
        orgWordList = orgWl;
        basicWordList = bsWl;        
        matchedInputWord = miw;
        StringBuffer buf = new StringBuffer("(");
        for (Iterator i = basicWordList.iterator(); i.hasNext();) {
            String word = (String) i.next();
            if (i.hasNext()) {
                buf.append(word + "+");
            } else {
                buf.append(word + ")");
            }
        }
        wordListStr = buf.toString();
        ambiguousCnt = ac;
        matchedPoint = mp;
    }

    public void setIsSystemAdded(boolean t) {
        isSystemAdded = t;
    }

    public boolean isSystemAdded() {
        return isSystemAdded;
    }

    // 部分照合かどうか
    public boolean isPartialMatchWord() {
        // 1 < wordList.size()の条件を2006/10/5に追加
        // 「打合せ」が「打合す」と照合してしまうため
        return !inputWord.equals(matchedInputWord) && 1 < basicWordList.size(); 
    }

    // 完全照合かどうか
    public boolean isPerfectMatchWord() {
        return !isPartialMatchWord();
    }

    public int compareTo(Object o) {
        InputWordModel oiwModel = (InputWordModel) o;
        int onum = oiwModel.getAmbiguousCnt();
        String oword = oiwModel.getWord();
        if (this.ambiguousCnt < onum) {
            return 1;
        } else if (this.ambiguousCnt > onum) {
            return -1;
        } else {
            return oword.compareTo(inputWord);
        }
    }

    public String getWord() {
        return inputWord;
    }

    public String getMatchedWord() {
        return matchedInputWord;
    }

    public int getMatchedPoint() {
        return matchedPoint;
    }

    public int getAmbiguousCnt() {
        return ambiguousCnt;
    }

    public List<String> getBasicWordList() {
        return basicWordList;
    }
    
    public List<String> getOrgWordList() {
        return orgWordList;
    }

    public String getTopBasicWord() {
        return basicWordList.get(0);
    }

    public String getBasicWordWithoutTopWord() {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < basicWordList.size(); i++) {
            builder.append(basicWordList.get(i));
        }
        return builder.toString();
    }

    public int getComplexWordLength() {
        return basicWordList.size();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(inputWord);
        if (isPartialMatchWord() && project.isPartialMatchedComplexWordCheckBox()) {
            buf.append(" " + wordListStr);
        }
        if (isPartialMatchWord() && project.isPartialMatchedMatchedWordBox()) {
            buf.append(" (" + matchedInputWord + ") ");
        }
        if (isPartialMatchWord() && project.isPartialMatchedAmbiguityCntCheckBox()) {
            buf.append(" (" + ambiguousCnt + ")");
        }
        if (isPerfectMatchWord() && project.isPerfectMatchedAmbiguityCntCheckBox()) {
            buf.append(" (" + ambiguousCnt + ")");
        }
        if (isSystemAdded() && project.isPerfectMatchedSystemAddedWordCheckBox()) {
            buf.append(" (added)");
        }
        return buf.toString();
    }
}
