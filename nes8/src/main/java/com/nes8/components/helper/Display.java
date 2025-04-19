package com.nes8.components.helper;

import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;

public class Display extends JPanel{
    private int w, h, scale;
    Color[][] pixels ;
    public static Display init(int w, int h, int scale, Color[][] pixels, String name){
        return new Display(w, h, scale, pixels,name);
    }
    private Display(int w, int h, int scale,Color[][] pixels, String name){
        this.w = w;
        this.h = h;
        this.scale = scale;
        this.pixels = pixels;
        setPreferredSize(new Dimension(w * scale, w * scale));

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(name);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(this);
            frame.pack();
            frame.setVisible(true);
        });
        rerender();
    }

    public void rerender(){
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                g.setColor(pixels[x][y]);
                g.fillRect(x * scale, y * scale, scale, scale);
            }
        }
    }
}
