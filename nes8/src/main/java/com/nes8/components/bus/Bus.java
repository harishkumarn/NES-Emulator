package com.nes8.components.bus;

import com.nes8.memory.RAM;
import com.nes8.memory.ROM;
import com.nes8.components.processor.*;

public class Bus{

    public ROM rom ;
    private RAM ram = new RAM();
    CPU cpu;
    PPU ppu;

    public Bus(ROM rom){
        this.rom = rom;
    }

    public byte cpuRead(int address){
        if(address >= 0x8000 && address <= 0xBFFF){
            return rom.pgr_ROM[address - 0x8000 ];
        }else if(address >= 0x2000 && address <= 0x2007){
            
        }
        return ram.read(address);
    }

    public byte ppuRead(int address){
        if(address >= 0x0000 && address <= 0x1FFF){
            return rom.chr_ROM[address];
        }else if(address >= 0x2000 && address <= 0x27FF){
            // TODO : Handle mirrors
            return ppu.nt.vram[address - 0x2000];
        }else if(address >=0x3000 && address <= 0x3FF ){
            // TODO : Handle mirrors
            return ppu.pallete.readPallete(address);
        }
        return 0;
    }

    public void cpuWrite(int address, byte value){
        ram.write(address, value);
    }


    public void ppuWrite(int address, byte value){

    }

    public void setCPU(CPU cpu){
        this.cpu = cpu;
    }

    public void setPPU(PPU ppu){
        this.ppu = ppu;
    }
    
}
