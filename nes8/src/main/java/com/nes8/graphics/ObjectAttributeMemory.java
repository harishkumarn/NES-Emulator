package com.nes8.graphics;

/**
 * 256 bytes in size, holds a total of 64 sprites ( 4 byte per sprite)
 * ->Byte 0 - Y  position
 * ->Byte 1 - Tile index
 * ->Byte 2 - Attributes ( Pallete, flip, priority)
 * ->Byte 3 - X position
 */
public class ObjectAttributeMemory {

    public byte[] oam = new byte[256];

    public void write(int address, byte value){
        this.oam[address] = value;
    }
}
