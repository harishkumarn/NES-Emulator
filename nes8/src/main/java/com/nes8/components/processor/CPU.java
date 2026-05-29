package com.nes8.components.processor;

import com.nes8.Settings;
import com.nes8.components.software.ISA;
import java.util.concurrent.locks.ReentrantLock;

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
    
    // Used for NMI
    private ReentrantLock lock = new ReentrantLock();
    private int currentCycles = 0;

    public enum Flag{
        C(1<<0), // Carry Flag
        Z(1<<1), // Zero flag
        I(1<<2), // IRQ disable flag
        D(1<<3), // BCD Flag ( Not used in NES)
        B(1<<4), // Break flag
        U(1<<5), // Unused
        V(1<<6), // Unsigned overflow
        N(1<<7); // Negative
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
    ISA isa = new ISA(this);


    public CPU(Bus bus, int pgr_rom_size){
        this.bus = bus;
        bus.setCPU(this);
        reset();
    }

    public void reset(){
        byte low = bus.cpuRead(0xFFFC);
        byte high = bus.cpuRead(0xFFFD);
        this.programCounter = (( ( high & 0xFF ) << 8)  | ( low & 0xFF ) ) & 0xFFFF;
    }

    public void NMI(){
        lock.lock();
        byte low, high;
        high = (byte)( ( programCounter >> 8 ) & 0xFF );
        low = (byte)( programCounter & 0xFF); 
        stackPush(high);
        stackPush(low);
        stackPush((byte)((statusRegister & ~0x10) | 0x20));
        updateFlag(Flag.I, true);
        low = bus.cpuRead(0xFFFA);
        high = bus.cpuRead(0xFFFB);
        programCounter = (((high & 0xFF) <<8) | (low & 0xFF) ) & 0xFFFF;
        lock.unlock();
    }

    public void IRQ(){
        if(getFlag(Flag.I) == 1) return;
        programCounter++; 
        pushAddressToStack(programCounter);
        stackPush((byte)((statusRegister & ~0x10) | 0x20));
        updateFlag(Flag.I, true);
        byte low = bus.cpuRead(0xFFFE);
        byte high = bus.cpuRead(0xFFFF);
        programCounter = (((high & 0xFF) <<8) | (low & 0xFF) ) & 0xFFFF;
    }

    private void cycle(byte cycles) throws InterruptedException{
        // 1.79 MHz is roughly  558 nano sec per cycle
        Thread.sleep(0,(int)(cycles *  558 / Settings.GAME_SPEED));
        // Batch sleep to avoid Thread.sleep granularity issues ( ~ 1 ms minimum)
        currentCycles += cycles;
        if(currentCycles >= 3){
            long nanos = (long)(currentCycles *  558 / Settings.GAME_SPEED);
            Thread.sleep(nanos / 1_000_000, (int) (nanos % 1_000_000));
            currentCycles = 0;
        }
    }


    public void interpret() throws InterruptedException{
        byte inst = (byte)0;
        try{
            while(true){
                try{
                    lock.lock();
                    if(Settings.DISASSEMBLE_ASM) System.out.print("0x" + Integer.toHexString(programCounter) + "    ");
                    inst = bus.cpuRead(programCounter++);
                    byte cycles = isa.getOpcode(inst).execute();
                    lock.unlock();
                    cycle(cycles);
                }catch(Exception e){
                    lock.unlock();
                    throw e;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(Integer.toHexString(inst & 0xFF));
        }
    }

    public void updateFlag(Flag flag, boolean yes){
        if(yes) {
            this.statusRegister |= flag.index;
        }else if((this.statusRegister & flag.index) > 0){
            this.statusRegister &= ~flag.index;
        }
    }

    public byte getFlag(Flag flag){
        return (byte) ((this.statusRegister & flag.index) != 0 ? 1 : 0) ;
    }

    public void stackPush(byte value){
        bus.cpuWrite(0x100 + ( stackPointer & 0xFF), value);
        stackPointer--;
    }

    public byte stackPop(){
        stackPointer++;
        return bus.cpuRead(0x100 + (stackPointer & 0xFF));
    }

    public void pushAddressToStack(int address){
        byte high = (byte)((address >> 8 ) & 0xFF);
        stackPush(high);
        byte low = (byte)(address & 0xFF);
        stackPush(low);
    }

    // Addressing modes of 6502
    public int getZeroPage(){
        return bus.cpuRead(programCounter++) & 0xFF;
    }

    public int getZeroPageX(){
        int address = (bus.cpuRead(programCounter++) + ( indexX  & 0xFF)) & 0xFF; 
        return address;
    }

    public int getZeroPageY(){
        int address = (bus.cpuRead(programCounter++) + ( indexY  & 0xFF)) & 0xFF; 
        return address;
    }

    public int getAbsolute(){
        byte low = bus.cpuRead(programCounter++);
        byte high = bus.cpuRead(programCounter++);
        return  ((high << 8 ) | low) & 0xFFFF;
    }

    public int getAbsoluteX(){
        byte low = bus.cpuRead(programCounter++);
        byte high = bus.cpuRead(programCounter++);
        int address =  (((high << 8 ) | ( low & 0xFF) )+ ( indexX & 0xFF)) & 0xFFFF;
        return address;
    }

    public int getAbsoluteY(){
        byte low = bus.cpuRead(programCounter++);
        byte high = bus.cpuRead(programCounter++);
        int address =  (((high << 8 ) | ( low & 0xFF) ) + ( indexY & 0xFF)) & 0xFFFF;
        return address;
    }

    public int getIndirect(){
       byte low = bus.cpuRead(programCounter++);
       byte high = bus.cpuRead(programCounter++);
       int address =  ((  ( high & 0xFF) << 8) | (low & 0xFF) ) & 0xFFFF;
       low  = bus.cpuRead(address);
       high = bus.cpuRead((address & 0xFF00) | ((address + 1) & 0xFF));
       address =  ((high << 8) | (low & 0xFF) ) & 0xFFFF;
       return address;
    }

    public int getIndirectX(){
        int address = getZeroPageX();
        int low = bus.cpuRead(address) & 0xFF;
        int high = bus.cpuRead((address+1 ) & 0xFF) & 0xFF;
        return ((high << 8 ) | (low & 0xFF) ) & 0xFFFF;
    }

    public int getIndirectY(){
        int address = getZeroPage();
        int low = bus.cpuRead(address) & 0xFF;
        int  high = bus.cpuRead((address+1 ) & 0xFF) & 0xFF;
        address = ((( high << 8 ) | low ) + ( indexY & 0xFF)) & 0xFFFF;
        return address;
    }

}
