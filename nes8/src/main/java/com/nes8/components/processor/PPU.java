package com.nes8.components.processor;


import com.nes8.Settings;
import com.nes8.components.helper.RenderingUtils;
import com.nes8.graphics.*;

import java.awt.Color;

import com.nes8.components.bus.Bus;

/**
 * Memory range : 0x0000 to 0x3FFF
 */
public class PPU {
    Bus bus;
    static int[][] tileQuadrantMapping = new int[][]{{0,1,2,3}};

    PatternTable pt1, pt2 ;
    public NameTable nt =  new NameTable();    
    public Pallete pallete = new Pallete();
    public ObjectAttributeMemory oam  = new ObjectAttributeMemory();
    OutputBuffer gui ;

    //8 PPU registers memory mapped from 0x2000 to 0x2007
    public byte[] registers = new byte[8];

    public PPU(Bus bus){
        this.bus = bus;
        this.pt1 = new PatternTable(bus,"PT-1");
        this.pt2 = new PatternTable(bus,"PT-2");
        this.gui = new OutputBuffer(bus);
        bus.setPPU(this);
    }

    public void start(){
        initPatternTables();
        renderGUI();
    }

    private void initPatternTables(){
        pt1.init(0x0000);
        pt2.init(0x1000);
    }


    private void renderGUI(){
        while(true){
            // Each iteration renders a frame
            int vramOffset = getVRAMOffset();
            int ptOffset = getPTOffsetForBackground();
            for(int i = 0 ; i < 240; i += 8 ){
                for(int j = 0 ; j < 256; i += j ){
                    byte tileIndex = bus.ppuRead(vramOffset ++);
                    RenderingUtils.renderTile(i, j, ptOffset + tileIndex * 16, gui.outputBuffer,getPalleteForBackground(i,j, vramOffset) , bus);
                }
            }
            bus.cpu.NMI();
        }
    }

    public byte read(int registerIndex){
        if(registerIndex == 2){
            if(registers[2] < 0) registers[2]  ^= 0x80;
        }

        return registers[registerIndex];
    }

    public void write(int address, byte data){
        if(address >= 0x2000 && address <= 0x3FFF){
            address = (address - 0x2000 ) & 0x7;
        }
        registers[address ] = data;
        switch(address ){
            case 0:// PPUCTRL
            break;
            case 1:// PPUMASK
            break;
            case 2:// PPUSTATUS
            if((data & 0x80 ) == 0x80) bus.cpu.NMI();
            break;
            case 4:// OAMDATA
            oam.write(registers[3], data); 
            break;
            case 5:// PPUSCROLL
            break;
            case 7:// PPUDATA
            System.out.println("Nametable init");
            nt.write(registers[6], data);
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
        switch((registers[2] & 3)){
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
        int attributeTableOffset = vramOffset + 960;
        byte data = bus.ppuRead(attributeTableOffset + i * 8 + j * 8);
        int pIndex = 0;
        switch(tileQuadrantMapping[(j % 4 ) / 2][(i % 4 ) /2]){
            case 0:
            pIndex = (data >> 6) & 3;
            break;
            case 1:
            pIndex = (data >> 4) & 3;
            break;
            case 2:
            pIndex = (data >> 2) & 3;
            break;
            default:
            pIndex = data & 3;
        }
        Color[] c = new Color[4];
        for(int k = 0 ; k < 4;++k) c[i] = Pallete.pallete[pallete.backGround[pIndex][k]];
        return c;
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
