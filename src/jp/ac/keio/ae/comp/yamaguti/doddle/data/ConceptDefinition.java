package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.apache.log4j.*;

/**
 * @author takeshi morita
 */
public class ConceptDefinition {

    private Map agentMap;
    private Map objectMap;
    private Map goalMap;
    private Map implementMap;
    private Map a_objectMap;
    private Map placeMap;
    private Map sceneMap;
    private Map causeMap;

    private Set<String> verbSet;

    private Map relationMap;
    public static final String[] relationList = { "agent", "object", "goal", "implement", "a-object", "place", "scene",
            "cause"};
    public static String CONCEPT_DEFINITION = DODDLE.DODDLE_DIC + "conceptDefinitionforEDR.txt";

    private static ConceptDefinition conceptDefintion;

    public static ConceptDefinition getInstance() {
        if (conceptDefintion == null) {
            conceptDefintion = new ConceptDefinition();
        }
        return conceptDefintion;
    }

    private ConceptDefinition() {
        if (DODDLE.IS_USING_DB) {
            DBManager dbManager = EDRDic.getEDRDBManager();
            agentMap = dbManager.getVerbIDAgentIDSetMap();
            objectMap = dbManager.getVerbIDObjectIDSetMap();

            Map[] relationMapList = { agentMap, objectMap, goalMap, implementMap, a_objectMap, placeMap, sceneMap,
                    causeMap};
            relationMap = new HashMap();
            for (int i = 0; i < relationList.length; i++) {
                relationMap.put(relationList[i], relationMapList[i]);
            }
        } else {
            agentMap = new HashMap();
            objectMap = new HashMap();
            goalMap = new HashMap();
            implementMap = new HashMap();
            a_objectMap = new HashMap();
            placeMap = new HashMap();
            sceneMap = new HashMap();
            causeMap = new HashMap();

            verbSet = new TreeSet<String>();

            Map[] relationMapList = { agentMap, objectMap, goalMap, implementMap, a_objectMap, placeMap, sceneMap,
                    causeMap};
            relationMap = new HashMap();
            for (int i = 0; i < relationList.length; i++) {
                relationMap.put(relationList[i], relationMapList[i]);
            }

            try {
                InputStream inputStream = new FileInputStream(CONCEPT_DEFINITION);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "Shift_JIS"));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    String[] lines = line.replaceAll("\n", "").split("\\|");
                    String verbID = lines[0];
                    // for (int i = 0; i < relationMapList.length; i++) {
                    for (int i = 0; i < 2; i++) { // 当面，メモリ節約のためにagentとobjectのみを格納
                        putVerbID(relationMapList[i], verbID, makeIDSet(lines[i + 1].split("\t")));
                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                DODDLE.getLogger().log(Level.INFO, CONCEPT_DEFINITION + " (指定されたパスが見つかりません.)");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void printMap() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("conceptDescription3.1ex.txt"));
            int n = 0;
            // System.out.println(verbSet.size());
            for (Iterator i = verbSet.iterator(); i.hasNext();) {
                StringBuffer buf = new StringBuffer("");
                String verbID = (String) i.next();
                buf.append(verbID + "||");
                for (int j = 0; j < relationList.length; j++) {
                    String relation = relationList[j];
                    buf.append(relation + "\t");
                    Map map = (Map) relationMap.get(relation);
                    if (map.get(verbID) == null) {
                        buf.append("||");
                    } else {
                        Set idSet = (Set) map.get(verbID);
                        for (Iterator k = idSet.iterator(); k.hasNext();) {
                            String id = (String) k.next();
                            buf.append(id + "\t");
                        }
                    }
                }
                System.out.println(n++);
                buf.append("\n");
                writer.write(buf.toString());
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void putVerbID(Map map, String verbID, Set idSet) {
        if (idSet.size() == 0) { return; }
        map.put(verbID, idSet);
    }

    private void putExpandVerbID(Map map, String verbID, Set idSet) {
        if (idSet.size() == 0) { return; }
        Set<String> verbIDSet = getSubIDSet(verbID);
        verbIDSet.add(verbID);
        for (Iterator i = verbIDSet.iterator(); i.hasNext();) {
            String vid = (String) i.next();
            verbSet.add(vid);
            // 下位動詞的概念を登録
            // 登録されていたら，さらにidSetを追加する
            if (map.get(vid) == null) {
                map.put(vid, idSet);
            } else {
                Set set = (Set) map.get(vid);
                set.addAll(idSet);
                map.put(vid, set);
            }
        }
    }

    public Set makeIDSet(String[] idArray) {
        Set idSet = new HashSet();
        if (1 < idArray.length) {
            for (int i = 1; i < idArray.length; i++) {
                String id = idArray[i];
                idSet.add(id);
            }
        }
        return idSet;
    }

    public Set makeExpandIDSet(String[] idArray) {
        Set idSet = new HashSet();
        if (1 < idArray.length) {
            for (int i = 1; i < idArray.length; i++) {
                String id = idArray[i];
                idSet.add(id);
                idSet.addAll(getSubIDSet(id));
            }
        }
        return idSet;
    }

    /**
     * とりあえず，「移動」，「行為」，「状態」，「変化」，「現象」概念の下位概念については，動詞的概念とみなす．ほとんどの動詞的概念は，
     * 上記概念の下位概念のため．それ以外の概念について概念記述を参照してしまうと，
     * クラスとプロパティの区別がつかなくなってしまうため，当面はこれだけを考慮する．
     * 
     * これをファイルから読み込めるようにすれば，上位オントロジーと同様に，動詞的概念を判別することができる
     * 名詞的概念階層から動詞的概念を削除するときにも，verbIDSetを用いる．
     */
    public static String[] verbIDSet = new String[] { "ID30f83e", "ID30f801", "ID3aa963", "ID30f7e5", "ID3f9856"};

    /**
     * 
     * 入力概念集合を入力として，その中から動詞的概念の集合を返す
     * 
     */
    public Set<Concept> getVerbConceptSet(Set<Concept> inputConceptSet) {
        Set<Concept> verbConceptSet = new HashSet<Concept>();
        Set<String> allVerbURISet = new HashSet<String>();
        // for (Iterator i = inputIDSet.iterator(); i.hasNext();) {
        // String id = (String) i.next();
        //
        // agentとobjectの場合のみを考慮
        // if (agentMap.get(id) != null || objectMap.get(id) != null) {
        // verbIDSet.add(id);
        // allVerbIDSet.addAll(getSubIDSet(id));
        // }
        // }

        for (int i = 0; i < verbIDSet.length; i++) {
            allVerbURISet.addAll(getSubIDSet(verbIDSet[i]));
        }
        for (Concept c : inputConceptSet) {
            if (allVerbURISet.contains(c.getURI())) {
                verbConceptSet.add(c);
            }
            // WordNetの場合についても，ここで識別しようと思えばできるはず．
        }
        return verbConceptSet;
    }

    /**
     * IDと関係子を受け取り，関係の値としてIDを含む動詞的概念の セットを返す
     */
    public Set getVerbIDSet(String id, String relation) {
        Set verbIDSet = new HashSet();
        Map map = (Map) relationMap.get(relation);
        for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
            Entry entry = (Entry) i.next();
            String verbID = (String) entry.getKey();
            Set idSet = (Set) entry.getValue();
            if (idSet.contains(id)) {
                verbIDSet.add(verbID);
                continue;
            }
            // マッチしない場合には，対象概念の上位概念に概念集合マッチするかどうかを調べていく
            Set supIDSet = getSupIDSet(id);
            for (Iterator j = idSet.iterator(); j.hasNext();) {
                String tid = (String) j.next();
                if (supIDSet.contains(tid)) {
                    verbIDSet.add(verbID);
                    break;
                }
            }
        }
        return verbIDSet;
    }

    public Set getSupIDSet(String id) {
        Set idSet = new HashSet();
        Set supIDSet = EDRTree.getEDRTree().getPathToRootSet(id);
        for (Iterator i = supIDSet.iterator(); i.hasNext();) {
            idSet.addAll((List) i.next());
        }
        return idSet;
    }

    public Set<String> getSubIDSet(String id) {
        Set<String> conceptSet = new HashSet<String>();
        Set<Set<Concept>> subConceptSet = EDRTree.getEDRTree().getSubConceptSet(id);
        for (Set<Concept> cset : subConceptSet) {
            for (Concept c : cset) {
                conceptSet.add(c.getURI());
            }
        }
        return conceptSet;
    }

    /**
     * 動詞的概念と関係子を引数として受け取り，動詞的概念と関係子を 通して関係のある名詞的概念のセットを返す
     * 
     * @param verbID
     * @param relation
     * @return
     */
    public Set getIDSet(String verbID, String relation) {
        Map map = (Map) relationMap.get(relation);
        Set idSet = (Set) map.get(verbID);
        return idSet;
    }

    public Set getURISet(String relation, String verbID, List<List<Concept>> trimmedConceptList) {
        Map map = (Map) relationMap.get(relation);
        // System.out.println(relation + " map key size: " +
        // map.keySet().size());
        Set uriSet = new HashSet();
        if (map.get(verbID) != null) {
            Set<String> verbIDSet = (Set<String>) map.get(verbID);
            for (String vID: verbIDSet) {
                uriSet.add(DODDLE.EDR_URI+vID);  
            }
        }
        for (List<Concept> list : trimmedConceptList) {
            for (Concept trimmedConcept : list) {
                if (map.get(trimmedConcept.getLocalName()) != null) {
                    Set<String> verbIDSet = (Set<String>) map.get(trimmedConcept.getLocalName());
                    for (String vID: verbIDSet) {
                        uriSet.add(DODDLE.EDR_URI+vID);  
                    }
                }
            }
        }
        return uriSet;
    }

    public static void main(String[] args) {
        // EDRTree.init();
        ConceptDefinition cd = ConceptDefinition.getInstance();
        // System.out.println("init done");
        Set verbIDSet = cd.getVerbIDSet("0a78fb", "agent");
        // System.out.println(verbIDSet.size());
        for (Iterator i = verbIDSet.iterator(); i.hasNext();) {
            String verbID = (String) i.next();
            System.out.println("verb id: " + verbID);
        }
        Set idSet = cd.getIDSet("061c7d", "agent");
        for (Iterator i = idSet.iterator(); i.hasNext();) {
            String id = (String) i.next();
            System.out.println("id: " + id);
        }
    }
}
