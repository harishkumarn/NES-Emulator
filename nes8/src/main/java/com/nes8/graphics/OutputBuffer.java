package com.nes8.graphics;

import com.nes8.Settings;
import com.nes8.components.bus.Bus;
import com.nes8.components.helper.display.Display;

import java.awt.Color;

public class OutputBuffer {

    private static int DISPLAY_WIDTH = 32*8;
    private static int DISPLAY_HEIGHT = 30*8;
    Bus bus;
    
    Display display;

    public Color[][] outputBuffer;
    Color[][] displayBuffer;


    public OutputBuffer(Bus bus){
        this.outputBuffer = new Color[DISPLAY_HEIGHT][DISPLAY_WIDTH];
        this.displayBuffer = new Color[DISPLAY_HEIGHT][DISPLAY_WIDTH];
        this.bus = bus;
    }

    public void initDisplay(){
        this.display = Display.init(DISPLAY_WIDTH, DISPLAY_HEIGHT,Settings.DISPLAY_SCALE, displayBuffer, "NES8");
    }

    public void rerender(){
        for(int i = 0 ; i < DISPLAY_HEIGHT; i++){
            System.arraycopy(outputBuffer[i], 0,  displayBuffer[i], 0, DISPLAY_WIDTH);
        }
        this.display.rerender();
    }

    public void setPixel(int i, int j, Color color){
        this.outputBuffer[i][j] = color;
    }
}
