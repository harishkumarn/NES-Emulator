package com.nes8.components;

import com.nes8.components.software.ISA;
import com.nes8.graphics.ObjectAttributeMemory;
import java.util.Stack;


public class CPU {
    // Registers
    public short programCounter = 0;
    public byte stackPointer = 0 ; 
    public byte statusRegister = 0;
    public byte indexX = 0;
    public byte indexY = 0;
    public byte accumulator = 0 ;

    public enum Flag{
        C(1<<7),
        Z(1<<6),
        I(1<<5),
        D(1<<4),
        B(1<<3),
        U(1<<2),
        V(1<<1),
        N(1);
        int index;
        Flag(int index){
            this.index = index;
        }
        public int index(){
            return this.index;
        }
    }

    // Components
    public Bus bus;
    DMA dma = new DMA();
    ObjectAttributeMemory oam = new ObjectAttributeMemory();
    public Stack<Integer> stack = new Stack<Integer>();
    ISA isa = new ISA(this);


    public CPU(Bus bus){
        this.bus = bus;
    }

    private void cycle(byte cycles) throws InterruptedException{
        // 1.79 MHz is roughly  558 nano sec per cycle
        Thread.sleep(0,cycles *  558);
    }

    public void interpret() throws InterruptedException{
        
        while(programCounter <= this.bus.byteCodeLastAddress){
            byte inst = bus.getByteCode(programCounter);
            byte cycles = isa.getOpcode(inst).execute();
            cycle(cycles);
        }
    }

    public void updateFlag(Flag flag, boolean yes){
        if(yes) {
            this.stackPointer |= flag.index;
        }else if((this.statusRegister & flag.index) > 0){
            this.stackPointer ^= flag.index;
        }
    }

    public byte getFlag(Flag flag){
        return (byte) ((this.statusRegister & flag.index) > 0 ? 1 : 0) ;
    }
}
