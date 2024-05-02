package com.nes8.components.software;

public class Opcode {
    byte  cycle;
    public Opcode( byte cycle){
        this.cycle = cycle;
    }
    public byte execute(){
        return cycle;
    }
} 