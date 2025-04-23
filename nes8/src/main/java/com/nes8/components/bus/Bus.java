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
        if(address >= 0x0000 && address <= 0x1FFF){
            return ram.read(address & 0x07FF);
        }
        else if(address >= 0x2000 && address <= 0x3FFF){
            // TODO : mirror of every 8 bytes
            return ppu.registers[address - 0x2000];
        }
        else if(address >= 0x4000 && address <= 0x4017){
            // TODO : APU
        }
        else if(address >= 4020 && address <= 0x5FFF){
            // TODO :Cartridge expansion
        }
        else if(address >= 6000 && address <= 0x7FFF){
            // TODO : Cartridge SRAM ( If present - battery backed save RAM)
        }    
        else if(address >= 0x8000 && address <= 0xFFFF){
            // TODO : Bank switched by Mapper
            return rom.pgr_ROM[address - 0x8000 ];
        }
        return 0;
    }

    public byte ppuRead(int address){
        if(address >= 0x0000 && address <= 0x1FFF){
            return rom.chr_ROM[address];
        }else if(address >= 0x2000 && address <= 0x27FF){
            // TODO : Handle mirrors
            return ppu.nt.vram[address - 0x2000];
        }else if(address >=0x3000 && address <= 0x3FFF ){
            // TODO : Handle mirrors
            return ppu.pallete.readPallete(address);
        }
        return 0;
    }

    public void cpuWrite(int address, byte value){
        if(address >= 0x0000 && address <= 0x1FFF){
            ram.write(address & 0x07FF, value);
        }
        else if(address >= 0x2000 && address <= 0x3FFF){
            // TODO : mirror of every 8 bytes
            ppu.registers[address - 0x2000] = value;
        }
        else if(address >= 0x4000 && address <= 0x4017){
            // TODO : APU
        }
        else if(address >= 4020 && address <= 0x5FFF){
            // TODO : Cartridge expansion
        }
        else if(address >= 6000 && address <= 0x7FFF){
            // TODO : Cartridge SRAM ( If present - battery backed save RAM)
        }    
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
