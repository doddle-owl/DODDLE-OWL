/*
 * @(#)  2007/03/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class SwoogleWebServiceData {

    private Map<String, Double> swtTermRankMap;

    private Set<Resource> classSet;
    private Set<Resource> propertySet;
    private Set<Resource> relatedPropertySet;
    private Set<Resource> expandClassSet;
    private Map<Resource, Set<Resource>> propertyDomainSetMap;
    private Map<Resource, Set<Resource>> propertyRangeSetMap;

    private Map<String, SwoogleOWLMetaData> uriSwoogleOWLMetaDataMap;
    private static Map<String, ReferenceOWLOntology> uriRefOntologyMap;

    public SwoogleWebServiceData() {
        swtTermRankMap = new HashMap<String, Double>();
        classSet = new HashSet<Resource>();
        expandClassSet = new HashSet<Resource>();
        propertySet = new HashSet<Resource>();
        relatedPropertySet = new HashSet<Resource>();
        propertyDomainSetMap = new HashMap<Resource, Set<Resource>>();
        propertyRangeSetMap = new HashMap<Resource, Set<Resource>>();
        uriSwoogleOWLMetaDataMap = new HashMap<String, SwoogleOWLMetaData>();
        uriRefOntologyMap = new HashMap<String, ReferenceOWLOntology>();
    }

    public void initData() {
        swtTermRankMap.clear();
        classSet.clear();
        expandClassSet.clear();
        propertySet.clear();
        relatedPropertySet.clear();
        propertyDomainSetMap.clear();
        propertyRangeSetMap.clear();
        uriRefOntologyMap.clear();
    }

    public void putTermRank(String uri, double rank) {
        swtTermRankMap.put(uri, rank);
    }

    public Double getTermRank(String uri) {
        return swtTermRankMap.get(uri);
    }

    public void putRefOntology(String uri, ReferenceOWLOntology refOntology) {
        if (isRelatedOntology(refOntology)) {
            uriRefOntologyMap.put(uri, refOntology);
        } else {
            System.out.println("不要オントロジー: "+uri);
        }
    }

    public ReferenceOWLOntology getRefOntology(String uri) {
        return uriRefOntologyMap.get(uri);
    }

    public Set<String> getRefOntologyURISet() {
        return uriRefOntologyMap.keySet();
    }

    public Collection<ReferenceOWLOntology> getRefOntologies() {
        return uriRefOntologyMap.values();
    }

    public void putSwoogleOWLMetaData(String uri, SwoogleOWLMetaData data) {
        uriSwoogleOWLMetaDataMap.put(uri, data);
    }

    public SwoogleOWLMetaData getSwoogleOWLMetaData(String uri) {
        return uriSwoogleOWLMetaDataMap.get(uri);
    }

    public void addClass(Resource cls) {
        classSet.add(cls);
    }

    public Set<Resource> getClassSet() {
        return classSet;
    }

    public void addProperty(Resource property) {
        propertySet.add(property);
    }

    public void addRelatedProperty(Resource property) {
        relatedPropertySet.add(property);
    }

    public Set<Resource> getPropertySet() {
        return propertySet;
    }
    
    public Set<Resource> getRelatedPropertySet() {
        return relatedPropertySet;
    }
    
    public Set<Resource> getAllProperty() {
        Set<Resource> allPropertySet = new HashSet<Resource>();
        allPropertySet.addAll(propertySet);
        allPropertySet.addAll(relatedPropertySet);
        return allPropertySet;
    }
    
    public Set<Resource> getConceptSet() {
        Set<Resource> conceptSet = new HashSet<Resource>();
        conceptSet.addAll(classSet);
        conceptSet.addAll(propertySet);
        conceptSet.addAll(relatedPropertySet);
        return conceptSet;
    }

    private void addPropertyRegion(Map<Resource, Set<Resource>> propertyRegionSetMap, Resource property, Resource region) {
        if (propertyRegionSetMap.get(property) != null) {
            Set<Resource> regionSet = propertyRegionSetMap.get(property);
            regionSet.add(region);
        } else {
            Set<Resource> regionSet = new HashSet<Resource>();
            regionSet.add(region);
            propertyRegionSetMap.put(property, regionSet);
        }
    }

    public void addPropertyDomain(Resource property, Resource domain) {
        addPropertyRegion(propertyDomainSetMap, property, domain);
    }

    public void addPropertyRange(Resource property, Resource range) {
        addPropertyRegion(propertyRangeSetMap, property, range);
    }

    private Set<Resource> getExpandClassSet(Resource cls) {
        Set<Resource> expandClassSet = new HashSet<Resource>();
        for (String uri : uriRefOntologyMap.keySet()) {
            ReferenceOWLOntology refOnto = uriRefOntologyMap.get(uri);
            Set<List<Concept>> pathToRoot = refOnto.getPathToRootSet(cls.getURI());
            for (List<Concept> clist : pathToRoot) {
                for (Concept c : clist) {
                    expandClassSet.add(c.getResource());
                }
            }
        }
        return expandClassSet;
    }

    private void setExpandClassSet() {
        for (Resource cls : classSet) {
            expandClassSet.addAll(getExpandClassSet(cls));
        }
    }

    private void removeUnnecessaryRegionSet(Map<Resource, Set<Resource>> propertyRegionSetMap) {
        for (Resource property : propertyRegionSetMap.keySet()) {
            Set<Resource> unnecessaryRegionSet = new HashSet<Resource>();
            Set<Resource> regionSet = propertyRegionSetMap.get(property);
            for (Resource region : regionSet) {
                if (!expandClassSet.contains(region)) {
                    unnecessaryRegionSet.add(region);
                }
            }
            regionSet.removeAll(unnecessaryRegionSet);
        }
    }

    /**
     * 定義域と値域の両方が定義されているプロパティを獲得
     * 
     * @param property
     * @return
     */
    private boolean isNecessaryProperty(Resource property) {
        return (getDomainSet(property) != null && getRangeSet(property) != null);
    }

    /**
     * 定義域と値域の両方に入力単語に関連するクラスが定義されていないプロパティは削除する
     */
    public void addNecessaryPropertySet() {
        for (Resource property : relatedPropertySet) {
            if (isNecessaryProperty(property)) {
                propertySet.add(property);
            }
        }
    }

    /**
     * 入力概念または入力概念の上位概念以外の定義域と値域は削除する
     */
    public void removeUnnecessaryRegionSet() {
        setExpandClassSet();
        removeUnnecessaryRegionSet(propertyDomainSetMap);
        removeUnnecessaryRegionSet(propertyRangeSetMap);
    }

    /**
     * propertyの定義域を返す
     * 
     * @param property
     * @return
     */
    public Set<Resource> getDomainSet(Resource property) {
        return propertyDomainSetMap.get(property);
    }

    /**
     * propertyの値域を返す
     * 
     * @param property
     * @return
     */
    public Set<Resource> getRangeSet(Resource property) {
        return propertyRangeSetMap.get(property);
    }
    
    /** 
     *  クラス，プロパティ，関連プロパティのいずれかを含むオントロジーを関連オントロジーとする
     * 
     */
    public boolean isRelatedOntology(ReferenceOWLOntology refOnto) {
        Set<Resource> conceptSet = getConceptSet();
        for (Resource concept: conceptSet) {
            if (refOnto.getConcept(concept.getURI())!=null) {
                return true;
            }
        }
        return false;
    }

    public int getAllRelationCount() {
        int relCnt= 0;
        for (Resource property : getAllProperty()) {
            if (propertyDomainSetMap.get(property) != null && propertyRangeSetMap.get(property) != null) {
                int cnt = propertyDomainSetMap.get(property).size() * propertyRangeSetMap.get(property).size();
                relCnt += cnt;
            }
        }
        return relCnt;
    }
    
    public int getValidRelationCount() {
        int relCnt= 0;
        for (Resource property : propertySet) {
            if (propertyDomainSetMap.get(property) != null && propertyRangeSetMap.get(property) != null) {
                int cnt = propertyDomainSetMap.get(property).size() * propertyRangeSetMap.get(property).size();
                relCnt += cnt;
                for (Resource domain: propertyDomainSetMap.get(property)) {
                    for (Resource range: propertyRangeSetMap.get(property)) {
                        System.out.println(domain+" = "+property+" => "+range);
                    }
                }
            }
        }
        return relCnt;
    }
    
    
    /**
     * inputWordRatio, relationCountを計算する
     * 
     */
    public void calcOntologyRank(Set<String> inputWordSet) {
        Set<String> unnecessaryOntologyURISet = new HashSet<String>();
        for (String uri : uriRefOntologyMap.keySet()) {
            ReferenceOWLOntology refOnto = uriRefOntologyMap.get(uri);
            double inputConceptCnt = 0;
            for (String inputWord : inputWordSet) {
                if (refOnto.getURISet(inputWord) != null) {
                    inputConceptCnt++;
                }
            }
            refOnto.getOntologyRank().setInputWordCount(inputConceptCnt);
            refOnto.getOntologyRank().setInputWordRatio(inputConceptCnt / inputWordSet.size());
            int relationCnt = 0;
            for (Resource property : getAllProperty()) {
                if (refOnto.getConcept(property.getURI()) != null) {
                    if (propertyDomainSetMap.get(property) != null && propertyRangeSetMap.get(property) != null) {
                        int cnt = propertyDomainSetMap.get(property).size() * propertyRangeSetMap.get(property).size();
                        relationCnt += cnt;
                    }
                }
            }
            refOnto.getOntologyRank().setRelationCount(relationCnt);
            if (inputConceptCnt == 0 && relationCnt == 0) {
                System.out.println("不要: " + refOnto);
                unnecessaryOntologyURISet.add(refOnto.getURI());
            } else {
                System.out.println("関連オントロジー" + refOnto);
            }
        }
        for (String uri : unnecessaryOntologyURISet) {
            uriRefOntologyMap.remove(uri);
        }
    }

    public String toString() {
        return uriSwoogleOWLMetaDataMap.toString();
    }
}
