package com.nes8;

import java.io.IOException;

import com.nes8.memory.ROM;
import com.nes8.components.bus.Bus;
import com.nes8.components.processor.CPU;
import com.nes8.components.processor.PPU;
import com.nes8.components.Controller;


public class MainLoop 
{
    public static void main(String[] args) throws IOException, InterruptedException{
        ROM rom = new ROM(Settings.ROM_PATH);
        if(!rom.initROM()) return;
        Bus bus = new Bus(rom);
        CPU cpu = new CPU(bus,rom.pgr_rom_size);

        PPU ppu = new PPU(bus);
       
        Controller.init(bus);

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
