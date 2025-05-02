package com.nes8.graphics;

import com.nes8.components.bus.Bus;

import java.awt.Color;
 
/**
 * Range : 0X0000 to 0x1FFF
 * -> This range is memory mapped to the first 8 KB of the ROM, i.e., CHR-ROM
 * -> PPU can access this using ppuRead API
 * PT1 - 0x0000 to 0x0FFF - 4KB
 * PT2 - 0X1000 to 0x1FFF - 4KB  
 */
public class PatternTable {
    Bus bus;

    public static int PT_WIDTH = 16*8;
    public static int PT_HEIGHT = 16*8;

    private Color[][] pt = new Color[PT_HEIGHT][PT_WIDTH];

    public PatternTable(Bus bus ){
        this.bus = bus;
    }

    public Color[][] getPixels(){
        return pt;
    }

    public void init(int address){
        for(int j = 0 ; j < 128;j += 8){
            for(int i = 0 ; i < 128; i += 8  ){
                renderTile(i,j, address, Pallete.PATTERN_TABLE_COLORS);
                address += 16;
            }
        }
    }

    public void renderTile(int i, int j, int address, Color[] pallColors){
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
                pt[x][y] = pallColors[c];
            }
        }
    }
}
