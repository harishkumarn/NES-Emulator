package com.nes8.components.software;

import java.util.HashMap;

import com.nes8.components.CPU;
import com.nes8.components.CPU.Flag;

// It can disassemble byte code as well
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

    private void updateANDFlags(int val){
        cpu.updateFlag(Flag.Z, (val & 0xFF) == 0);
        cpu.updateFlag(Flag.N, val < 0 );
    }

    private void updateASLFlags(int val){
        cpu.updateFlag(Flag.C, (val & 0xFF00 )> 0);
        cpu.updateFlag(Flag.Z, (val & 0xFF) == 0);
        cpu.updateFlag(Flag.N, val < 0 );
    }

    private void updateCMPFlags(int val){
        cpu.updateFlag(Flag.C, val >= 0);
        cpu.updateFlag(Flag.Z, (val & 0xFF) == 0);
        cpu.updateFlag(Flag.N, val < 0 );
    }

    private void updateDEC_INC_Flags(byte val){
        cpu.updateFlag(Flag.Z, val == 0);
        cpu.updateFlag(Flag.N, val < 0 );
    }

    private void initOpcodes(){
        // ADC
        opcodes.put(0x69, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator + operand  + cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator, operand , cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC #"+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put(0x65, new Opcode((byte)3){
            // Zero page
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator + cpu.bus.read(operand) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator,cpu.bus.read(operand), cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put(0x75,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator + cpu.bus.read(operand + cpu.indexX) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator, cpu.bus.read(operand + cpu.indexX), cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC "+ Integer.toHexString(operand) + " ,X");
                return (byte)cycle;
            }
        });
        opcodes.put(0x6D,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator + cpu.bus.read( (operand2 << 8 ) + operand1) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator,cpu.bus.read( (operand2 << 8 ) + operand1) , cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC "+ Integer.toHexString((operand2 << 8 ) + operand1) );
                return (byte)cycle;
            }
        });
        opcodes.put(0x7D,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator + cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexX) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator, cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexX), cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC "+ Integer.toHexString((operand1 << 8 ) + operand2) + " ,X" );
                return (byte)cycle;
            }
        });
        opcodes.put(0x79,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator + cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexY) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator,cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexY)  , cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC "+ Integer.toHexString((operand1 << 8 ) + operand2) + " ,Y" );
                return (byte)cycle;
            }
        });
        opcodes.put(0x61,new Opcode((byte)6){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int address = (cpu.bus.read(operand + 1) << 8 )  + cpu.bus.read(operand );
                int temp = cpu.accumulator + cpu.bus.read(address ) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator, cpu.bus.read(address ), cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC ("+ Integer.toHexString(operand) + " ,X)" );
                return (byte)cycle;
            }
        });
        opcodes.put(0x71,new Opcode((byte)5){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int address = (cpu.bus.read(operand + 1) << 8 )  + cpu.bus.read(operand ) +  cpu.indexY;
                int temp = cpu.accumulator + cpu.bus.read(address ) +  cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator,cpu.bus.read(address ) , cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC ("+ Integer.toHexString(operand) + ") ,Y" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //AND

        opcodes.put(0x29, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator & operand ;
                updateANDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND #"+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put(0x25, new Opcode((byte)3){
            // Zero page
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator & cpu.bus.read(operand);
                updateANDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put(0x35,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator & cpu.bus.read(operand + cpu.indexX);
                updateANDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND "+ Integer.toHexString(operand) + " ,X");
                return (byte)cycle;
            }
        });
        opcodes.put(0x2D,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator & cpu.bus.read( (operand1 << 8 ) + operand2) ;
                updateANDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND "+ Integer.toHexString((operand1 << 8 ) + operand2) );
                return (byte)cycle;
            }
        });
        opcodes.put(0x3D,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator & cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexX) ;
                updateANDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND "+ Integer.toHexString((operand1 << 8 ) + operand2) + " ,X" );
                return (byte)cycle;
            }
        });
        opcodes.put(0x39,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator & cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexY) ;
                updateANDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND "+ Integer.toHexString((operand1 << 8 ) + operand2) + " ,Y" );
                return (byte)cycle;
            }
        });
        opcodes.put(0x21,new Opcode((byte)6){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int address = (cpu.bus.read(operand + 1) << 8 )  + cpu.bus.read(operand );
                int temp = cpu.accumulator & cpu.bus.read(address ) ;
                updateANDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND ("+ Integer.toHexString(operand) + " ,X)" );
                return (byte)cycle;
            }
        });
        opcodes.put(0x31,new Opcode((byte)5){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int address = (cpu.bus.read(operand + 1) << 8 )  + cpu.bus.read(operand ) +  cpu.indexY;
                int temp = cpu.accumulator & cpu.bus.read(address ) ;
                updateANDFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND ("+ Integer.toHexString(operand) + ") ,Y" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //ASL

        opcodes.put(0x0A, new Opcode((byte)2){
            @Override
            public byte execute(){
                cpu.accumulator <<= 1;
                int temp = cpu.accumulator << 1;
                updateASLFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ASL A");
                return (byte)cycle;
            }
        });
        opcodes.put(0x06, new Opcode((byte)5){
            // Zero page
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.bus.read(operand) << 1;
                updateASLFlags(temp);
                cpu.bus.write(operand, (byte)(temp & 0xFF));
                printASM("ASL "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put(0x16,new Opcode((byte)6){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.bus.read(operand + cpu.indexX) << 1;
                updateASLFlags(temp);
                cpu.bus.write(operand + cpu.indexX, (byte)(temp & 0xFF));
                printASM("ASL "+ Integer.toHexString(operand) + " ,X");
                return (byte)cycle;
            }
        });
        opcodes.put(0x0E,new Opcode((byte)6){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                int temp =  cpu.bus.read( (operand1 << 8 ) + operand2) << 1;
                updateASLFlags(temp);
                cpu.bus.write((operand1 << 8 ) + operand2, (byte)(temp & 0xFF));
                printASM("ASL "+ Integer.toHexString((operand1 << 8 ) + operand2) );
                return (byte)cycle;
            }
        });
        opcodes.put(0x1E,new Opcode((byte)7){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexX) << 1;
                updateASLFlags(temp);
                cpu.bus.write((operand1 << 8 ) + operand2 + cpu.indexX, (byte)(temp & 0xFF));
                printASM("ASL "+ Integer.toHexString((operand1 << 8 ) + operand2) + " ,X" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BCC

        opcodes.put(0x90,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
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
                byte operand = cpu.bus.read(cpu.programCounter++);
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
                byte operand = cpu.bus.read(cpu.programCounter++);
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
                byte operand = cpu.bus.read(cpu.programCounter++);
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
                byte operand = cpu.bus.read(cpu.programCounter++);
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
                byte operand = cpu.bus.read(cpu.programCounter++);
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
                byte operand = cpu.bus.read(cpu.programCounter++);
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
                byte operand = cpu.bus.read(cpu.programCounter++);
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
                // IMPLEMENT
                // IRQ
                cpu.programCounter ++;
                cpu.stack.push((cpu.programCounter >> 8 ) & 0xFF);
                cpu.stack.push(cpu.programCounter  & 0xFF);
                cpu.updateFlag(Flag.I, true);
                cpu.stack.push(cpu.statusRegister +  0);
                cpu.updateFlag(Flag.I, false);// ?? is this needed

                cpu.programCounter = (short)((cpu.bus.read(0xFFFF) << 8 ) | (cpu.bus.read(0xFFFE)));
                printASM("BRK" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BIT
        opcodes.put(0x24,new Opcode((byte)3){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                byte m = cpu.bus.read(operand);
                cpu.updateFlag(Flag.N, ( m & 0x80) > 0 );
                cpu.updateFlag(Flag.V, ( m & 0x40) > 0);
                cpu.updateFlag(Flag.Z, ( (cpu.accumulator & m ) & 0xFF )  == 0);
                printASM("BIT " + Integer.toHexString(operand) );
                return (byte)cycle;
            }
        });

        opcodes.put(0x2C,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte m = cpu.bus.read((operand2 << 8) + operand1);
                cpu.updateFlag(Flag.N, ( m & 0x80) > 0 );
                cpu.updateFlag(Flag.V, ( m & 0x40) > 0);
                cpu.updateFlag(Flag.Z, ( (cpu.accumulator & m ) & 0xFF )  == 0);
                printASM("BIT " + Integer.toHexString((operand2 << 8) + operand1) );
                return (byte)cycle;
            }
        });

         //-----------------------------------
         //CMP

         opcodes.put(0xC9, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator - operand ;
                updateCMPFlags(temp);
                printASM("CMP #"+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put(0xC5, new Opcode((byte)3){
            // Zero page
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator - cpu.bus.read(operand) ;
                updateCMPFlags(temp);
                printASM("CMP "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put(0xD5,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator - cpu.bus.read(operand + cpu.indexX) ;
                updateCMPFlags(temp);
                printASM("CMP "+ Integer.toHexString(operand) + " ,X");
                return (byte)cycle;
            }
        });
        opcodes.put(0xCD,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator - cpu.bus.read( (operand2 << 8 ) + operand1) ;
                updateCMPFlags(temp);
                printASM("CMP "+ Integer.toHexString((operand2 << 8 ) + operand1) );
                return (byte)cycle;
            }
        });
        opcodes.put(0xDD,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator - cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexX) ;
                updateCMPFlags(temp);
                printASM("CMP "+ Integer.toHexString((operand1 << 8 ) + operand2) + " ,X" );
                return (byte)cycle;
            }
        });
        opcodes.put(0xD9,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.accumulator - cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexY) ;
                updateCMPFlags(temp);
                printASM("CMP "+ Integer.toHexString((operand1 << 8 ) + operand2) + " ,Y" );
                return (byte)cycle;
            }
        });
        opcodes.put(0xC1,new Opcode((byte)6){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int address = (cpu.bus.read(operand + 1) << 8 )  + cpu.bus.read(operand );
                int temp = cpu.accumulator - cpu.bus.read(address );
                updateCMPFlags(temp);
                printASM("CMP ("+ Integer.toHexString(operand) + " ,X)" );
                return (byte)cycle;
            }
        });
        opcodes.put(0xD1,new Opcode((byte)5){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int address = (cpu.bus.read(operand + 1) << 8 )  + cpu.bus.read(operand ) +  cpu.indexY;
                int temp = cpu.accumulator - cpu.bus.read(address );
                updateCMPFlags(temp);
                printASM("CMP ("+ Integer.toHexString(operand) + ") ,Y" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CPX
        opcodes.put(0xE0, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.indexX - operand ;
                updateCMPFlags(temp);
                printASM("CPX #"+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put(0xE4, new Opcode((byte)3){
            // Zero page
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.indexX - cpu.bus.read(operand) ;
                updateCMPFlags(temp);
                printASM("CPX "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put(0xEC,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.indexX - cpu.bus.read( (operand2 << 8 ) + operand1) ;
                updateCMPFlags(temp);
                printASM("CPX "+ Integer.toHexString((operand2 << 8 ) + operand1) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CPY

        opcodes.put(0xC0, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.indexY - operand ;
                updateCMPFlags(temp);
                printASM("CPY #"+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put(0xC4, new Opcode((byte)3){
            // Zero page
            @Override
            public byte execute(){
                byte operand = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.indexY - cpu.bus.read(operand) ;
                updateCMPFlags(temp);
                printASM("CPY "+ Integer.toHexString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put(0xCC,new Opcode((byte)4){
            @Override
            public byte execute(){
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                int temp = cpu.indexY - cpu.bus.read( (operand2 << 8 ) + operand1) ;
                updateCMPFlags(temp);
                printASM("CPY "+ Integer.toHexString((operand2 << 8 ) + operand1) );
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
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte val = (byte) (cpu.bus.read( operand1) - 1);
                updateDEC_INC_Flags( val  );
                cpu.bus.write(operand1, val);
                printASM("DEC " + Integer.toHexString(operand1));
                return (byte)cycle;
            }
        });

        opcodes.put(0xD6,new Opcode((byte)6){
            @Override
            public byte execute(){
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte val = (byte) (cpu.bus.read( operand1 + cpu.indexX) - 1); 
                updateDEC_INC_Flags( val  );
                cpu.bus.write(  operand1 + cpu.indexX, val);
                printASM("DEC " +  Integer.toHexString(operand1) + " ,X");
                return (byte)cycle;
            }
        });

        opcodes.put(0xCE,new Opcode((byte)6){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte val = (byte) (cpu.bus.read( (operand1 << 8 ) + operand2 ) - 1);
                updateDEC_INC_Flags( val  );
                cpu.bus.write( (operand1 << 8 ) + operand2 , val);
                printASM("DEC " +  Integer.toHexString((operand1 << 8 ) + operand2));
                return (byte)cycle;
            }
        });

        opcodes.put(0xDE,new Opcode((byte)7){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte val = (byte) (cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexX) - 1) ;
                updateDEC_INC_Flags( val  );
                cpu.bus.write( (operand1 << 8 ) + operand2 + cpu.indexX, val);
                printASM("DEC " +  Integer.toHexString((operand1 << 8 ) + operand2) + ", X");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //DEX

        opcodes.put(0xCA,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateDEC_INC_Flags( --cpu.indexX  );
                printASM("DEX");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //DEY

        opcodes.put(0x88,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateDEC_INC_Flags( --cpu.indexY  );
                printASM("DEY");
                return (byte)cycle;
            }
        });

         //-----------------------------------
        //INX

        opcodes.put(0xE8,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateDEC_INC_Flags( ++cpu.indexX  );
                printASM("INX");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //INY

        opcodes.put(0xC8,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateDEC_INC_Flags( ++cpu.indexY  );
                printASM("INY");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //INC

        opcodes.put(0xE6,new Opcode((byte)5){
            @Override
            public byte execute(){
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte val = (byte) (cpu.bus.read( operand1) + 1);
                updateDEC_INC_Flags( val  );
                cpu.bus.write(operand1, val);
                printASM("INC " + Integer.toHexString(operand1));
                return (byte)cycle;
            }
        });

        opcodes.put(0xF6,new Opcode((byte)6){
            @Override
            public byte execute(){
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte val = (byte) (cpu.bus.read( operand1 + cpu.indexX) + 1); 
                updateDEC_INC_Flags( val  );
                cpu.bus.write(  operand1 + cpu.indexX, val);
                printASM("INC " +  Integer.toHexString(operand1) + " ,X");
                return (byte)cycle;
            }
        });

        opcodes.put(0xEE,new Opcode((byte)6){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte val = (byte) (cpu.bus.read( (operand1 << 8 ) + operand2 ) + 1);
                updateDEC_INC_Flags( val  );
                cpu.bus.write( (operand1 << 8 ) + operand2 , val);
                printASM("INC " +  Integer.toHexString((operand1 << 8 ) + operand2));
                return (byte)cycle;
            }
        });

        opcodes.put(0xFE,new Opcode((byte)7){
            @Override
            public byte execute(){
                byte operand2 = cpu.bus.read(cpu.programCounter++);
                byte operand1 = cpu.bus.read(cpu.programCounter++);
                byte val = (byte) (cpu.bus.read( (operand1 << 8 ) + operand2 + cpu.indexX) + 1) ;
                updateDEC_INC_Flags( val  );
                cpu.bus.write( (operand1 << 8 ) + operand2 + cpu.indexX, val);
                printASM("INC " +  Integer.toHexString((operand1 << 8 ) + operand2) + ", X");
                return (byte)cycle;
            }
        });

         //------------------------------
        //EOR
        opcodes.put(0x41, new Opcode((byte)2){
            @Override
            public byte execute(){

                return (byte)cycle;
            }
        });

        opcodes.put(0x45, new Opcode((byte)2){
            @Override
            public byte execute(){
                return (byte)cycle;
            }
        });

        opcodes.put(0x4D, new Opcode((byte)3){
            @Override
            public byte execute(){
                return (byte)cycle;
            }
        });

        opcodes.put(0x51, new Opcode((byte)2){
            @Override
            public byte execute(){
                return (byte)cycle;
            }
        });

        opcodes.put(0x55, new Opcode((byte)2){
            @Override
            public byte execute(){
                return (byte)cycle;
            }
        });

        opcodes.put(0x59, new Opcode((byte)3){
            @Override
            public byte execute(){
                return (byte)cycle;
            }
        });

        opcodes.put(0x5D, new Opcode((byte)3){
            @Override
            public byte execute(){
                return (byte)cycle;
            }
        });
    }

    // JMP, JSR, LDA, LDX, LDY, LSR, ORA, PHA, PHP, PLA, PLP, ROL, ROR, RTI, RTS, SBC, SEC, SED, SEI, STA, STX, STY, 
    //TAX, TAY, TSX, TXA, TXS, TYA
}
