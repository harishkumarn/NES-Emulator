package com.nes8.components;


import com.nes8.Settings;
import com.nes8.components.helper.Display;
import com.nes8.graphics.*;

/**
 * Memory range : 0x0000 to 0x3FFF
 */
public class PPU {
    Bus bus;

    /**
     * Range : 0X0000 to 0x1FFF
     * PT1 - 0x0000 to 0x0FFF - 4KB
     * PT2 - 0X1000 to 0x1FFF - 4KB  
     */
    PatternTable pt1, pt2 ;

    /**
     * Range : 0x2000 to 0x3EFF
     * NT 0 : 0x2000 to 0x23FF - 1 KB
     * NT 1 : 0x2400 to 0x27FF - 1 KB
     * NT 2 ( Mirror ) : 0x2800 to 0x2BFF - 1 KB
     * NT 3 ( Mirror ) : 0x2C00 to 0x2FFF - 1 KB
     * Mirrors of 0x2000 to 0x2EFF : 0x3000 - 0x3EFF - 1 KB
     */
    NameTable nt;

    /**
     * Range : 0x3F00 to 0x3FFF 
     * 0x3F00 to 0x3F1F - 32 B
     * Mirros of 0x3F00 to 0x3F1F : 0x3F20 to 0x3FFF 
     */
    Pallete pallete;
    GameUI gui ;

    public PPU(Bus bus){
        this.bus = bus;
        this.pt1 = new PatternTable(bus);
        this.pt2 = new PatternTable(bus);
        this.gui = new GameUI();
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

    private void cycle(int cycles) throws InterruptedException{
        // 5.32 MHz is roughly  188 nano sec per cycle
        Thread.sleep(0,(int)(cycles *  188 * Settings.GAME_SPEED));
    }

    private void hBlank() throws InterruptedException{
        cycle(341);
    }

    private void vBlank() throws InterruptedException{
        cycle(21 * 341);
    }
}
