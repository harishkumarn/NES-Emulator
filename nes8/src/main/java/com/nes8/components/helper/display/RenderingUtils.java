package com.nes8.components.helper.display;

import java.awt.Color;
import com.nes8.components.bus.Bus;


public class RenderingUtils {
    /**
     * 
     * @param i - vertical co-ordinate
     * @param j - horizontal co-ordinate
     * @param address
     * @param display
     * @param pallColors
     * @param bus
     */
    public static void renderTile(int i, int j, int address, Color[][] display,Color[] pallColors, Bus bus){ 
        if(pallColors == null || pallColors[0] == null) return;
        byte[] lowByte = new byte[8], highByte = new byte[8];
        int x,y, c;
        for(int k = 0; k < 8;++k) lowByte[k] = bus.ppuRead(address++);//plane 1
        for(int k = 0; k < 8;++k) highByte[k] = bus.ppuRead(address++);//plane 2
        for(int k = 0; k < 8;++k){
            for(int l = 7; l >= 0;--l){
                x = i + k;
                y = j + ( 7 - l );
                if(x >= display.length || y >= display[0].length) continue;
                c = 0 ;
                if((highByte[k] & ( 1<< l)) > 0 ) c = 2;
                if((lowByte[k] & ( 1<< l)) > 0) c += 1;
                display[x][y] = pallColors[c];
            }
        }
    }

    public static void renderSprite(int i, int j, int baseAddress, int priority, boolean horizontalFlip, boolean verticalFlip,Color[][] display, Color[] pallColors, Bus bus){
        if(pallColors == null) return;
        // TODO : Handle priority and flipping
        byte[] lowByte = new byte[8], highByte = new byte[8];
        int x,y,c;
        for(int k = 0; k < 8;++k) lowByte[k] = bus.ppuRead(baseAddress ++ );//plane 1
        for(int k = 0; k < 8;++k) highByte[k] = bus.ppuRead(baseAddress ++);//plane 2
        Color[][] sprite = new Color[8][8];
        for(int k = 0; k < 8;++k){
            for(int l = 7; l >= 0;--l){
                x = i + k;
                y = j + ( 7 - l );
                if(x >= display.length || y >= display[0].length) continue;
                c = 0 ;
                if((highByte[k] & ( 1<< l)) > 0 ) c = 2;
                if((lowByte[k] & ( 1<< l)) > 0) c += 1;
                if(c == 0 ) continue; // Transparent pixel, takes color of BG
                sprite[k][l] = pallColors[c];
            }
        }

        applyOrientation(sprite, horizontalFlip, verticalFlip);

        for(int k = 0; k < 8;++k){
            for(int l = 7; l >= 0;--l){
                x = i + k;
                y = j + ( 7 - l );
                if(x >= display.length || y >= display[0].length) continue;
                if(sprite[k][l] != null) display[x][y] = sprite[k][l];
            }
        }
    }

    private static void applyOrientation(Color[][] sprite, boolean horizontalFlip, boolean verticalFlip){
        if(horizontalFlip){
            for(int i = 0 ; i < 4; ++i){
                for(int j = 0 ; j < 8; ++j){
                    Color temp = sprite[i][j];
                    sprite[i][j] = sprite[7 - i][j];
                    sprite[7 - i][j] = temp;
                }
            }
        }

        if(verticalFlip){
            for(int i = 0 ; i < 8; ++i){
                for(int j = 0 ; j < 4; ++j){
                    Color temp = sprite[i][j];
                    sprite[i][j] = sprite[i][7 - j];
                    sprite[i][7 - j] = temp;
                }
            }
        }
    }
}
