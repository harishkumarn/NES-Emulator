package com.nes8.components;


import com.nes8.Settings;
import com.nes8.components.helper.Display;
import com.nes8.graphics.*;
import com.nes8.components.bus.Bus;

/**
 * Memory range : 0x0000 to 0x3FFF
 */
public class PPU {
    Bus bus;

    PatternTable pt1, pt2 ;
    NameTable nt;    
    Pallete pallete;
    OutputBuffer gui ;

    //8 PPU registers memory mapped from 0x2000 to 0x2007
    byte[] registers = new byte[8];

    public PPU(Bus bus){
        this.bus = bus;
        this.pt1 = new PatternTable(bus);
        this.pt2 = new PatternTable(bus);
        this.gui = new OutputBuffer();
    }

    public void start(){
        initPatternTables();
        initNameTable();
    }

    private void initPatternTables(){
        pt1.init(0x0000);
        pt2.init(0x1000);
        if(Settings.RENDER_PATTERN_TABLE){
            Display.init(PatternTable.PT_WIDTH, PatternTable.PT_HEIGHT, Settings.PT_SCALE, pt1.getPixels(),  "PT-1");
            Display.init(PatternTable.PT_WIDTH, PatternTable.PT_HEIGHT, Settings.PT_SCALE, pt2.getPixels(), "PT-2");
        }
    }

    private void initNameTable(){

    }

    public void write(int address, byte data){
        registers[address - 0x2000] = data;
    }

    private void cycle(int cycles) throws InterruptedException{
        // 5.32 MHz is roughly  188 nano sec per cycle
        Thread.sleep(0,(int)(cycles *  188  / Settings.GAME_SPEED));
    }

    private void hBlank() throws InterruptedException{
        cycle(341);
    }

    private void vBlank() throws InterruptedException{
        cycle(21 * 341);
    }
}
