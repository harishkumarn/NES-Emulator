package com.nes8.graphics;

import com.nes8.components.Bus;

import java.awt.Color;

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
        byte[] lowByte = new byte[8], highByte = new byte[8];
        int x,y, c;
       
        for(int j = 0 ; j < 128;j+=8){
            for(int i = 0 ; i < 128; i+= 8  ){
                for(int k = 0; k < 8;++k) lowByte[k] = bus.rom.chr_ROM[address++];//plane 1
                for(int k = 0; k < 8;++k) highByte[k] = bus.rom.chr_ROM[address++];//plane 2
                for(int k = 0; k < 8;++k){
                    for(int l = 7; l >= 0;--l){
                        x = i + ( 7 - l );
                        y = j + k ;
                        c = 0 ;
                        if((highByte[k] & ( 1<< l)) > 0 ) c = 2;
                        if((lowByte[k] & ( 1<< l)) > 0) c += 1;
                        pt[x][y] = Pallete.PATTERN_TABLE_COLORS[c];
                    }
                }
            }
        }
    }
}
