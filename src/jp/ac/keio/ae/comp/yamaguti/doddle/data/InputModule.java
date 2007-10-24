package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;
import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.java.sen.*;
import net.java.sen.dictionary.*;

/**
 * 
 * @author takeshi morita
 * 
 */
public class InputModule {

    private Set<InputTermModel> inputTermModelSet;
    private Map<String, Set<Concept>> termConceptSetMap;
    private Set<String> undefinedTermSet;

    public static int INIT_PROGRESS_VALUE = 887253;
    private DODDLEProject project;

    public InputModule(DODDLEProject p) {
        project = p;
        inputTermModelSet = new TreeSet<InputTermModel>();
        termConceptSetMap = new HashMap<String, Set<Concept>>();
        undefinedTermSet = new TreeSet<String>();
    }

    static class WordIDsLinesComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String l1 = (String) o1;
            String l2 = (String) o2;
            String w1 = l1.split("\t")[0];
            String w2 = l2.split("\t")[0];
            return w1.compareTo(w2);
        }
    }

    static class IDDefinitionLinesComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String l1 = (String) o1;
            String l2 = (String) o2;
            return l1.compareTo(l2);
        }
    }

    private void clearData() {
        inputTermModelSet.clear();
        undefinedTermSet.clear();
        termConceptSetMap.clear();
    }

    public InputTermModel makeInputTermModel(String iw) {
        Map<String, Set<Concept>> wcSetMap = null;
        if (DODDLEConstants.IS_USING_DB) {
            wcSetMap = EDRDic.getEDRDBManager().getTermConceptSetMap();
        } else {
            wcSetMap = termConceptSetMap;
        }
        return makeInputTermModel(iw, wcSetMap);
    }

    /**
     * 
     * 複合語の先頭の形態素から除いていき照合を行う．
     */
    public InputTermModel makeInputTermModel(String iw, Map<String, Set<Concept>> wcSetMap) {
        if (iw.length() == 0) { return null; }
        List<Token> tokenList = getTokenList(iw);
        StringBuilder subIW = null;
        Set<Concept> conceptSet = null;
        boolean isEnglish = isEnglish(iw);
        int matchedPoint = 0;
        for (int i = 0; i < tokenList.size(); i++) {
            List<Token> subList = tokenList.subList(i, tokenList.size());
            subIW = new StringBuilder();
            for (Token morpheme : subList) {
                if (isEnglish) {
                    //subIW.append(morpheme.getBasicString() + " ");
                    subIW.append(morpheme.getMorpheme().getBasicForm() + " ");
                } else {
                    //subIW.append(morpheme.getBasicString());
                    subIW.append(morpheme.getMorpheme().getBasicForm());
                }
            }
            if (isEnglish) {
                subIW.deleteCharAt(subIW.length() - 1);
            }
            conceptSet = getConceptSet(subIW.toString());
            if (0 < conceptSet.size()) {
                matchedPoint = i;
                break;
            }
        }
        if (conceptSet.size() == 0) { return null; }
        InputTermModel iwModel = new InputTermModel(iw, tokenList, subIW.toString(), conceptSet.size(), matchedPoint,
                project);
        if (wcSetMap.get(iwModel.getMatchedTerm()) == null) {
            wcSetMap.put(iwModel.getMatchedTerm(), conceptSet);
        }
        return iwModel;
    }

    private Set<Concept> getConceptSet(String subIW) {
        Set<Concept> conceptSet = new HashSet<Concept>();
        setEDRConceptSet(subIW, conceptSet);
        setEDRTConceptSet(subIW, conceptSet);
        setWordNetConceptSet(subIW.replaceAll(" ", "_"), conceptSet);
        OWLOntologyManager.setOWLConceptSet(subIW, conceptSet);
        return conceptSet;
    }

    private void setWordNetConceptSet(String subIW, Set<Concept> conceptSet) {
        if (!project.getOntologySelectionPanel().isWordNetEnable()) { return; }
        if (!isEnglish(subIW)) { return; }
        try {
            // getAllIndexWordも使えそう
            IndexWord indexWord = WordNetDic.getInstance().getNounIndexWord(subIW);
            // if (indexWord == null) {
            // indexWord = WordNetDic.getInstance().getVerbIndexWord(subIW);
            // }
            if (indexWord == null) { return; }
            for (int i = 0; i < indexWord.getSenseCount(); i++) {
                Synset synset = indexWord.getSense(i + 1);
                if (synset.containsWord(subIW)) {
                    Concept c = WordNetDic.getWNConcept(new Long(synset.getOffset()).toString());
                    conceptSet.add(c);
                }
            }
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    private void setEDRConceptSet(String subIW, Set<Concept> conceptSet) {
        if (!project.getOntologySelectionPanel().isEDREnable()) { return; }
        Set<String> idSet = null;
        if (DODDLEConstants.IS_USING_DB) {
            idSet = EDRDic.getEDRDBManager().getEDRIDSet(subIW);
        } else {
            idSet = EDRDic.getEDRIDSet(subIW);
        }
        if (idSet == null) { return; }
        for (String id : idSet) {
            Concept c = EDRDic.getEDRConcept(id);
            if (c != null) {
                conceptSet.add(c);
            }
        }
    }

    private void setEDRTConceptSet(String subIW, Set<Concept> conceptSet) {
        if (!project.getOntologySelectionPanel().isEDRTEnable()) { return; }
        Set<String> idSet = null;
        if (DODDLEConstants.IS_USING_DB) {
            idSet = EDRDic.getEDRTDBManager().getEDRIDSet(subIW);
        } else {
            idSet = EDRDic.getEDRTIDSet(subIW);
        }
        if (idSet == null) { return; }
        for (String id : idSet) {
            Concept c = EDRDic.getEDRTConcept(id);
            if (c != null) {
                conceptSet.add(c);
            }
        }
    }

    private boolean isEnglish(String iw) {
        return iw.matches("(\\w|\\s)*");
    }

    private List<Token> getTokenList(String iw) {
        if (isEnglish(iw)) { return getEnTokenList(iw); }
        return getJaTokenList(iw);
    }

    private List<Token> getEnTokenList(String iw) {
        if (iw.indexOf(" ") == -1) {
            Token token = new Token();
            token.setSurface(iw);
            // token.setBasicString(iw);
            token.getMorpheme().setBasicForm(iw);
            return Arrays.asList(new Token[] { token});
        }
        String[] ws = iw.split(" ");
        List<Token> tokenList = new ArrayList<Token>();
        for (int i = 0; i < ws.length; i++) {
            Token token = new Token();
            token.setSurface(ws[i]);
            // token.setBasicString(ws[i]);
            token.getMorpheme().setBasicForm(ws[i]);
            tokenList.add(token);
        }
        return tokenList;
    }

    /**
     * @param iw
     */
    private List<Token> getJaTokenList(String iw) {
        List<Token> tokenList = null;
        try {
            StringTagger tagger = SenFactory.getStringTagger(DODDLEConstants.GOSEN_CONFIGURATION_FILE);
            tokenList = tagger.analyze(iw);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return tokenList;
    }

    public void initDataWithMem(Set<String> wordSet) {
        clearData();
        Set<String> matchedWordSet = new HashSet<String>();
        for (String word : wordSet) {
            InputTermModel iwModel = makeInputTermModel(word, termConceptSetMap);
            if (iwModel != null) {
                inputTermModelSet.add(iwModel);
                matchedWordSet.add(iwModel.getMatchedTerm());
            } else {
                if (0 < word.length()) {
                    undefinedTermSet.add(word);
                }
            }
        }

        // 部分照合した複合語中で，完全照合単語リストに含まれない照合した単語を完全照合単語として追加
        matchedWordSet.removeAll(wordSet);
        for (String matchedWord : matchedWordSet) {
            InputTermModel iwModel = makeInputTermModel(matchedWord, termConceptSetMap);
            if (iwModel != null) {
                iwModel.setIsSystemAdded(true);
                inputTermModelSet.add(iwModel);
            }
        }
    }

    public void initDataWithDB(Set<String> iwSet) {
        DBManager edrDBManager = EDRDic.getEDRDBManager();
        if (edrDBManager == null) {
            EDRDic.initEDRDic();
            edrDBManager = EDRDic.getEDRDBManager();
        }
        if (edrDBManager != null) {
            edrDBManager.initDataWithDB(iwSet, project);
            inputTermModelSet = new TreeSet<InputTermModel>(edrDBManager.getInputTermModelSet());
            termConceptSetMap = new HashMap<String, Set<Concept>>(edrDBManager.getTermConceptSetMap());
            undefinedTermSet = new TreeSet<String>(edrDBManager.getUndefinedTermSet());
        }
    }

    public Set<InputTermModel> getInputTermModelSet() {
        return inputTermModelSet;
    }

    public Map<String, Set<Concept>> getTermConceptSetMap() {
        return termConceptSetMap;
    }

    public Set<String> getUndefinedTermSet() {
        return undefinedTermSet;
    }
}