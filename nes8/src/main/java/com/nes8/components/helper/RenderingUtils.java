package com.nes8.components.helper;

import java.awt.Color;
import com.nes8.components.bus.Bus;


public class RenderingUtils {
    public static void renderTile(int i, int j, int address, Color[][] display,Color[] pallColors, Bus bus){
        byte[] lowByte = new byte[8], highByte = new byte[8];
        int x,y, c;
        for(int k = 0; k < 8;++k) lowByte[k] = bus.ppuRead(address++);//plane 1
        for(int k = 0; k < 8;++k) highByte[k] = bus.ppuRead(address++);//plane 2
        for(int k = 0; k < 8;++k){
            for(int l = 7; l >= 0;--l){
                x = i + ( 7 - l );
                y = j + k ;
                c = 0 ;
                if((highByte[k] & ( 1<< l)) > 0 ) c = 2;
                if((lowByte[k] & ( 1<< l)) > 0) c += 1;
                display[x][y] = pallColors[c];
            }
        }
    }

    public void renderSprite(int i, int j, int priority, boolean horizontalFlip, boolean verticalFlip,Color[][] display, Color[] pallColors, Bus bus){

    }
}
