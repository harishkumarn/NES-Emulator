package com.nes8.components.mappers;

import com.nes8.memory.ROM;

public class MMC2 implements MemoryMappingController{

    ROM rom ;

    public MMC2(ROM rom){
        this.rom = rom;
    }

    @Override
    public byte read(int address) {
        if(address >= 0x0000 && address <= 0x1FFF){
            return rom.pt_data[address];
        }
        return 0;
    }

    @Override
    public void write(int address, byte value) {
        if(address >= 0x0000 && address <= 0x1FFF){
            rom.pt_data[address] = value;
        }
    }
    
}
