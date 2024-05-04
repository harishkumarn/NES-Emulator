package com.nes8.components;

import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;

public class Display extends JPanel{
    private Color[] palette = null;
    private int w, h, scale;
    byte[][] pixels ;
    public Display(int w, int h, int scale,byte[][] pixels, Color[] palette, String name){
        this.w = w;
        this.h = h;
        this.scale = scale;
        this.pixels = pixels;
        this.palette = palette;
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
                g.setColor(palette[pixels[x][y]]);
                g.fillRect(x * scale, y * scale, scale, scale);
            }
        }
    }
}
