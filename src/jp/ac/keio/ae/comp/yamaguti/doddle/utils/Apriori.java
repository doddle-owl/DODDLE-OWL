package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import net.java.sen.*;
import net.java.sen.dictionary.*;

/**
 * @author Yoshihiro Shigeta
 * 
 * 2004-07-26 modified by takeshi morita (mail: t_morita@ae.keio.ac.jp)
 */
public class Apriori {

    private List lineList;

    private Set pairSet;
    private List allRelation;
    private Map aprioriResult;
    private Map indexPairAppearence;
    private double minSupport;
    private double minConfidence;
    private List<String> inputWordList;

    private Document document;
    private ConceptDefinitionPanel conceptDefinitionPanel;

    public Apriori(ConceptDefinitionPanel cdp, Document doc) {
        document = doc;
        conceptDefinitionPanel = cdp;
        pairSet = new HashSet();
        aprioriResult = new HashMap();
        indexPairAppearence = new HashMap();
        allRelation = new ArrayList();
        inputWordList = conceptDefinitionPanel.getInputTermList();
        makeLineList();
    }

    public Document getDocument() {
        return document;
    }

    public void setParameters(double mins, double minc) {
        minSupport = mins;
        minConfidence = minc;
    }

    // 1行単位での形態素解析
    private List getJaLineWordList(String line) {
        List lineWordList = new ArrayList();
        try {
            StringTagger tagger = SenFactory.getStringTagger(DODDLEConstants.GOSEN_CONFIGURATION_FILE);
            List<Token> tokenList = tagger.analyze(line);
            if (tokenList == null) { return lineWordList; }
            for (Token token: tokenList) {
                String basicStr = token.getMorpheme().getBasicForm();
                lineWordList.add(basicStr);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        Utils.addJaCompoundWord(lineWordList, inputWordList); // 複合語の追加

        return lineWordList;
    }

    // 1行単位での形態素解析
    private List getEnLineWordList(String line) {
        line = line.replaceAll("\\.|．", "");
        List<String> lineWordList = new ArrayList<String>();
        for (String lineWord : line.split("\\s+")) {
            lineWordList.add(lineWord.toLowerCase());
        }
        Utils.addEnCompoundWord(lineWordList, inputWordList); // 複合語の追加
        return lineWordList;
    }

    /**
     * 入力概念がセットされた時に一度だけ実行され， 入力単語リストのリストを生成する
     */
    private void makeLineList() {
        lineList = new ArrayList();
        //String corpusString = DocumentSelectionPanel.getTextString(document);
        String corpusString = document.getText();
        if (corpusString == null) { return; }
        String[] lines = corpusString.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (document.getLang().equals("en")) {
                lineList.add(getEnLineWordList(lines[i]));
            } else if (document.getLang().equals("ja")) {
                lineList.add(getJaLineWordList(lines[i]));
            }
        }
    }

    /**
     * corpusStringは，あらかじめピリオドまたは丸で改行されているものとする
     * 
     */
    public Map calcAprioriResult(List<String> targetInputWordList) {
        pairSet.clear();
        allRelation.clear();
        aprioriResult.clear();
        if (targetInputWordList == null) { return aprioriResult; }
        int conceptAppearence[] = new int[targetInputWordList.size()];

        List itemList = new ArrayList();
        int lineNum = lineList.size();
        // System.out.println("line_num: " + lineNum);
        for (Iterator i = lineList.iterator(); i.hasNext();) {
            List lineWordList = (List) i.next();
            for (Iterator j = lineWordList.iterator(); j.hasNext();) {
                String lineWord = (String) j.next();
                for (int k = 0; k < targetInputWordList.size(); k++) {
                    String word = targetInputWordList.get(k);
                    if (word.equals(lineWord)) {
                        itemList.add(new Integer(k));
                        ++conceptAppearence[k];
                        break;
                    }
                }
            }
            // System.out.println(itemList);
            culAprioriPair(itemList);
            itemList.clear();
        }
        DODDLE.STATUS_BAR.addProjectValue();
        makePair(conceptAppearence, lineNum, targetInputWordList);
        DODDLE.STATUS_BAR.addProjectValue();
        return aprioriResult;
    }

    private void culAprioriPair(List itemList) {
        for (int i = 0; i < itemList.size(); i++) {
            for (int j = 0; j < itemList.size(); j++) {
                if (i != j) {
                    List pair = new ArrayList();
                    pair.add(itemList.get(i));
                    pair.add(itemList.get(j));
                    pairSet.add(pair);
                    if (indexPairAppearence.containsKey(pair)) {
                        indexPairAppearence.put(pair, new Integer(
                                ((Integer) indexPairAppearence.get(pair)).intValue() + 1));
                    } else {
                        indexPairAppearence.put(pair, new Integer(1));
                    }
                }
            }
        }
    }

    public void setConfidence(double dou) {
        minConfidence = dou;
    }

    /**
     * 
     * conceptAppearance ...
     * 全文の中にconceptData.getConcepts()リストの番号に対応する概念がいくつ出現したかを保存
     * concept[A|B]Support ... ある概念の出現回数/全文の数(いくつの文に含まれていたか．一回でも出現すればよい）
     * pairAppearance ... 全文の中である概念対が出現する回数
     * 
     * @param conceptAppearence
     * @param lineNum
     */
    private void makePair(int conceptAppearence[], double lineNum, List inputWordList) {
        List rpList;
        for (Iterator i = pairSet.iterator(); i.hasNext();) {
            List pair = (List) i.next();
            int conceptAIndex = ((Integer) pair.get(0)).intValue();
            String word1 = (String) inputWordList.get(conceptAIndex);
            int conceptBIndex = ((Integer) pair.get(1)).intValue();
            String word2 = (String) inputWordList.get(conceptBIndex);
            // System.out.println(conceptAIndex + ":" + conceptBIndex);
            double conceptASupport = conceptAppearence[conceptAIndex] / lineNum;
            double conceptBSupport = conceptAppearence[conceptBIndex] / lineNum;

            // System.out.println(conceptAppearence[conceptAIndex]+"/"+lines+
            // "=" +conceptASupport);
            // System.out.println(conceptAppearence[conceptBIndex]+"/"+lines+
            // "=" +conceptBSupport);

            double aprioriValue = 0;
            if (conceptASupport >= minSupport && conceptBSupport >= minSupport) {
                int pairAppearence = ((Integer) indexPairAppearence.get(pair)).intValue();
                aprioriValue = (double) pairAppearence / (double) conceptAppearence[conceptAIndex];
            }

            if (aprioriValue > minConfidence) {
                ConceptPair rp = new ConceptPair(word1, word2, new Double(aprioriValue));
                // rp.setrelationValue(value);
                // System.out.println(conceptA + "<>" + rp.toString());
                // System.out.println("Apriori:" + rp.toString());
                allRelation.add(rp);

                if (aprioriResult.containsKey(word1)) {
                    rpList = (List) (aprioriResult.get(word1));
                    rpList.add(rp);
                    aprioriResult.put(word1, rpList);
                } else {
                    rpList = new ArrayList();
                    rpList.add(rp);
                    aprioriResult.put(word1, rpList);
                }
            }
        }
    }

    public Map getAprioriResult() {
        return aprioriResult;
    }

    public List getAllRelation() {
        return allRelation;
    }
}