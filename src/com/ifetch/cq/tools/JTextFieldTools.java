package com.ifetch.cq.tools;

import com.intellij.ui.components.JBTextField;


/**
 * Created by cq on 19-10-23.
 */
public class JTextFieldTools {

    public static JBTextField createJBTextField(String name) {
        return createJBTextField(name, "", 15, true);
    }

    public static JBTextField createJBTextField(String name, String reminder) {
        return createJBTextField(name, reminder, 12, true);
    }

    public static JBTextField createJBTextField(String name, int weight) {
        return createJBTextField(name, "", weight, true);
    }

    public static JBTextField createJBTextField(String name, boolean enabled) {
        return createJBTextField(name, "", 12, enabled);
    }

    public static JBTextField createJBTextField(String name, String reminder, int weight) {
        return createJBTextField(name, reminder, weight, true);
    }

    public static JBTextField createJBTextField(String name, int weight, boolean enabled) {
        return createJBTextField(name, "", weight, enabled);
    }

    public static JBTextField createJBTextField(String name, String reminder, int weight, boolean enabled) {
        JBTextField jbTextField = new JBTextField(weight);
        jbTextField.setText(reminder);
        jbTextField.setEnabled(enabled);
        return jbTextField;
    }

    public JTextFieldTools() {

    }
}
