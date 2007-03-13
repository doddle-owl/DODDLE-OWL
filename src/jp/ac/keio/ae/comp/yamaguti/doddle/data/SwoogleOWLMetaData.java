/*
 * @(#)  2007/03/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;
import java.net.*;

/**
 * @author takeshi morita
 */
public class SwoogleOWLMetaData {
    
    private String url;
    private String encodedURL;
    private String fileEncoding;    
    private String rdfType;

    public SwoogleOWLMetaData(String u, String f, String r) {
        try {
            url = u;
            encodedURL = URLEncoder.encode(url, "UTF-8");
            fileEncoding = f;
            rdfType = r;
        } catch(UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
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
    
    public String getEncodedURL() {
        return encodedURL;
    }
}
