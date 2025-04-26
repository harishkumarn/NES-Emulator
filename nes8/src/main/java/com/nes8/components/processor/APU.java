package com.nes8.components.processor;

import com.nes8.components.bus.Bus;

/**
 * NES has over 5 Audio channels
 */
public class APU {
    byte[] pulseChannel1 = new byte[4];
    byte[] pulseChannel2 = new byte[4];
    byte[] triangleChannel = new byte[4];
    byte[] noiseChannel = new byte[4];
    byte[] deltaModulationChannel = new byte[4];

    byte statusRegister, frameCounter;

    public APU(Bus bus){
        bus.setAPU(this);
    }

    public void startSound(){

    }

    public void write(int address, byte value){
        if(address >= 0x4000 && address <= 0x4003){
            pulseChannel1[address - 0x4000] = value;
        }else  if(address >= 0x4004 && address <= 0x4007){
            pulseChannel2[address - 0x4004] = value;
        }else  if(address >= 0x4008 && address <= 0x400B){
            triangleChannel[address - 0x4008] = value;
        }else  if(address >= 0x400C && address <= 0x400F){
            noiseChannel[address - 0x400C] = value;
        }else  if(address >= 0x4010 && address <= 0x4013){
            deltaModulationChannel[address - 0x4010] = value;
        }else if(address == 0x4015){
            statusRegister = value;
        }else if(address == 0x4017){
            frameCounter = value;
        }
    }

    public byte getStatusRegister(){
        return this.statusRegister;
    }
}
