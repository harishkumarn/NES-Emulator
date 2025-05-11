package com.nes8.graphics;

import com.nes8.Constants;

/*
 * This is basically the "VRAM" of the NES
 * Range : 0x2000 to 0x3EFF
 * NT 0 : 0x2000 to 0x23FF - 1 KB
 * -> Attribute table : 0x23C0 to 0x23FF
 * NT 1 : 0x2400 to 0x27FF - 1 KB
 * -> Attribute table : 0x27C0 to 0x27FF
 * NT 2 ( Mirror ) : 0x2800 to 0x2BFF - 1 KB
 * -> Attribute table : 0x2BC0 to 0x2BFF
 * NT 3 ( Mirror ) : 0x2C00 to 0x2FFF - 1 KB
 * -> Attribute table : 0x2FC0 to 0x2FFF
 * Mirrors of 0x2000 to 0x2EFF : 0x3000 - 0x3EFF - 1 KB
 * 
 */
public class NameTable {
    byte[] vram = new byte[2 * Constants.ONE_KB]; // 2 KB

    // TODO: Handle horizontal, vertical mirroring
    public void write(int address, byte value){
        if(address >= 0x2000 && address <= 0x27FF){
            vram[address - 0x2000] = value;
        }
    }

    public byte read(int address){
        return vram[address];
    }
}

