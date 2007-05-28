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
    
    public String getFormattedText(String text) {
        String[] words = text.split(" ");
        StringBuilder newText = new StringBuilder();
        newText.append("<html><body>");
        int size = 0;
        for (int i = 0; i < words.length; i++) {
            newText.append(words[i]);
            newText.append(" ");
            size += words[i].length();
            if (30 < size) {
                newText.append("<br>");
                size = 0;
            }
        }
        newText.append("</body></html>");
        return newText.toString();
    }

    @Override
    public String toString() {
        return getFormattedText(string);
    }
}
