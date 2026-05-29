package com.nes8.components.processor;


import com.nes8.Settings;
import com.nes8.components.helper.RenderingUtils;
import com.nes8.graphics.*;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

import com.nes8.components.bus.Bus;

/**
 * Memory range : 0x0000 to 0x3FFF
 */
public class PPU {
    Bus bus;
    static int[][] tileQuadrantMapping = new int[][]{{0,1},{2,3}};
    

    int patternTableAddress;

    PatternTable pt1, pt2 ;
    public NameTable nt =  new NameTable();    
    public Pallete pallete = new Pallete();
    public ObjectAttributeMemory oam  = new ObjectAttributeMemory();
    OutputBuffer gui ;

    //8 PPU registers memory mapped from 0x2000 to 0x2007
    public byte[] registers = new byte[8];
    public AtomicInteger ppuStatus  = new AtomicInteger(0);

    private int addressLatch = 0;
    private int vramAddress = 0;
    private byte vramBuffer = 0;

    public PPU(Bus bus){
        this.bus = bus;
        this.pt1 = new PatternTable(bus,"PT-1");
        this.pt2 = new PatternTable(bus,"PT-2");
        this.gui = new OutputBuffer(bus);
        this.gui.initDisplay();
        bus.setPPU(this);
    }

    public void start() throws InterruptedException{
        initPatternTables();
        renderGUI();
    }

    private void initPatternTables(){
        pt1.init(0x0000);
        pt2.init(0x1000);
    }


    private void renderGUI() throws InterruptedException{
        while(true){
            // 261 - Pre-Render Scanline
            ppuStatus.set(ppuStatus.get() & ~0x80);
            hBlank(); 

            // Each iteration renders a frame
            int vramOffset = getVRAMOffset();
            int ptOffset = getPTOffsetForBackground();
            //0 - 239 -> Visible. Each scanline = 256 Pixels + HBLANK
            for(int i = 0 ; i < 240; i += 8 ){

                // Each scan line is 341 PPU Cycles
                // 0-255 Pixel-rendering
                // 256 - 340 - HBLANK
                //      256 - 320 Sprite fetch for next line
                //      321 - 340 Fetches tile data for next line
                for(int j = 0 ; j < 256; j += 8 ){
                    int tileIndex = bus.ppuRead(vramOffset ++) & 0xFF;
                    RenderingUtils.renderTile(i, j, ptOffset + tileIndex * 16, gui.outputBuffer, getPalleteForBackground(i,j, vramOffset) , bus);
                }
                //H-BLANK
            }
            // 240 - Post-Render   
            hBlank();

            this.gui.rerender();

            // 241 - 260 V-BLANK
            ppuStatus.set(ppuStatus.get() | 0x80);
            if((registers[0] & 0x80) != 0){
                bus.cpu.NMI();
            } 
            vBlank();
        }
    }

    public byte read(int registerIndex){
        switch(registerIndex){
            case 2:
                int val = ppuStatus.getAndUpdate(v -> v & 0x7F);
                addressLatch = 0;
                return (byte)val;
            case 7:
                byte data  = vramBuffer;
                vramBuffer = bus.ppuRead(vramAddress);
                if(vramAddress >= 0x3F00){
                    data = vramBuffer;
                }
                vramAddress += (registers[0] & 4) != 0 ? 32 : 1;
                return data;
            default:
                return registers[registerIndex];
        }
    }

    public void write(int address, byte data){
        if(address >= 0x2000 && address <= 0x3FFF){
            address = (address - 0x2000 ) & 0x7;
        }
        switch(address){
            case 0:// PPUCTRL
            registers[0] = data;
            break;
            case 1:// PPUMASK
            registers[1] = data;
            break;
            case 2:// PPUSTATUS - read only, writes are ignored
            break;
            case 3:// OAMADDR
            registers[3] = data;
            break;
            case 4:// OAMDATA
            registers[4] = data;
            oam.write(registers[3], data); 
            break;
            case 5:// PPUSCROLL
            registers[5] = data;
            addressLatch ^= 1;
            break;
            case 6:// PPUADDR
            if( addressLatch == 0){
                vramAddress = ( (data & 0x3F) << 8) | (vramAddress & 0x00FF);
            } else {
                vramAddress = (vramAddress & 0xFF00) | (data & 0xFF);
            }
            addressLatch ^= 1;
            registers[6] = data;
            break;
            case 7:// PPUDATA
            bus.ppuWrite(vramAddress, data);
            vramAddress += (registers[0] & 4) != 0 ? 32 : 1;
            registers[7] = data;
            break;
        }
    }

    public int getPTOffsetForBackground(){
        return (registers[0] & 16 ) == 16  ? 0x1000 : 0x0000;
    } 


    public int getPTOffsetForForeground(){
        return (registers[0] & 8 ) == 8  ? 0x1000 : 0x0000;
    }

    public int getVRAMOffset(){
        switch((registers[0] & 3)){
            case 0:
            return 0x2000;
            case 1:
            return 0x2400;
            case 2:
            return 0x2800;
            case 3:
            return 0x2C00;
        }
        return 0 ;
    }

    public Color[] getPalleteForBackground(int i, int j, int vramOffset){
        int baseNT = getVRAMOffset();
        int attributeTableOffset = baseNT + 960 ;
        int tileX = j / 8;
        int tileY = i / 8;
        int attrIndex = ( tileY / 4 ) * 8 + ( tileX / 4 );
        byte data = bus.ppuRead(attributeTableOffset + attrIndex);
        int shift = ((tileY & 2) << 1 ) | ( tileX & 2);
        int pIndex = (data >> shift) & 0x3;
        Color[] c = new Color[4];
        c[0] = Pallete.pallete[pallete.backGround[0][0] & 0x3F];
        for(int k = 1 ; k < 4; k++){
            c[k] = Pallete.pallete[pallete.backGround[pIndex][k] & 0x3F];
        }
        return c;
    }

    private void cycle(int cycles) throws InterruptedException{
        // 5.32 MHz is roughly  188 nano sec per cycle
        long nanos = (long)(cycles * 188 / Settings.GAME_SPEED);
        long millis = nanos / 1_000_000; 
        int remainNanos = (int)(nanos % 1_000_000);
        Thread.sleep(millis, remainNanos);
    }

    private void hBlank() throws InterruptedException{
        cycle(341);
    }

    private void vBlank() throws InterruptedException{
        cycle(20 * 341);
    }
}
