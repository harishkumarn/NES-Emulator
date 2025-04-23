package com.nes8.components.processor;

import com.nes8.Settings;
import com.nes8.components.software.ISA;
import com.nes8.graphics.ObjectAttributeMemory;
import java.util.Stack;

import com.nes8.components.DMA;
import com.nes8.components.bus.Bus;


/**
 * This is an attempt to emulate the 6502, I'm pretty sure this is ridden with errors in many instructions :p
 * Memory range : 0x0000 to 0xFFFF
 */

public class CPU{
    // Registers
    public int programCounter =  0x8000;
    public byte stackPointer = 0 ; 
    public byte statusRegister = 0;
    public byte indexX = 0;
    public byte indexY = 0;
    public byte accumulator = 0 ;

    private int byteCodeLastAddress ;

    public enum Flag{
        C(1<<7), // Carry Flag
        Z(1<<6), // Zero flag
        I(1<<5), // IRQ disable flag
        D(1<<4), // BCD Flag ( Not used in NES)
        B(1<<3), // Break flag
        U(1<<2), // Unused
        V(1<<1), // Unsigned overflow
        N(1); // Negative
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


    public CPU(Bus bus, int pgr_rom_size){
        this.bus = bus;
        this.byteCodeLastAddress = this.programCounter + pgr_rom_size;
        bus.setCPU(this);
    }

    private void cycle(byte cycles) throws InterruptedException{
        // 1.79 MHz is roughly  558 nano sec per cycle
        Thread.sleep(0,(int)(cycles *  558 / Settings.GAME_SPEED));
    }


    public void interpret() throws InterruptedException{
        while(programCounter <= this.byteCodeLastAddress){
            byte inst = bus.cpuRead(programCounter);
            System.out.print(Integer.toHexString(programCounter) + "    ");
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
