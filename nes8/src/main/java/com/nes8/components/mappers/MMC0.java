package com.nes8.components.mappers;

import com.nes8.memory.ROM;

public class MMC0 implements MemoryMappingController{

    ROM rom;

    public MMC0(ROM rom){
        this.rom = rom;
    }

    @Override
    public byte read(int address) {
        if(address >= 0x0000 && address <= 0x1FFF){
            return rom.pt_data[address];
        }
        if(address >=  0x6000 && address <= 0x7FFF){
            // TODO : SRAM
            System.out.println("Attempt to write to SRAM");
        }
        else if(address >= 0x8000 && address <= 0xBFFF){
            return rom.pgr_ROM[address - 0x8000];
        }
        else if(address >= 0xC000 && address <= 0xFFFF){
            // TODO : Last 16KB if NROM- 256, mirror of 0x8000 - 0xBFFF if NROM - 128
        }
        return 0;
    }

    @Override
    public void write(int address, byte value) {
        
    }
}
