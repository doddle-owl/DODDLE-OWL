/*
 * @(#)  2007/03/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class SwoogleOWLMetaData {

    private String url;
    private String fileEncoding;
    private String rdfType;
    private String fileType;
    private double ontoRank;

    public SwoogleOWLMetaData(Resource u, Literal fe, Literal ft, Resource rt, Literal rank) {
        url = u.getURI();
        fileEncoding = fe.getString();
        fileType = ft.getString();
        rdfType = rt.getURI();
        ontoRank = rank.getDouble();
    }

    public double getOntoRank() {
        return ontoRank;
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public String getFileType() {
        return fileType;
    }

    public String getRdfType() {
        return rdfType;
    }

    public String getURL() {
        return url;
    }

    public String toString() {
        return url + ", " + rdfType + "," + fileEncoding;
    }
}
