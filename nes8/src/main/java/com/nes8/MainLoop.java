package com.nes8;

import java.io.IOException;

import com.nes8.memory.*;
import com.nes8.components.bus.Bus;
import com.nes8.components.processor.*;
import com.nes8.components.Controller;


public class MainLoop 
{
    public static void main(String[] args) throws IOException, InterruptedException{
        ROM rom = new ROM(Settings.ROM_PATH);
        if(!rom.initROM()) return;
        Bus bus = new Bus(rom, new RAM());
        CPU cpu = new CPU(bus,rom.pgr_rom_size);
        PPU ppu = new PPU(bus);
        APU apu = new APU(bus);
       
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

        new Thread(() -> {
            try{
                apu.startSound();// 1.79 Mhz
            }catch(Exception e){}
        });
        
    }
}
