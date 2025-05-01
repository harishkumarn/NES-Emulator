package com.nes8.components.software;

import java.util.HashMap;

import com.nes8.components.processor.CPU;
import com.nes8.components.processor.CPU.Flag;

/**
 * It can disassemble byte code as well
 * TODO: 
 * -> IRQ, NMI
 * -> Revisit all Flag register updations
*/
public class ISA {
    HashMap<Integer, Opcode> opcodes  = new HashMap<Integer, Opcode>();

    final CPU cpu ;

    public ISA(CPU cpu){
        this.cpu = cpu;
        initOpcodes();
    }

    public Opcode getOpcode(byte inst){
        return opcodes.get(inst);
    }

    private void updateADCFlags(byte a, byte o, byte c){
        cpu.updateFlag(Flag.C, (a + o + c) > 255);
        cpu.updateFlag(Flag.Z, ((a + o + c) & 0xFF) == 0);
        cpu.updateFlag(Flag.N, ((a + o + c) & 0x80) > 0 );
        cpu.updateFlag(Flag.V, ((~(a ^ o) & (a ^ (a + o + c))) & 0x0080)  > 0 );
    } 

    private void updateAND_OR_LDFlags(int val){
        cpu.updateFlag(Flag.Z, (val & 0xFF) == 0);
        cpu.updateFlag(Flag.N,  (val & 0x80) > 0 );
    }
    private void updateASFlags(int val, boolean carry){

        cpu.updateFlag(Flag.C, carry);
        cpu.updateFlag(Flag.Z, (val & 0xFF) == 0);
        cpu.updateFlag(Flag.N, (val & 0x80) > 0 );
    }

    private void updateCMPFlags(int val){
        cpu.updateFlag(Flag.C, val >= 0);
        cpu.updateFlag(Flag.Z, (val & 0xFF) == 0);
        cpu.updateFlag(Flag.N,  (val & 0x80) > 0 );
    }

    private void updateDEC_INC_EOR_Flags(byte val){
        cpu.updateFlag(Flag.Z, val == 0);
        cpu.updateFlag(Flag.N,  (val & 0x80) > 0 );
    }

