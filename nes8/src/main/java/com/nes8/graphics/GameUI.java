package com.nes8.graphics;

import com.nes8.Settings;
import com.nes8.components.helper.Display;

import java.awt.Color;

public class GameUI {

    private static int DISPLAY_WIDTH = 32*8;
    private static int DISPLAY_HEIGHT = 30*8;
    
    Display display;

    Color[][] pixels;

    public GameUI(){
        this.pixels = new Color[DISPLAY_WIDTH][DISPLAY_HEIGHT];
    }

    public void initDisplay(){
        this.display = Display.init(DISPLAY_WIDTH, DISPLAY_HEIGHT,Settings.DISPLAY_SCALE, pixels, "NES8");
    }
    
}
