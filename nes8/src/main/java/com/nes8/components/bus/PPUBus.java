package com.nes8.components.bus;

import com.nes8.components.processor.Processor;

import com.nes8.memory.ROM;

public class PPUBus implements Bus{

    ROM rom ;
    Processor chip;

    public PPUBus(ROM rom){
        this.rom = rom;
    }

    @Override
    public byte read(int address){
        if(address >= 0x0000 && address <= 0x1FFF){
            return rom.chr_ROM[address];
        }
        return 0;
    }

    @Override
    public void write(int address, byte value){

    }

    @Override
    public void setProcessor(Processor p){
        this.chip = p;
    }
}