    //----------------------
    // ADC
    private void initOpcodes(){
        opcodes.put(0x69, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                int temp = cpu.accumulator + operand  + cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator, operand , cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put(0x65, new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                int temp = cpu.accumulator + cpu.bus.cpuRead(address) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator,cpu.bus.cpuRead(address), cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC Z "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x75,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                int temp = cpu.accumulator + cpu.bus.cpuRead(address ) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator, cpu.bus.cpuRead(address), cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC Zx "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x6D,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                int temp = cpu.accumulator + cpu.bus.cpuRead(address) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator,cpu.bus.cpuRead( address) , cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC A "+ Integer.toHexString(address) );
                return (byte)cycle;
            }
        });
        opcodes.put(0x7D,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                int temp = cpu.accumulator + cpu.bus.cpuRead( address) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator, cpu.bus.cpuRead(address), cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC Ax "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x79,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteY();
                int temp = cpu.accumulator + cpu.bus.cpuRead( address) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator,cpu.bus.cpuRead(address)  , cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC Ay "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x61,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getIndirectX();
                int temp = cpu.accumulator + cpu.bus.cpuRead(address ) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator, cpu.bus.cpuRead(address ), cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC Ix "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x71,new Opcode((byte)5){
            @Override
            public byte execute(){
                int address = cpu.getIndirectY();
                int temp = cpu.accumulator + cpu.bus.cpuRead(address ) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator,cpu.bus.cpuRead(address ) , cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC Iy "+ Integer.toHexString(address) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //AND

        opcodes.put(0x29, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                int temp = cpu.accumulator & operand ;
                updateAND_OR_LDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put(0x25, new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                int temp = cpu.accumulator & cpu.bus.cpuRead(address);
                updateAND_OR_LDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND Z "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x35,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                int temp = cpu.accumulator & cpu.bus.cpuRead(address);
                updateAND_OR_LDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND Zx "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x2D,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                int temp = cpu.accumulator & cpu.bus.cpuRead(address) ;
                updateAND_OR_LDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND A "+ Integer.toHexString(address ));
                return (byte)cycle;
            }
        });
        opcodes.put(0x3D,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                int temp = cpu.accumulator & cpu.bus.cpuRead(address) ;
                updateAND_OR_LDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND Ax "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x39,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteY();
                int temp = cpu.accumulator & cpu.bus.cpuRead(address) ;
                updateAND_OR_LDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND Ay "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x21,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getIndirectX();
                int temp = cpu.accumulator & cpu.bus.cpuRead(address ) ;
                updateAND_OR_LDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND Ix "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x31,new Opcode((byte)5){
            @Override
            public byte execute(){
                int address = cpu.getIndirectY();
                int temp = cpu.accumulator & cpu.bus.cpuRead(address ) ;
                updateAND_OR_LDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND Iy "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //ASL

        opcodes.put(0x0A, new Opcode((byte)2){
            @Override
            public byte execute(){
                boolean carry = (cpu.accumulator & 0x80 ) == 0x80;
                int value = cpu.accumulator << 1;
                cpu.accumulator = (byte)(value & 0xFF);
                updateASFlags(cpu.accumulator, carry);
                printASM("ASL Accumulator");
                return (byte)cycle;
            }
        });
        opcodes.put(0x06, new Opcode((byte)5){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                int value = cpu.bus.cpuRead(address);
                boolean carry =( value & 0x80) == 0x80;
                value <<= 1;
                byte result = (byte)( value & 0xff);
                updateASFlags(result, carry);
                cpu.bus.cpuWrite(address, result);
                printASM("ASL Z "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x16,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                int value = cpu.bus.cpuRead(address);
                boolean carry =( value & 0x80) == 0x80;
                value <<= 1;
                byte result = (byte)( value & 0xff);
                updateASFlags(result, carry);
                cpu.bus.cpuWrite(address, result);
                printASM("ASL Zx "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0x0E,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                int value = cpu.bus.cpuRead(address);
                boolean carry =( value & 0x80) == 0x80;
                value <<= 1;
                byte result = (byte)( value & 0xff);
                updateASFlags(result, carry);
                cpu.bus.cpuWrite(address, result);
                printASM("ASL A "+ Integer.toHexString(address) );
                return (byte)cycle;
            }
        });
        opcodes.put(0x1E,new Opcode((byte)7){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                int value = cpu.bus.cpuRead(address);
                boolean carry =( value & 0x80) == 0x80;
                value <<= 1;
                byte result = (byte)( value & 0xff);
                updateASFlags(result, carry);
                cpu.bus.cpuWrite(address, result);
                printASM("ASL Ax "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BCC

        opcodes.put(0x90,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.C) == 0 ){
                    cpu.programCounter = operand;
                }
                printASM("BCC "+ Integer.toHexString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BCS
        opcodes.put(0xB0,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.C) == 1 ){
                    cpu.programCounter = operand;

                }
                printASM("BCS "+ Integer.toHexString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BCZ
        opcodes.put(0xF0,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.Z) == 1 ){
                    cpu.programCounter = operand;
                }
                printASM("BEQ "+ Integer.toHexString(operand) );
                return (byte)cycle;
            }
        });


        //-----------------------------------
        //BMI
        opcodes.put(0x30,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.N) == 1 ){
                    cpu.programCounter = operand;
                }
                printASM("BMI "+ Integer.toHexString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BNE
        opcodes.put(0xD0,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.Z) == 0 ){
                    cpu.programCounter = operand;
                }
                printASM("BNE "+ Integer.toHexString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BPL
        opcodes.put(0x10,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.N) == 0 ){
                    cpu.programCounter = operand;
                }
                printASM("BPL "+ Integer.toHexString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BVC
        opcodes.put(0x50,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.V) == 0 ){
                    cpu.programCounter = operand;
                }
                printASM("BVC "+ Integer.toHexString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BVS
        opcodes.put(0x70,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.V) == 1 ){
                    cpu.programCounter = operand;
                }
                printASM("BVS "+ Integer.toHexString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CLC
        opcodes.put(0x18,new Opcode((byte)2){
            @Override
            public byte execute(){
                cpu.updateFlag(Flag.C, false);
                printASM("CLC" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CLD
        opcodes.put(0xD8,new Opcode((byte)2){
            @Override
            public byte execute(){
                cpu.updateFlag(Flag.D, false);
                printASM("CLD" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CLI
        opcodes.put(0x58,new Opcode((byte)2){
            @Override
            public byte execute(){
                cpu.updateFlag(Flag.I, false);
                printASM("CLI" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CLV
        opcodes.put(0xB8,new Opcode((byte)2){
            @Override
            public byte execute(){
                cpu.updateFlag(Flag.V, false);
                printASM("CLV" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //INX
        opcodes.put(0xE8,new Opcode((byte)2){
            @Override
            public byte execute(){
                int val = cpu.indexX + 1;
                cpu.updateFlag(Flag.Z, (val & 0xFF) == 0);
                cpu.updateFlag(Flag.N, (val & 0x80) > 0);
                cpu.indexX = (byte)(val & 0xFF);
                printASM("INX" );
                return (byte)cycle;
            }
        });


        //-----------------------------------
        //INY

        opcodes.put(0xC8,new Opcode((byte)2){
            @Override
            public byte execute(){
                int val = cpu.indexY + 1;
                cpu.updateFlag(Flag.Z, (val & 0xFF) == 0);
                cpu.updateFlag(Flag.N, (val & 0x80) > 0);
                cpu.indexY = (byte)(val & 0xFF);
                printASM("INY" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BRK
        opcodes.put(0x00,new Opcode((byte)7){
            @Override
            public byte execute(){
                // TODO: IRQ

                cpu.programCounter ++;
                cpu.pushAddressToStack(cpu.programCounter);
                cpu.updateFlag(Flag.I, true);
                cpu.stack.push(cpu.statusRegister );
                cpu.updateFlag(Flag.I, false);// ?? is this needed

                cpu.programCounter = (short)((cpu.bus.cpuRead(0xFFFF) << 8 ) | (cpu.bus.cpuRead(0xFFFE)));
                printASM("BRK" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BIT
        opcodes.put(0x24,new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                byte m = cpu.bus.cpuRead(address);
                cpu.updateFlag(Flag.N, ( m & 0x80) > 0 );
                cpu.updateFlag(Flag.V, ( m & 0x40) > 0);
                cpu.updateFlag(Flag.Z, ( (cpu.accumulator & m ) & 0xFF )  == 0);
                printASM("BIT Z " + Integer.toHexString(address) );
                return (byte)cycle;
            }
        });

        opcodes.put(0x2C,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                byte m = cpu.bus.cpuRead(address);
                cpu.updateFlag(Flag.N, ( m & 0x80) > 0 );
                cpu.updateFlag(Flag.V, ( m & 0x40) > 0);
                cpu.updateFlag(Flag.Z, ( (cpu.accumulator & m ) & 0xFF )  == 0);
                printASM("BIT A " + Integer.toHexString(address ));
                return (byte)cycle;
            }
        });

         //-----------------------------------
         //CMP

         opcodes.put(0xC9, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                int temp = cpu.accumulator - operand ;
                updateCMPFlags(temp);
                printASM("CMP "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put(0xC5, new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                int temp = cpu.accumulator - cpu.bus.cpuRead(address) ;
                updateCMPFlags(temp);
                printASM("CMP Z "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0xD5,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                int temp = cpu.accumulator - cpu.bus.cpuRead(address) ;
                updateCMPFlags(temp);
                printASM("CMP Zx "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0xCD,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                int temp = cpu.accumulator - cpu.bus.cpuRead(address) ;
                updateCMPFlags(temp);
                printASM("CMP A "+ Integer.toHexString(address ));
                return (byte)cycle;
            }
        });
        opcodes.put(0xDD,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                int temp = cpu.accumulator - cpu.bus.cpuRead( address);
                updateCMPFlags(temp);
                printASM("CMP Ax "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0xD9,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteY();
                int temp = cpu.accumulator - cpu.bus.cpuRead( address) ;
                updateCMPFlags(temp);
                printASM("CMP Ay "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });
        opcodes.put(0xC1,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getIndirectX();
                int temp = cpu.accumulator - cpu.bus.cpuRead(address );
                updateCMPFlags(temp);
                printASM("CMP Ix "+ Integer.toHexString(address) );
                return (byte)cycle;
            }
        });
        opcodes.put(0xD1,new Opcode((byte)5){
            @Override
            public byte execute(){
                int address = cpu.getIndirectY();
                int temp = cpu.accumulator - cpu.bus.cpuRead(address );
                updateCMPFlags(temp);
                printASM("CMP Iy "+ Integer.toHexString(address) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CPX
        opcodes.put(0xE0, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                int temp = cpu.indexX - operand ;
                updateCMPFlags(temp);
                printASM("CPX "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put(0xE4, new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                int temp = cpu.indexX - cpu.bus.cpuRead(address) ;
                updateCMPFlags(temp);
                printASM("CPX Z "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });

        opcodes.put(0xEC,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                int temp = cpu.indexX - cpu.bus.cpuRead( address) ;
                updateCMPFlags(temp);
                printASM("CPX A "+ Integer.toHexString(address ));
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CPY

        opcodes.put(0xC0, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                int temp = cpu.indexY - operand ;
                updateCMPFlags(temp);
                printASM("CPY "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put(0xC4, new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                int temp = cpu.indexY - cpu.bus.cpuRead(address) ;
                updateCMPFlags(temp);
                printASM("CPY Z "+ Integer.toHexString(address));
                return (byte)cycle;
            }
        });

        opcodes.put(0xCC,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                int temp = cpu.indexY - cpu.bus.cpuRead( address) ;
                updateCMPFlags(temp);
                printASM("CPY A "+ Integer.toHexString(address) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //NOP
        opcodes.put(0xEA,new Opcode((byte)2){
            @Override
            public byte execute(){
                printASM("NOP");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //DEC

        opcodes.put(0xC6,new Opcode((byte)5){
            @Override
            public byte execute(){
                byte address = cpu.getZeroPage();
                byte val = (byte) (cpu.bus.cpuRead( address) - 1);
                updateDEC_INC_EOR_Flags( val  );
                cpu.bus.cpuWrite(address, val);
                printASM("DEC Z " + Integer.toHexString(address)); 
                return (byte)cycle;
            }
        });

        opcodes.put(0xD6,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                byte val = (byte) (cpu.bus.cpuRead( address) - 1); 
                updateDEC_INC_EOR_Flags( val  );
                cpu.bus.cpuWrite(  address, val);
                printASM("DEC Zx " +  Integer.toHexString(address) );
                return (byte)cycle;
            }
        });

        opcodes.put(0xCE,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                byte val = (byte) (cpu.bus.cpuRead(address ) - 1);
                updateDEC_INC_EOR_Flags( val  );
                cpu.bus.cpuWrite( address , val);
                printASM("DEC A " +  Integer.toHexString(address));
                return (byte)cycle;
            }
        });

        opcodes.put(0xDE,new Opcode((byte)7){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                byte val = (byte) (cpu.bus.cpuRead(address) - 1) ;
                updateDEC_INC_EOR_Flags( val  );
                cpu.bus.cpuWrite( address, val);
                printASM("DEC Ax " +  Integer.toHexString(address ));
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //DEX

        opcodes.put(0xCA,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateDEC_INC_EOR_Flags( --cpu.indexX  );
                printASM("DEX");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //DEY

        opcodes.put(0x88,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateDEC_INC_EOR_Flags( --cpu.indexY  );
                printASM("DEY");
                return (byte)cycle;
            }
        });

         //-----------------------------------
        //INX

        opcodes.put(0xE8,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateDEC_INC_EOR_Flags( ++cpu.indexX  );
                printASM("INX");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //INY

        opcodes.put(0xC8,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateDEC_INC_EOR_Flags( ++cpu.indexY  );
                printASM("INY");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //INC

        opcodes.put(0xE6,new Opcode((byte)5){
            @Override
            public byte execute(){
                byte address = cpu.getZeroPage();
                byte val = (byte) (cpu.bus.cpuRead( address) + 1);
                updateDEC_INC_EOR_Flags( val  );
                cpu.bus.cpuWrite(address, val);
                printASM("INC Z " + Integer.toHexString(address));
                return (byte)cycle;
            }
        });

        opcodes.put(0xF6,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                byte val = (byte) (cpu.bus.cpuRead(address) + 1); 
                updateDEC_INC_EOR_Flags( val  );
                cpu.bus.cpuWrite( address, val);
                printASM("INC Zx " +  Integer.toHexString(address));
                return (byte)cycle;
            }
        });

        opcodes.put(0xEE,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                byte val = (byte) (cpu.bus.cpuRead(address ) + 1);
                updateDEC_INC_EOR_Flags( val  );
                cpu.bus.cpuWrite( address , val);
                printASM("INC A " +  Integer.toHexString(address));
                return (byte)cycle;
            }
        });

        opcodes.put(0xFE,new Opcode((byte)7){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                byte val = (byte) (cpu.bus.cpuRead( address) + 1) ;
                updateDEC_INC_EOR_Flags( val  );
                cpu.bus.cpuWrite(address, val);
                printASM("INC Ax " +  Integer.toHexString(address));
                return (byte)cycle;
            }
        });

         //------------------------------
        //EOR
        opcodes.put(0x41, new Opcode((byte)6){
            @Override
            public byte execute(){
                int ptr = cpu.getIndirectX();
                cpu.accumulator = (byte) (cpu.accumulator ^ cpu.bus.cpuRead(ptr));
                updateDEC_INC_EOR_Flags(cpu.accumulator);
                printASM("EOR Ix "+ Integer.toHexString(ptr));
                return (byte)cycle;
            }
        });

        opcodes.put(0x45, new Opcode((byte)3){
            @Override
            public byte execute(){
                byte operand = cpu.getZeroPage();
                cpu.accumulator = (byte) (cpu.bus.cpuRead(operand) ^ cpu.accumulator);
                updateDEC_INC_EOR_Flags(cpu.accumulator);
                printASM("EOR Z " + Integer.toHexString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put(0x49,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                cpu.accumulator = (byte) (operand ^ cpu.accumulator);
                updateDEC_INC_EOR_Flags(cpu.accumulator);
                printASM("EOR " + Integer.toHexString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put(0x4D, new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                cpu.accumulator = (byte) (cpu.bus.cpuRead( address ) ^ cpu.accumulator) ;
                updateDEC_INC_EOR_Flags(cpu.accumulator);
                printASM("EOR A " + Integer.toHexString(address));
                return (byte)cycle;
            }
        });

        opcodes.put(0x51, new Opcode((byte)5){
            @Override
            public byte execute(){
                int ptr = cpu.getIndirectY();

                cpu.accumulator = (byte) (cpu.accumulator ^ cpu.bus.cpuRead(ptr));
                updateDEC_INC_EOR_Flags(cpu.accumulator);
                printASM("EOR Iy "+ Integer.toHexString(ptr));
                return (byte)cycle;
            }
        });

        opcodes.put(0x55, new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                cpu.accumulator = (byte) (cpu.bus.cpuRead(  address ) ^ cpu.accumulator) ;
                updateDEC_INC_EOR_Flags(cpu.accumulator);
                printASM("EOR Zx " + Integer.toHexString(address ));
                return (byte)cycle;
            }
        });

        opcodes.put(0x59, new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteY();
                cpu.accumulator = (byte) (cpu.bus.cpuRead(address) ^ cpu.accumulator) ;
                updateDEC_INC_EOR_Flags( cpu.accumulator  );
                printASM("EOR Ay " +  Integer.toHexString(address));
                return (byte)cycle;
            }
        });

        opcodes.put(0x5D, new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                cpu.accumulator = (byte) (cpu.bus.cpuRead(address) ^ cpu.accumulator) ;
                updateDEC_INC_EOR_Flags( cpu.accumulator  );
                printASM("EOR Ax " +  Integer.toHexString(address));
                return (byte)cycle;
            }
        });
    
    //------------------------
    //JMP
        opcodes.put(0x4C, new Opcode((byte)3){
            @Override
            public byte execute(){
                cpu.programCounter = cpu.getAbsolute();
                printASM("JMP A " + Integer.toHexString(cpu.programCounter));
                return (byte)cycle;
            }
        });

        opcodes.put(0x6C, new Opcode((byte)5){
            @Override
            public byte execute(){
                cpu.programCounter = cpu.getIndirect();
                printASM("JMP I " + Integer.toHexString(cpu.programCounter));
                return (byte)cycle;
            }
        });

    //-----------------------------
    //JSR
    opcodes.put(0x29, new Opcode((byte)6){
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.pushAddressToStack(cpu.programCounter - 1);
            cpu.programCounter = address;
            printASM("JSR A " + Integer.toHexString(cpu.programCounter));
            return (byte)cycle;
        }   
    });
    //-----------------------------------
    //RTS
    opcodes.put(0x60, new Opcode((byte)6){
        @Override
        public byte execute(){
            byte low = cpu.stack.pop();
            byte high = cpu.stack.pop();
            cpu.programCounter = ((high << 8 ) + low) + 1;
            printASM("RTS");
            return (byte)cycle;
        }
    });

    //-----------------------------------
    //RTI
    opcodes.put(0x40, new Opcode((byte)6){
        @Override
        public byte execute(){
            cpu.statusRegister = cpu.stack.pop();
            byte low = cpu.stack.pop();
            byte high = cpu.stack.pop();
            cpu.programCounter = (high << 8) + low;
            printASM("RTI");
            return (byte)cycle;
        }
    });

    //-----------------------
    //STA
    opcodes.put(0x85,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Z "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0x95,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Zx "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0x8D,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA A "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0x9D,new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Ax "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0x99,new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteY();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Ay "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0x81,new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectX();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Ix "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0x91,new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectY();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Iy "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    //-----------------------
    //STX
    opcodes.put(0x86,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            cpu.bus.cpuWrite(address,cpu.indexX);
            printASM("STX Z "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });    

    opcodes.put(0x96,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageY();
            cpu.bus.cpuWrite(address,cpu.indexX);
            printASM("STX Zy "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0x8E,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.bus.cpuWrite(address,cpu.indexX);
            printASM("STX A "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    //-----------------------
    //STY
    opcodes.put(0x84,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            cpu.bus.cpuWrite(address,cpu.indexY);
            printASM("STY Z "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });    

    opcodes.put(0x94,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            cpu.bus.cpuWrite(address,cpu.indexY);
            printASM("STY Zx "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0x8C,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.bus.cpuWrite(address,cpu.indexY);
            printASM("STY A "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    //-----------------------
    //LDA
    opcodes.put(0XA9,new Opcode((byte)2) {
        @Override
        public byte execute(){
            byte value = cpu.bus.cpuRead(cpu.programCounter++);
            cpu.accumulator = value;
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("LDA "+Integer.toHexString(value));
            return (byte)cycle;
        }
    });

    opcodes.put(0xA5,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            cpu.accumulator = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("LDA Z "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xB5,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            cpu.accumulator = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("LDA Zx "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xAD,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.accumulator = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("LDA A "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xBD,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            cpu.accumulator = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("LDA Ax "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xB9,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteY();
            cpu.accumulator = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("LDA Ay "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xA1,new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectX();
            cpu.accumulator = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("LDA Ix "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xB1,new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectY();
            cpu.accumulator = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("LDA Iy "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    //-----------------------
    //LDX
    opcodes.put(0xA2,new Opcode((byte)2) {
        @Override
        public byte execute(){
            byte value = cpu.bus.cpuRead(cpu.programCounter++);
            cpu.indexX = value;
            updateAND_OR_LDFlags(cpu.indexX);
            printASM("LDX "+Integer.toHexString(value));
            return (byte)cycle;
        }
    });

    opcodes.put(0xA6,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            cpu.indexX = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.indexX);
            printASM("LDX Z "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xB6,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageY();
            cpu.indexX = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.indexX);
            printASM("LDX Zy "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xAE,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.indexX = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.indexX);
            printASM("LDX A "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xBE,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteY();
            cpu.indexX = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.indexX);
            printASM("LDX Ay "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    //-----------------------
    //LDY
    opcodes.put(0xA0,new Opcode((byte)2) {
        @Override
        public byte execute(){
            byte value =  cpu.bus.cpuRead(cpu.programCounter++);
            cpu.indexY = value;
            updateAND_OR_LDFlags(cpu.indexY);
            printASM("LDY "+Integer.toHexString(value));
            return (byte)cycle;
        }
    });

    opcodes.put(0xA4,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            cpu.indexY = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.indexY);
            printASM("LDY Z "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xB4,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            cpu.indexY = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.indexY);
            printASM("LDY Zx "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xAC,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.indexY = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.indexY);
            printASM("LDY A "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    opcodes.put(0xBC,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            cpu.indexY = cpu.bus.cpuRead(address);
            updateAND_OR_LDFlags(cpu.indexY);
            printASM("LDY Ax "+Integer.toHexString(address));
            return (byte)cycle;
        }
    });

    //-------------------------
    //LSR
    opcodes.put(0x4A, new Opcode((byte)2) {
        @Override
        public byte execute(){
            int value = cpu.accumulator;
            boolean carry =( value & 1) == 1;
            value >>= 1;
            byte result = (byte)( value & 0xff);
            updateASFlags(result, carry);
            cpu.accumulator = result;
            printASM("LSR Accumulator");
            return (byte)cycle;
        }
    });

    opcodes.put(0x46, new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            int value = cpu.bus.cpuRead(address);
            boolean carry =( value & 1) == 1;
            value >>= 1;
            byte result = (byte)( value & 0xff);
            updateASFlags(result, carry);
            cpu.bus.cpuWrite(address, result);
            printASM("LSR Z " +  Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    opcodes.put(0x56, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            int value = cpu.bus.cpuRead(address);
            boolean carry =( value & 1) == 1;
            value >>= 1;
            byte result = (byte)( value & 0xff);
            updateASFlags(result, carry);
            cpu.bus.cpuWrite(address, result);
            printASM("LSR Zx" + Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    opcodes.put(0x4E, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            int value = cpu.bus.cpuRead(address);
            boolean carry =( value & 1) == 1;
            value >>= 1;
            byte result = (byte)( value & 0xff);
            updateASFlags(result, carry);
            cpu.bus.cpuWrite(address, result);
            printASM("LSR A" + Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    opcodes.put(0x5E, new Opcode((byte)7) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            int value = cpu.bus.cpuRead(address);
            boolean carry =( value & 1) == 1;
            value >>= 1;
            byte result = (byte)( value & 0xff);
            updateASFlags(result, carry);
            cpu.bus.cpuWrite(address, result);
            printASM("LSR Ax" + Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    //--------------------
    //ORA
    opcodes.put(0x09, new Opcode((byte)2) {
        @Override
        public byte execute(){
            byte value = cpu.bus.cpuRead(cpu.programCounter++);
            cpu.accumulator |= value;
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("ORA " + Integer.toHexString(value));
            return (byte)cycle;
        }
    });
    opcodes.put(0x05, new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            byte value = cpu.bus.cpuRead(address);
            cpu.accumulator |= value;
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("ORA Z " + Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    opcodes.put(0x15, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            byte value = cpu.bus.cpuRead(address);
            cpu.accumulator |= value;
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("ORA Zx " + Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    opcodes.put(0x0D, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            byte value = cpu.bus.cpuRead(address);
            cpu.accumulator |= value;
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("ORA A " + Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    opcodes.put(0x1D, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            byte value = cpu.bus.cpuRead(address);
            cpu.accumulator |= value;
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("ORA Ax " + Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    opcodes.put(0x19, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteY();
            byte value = cpu.bus.cpuRead(address);
            cpu.accumulator |= value;
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("ORA Ay " + Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    opcodes.put(0x01, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectX();
            byte value = cpu.bus.cpuRead(address);
            cpu.accumulator |= value;
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("ORA Ix " + Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    opcodes.put(0x11, new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectY();
            byte value = cpu.bus.cpuRead(address);
            cpu.accumulator |= value;
            updateAND_OR_LDFlags(cpu.accumulator);
            printASM("ORA Iy " + Integer.toHexString(address));
            return (byte)cycle;
        }
    });
    
    //PHA, PHP, PLA, PLP, ROL, ROR, SBC, SEC, SED, SEI, 
    //TAX, TAY, TSX, TXA, TXS, TYA
    }
}
