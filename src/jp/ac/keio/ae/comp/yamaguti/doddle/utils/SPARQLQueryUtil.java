/*
 * @(#)  2007/03/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;

/**
 * @author takeshi morita
 */
public class SPARQLQueryUtil {

    public static String getQueryString(InputStream inputStream) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            // UTF-8にすると一行目がうまく解析できない
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-16"));
            while (reader.ready()) {
                String line = reader.readLine();
                builder.append(line);
                builder.append(" ");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return builder.toString();
    }
}
