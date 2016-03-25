package org.forgerock.cuppa.intellij;

import javax.swing.*;

import com.intellij.openapi.util.IconLoader;

public class CuppaIcons {
    public static final Icon CUPPA = load("/icons/cuppa.png");

    private static Icon load(String path) {
        return IconLoader.getIcon(path, CuppaIcons.class);
    }
}
