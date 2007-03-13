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

    private Set<Resource> classSet;
    private Set<Resource> propertySet;

    private Map<String, SwoogleOWLMetaData> uriSwoogleOWLMetaDataMap;

    public SwoogleWebServiceData() {
        classSet = new HashSet<Resource>();
        propertySet = new HashSet<Resource>();
        uriSwoogleOWLMetaDataMap = new HashMap<String, SwoogleOWLMetaData>();
    }

    public void putSwoogleOWLMetaData(String uri, SwoogleOWLMetaData data) {
        uriSwoogleOWLMetaDataMap.put(uri, data);
    }
    
    public SwoogleOWLMetaData getSwoogleOWLMetaData(String uri) {
        return uriSwoogleOWLMetaDataMap.get(uri);
    }

    public void addClass(Resource property) {
        classSet.add(property);
    }

    public void addProperty(Resource property) {
        propertySet.add(property);
    }
}
