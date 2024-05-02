package com.nes8.components;
import com.nes8.memory.*;
public class Bus {
    ROM rom ;
    RAM ram = new RAM();
    public Bus(ROM rom){
        this.rom = rom;
    }

    /**
     * Read 6502 Byte code from ROM
     * @param address
     * @return
     */
    public byte getByteCode(int address){
        return 0;
    }

    /**
     * RAM read
     * @param address
     * @return
     */
    public byte cpuRead(int address){
        return ram.read(address);
    }

    /**
     * RAM write
     * @param address
     * @param i
     * @return
     */
    public boolean cpuWrite(int address, byte i){
        ram.write(address, i);
        return true;
    }

    /**
     * PPU ROM read
     * @param address
     * @return
     */
    public int ppuRead(int address){
        return 0 ;
    }
}
