package com.nes8.components.mappers;

import com.nes8.memory.ROM;

public class MMC1 implements MemoryMappingController{

    ROM rom ;

    public MMC1(ROM rom){
        this.rom = rom;
    }

    @Override
    public byte read(int address) {
        if(address >= 0x0000 && address <= 0x1FFF){
            return  rom.pt_data[address];
        }else if(address >= 0xA000 && address <= 0xFFFF){
            return rom.pt_data[address];
        }
        return 0;
    }

    @Override
    public void write(int address, byte value) {
        // Will be from PPU, as CPU writes to 0x2006/0x2007 to access this address range as 0x0000 to 0x1FFF is mapped to CPU's RAM in the BUS
        if(address >= 0x0000 && address <= 0x1FFF){
            rom.pt_data[address] = value;
        }
        else if(address >= 0x8000 && address <= 0x9FFF){

        }
    }
    
}
