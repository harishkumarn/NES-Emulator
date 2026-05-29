package com.nes8.components.helper.display;

import javax.swing.JFrame;

public class JFrameSingleton {
    private static JFrame instance = null;

    private JFrameSingleton() {
        // Private constructor to prevent instantiation
    }

    public static JFrame getInstance() {
        synchronized (JFrameSingleton.class) {
            if (instance == null) {
                instance = new JFrame();
            }
        }
        return instance;
    }


}
