package com.nes8;

import java.io.IOException;

import com.nes8.memory.ROM;
import com.nes8.components.bus.*;
import com.nes8.components.CPU;
import com.nes8.components.PPU;
import com.nes8.components.Controller;


public class MainLoop 
{
    public static void main(String[] args) throws IOException, InterruptedException{
        ROM rom = new ROM(Settings.ROM_PATH);
        if(!rom.initROM()) return;
        CPUBus cpuBus = new CPUBus(rom);
        CPU cpu = new CPU(cpuBus,rom.pgr_rom_size);

        PPUBus ppuBus = new PPUBus(rom);
        PPU ppu = new PPU(ppuBus);
       
        Controller.init(cpuBus);

        new Thread( () -> {
                try{
                    cpu.interpret();// 1.79 Mhz
                }catch(Exception e){}
        }).start();

        new Thread( () -> {
                try{
                    ppu.start();// 5.32 Mhz
                }catch(Exception e){}
        }).start();
        
    }
}
