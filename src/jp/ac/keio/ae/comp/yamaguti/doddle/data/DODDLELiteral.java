/*
 * @(#)  2007/02/25
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;

/**
 * @author takeshi morita
 */
public class DODDLELiteral implements Serializable{

    private String lang;
    private String string;
    
    public DODDLELiteral(String l, String str) {
        lang = l;
        string = str;
    }

    public boolean equals(Object obj) {
        DODDLELiteral literal = (DODDLELiteral)obj; 
        return literal.getString().equals(string) && literal.getLang().equals(lang);
    }
    
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getString() {
        return string;
    }

    public void setString(String text) {
        this.string = text;
    }
    
    public String toString() {
        return string;
    }
}
