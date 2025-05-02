package com.nes8.components.bus;

import com.nes8.memory.RAM;
import com.nes8.memory.ROM;
import com.nes8.components.processor.*;
import com.nes8.components.Controller;
import com.nes8.components.DMA;

public class Bus{

    public ROM rom ;
    private RAM ram;
    public CPU cpu;
    PPU ppu;
    APU apu;
    Controller controller;

    public Bus(ROM rom, RAM ram){
        this.rom = rom;
        this.ram = ram;
    }

    public byte cpuRead(int address){
        byte res = 0 ;
        if(address >= 0x0000 && address <= 0x1FFF){
            res = ram.read(address & 0x07FF);// Mirrored every 2KB
        }
        else if(address >= 0x2000 && address <= 0x3FFF){
            res = ppu.registers[(address - 0x2000 ) & 0x7];// mirrored every 8 bytes
        }
        else if( address == 0x4015){
            res =  apu.getStatusRegister();
        }else if(address == 0x4016 || address == 0x4017){
            res = controller.rightShiftRegister(address);
        }
        else if(address >= 4020 && address <= 0xFFFF){
            res = rom.mmc.read(address);
        }
        return (byte)(res & 0xFF);
    }

    public byte ppuRead(int address){
        if(address >= 0x0000 && address <= 0x1FFF){
            return rom.mmc.read(address);
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
            System.out.println("PPU registers written");
            ppu.registers[(address - 0x2000 ) & 0x7] = value;// mirrored every 8 bytes
        }
        else if((address >= 0x4000 && address <= 0x4013) || address == 0x4015 || address == 0x4017){
            apu.write(address, value);
        }else if(address == 0x4014){
            DMA.startDMATransfer(value, ram, ppu);
        }
        else if(address == 0x4016 ){
            if(value == 1){
                controller.loadShiftRegisters();
            }
        }
        else if(address >= 4020 && address <= 0xFFFF){
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
