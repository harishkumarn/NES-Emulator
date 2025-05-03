package com.nes8.graphics;

import com.nes8.Settings;
import com.nes8.components.helper.Display;
import com.nes8.components.bus.Bus;

import java.awt.Color;

public class OutputBuffer {

    private static int DISPLAY_WIDTH = 32*8;
    private static int DISPLAY_HEIGHT = 30*8;
    Bus bus;
    
    Display display;

    public Color[][] outputBuffer;


    public OutputBuffer(Bus bus){
        this.outputBuffer = new Color[DISPLAY_HEIGHT][DISPLAY_WIDTH];
        this.bus = bus;
    }

    public void initDisplay(){
        this.display = Display.init(DISPLAY_WIDTH, DISPLAY_HEIGHT,Settings.DISPLAY_SCALE, outputBuffer, "NES8");
    }

    public void setPixel(int i, int j, Color color){
        this.outputBuffer[i][j] = color;
    }
}
