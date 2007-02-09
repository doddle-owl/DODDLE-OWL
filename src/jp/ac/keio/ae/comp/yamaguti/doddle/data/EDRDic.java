package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;
import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.apache.log4j.*;

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

    public static void initEDRDic() {
        if (edrIDConceptMap != null) { return; }
        if (DODDLE.IS_USING_DB) {
            try {
                edrDBManager = new DBManager(true, DODDLE.DODDLE_DIC);
                DODDLE.getLogger().log(Level.INFO, "init EDR Concept Classification Dictionary on DB");
            } catch (Exception e) {
                // If an exception reaches this point, the last transaction did
                // not
                // complete. If the exception is RunRecoveryException, follow
                // the Berkeley DB recovery procedures before running again.
                DODDLE.getLogger().log(Level.INFO, "cannot open EDR Dic");
            }
        } else {
            edrIDConceptMap = new HashMap<String, Concept>();
            // System.out.println(GregorianCalendar.getInstance().getTime());
            makeIDDefinitionMap(edrIDConceptMap, ID_DEFINITION_MAP, false);
            edrWordIDSetMap = new HashMap<String, Set<String>>();
            makeWordIDSetMap(edrWordIDSetMap, WORD_IDSET_MAP, false);
            // System.out.println(GregorianCalendar.getInstance().getTime());
            DODDLE.getLogger().log(Level.INFO, "init EDR Concept Classification Dictionary on Memory");
        }
    }

    public static void initEDRTDic() {
        if (edrtIDConceptMap != null) { return; }
        edrtIDConceptMap = new HashMap<String, Concept>();
        if (DODDLE.IS_USING_DB) {
            try {
                edrtDBManager = new DBManager(true, DODDLE.DODDLE_EDRT_DIC);
            } catch (Exception e) {
                DODDLE.getLogger().log(Level.INFO, "cannot open EDRT Dic");
            }
        } else {
            // System.out.println(GregorianCalendar.getInstance().getTime());
            makeIDDefinitionMap(edrtIDConceptMap, EDRT_ID_DEFINITION_MAP, true);
            edrtWordIDSetMap = new HashMap<String, Set<String>>();
            makeWordIDSetMap(edrtWordIDSetMap, EDRT_WORD_IDSET_MAP, true);
            // System.out.println(GregorianCalendar.getInstance().getTime());
        }
    }

    public static Set<String> getIDSet(String word, DBManager dbManager, Map<String, Set<String>> wordIDSetMap,
            boolean isSpecial) {
        if (DODDLE.IS_USING_DB) {
            if (dbManager.getEDRIDSet(word) == null) { return null; }
            return dbManager.getEDRIDSet(word);
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

    public static Concept getConcept(String id, String nameSpace, DBManager dbManager,
            Map<String, Concept> uriConceptMap) {
        if (DODDLE.IS_USING_DB) {
            if (uriConceptMap.get(nameSpace + id) != null) { return uriConceptMap.get(nameSpace + id); }
            if (dbManager == null) { return null; }
            dbManager.setEDRConcept(id);
            Concept c = dbManager.getEDRConcept();
            uriConceptMap.put(nameSpace + id, c);
            return c;
        }
        if (uriConceptMap.get(nameSpace + id) != null) { return uriConceptMap.get(nameSpace + id); }
        String[] allIDList = null;
        if (nameSpace.equals(DODDLE.EDR_URI)) {
            allIDList = edrAllIDList;
        } else if (nameSpace.equals(DODDLE.EDRT_URI)) {
            allIDList = edrtAllIDList;
        }
        if (allIDList == null) { return null; }
        int index = Arrays.binarySearch(allIDList, id);
        if (index < 0) {
            DODDLE.getLogger().log(Level.DEBUG, "EDRに存在しない概念ID: " + id);
            return null;
        }
        Concept c = null;
        if (nameSpace.equals(DODDLE.EDR_URI)) {
            c = new Concept(nameSpace + id, edrDefinitionList[index].split("\\^"));
        } else if (nameSpace.equals(DODDLE.EDRT_URI)) {
            c = new Concept(nameSpace + id, edrtDefinitionList[index].split("\\^"));
        }
        uriConceptMap.put(nameSpace + id, c);
        return c;
    }

    public static Concept getEDRTConcept(String id) {
        return getConcept(id, DODDLE.EDRT_URI, edrtDBManager, edrtIDConceptMap);
    }

    public static Concept getEDRConcept(String id) {
        return getConcept(id, DODDLE.EDR_URI, edrDBManager, edrIDConceptMap);
    }

    private static void makeWordIDSetMap(Map<String, Set<String>> wordIDSetMap, String wordIDSetMapPath,
            boolean isSpecial) {
        BufferedReader reader = null;
        try {
            if (!new File(wordIDSetMapPath).exists()) { return; }
            InputStream inputStream = new FileInputStream(wordIDSetMapPath);
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
    }

    private static void makeIDDefinitionMap(Map<String, Concept> idConceptMap, String idDefinitionMapPath,
            boolean isSpecial) {
        BufferedReader reader = null;
        try {
            if (!new File(idDefinitionMapPath).exists()) { return; }
            InputStream inputStream = new FileInputStream(idDefinitionMapPath);
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF16"));

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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
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
