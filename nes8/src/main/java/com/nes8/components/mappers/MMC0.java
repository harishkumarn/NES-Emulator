package com.nes8.components.mappers;

public class MMC0 implements MemoryMappingController{

    @Override
    public byte read(int address) {
        return 0;
    }

    @Override
    public void write(int address, byte value) {
        
    }
    
}
