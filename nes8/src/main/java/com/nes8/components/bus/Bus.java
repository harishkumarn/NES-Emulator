package com.nes8.components.bus;

import com.nes8.memory.RAM;
import com.nes8.memory.ROM;
import com.nes8.components.processor.*;
import com.nes8.components.Controller;

public class Bus{

    public ROM rom ;
    private RAM ram;
    CPU cpu;
    PPU ppu;
    APU apu;
    Controller controller;

    public Bus(ROM rom, RAM ram){
        this.rom = rom;
        this.ram = ram;
    }

    public byte cpuRead(int address){
        if(address >= 0x0000 && address <= 0x1FFF){
            return ram.read(address & 0x07FF);// Mirrored every 2KB
        }
        else if(address >= 0x2000 && address <= 0x3FFF){
            return ppu.registers[(address - 0x2000 ) & 0x7];// mirrored every 8 bytes
        }
        else if(address >= 0x4000 && address <= 0x4015){
            // TODO : APU
        }else if(address == 0x4016 || address == 0x4017){
            return controller.rightShiftRegister(address);
        }
        else if(address >= 4020 && address <= 0x5FFF){
            // TODO :Cartridge expansion
        }
        else if(address >= 6000 && address <= 0x7FFF){
            return rom.mmc.read(address);
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
        }else if(address >= 0x2000 && address <= 0x3EFF){
            return ppu.nt.vram[(address - 0x2000 ) & 0x7FF];// Mirrored every 2KB
        }else if(address >=0x3F00 && address <= 0x3FFF ){
            // TODO : Handle mirrors
            return ppu.pallete.readPallete(address);
        }
        return 0;
    }

    public void cpuWrite(int address, byte value){
        if(address >= 0x0000 && address <= 0x1FFF){
            ram.write(address & 0x07FF, value);// Mirrored every 2KB
        }
        else if(address >= 0x2000 && address <= 0x3FFF){
            ppu.registers[(address - 0x2000 ) & 0x7] = value;// mirrored every 8 bytes
        }
        else if((address >= 0x4000 && address <= 0x4013) || address == 0x4015 || address == 0x4017){
            apu.write(address, value);
        }
        else if(address == 0x4016 ){
            if(value == 1){
                controller.loadShiftRegisters();
            }
        }
        else if(address >= 4020 && address <= 0x5FFF){
            // TODO : Cartridge expansion
        }
        else if(address >= 6000 && address <= 0x7FFF){
            rom.mmc.write(address, value);
        }    
    }


    public void ppuWrite(int address, byte value){
        // PPU can write only to its 8 registers
    }

    public void setCPU(CPU cpu){
        this.cpu = cpu;
    }

    public void setPPU(PPU ppu){
        this.ppu = ppu;
    }

    public void setController(Controller c){
        this.controller = c;
    }
    
    public void setAPU(APU apu){
        this.apu = apu;
    }
}
