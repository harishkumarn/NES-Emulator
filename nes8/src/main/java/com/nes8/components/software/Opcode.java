package com.nes8.components.software;

import com.nes8.Settings;

public abstract class Opcode {
    byte  cycle;
    public Opcode( byte cycle){
        this.cycle = cycle;
    }
    public byte execute(){
        return cycle;
    }
    public void printASM(String code){
        if(Settings.DISASSEMBLE_ASM) System.out.println(code);
    }
} 