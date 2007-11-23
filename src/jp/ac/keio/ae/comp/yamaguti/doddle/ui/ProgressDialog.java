/*
 * @(#)  2007/11/19
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;

/**
 * @author takeshi morita
 */
public class ProgressDialog extends JDialog {

    private int division;
    private int maxValue;
    private int currentValue;
    private int progressCountSize;
    private JTextField messageField;
    private JProgressBar progressBar;

    public ProgressDialog(String title, int max) {
        super(DODDLE.rootFrame, title, false);
        messageField = new JTextField(50);
        progressBar = new JProgressBar();
        initProgressBar(max);
        getContentPane().add(messageField, BorderLayout.CENTER);
        getContentPane().add(progressBar, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(DODDLE.rootFrame);
        setVisible(true);
    }

    private void initProgressBar(int max) {
        currentValue = 0;
        maxValue = max;
        progressCountSize = 50;
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(0);
        if (maxValue < progressCountSize) {
            progressBar.setMaximum(maxValue);
        } else {
            division = maxValue / progressCountSize;
            progressBar.setMaximum(progressCountSize);
        }
        progressBar.setValue(0);
    }

    public void setMessage(String msg) {
        currentValue++;
        if (maxValue < progressCountSize) {
            progressBar.setValue(currentValue);
        } else if (currentValue % division == 0) {
            progressBar.setValue(currentValue);
        }
        messageField.setText(msg);
    }
}
