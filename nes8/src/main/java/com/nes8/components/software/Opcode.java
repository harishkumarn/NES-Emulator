package com.nes8.components.software;

import com.nes8.components.CPU;


public class Opcode {
    byte  cycle;
  //  CPU cpu ;
    public Opcode( byte cycle){
        this.cycle = cycle;
       // this.cpu = cpu;
    }
    public byte execute(){
        return cycle;
    }
} 