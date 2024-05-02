package com.nes8.memory;

public class RAM {
    byte[] memory = new byte[1<<12];

    public byte read(int i){
        return memory[i];
    }

    public void write(int i, byte val){
        memory[i] = val;
    }
}
