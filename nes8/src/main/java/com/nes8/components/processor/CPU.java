package com.nes8.components.processor;

import com.nes8.Settings;
import com.nes8.components.software.ISA;
import com.nes8.graphics.ObjectAttributeMemory;
import java.util.concurrent.locks.ReentrantLock;

import com.nes8.components.DMA;
import com.nes8.components.bus.Bus;

/**
 * This is an attempt to emulate the 6502, I'm pretty sure this is ridden with errors in many instructions :p
 * Memory range : 0x0000 to 0xFFFF
 */

public class CPU{
    // Registers
    public int programCounter ;
    public byte stackPointer = (byte)0xFD; 
    public byte statusRegister = 0;
    public byte indexX = 0;
    public byte indexY = 0;
    public byte accumulator = 0 ;

    private int byteCodeLastAddress ;
    
    // Used for NMI
    private ReentrantLock lock = new ReentrantLock();

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
    ISA isa = new ISA(this);


    public CPU(Bus bus, int pgr_rom_size){
        this.bus = bus;
        bus.setCPU(this);
        reset();
        this.byteCodeLastAddress = this.programCounter + pgr_rom_size;
    }

    public void reset(){
        byte low = bus.cpuRead(0xFFFC);
        byte high = bus.cpuRead(0xFFFD);
        this.programCounter = ((high << 8)  | low ) & 0xFFFF;
    }

    public void NMI(){
        lock.lock();
        byte low, high;
        high = (byte)( ( programCounter >> 8 ) & 0xFF );
        low = (byte)( programCounter & 0xFF); 
        stackPush(high);
        stackPush(low);
        stackPush((byte)(statusRegister | 0x20));
        updateFlag(Flag.I, true);
        low = bus.cpuRead(0xFFFA);
        high = bus.cpuRead(0xFFFB);
        programCounter = ((high<<8) | low ) & 0xFFFF;
        lock.unlock();
    }

    public void IRQ(){
        if(getFlag(Flag.I) == 1) return;
        programCounter ++; 
        pushAddressToStack(programCounter);
        stackPush((byte)(statusRegister | 0x20));
        updateFlag(Flag.I, true);
        byte low = bus.cpuRead(0xFFFE);
        byte high = bus.cpuRead(0xFFFF);
        programCounter = ((high<<8) | low ) & 0xFFFF;
    }

    private void cycle(byte cycles) throws InterruptedException{
        // 1.79 MHz is roughly  558 nano sec per cycle
        Thread.sleep(0,(int)(cycles *  558 / Settings.GAME_SPEED));
    }


    public void interpret() throws InterruptedException{
        byte inst = (byte)0;
        try{
            while(programCounter <= this.byteCodeLastAddress){
                lock.lock();
                int pc = programCounter;
                inst = bus.cpuRead(programCounter++);
                if(Settings.DISASSEMBLE_ASM) System.out.print("0x" + Integer.toHexString(pc) + "    ");
                byte cycles = isa.getOpcode(inst).execute();
                cycle(cycles);
                lock.unlock();
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(inst);
        }
    }

    public void updateFlag(Flag flag, boolean yes){
        if(yes) {
            this.statusRegister |= flag.index;
        }else if((this.statusRegister & flag.index) > 0){
            this.statusRegister ^= flag.index;
        }
    }

    public byte getFlag(Flag flag){
        return (byte) ((this.statusRegister & flag.index) > 0 ? 1 : 0) ;
    }

    public void stackPush(byte value){
        bus.cpuWrite(0x100 + stackPointer--, value);
    }

    public byte stackPop(){
        return bus.cpuRead(0x100 + stackPointer++);
    }

    public void pushAddressToStack(int address){
        byte high = (byte)((address >> 8 ) & 0xFF);
        stackPush(high);
        byte low = (byte)(address & 0xFF);
        stackPush(low);
    }

    // Addressing modes of 6502
    public byte getZeroPage(){
        return bus.cpuRead(programCounter++);
    }

    public byte getZeroPageX(){
        int address = (bus.cpuRead(programCounter++) + indexX ) & 0xFF; 
        return (byte)address;
    }

    public byte getZeroPageY(){
        int address = (bus.cpuRead(programCounter++) + indexY ) & 0xFF; 
        return (byte)address;
    }

    public int getAbsolute(){
        byte low = bus.cpuRead(programCounter++);
        byte high = bus.cpuRead(programCounter++);
        return  ((high << 8 ) | low) & 0xFFFF;
    }

    public int getAbsoluteX(){
        byte low = bus.cpuRead(programCounter++);
        byte high = bus.cpuRead(programCounter++);
        int address =  (((high << 8 ) | low )+ indexX) & 0xFFFF;
        return address;
    }

    public int getAbsoluteY(){
        byte low = bus.cpuRead(programCounter++);
        byte high = bus.cpuRead(programCounter++);
        int address =  (((high << 8 ) | low ) + indexY) & 0xFFFF;
        return address;
    }

    public int getIndirect(){
       byte low = bus.cpuRead(programCounter++);
       byte high = bus.cpuRead(programCounter++);
       int address =  ((high << 8) | low ) & 0xFFFF;
       low  = bus.cpuRead(address);
       
       //TODO: verify the following line once again
       high = bus.cpuRead((address & 0xFF00) | ((address + 1) & 0xFF));
       address =  ((high << 8) | low ) & 0xFFFF;
       return address;
    }

    public int getIndirectX(){
        int address = getZeroPageX();
        byte low = bus.cpuRead(address);
        byte high = bus.cpuRead(address+1);
        address = ((high << 8 ) | low ) & 0xFFFF;
        return address;
    }

    public int getIndirectY(){
        int address = getZeroPage();
        byte low = bus.cpuRead(address);
        byte high = bus.cpuRead(address+1);
        address = ((high << 8 ) | low  + indexY) & 0xFFFF;
        return address;
    }

}
