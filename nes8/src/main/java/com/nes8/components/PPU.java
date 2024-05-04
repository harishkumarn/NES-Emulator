package com.nes8.components;

public class PPU {
    Bus bus;
    private static int PT_WIDTH = 16*8;
    private static int PT_HEIGHT = 16*8;
    private static int PT_SCALE = 2;

    private static int DISPLAY_SCALE = 5;
    private static int DISPLAY_WIDTH = 32*8;
    private static int DISPLAY_HEIGHT = 30*8;

    private byte[][] display = new byte[DISPLAY_WIDTH][DISPLAY_HEIGHT];
    private byte[][] pt1  = new byte[PT_WIDTH][PT_HEIGHT];
    private byte[][] pt2  = new byte[PT_WIDTH][PT_HEIGHT];
    public PPU(Bus bus){
        this.bus = bus;
    }

    public void start(){
        initPatternTables();
    }

    private void initPatternTables(){
        renderPatternTable(0x0000, pt1);
        new Display(PT_WIDTH, PT_HEIGHT, PT_SCALE, pt1, "PT-1");
        renderPatternTable(0x1000, pt2);
        new Display(PT_WIDTH, PT_HEIGHT, PT_SCALE, pt2, "PT-2");
    }

    private void cycle(byte cycles) throws InterruptedException{
        // 5.32 MHz is roughly  188 nano sec per cycle
        Thread.sleep(0,cycles *  188);
    }

    private void renderPatternTable(int address, byte[][] pt){
        byte[] lowByte = new byte[16], highByte = new byte[16];
        for(int i = 0 ; i < 128; i+= 8  ){
            for(int j = 0 ; j < 128;j+=8){
                for(int k = 0; k < 8;++k) lowByte[k] = bus.rom.chr_ROM[address++];//plane 1
                for(int k = 0; k < 8;++k) highByte[k] = bus.rom.chr_ROM[address++];//plane 2
                for(int k = 0; k < 8;++k){
                    for(int l = 7; l >= 0;--l){
                        int x = i + ( 7 - l );
                        int y = j + k ;
                        if((highByte[k] & ( 1<< l)) > 0 ) pt[x][y] = 2;
                        if((lowByte[k] & ( 1<< l)) > 0) pt[x][y] += 1;
                    }
                }
            }
        }
    }
}
