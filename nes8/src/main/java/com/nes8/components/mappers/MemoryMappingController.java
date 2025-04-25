package com.nes8.components.mappers;

public interface MemoryMappingController {
    public byte read(int address);

    public void write(int address, byte value);
}
