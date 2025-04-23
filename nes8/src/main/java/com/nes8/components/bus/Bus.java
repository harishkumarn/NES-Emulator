package com.nes8.components.bus;

import com.nes8.components.processor.Processor;

public interface Bus{

    public byte read(int address);

    public void write(int address, byte value);

    public void setProcessor(Processor p);
    
}
