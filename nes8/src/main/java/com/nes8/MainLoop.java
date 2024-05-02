package com.nes8;

import java.io.IOException;

import com.nes8.memory.ROM;
import com.nes8.components.Bus;
import com.nes8.components.CPU;
import com.nes8.components.PPU;


public class MainLoop 
{
    public static void main(String[] args) throws IOException, InterruptedException{
        ROM rom = new ROM("/Users/harish-8433/Downloads/64-in-1 (J) [p1].nes");
        rom.initROM();
        Bus bus = new Bus(rom);
        CPU cpu = new CPU(bus);
        PPU ppu = new PPU(bus);
        cpu.interpret();// 1.79 Mhz
        ppu.start();// 5.32 Mhz
    }
}
