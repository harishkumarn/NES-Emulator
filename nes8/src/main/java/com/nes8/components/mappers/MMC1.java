package com.nes8.components.mappers;

import com.nes8.memory.ROM;

public class MMC1 implements MemoryMappingController{

    ROM rom ;

    public MMC1(ROM rom){
        this.rom = rom;
    }

    @Override
    public byte read(int address) {
       return 0;
    }

    @Override
    public void write(int address, byte value) {
        
    }
    
}
