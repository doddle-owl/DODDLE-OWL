package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.InputModule.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class EDRDic {

    private static String[] edrAllIDList;
    private static String[] edrtAllIDList;
    private static String[] edrDefinitionList;
    private static String[] edrtDefinitionList;

    private static String[] edrAllWordList;
    private static String[] edrtAllWordList;
    private static String[] edrWordIDSet;
    private static String[] edrtWordIDSet;

    private static Map<String, Concept> edrIDConceptMap;
    private static Map<String, Concept> edrtIDConceptMap;
    private static Map<String, Set<String>> edrWordIDSetMap;
    private static Map<String, Set<String>> edrtWordIDSetMap;

    private static DBManager edrDBManager;
    private static DBManager edrtDBManager;

    public static String ID_DEFINITION_MAP = DODDLE.DODDLE_DIC + "idDefinitionMapforEDR.txt";
    public static String WORD_IDSET_MAP = DODDLE.DODDLE_DIC + "wordIDSetMapforEDR.txt";

    public static String EDRT_ID_DEFINITION_MAP = DODDLE.DODDLE_EDRT_DIC + "idDefinitionMapforEDR.txt";
    public static String EDRT_WORD_IDSET_MAP = DODDLE.DODDLE_EDRT_DIC + "wordIDSetMapforEDR.txt";

    public static void init() {
        if (DODDLE.IS_USING_DB) {
            try {
                edrDBManager = new DBManager(true, DODDLE.DODDLE_DIC);
            } catch (Exception e) {
                // If an exception reaches this point, the last transaction did
                // not
                // complete. If the exception is RunRecoveryException, follow
                // the Berkeley DB recovery procedures before running again.
                DODDLE.getLogger().log(Level.INFO, "cannot open EDR Dic");
            }
            try {
                edrtDBManager = new DBManager(true, DODDLE.DODDLE_EDRT_DIC);
            } catch (Exception e) {
                DODDLE.getLogger().log(Level.INFO, "cannot open EDRT Dic");
            }
        } else {
            // System.out.println(GregorianCalendar.getInstance().getTime());
            edrIDConceptMap = new HashMap<String, Concept>();
            makeIDDefinitionMap(edrIDConceptMap, ID_DEFINITION_MAP, false);
            edrtIDConceptMap = new HashMap<String, Concept>();
            makeIDDefinitionMap(edrtIDConceptMap, EDRT_ID_DEFINITION_MAP, true);
            edrWordIDSetMap = new HashMap<String, Set<String>>();
            makeWordIDSetMap(edrWordIDSetMap, WORD_IDSET_MAP, false);
            edrtWordIDSetMap = new HashMap<String, Set<String>>();
            makeWordIDSetMap(edrtWordIDSetMap, EDRT_WORD_IDSET_MAP, true);
            // System.out.println(GregorianCalendar.getInstance().getTime());
        }
    }

    public static Set<String> getIDSet(String word, DBManager dbManager, Map<String, Set<String>> wordIDSetMap,
            boolean isSpecial) {
        if (DODDLE.IS_USING_DB) {
            // System.out.println(dbManager.getWordIDsMap().get(word));
            if (dbManager.getWordIDSetMap().get(word) == null) { return null; }
            String line = (String) dbManager.getWordIDSetMap().get(word);
            return new HashSet<String>(Arrays.asList(line.split(" ")));
        }
        if (wordIDSetMap.get(word) != null) { return wordIDSetMap.get(word); }
        String[] allWordList = null;
        if (isSpecial) {
            allWordList = edrtAllWordList;
        } else {
            allWordList = edrAllWordList;
        }
        int index = Arrays.binarySearch(allWordList, word);
        if (index < 0) {
            // System.out.println("none: " + word);
            return null;
        }
        String line = null;
        if (isSpecial) {
            line = edrtWordIDSet[index];
        } else {
            line = edrWordIDSet[index];
        }
        String[] attrs = line.split("\\s");
        Set<String> idSet = new HashSet<String>();
        for (int j = 0; j < attrs.length; j++) {
            try {
                idSet.add(attrs[j]);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }
        wordIDSetMap.put(word, idSet);
        return idSet;
    }

    public static Set<String> getEDRTIDSet(String word) {
        return getIDSet(word, edrtDBManager, edrtWordIDSetMap, true);
    }

    public static Set<String> getEDRIDSet(String word) {
        return getIDSet(word, edrDBManager, edrWordIDSetMap, false);
    }

    public static Concept getConcept(String id, String prefix, DBManager dbManager, Map<String, Concept> idConceptMap) {
        if (DODDLE.IS_USING_DB) {
            if (dbManager == null) { return null; }
            dbManager.setEDRConcept(id);
            return dbManager.getEDRConcept();
        }
        if (idConceptMap.get(id) != null) { return idConceptMap.get(id); }
        String[] allIDList = null;
        if (prefix.equals("edr")) {
            allIDList = edrAllIDList;
        } else if (prefix.equals("edrt")) {
            allIDList = edrtAllIDList;
        }
        if (allIDList == null) { return null; }
        int index = Arrays.binarySearch(allIDList, id);
        if (index < 0) {
            DODDLE.getLogger().log(Level.DEBUG, "辞書に存在しない概念ID: " + id);
            return null;
        }
        Concept c = null;
        if (prefix.equals("edr")) {
            c = new Concept(id, edrDefinitionList[index].split("\\^"));
        } else if (prefix.equals("edrt")) {
            c = new Concept(id, edrtDefinitionList[index].split("\\^"));
        }
        c.setPrefix(prefix);
        idConceptMap.put(id, c);
        return c;
    }

    public static Concept getEDRTConcept(String id) {
        return getConcept(id, "edrt", edrtDBManager, edrtIDConceptMap);
    }

    public static Concept getEDRConcept(String id) {
        return getConcept(id, "edr", edrDBManager, edrIDConceptMap);
    }

    private static void makeWordIDSetMap(Map<String, Set<String>> wordIDSetMap, String wordIDSetMapPath,
            boolean isSpecial) {
        try {
            if (!new File(wordIDSetMapPath).exists()) { return; }
            InputStream inputStream = new FileInputStream(wordIDSetMapPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));

            String line = reader.readLine().replaceAll("\n", "");
            if (isSpecial) {
                edrtAllWordList = line.split("\t");
            } else {
                edrAllWordList = line.split("\t");
            }

            // System.out.println("word size: " + allWordList.length);
            line = reader.readLine().replaceAll("\n", "");
            if (isSpecial) {
                edrtWordIDSet = line.split("\\|");
            } else {
                edrWordIDSet = line.split("\\|");
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void makeIDDefinitionMap(Map<String, Concept> idConceptMap, String idDefinitionMapPath,
            boolean isSpecial) {
        try {
            if (!new File(idDefinitionMapPath).exists()) { return; }
            InputStream inputStream = new FileInputStream(idDefinitionMapPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF16"));

            String line = reader.readLine().replaceAll("\n", "");
            if (isSpecial) {
                edrtAllIDList = line.split("\\|");
            } else {
                edrAllIDList = line.split("\\|");
            }
            // System.out.println("id size: " + allIDList.length);
            line = reader.readLine().replaceAll("\n", "");
            if (isSpecial) {
                edrtDefinitionList = line.split("\"");
            } else {
                edrDefinitionList = line.split("\"");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void closeDB(DBManager dbManager, String msg) {
        if (dbManager != null) {
            try {
                // Always attempt to close the database cleanly.
                dbManager.close();
                DODDLE.getLogger().log(Level.INFO, "Close " + msg);
            } catch (Exception e) {
                System.err.println("Exception during database close:");
                e.printStackTrace();
            }
        }
    }

    public static void closeDB() {
        closeDB(edrDBManager, "EDR DB");
        closeDB(edrtDBManager, "EDRT DB");
    }

    public static DBManager getEDRDBManager() {
        return edrDBManager;
    }

    public static DBManager getEDRTDBManager() {
        return edrtDBManager;
    }
}
