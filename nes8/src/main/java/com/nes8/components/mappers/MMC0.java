package com.nes8.components.mappers;

import com.nes8.Constants;
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
            return rom.sram[address - 0x6000];
        }
        else if(address >= 0x8000 && address <= 0xBFFF){
            return rom.pgr_ROM[address - 0x8000];
        }
        else if(address >= 0xC000 && address <= 0xFFFF){
            // Last 16KB if NROM- 256, mirror of 0x8000 - 0xBFFF if NROM - 128
            if(rom.pgr_ROM.length > 16 * Constants.ONE_KB) return rom.pgr_ROM[address];
            else return rom.pgr_ROM[address - 0xC000];
        }
        return 0;
    }

    @Override
    public void write(int address, byte value) {
        if(address >= 0x0000 && address <= 0x1FFF){
            rom.pt_data[address] = value;// write to CRH RAM
        }
        else if(address >=  0x6000 && address <= 0x7FFF){
            rom.sramModified = true;
            rom.sram[address - 0x6000] = value;
        }
    }
}
