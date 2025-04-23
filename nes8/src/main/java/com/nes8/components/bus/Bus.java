package com.nes8.components.bus;

public interface Bus {

    public byte read(int address);

    public void write(int address, byte value);
    
}
