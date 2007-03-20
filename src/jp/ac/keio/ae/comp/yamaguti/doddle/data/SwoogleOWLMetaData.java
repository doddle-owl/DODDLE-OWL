/*
 * @(#)  2007/03/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;


/**
 * @author takeshi morita
 */
public class SwoogleOWLMetaData {

    private String url;
    private String fileEncoding;
    private String rdfType;
    private double ontoRank;

    public SwoogleOWLMetaData(String u, String f, String r, double rank) {
        url = u;
        fileEncoding = f;
        rdfType = r;
        ontoRank = rank;
    }

    public double getOntoRank() {
        return ontoRank;
    }

    public String getFileEncoding() {
        return fileEncoding;
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
