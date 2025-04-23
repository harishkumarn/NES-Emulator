package com.nes8.components.bus;

import com.nes8.memory.RAM;
import com.nes8.memory.ROM;

public class CPUBus implements Bus{

    public ROM rom ;
    private RAM ram = new RAM();
    int pgr_rom_size;


    public CPUBus(ROM rom){
        this.rom = rom;
        this.pgr_rom_size = rom.pgr_rom_size;
    }

    @Override
    public byte read(int address){
        if(address >= 0x8000 && address <= 0xBFFF){
            return rom.pgr_ROM[address - 0x8000 ];
        }else if(address >= 0x2000 && address <= 0x2007){
            
        }
        return ram.read(address);
    }

    @Override
    public void write(int address, byte value){
        ram.write(address, value);
    }
}
