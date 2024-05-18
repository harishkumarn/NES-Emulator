package com.nes8.components.software;

public abstract class Opcode {
    byte  cycle;
    boolean disassemble = false;
    public Opcode( byte cycle){
        this.cycle = cycle;
    }
    public void disassembleASM(boolean yes){
        this.disassemble = yes;
    }
    public byte execute(){
        return cycle;
    }
    public void printASM(String code){
        if(disassemble) System.out.println(code);
    }
} 