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

    private static String[] allIDList;
    private static String[] definitionList;

    private static String[] allWordList;
    private static String[] wordIDSet;

    private static Map<String, Concept> idConceptMap;
    private static Map<String, Set<String>> wordIDSetMap;
    private static DBManager dbManager;

    public static String ID_DEFINITION_MAP = DODDLE.DODDLE_DIC + "idDefinitionMapforEDR.txt";
    public static String WORD_IDSET_MAP = DODDLE.DODDLE_DIC + "wordIDSetMapforEDR.txt";

    public static String EDRT_ID_DEFINITION_MAP = DODDLE.DODDLE_EDRT_DIC + "idDefinitionMapforEDR.txt";
    public static String EDRT_WORD_IDSET_MAP = DODDLE.DODDLE_EDRT_DIC + "wordIDSetMapforEDR.txt";

    public static void init() {
        if (DODDLE.IS_USING_DB) {
            try {
                dbManager = new DBManager(true, DODDLE.DODDLE_DIC);
            } catch (Exception e) {
                // If an exception reaches this point, the last transaction did
                // not
                // complete. If the exception is RunRecoveryException, follow
                // the Berkeley DB recovery procedures before running again.
                e.printStackTrace();
            }
        } else {
            // System.out.println(GregorianCalendar.getInstance().getTime());
            makeIDDefinitionMap();
            makeWordIDSetMap();
            // System.out.println(GregorianCalendar.getInstance().getTime());
        }
    }

    public static Set<String> getIDSet(String word) {
        if (DODDLE.IS_USING_DB) {
            // System.out.println(dbManager.getWordIDsMap().get(word));
            if (dbManager.getWordIDSetMap().get(word) == null) { return null; }
            String line = (String) dbManager.getWordIDSetMap().get(word);
            return new HashSet<String>(Arrays.asList(line.split(" ")));
        }
        if (wordIDSetMap.get(word) != null) { return wordIDSetMap.get(word); }
        int index = Arrays.binarySearch(allWordList, word);
        if (index < 0) {
            // System.out.println("none: " + word);
            return null;
        }
        String line = wordIDSet[index];
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

    public static String getWord(String id) {
        Concept c = getEDRConcept(id);
        if (c != null) { return c.getWord(); }
        return null;
    }

    public static Concept getEDRConcept(String id) {
        if (DODDLE.IS_USING_DB) {
            dbManager.setEDRConcept(id);
            return dbManager.getEDRConcept();
        }
        if (idConceptMap.get(id) != null) { return idConceptMap.get(id); }
        int index = Arrays.binarySearch(allIDList, id);
        if (index < 0) {
            DODDLE.getLogger().log(Level.DEBUG, "辞書に存在しない概念ID: " + id);
            return null;
        }
        Concept c = new Concept(id, definitionList[index].split("\\^"));
        c.setPrefix("edr");
        idConceptMap.put(id, c);
        return c;
    }

    private static void makeWordIDSetMap() {
        wordIDSetMap = new HashMap<String, Set<String>>();
        try {
            InputStream inputStream = new FileInputStream(WORD_IDSET_MAP);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));

            String line = reader.readLine().replaceAll("\n", "");
            allWordList = line.split("\t");
            // System.out.println("word size: " + allWordList.length);
            line = reader.readLine().replaceAll("\n", "");
            wordIDSet = line.split("\\|");
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void makeIDDefinitionMap() {
        idConceptMap = new HashMap<String, Concept>();
        try {
            InputStream inputStream = new FileInputStream(ID_DEFINITION_MAP);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF16"));

            String line = reader.readLine().replaceAll("\n", "");
            allIDList = line.split("\\|");
            // System.out.println("id size: " + allIDList.length);
            line = reader.readLine().replaceAll("\n", "");
            definitionList = line.split("\"");
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeDB() {
        if (dbManager != null) {
            try {
                // Always attempt to close the database cleanly.
                dbManager.close();
                System.out.println("Close DB");
            } catch (Exception e) {
                System.err.println("Exception during database close:");
                e.printStackTrace();
            }
        }
    }

    public static DBManager getDBManager() {
        return dbManager;
    }
}
