package com.nes8.graphics;

import com.nes8.Settings;
import com.nes8.components.bus.Bus;

import java.awt.Color;

import com.nes8.components.helper.Display;
import com.nes8.components.helper.RenderingUtils;
 
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
    String name ;

    private Color[][] pt = new Color[PT_HEIGHT][PT_WIDTH];

    public PatternTable(Bus bus , String name){
        this.bus = bus;
        this.name = name;
    }

    public void init(int address){
        if(!Settings.RENDER_META_DATA) return;
        for(int j = 0 ; j < 128;j += 8){
            for(int i = 0 ; i < 128; i += 8  ){
                RenderingUtils.renderTile(i,j, address, pt,Pallete.PATTERN_TABLE_COLORS, bus);
                address += 16;
            }
        }
        Display.init(PatternTable.PT_WIDTH, PatternTable.PT_HEIGHT, Settings.PT_SCALE, pt, name);
    }
}
