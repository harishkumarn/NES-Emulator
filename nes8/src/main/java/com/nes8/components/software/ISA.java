package com.nes8.components.software;

import java.util.Map;
import java.util.HashMap;

import com.nes8.components.processor.CPU;
import com.nes8.components.processor.CPU.Flag;

/**
 * It can disassemble byte code as well
 * TODO: 
 * -> IRQ, NMI
 * -> Revisit all Flag register updations
 * -> +1 cycle for indirect x,y, absolute x,y address modes i.e., page boundary penalty
*/
public class ISA {
    public Map<Byte, Opcode> opcodes  = new HashMap<Byte, Opcode>();

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

    private void updateZNFlags(int val){
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

    private void SBC(byte value){
        int result = cpu.accumulator - value - 1 + cpu.getFlag(Flag.C);
        int unsigned = result & 0xFF;

        cpu.updateFlag(Flag.Z, result == 0);
        cpu.updateFlag(Flag.C, result >= 0 );

        //TODO : Cross check the following 2 flags
        cpu.updateFlag(Flag.V,((cpu.accumulator ^ value) & 0x80) != 0 && ((cpu.accumulator ^ unsigned) & 0x80) != 0);
        cpu.updateFlag(Flag.N, (unsigned & 0x80) > 0 );
        cpu.accumulator = (byte)(unsigned & 0xFF);
    }

    private void LSR(int address){
        int value = cpu.bus.cpuRead(address);
        boolean carry =( value & 1) == 1;
        value >>= 1;
        byte result = (byte)( value & 0xff);
        updateASFlags(result, carry);
        cpu.bus.cpuWrite(address, result);
    }

    private void LDY(int address){
        cpu.indexY = cpu.bus.cpuRead(address);
        updateZNFlags(cpu.indexY);
    }

    private void LDX(int address){
        cpu.indexX = cpu.bus.cpuRead(address);
        updateZNFlags(cpu.indexX);
    }

    private void LDA(int address){
        cpu.accumulator = cpu.bus.cpuRead(address);
        updateZNFlags(cpu.accumulator);
    }

    private void INC(int address){
        byte val = (byte) (cpu.bus.cpuRead( address) + 1) ;
        updateZNFlags( val  );
        cpu.bus.cpuWrite(address, val);
    }

    private void CMP(int address){
        int temp = cpu.accumulator - cpu.bus.cpuRead(address );
        updateCMPFlags(temp);
    }

    private void CPY(int address){
        int temp = cpu.indexY - cpu.bus.cpuRead( address) ;
        updateCMPFlags(temp);
    }

    private void DEC(int address){
        byte val = (byte) (cpu.bus.cpuRead(address) - 1) ;
        updateZNFlags( val  );
        cpu.bus.cpuWrite( address, val);
    }

    private void EOR(int address){
        cpu.accumulator = (byte) (cpu.bus.cpuRead(address) ^ cpu.accumulator) ;
        updateZNFlags( cpu.accumulator  );
    }

    private void AND(int address){
        int temp = cpu.accumulator & cpu.bus.cpuRead(address ) ;
        updateZNFlags(temp);
        cpu.accumulator = (byte)(temp & 0xFF);
    }

    private void ADC(int address){
        int temp = cpu.accumulator + cpu.bus.cpuRead(address ) +  cpu.getFlag(Flag.C);
        updateADCFlags(cpu.accumulator,cpu.bus.cpuRead(address ) , cpu.getFlag(Flag.C));
        cpu.accumulator = (byte)(temp & 0xFF);
    }

    private void ASL(int address){
        int value = cpu.bus.cpuRead(address);
        boolean carry =( value & 0x80) == 0x80;
        value <<= 1;
        byte result = (byte)( value & 0xff);
        updateASFlags(result, carry);
        cpu.bus.cpuWrite(address, result);
    }

    private void BIT(int address){
        byte m = cpu.bus.cpuRead(address);
        cpu.updateFlag(Flag.N, ( m & 0x80) > 0 );
        cpu.updateFlag(Flag.V, ( m & 0x40) > 0);
        cpu.updateFlag(Flag.Z, ( (cpu.accumulator & m ) & 0xFF )  == 0);
    }

    private void CPX(int address){
        int temp = cpu.indexX - cpu.bus.cpuRead( address) ;
        updateCMPFlags(temp);
    }
    private void ROR(int address){
        byte value = cpu.bus.cpuRead(address);
        boolean carry = ( value & 1 ) == 1;
        int result = ((value >> 1) | (cpu.getFlag(Flag.C) << 8) ) & 0xFF;
        updateASFlags(cpu.accumulator, carry);
        cpu.bus.cpuWrite(address, (byte)result);
    }

    private void ROL(int address){
        byte value = cpu.bus.cpuRead(address);
        boolean carry = ( value& 0x80 ) == 0x80;
        int result = ((value << 1) | cpu.getFlag(Flag.C) ) & 0xFF;
        updateASFlags(cpu.accumulator, carry);
        cpu.bus.cpuWrite(address, (byte)result);
    }

    private void ORA(int address){
        byte value = cpu.bus.cpuRead(address);
        cpu.accumulator |= value;
        updateZNFlags(cpu.accumulator);
    }

    //----------------------
    // ADC
    private void initOpcodes(){
        opcodes.put((byte)(byte)0x69, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                int temp = cpu.accumulator + operand  + cpu.getFlag(Flag.C);
                updateADCFlags(cpu.accumulator, operand , cpu.getFlag(Flag.C));
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("ADC "+ Integer.toString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x65, new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                ADC(address);
                printASM("ADC Z "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x75,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                ADC(address);
                printASM("ADC Zx "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x6D,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                ADC(address);
                printASM("ADC A "+ Integer.toString(address) );
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x7D,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                ADC(address);
                printASM("ADC Ax "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x79,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteY();
                ADC(address);
                printASM("ADC Ay "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x61,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getIndirectX();
                ADC(address);
                printASM("ADC Ix "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x71,new Opcode((byte)5){
            @Override
            public byte execute(){
                int address = cpu.getIndirectY();
                ADC(address);
                printASM("ADC Iy "+ Integer.toString(address) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //AND

        opcodes.put((byte)0x29, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                int temp = cpu.accumulator & operand ;
                updateZNFlags(temp);
                cpu.accumulator = (byte)(temp & 0xFF);
                printASM("AND "+ Integer.toString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x25, new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                AND(address);
                printASM("AND Z "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x35,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                AND(address);
                printASM("AND Zx "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x2D,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                AND(address);
                printASM("AND A "+ Integer.toString(address ));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x3D,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                AND(address);
                printASM("AND Ax "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x39,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteY();
                AND(address);
                printASM("AND Ay "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x21,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getIndirectX();
                AND(address);
                printASM("AND Ix "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x31,new Opcode((byte)5){
            @Override
            public byte execute(){
                int address = cpu.getIndirectY();
                AND(address);
                printASM("AND Iy "+ Integer.toString(address));
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //ASL

        opcodes.put((byte)0x0A, new Opcode((byte)2){
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
        opcodes.put((byte)0x06, new Opcode((byte)5){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                ASL(address);
                printASM("ASL Z "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x16,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                ASL(address);
                printASM("ASL Zx "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x0E,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                ASL(address);
                printASM("ASL A "+ Integer.toString(address) );
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0x1E,new Opcode((byte)7){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                ASL(address);
                printASM("ASL Ax "+ Integer.toString(address));
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BCC

        opcodes.put((byte)0x90,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.C) == 0 ){
                    cpu.programCounter += operand;
                }
                printASM("BCC "+ Integer.toString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BCS
        opcodes.put((byte)0xB0,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.C) == 1 ){
                    cpu.programCounter += operand;

                }
                printASM("BCS "+ Integer.toString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BCZ
        opcodes.put((byte)0xF0,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.Z) == 1 ){
                    cpu.programCounter += operand;
                }
                printASM("BEQ "+ Integer.toString(operand) );
                return (byte)cycle;
            }
        });


        //-----------------------------------
        //BMI
        opcodes.put((byte)0x30,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.N) == 1 ){
                    cpu.programCounter += operand;
                }
                printASM("BMI "+ Integer.toString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BNE
        opcodes.put((byte)0xD0,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.Z) == 0 ){
                    cpu.programCounter += operand;
                }
                printASM("BNE "+ Integer.toString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BPL
        opcodes.put((byte)0x10,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++) ;
                if(cpu.getFlag(Flag.N) == 0 ){
                    cpu.programCounter += operand;
                }
                printASM("BPL "+ Integer.toString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BVC
        opcodes.put((byte)0x50,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.V) == 0 ){
                    cpu.programCounter += operand;
                }
                printASM("BVC "+ Integer.toString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BVS
        opcodes.put((byte)0x70,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                if(cpu.getFlag(Flag.V) == 1 ){
                    cpu.programCounter += operand;
                }
                printASM("BVS "+ Integer.toString(operand) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CLC
        opcodes.put((byte)0x18,new Opcode((byte)2){
            @Override
            public byte execute(){
                cpu.updateFlag(Flag.C, false);
                printASM("CLC" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CLD
        opcodes.put((byte)0xD8,new Opcode((byte)2){
            @Override
            public byte execute(){
                cpu.updateFlag(Flag.D, false);
                printASM("CLD" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CLI
        opcodes.put((byte)0x58,new Opcode((byte)2){
            @Override
            public byte execute(){
                cpu.updateFlag(Flag.I, false);
                printASM("CLI" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CLV
        opcodes.put((byte)0xB8,new Opcode((byte)2){
            @Override
            public byte execute(){
                cpu.updateFlag(Flag.V, false);
                printASM("CLV" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //INX
        opcodes.put((byte)0xE8,new Opcode((byte)2){
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

        opcodes.put((byte)0xC8,new Opcode((byte)2){
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
        opcodes.put((byte)0x00,new Opcode((byte)7){
            @Override
            public byte execute(){
                cpu.IRQ();
                printASM("BRK" );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //BIT
        opcodes.put((byte)(byte)0x24,new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                BIT(address);
                printASM("BIT Z " + Integer.toString(address) );
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0x2C,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                BIT(address);
                printASM("BIT A " + Integer.toString(address ));
                return (byte)cycle;
            }
        });

         //-----------------------------------
         //CMP

         opcodes.put((byte)0xC9, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                int temp = cpu.accumulator - operand ;
                updateCMPFlags(temp);
                printASM("CMP "+ Integer.toString(operand));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0xC5, new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                CMP(address);
                printASM("CMP Z "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0xD5,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                CMP(address);
                printASM("CMP Zx "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0xCD,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                CMP(address);
                printASM("CMP A "+ Integer.toString(address ));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0xDD,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                CMP(address);
                printASM("CMP Ax "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0xD9,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteY();
                CMP(address);
                printASM("CMP Ay "+ Integer.toString(address));
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0xC1,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getIndirectX();
                CMP(address);
                printASM("CMP Ix "+ Integer.toString(address) );
                return (byte)cycle;
            }
        });
        opcodes.put((byte)0xD1,new Opcode((byte)5){
            @Override
            public byte execute(){
                int address = cpu.getIndirectY();
                CMP(address);
                printASM("CMP Iy "+ Integer.toString(address) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CPX
        opcodes.put((byte)0xE0, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                int temp = cpu.indexX - operand ;
                updateCMPFlags(temp);
                printASM("CPX "+ Integer.toString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0xE4, new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                CPX(address);
                printASM("CPX Z "+ Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0xEC,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                CPX(address);
                printASM("CPX A "+ Integer.toString(address ));
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //CPY

        opcodes.put((byte)0xC0, new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                int temp = cpu.indexY - operand ;
                updateCMPFlags(temp);
                printASM("CPY "+ Integer.toString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0xC4, new Opcode((byte)3){
            @Override
            public byte execute(){
                int address = cpu.getZeroPage();
                CPY(address);
                printASM("CPY Z "+ Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0xCC,new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                CPY(address);
                printASM("CPY A "+ Integer.toString(address) );
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //NOP
        opcodes.put((byte)0xEA,new Opcode((byte)2){
            @Override
            public byte execute(){
                printASM("NOP");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //DEC

        opcodes.put((byte)0xC6,new Opcode((byte)5){
            @Override
            public byte execute(){
                byte address = cpu.getZeroPage();
                DEC(address);
                printASM("DEC Z " + Integer.toString(address)); 
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0xD6,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                DEC(address);
                printASM("DEC Zx " +  Integer.toString(address) );
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0xCE,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                DEC(address);
                printASM("DEC A " +  Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0xDE,new Opcode((byte)7){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                DEC(address);
                printASM("DEC Ax " +  Integer.toString(address ));
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //DEX

        opcodes.put((byte)0xCA,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateZNFlags( --cpu.indexX  );
                printASM("DEX");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //DEY

        opcodes.put((byte)0x88,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateZNFlags( --cpu.indexY  );
                printASM("DEY");
                return (byte)cycle;
            }
        });

         //-----------------------------------
        //INX

        opcodes.put((byte)0xE8,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateZNFlags( ++cpu.indexX  );
                printASM("INX");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //INY

        opcodes.put((byte)0xC8,new Opcode((byte)2){
            @Override
            public byte execute(){
                updateZNFlags( ++cpu.indexY  );
                printASM("INY");
                return (byte)cycle;
            }
        });

        //-----------------------------------
        //INC

        opcodes.put((byte)0xE6,new Opcode((byte)5){
            @Override
            public byte execute(){
                byte address = cpu.getZeroPage();
                INC(address);
                printASM("INC Z " + Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0xF6,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                INC(address);
                printASM("INC Zx " +  Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0xEE,new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                INC(address);
                printASM("INC A " +  Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0xFE,new Opcode((byte)7){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                INC(address);
                printASM("INC Ax " +  Integer.toString(address));
                return (byte)cycle;
            }
        });

         //------------------------------
        //EOR
        opcodes.put((byte)0x41, new Opcode((byte)6){
            @Override
            public byte execute(){
                int address = cpu.getIndirectX();
                EOR(address);
                printASM("EOR Ix "+ Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0x45, new Opcode((byte)3){
            @Override
            public byte execute(){
                byte address = cpu.getZeroPage();
                EOR(address);
                printASM("EOR Z " + Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0x49,new Opcode((byte)2){
            @Override
            public byte execute(){
                byte operand = cpu.bus.cpuRead(cpu.programCounter++);
                cpu.accumulator = (byte) (operand ^ cpu.accumulator);
                updateZNFlags(cpu.accumulator);
                printASM("EOR " + Integer.toString(operand));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0x4D, new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsolute();
                EOR(address);
                printASM("EOR A " + Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0x51, new Opcode((byte)5){
            @Override
            public byte execute(){
                int address = cpu.getIndirectY();
                EOR(address);
                printASM("EOR Iy "+ Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0x55, new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getZeroPageX();
                EOR(address);
                printASM("EOR Zx " + Integer.toString(address ));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0x59, new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteY();
                EOR(address);
                printASM("EOR Ay " +  Integer.toString(address));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0x5D, new Opcode((byte)4){
            @Override
            public byte execute(){
                int address = cpu.getAbsoluteX();
                EOR(address);
                printASM("EOR Ax " +  Integer.toString(address));
                return (byte)cycle;
            }
        });
    
    //------------------------
    //JMP
        opcodes.put((byte)0x4C, new Opcode((byte)3){
            @Override
            public byte execute(){
                cpu.programCounter = cpu.getAbsolute();
                printASM("JMP A " + Integer.toString(cpu.programCounter));
                return (byte)cycle;
            }
        });

        opcodes.put((byte)0x6C, new Opcode((byte)5){
            @Override
            public byte execute(){
                cpu.programCounter = cpu.getIndirect();
                printASM("JMP I " + Integer.toString(cpu.programCounter));
                return (byte)cycle;
            }
        });

    //-----------------------------
    //JSR
    opcodes.put((byte)0x29, new Opcode((byte)6){
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.pushAddressToStack(cpu.programCounter - 1);
            cpu.programCounter = address;
            printASM("JSR A " + Integer.toString(cpu.programCounter));
            return (byte)cycle;
        }   
    });
    //-----------------------------------
    //RTS
    opcodes.put((byte)0x60, new Opcode((byte)6){
        @Override
        public byte execute(){
            byte low = cpu.stackPop();
            byte high = cpu.stackPop();
            cpu.programCounter = ((high << 8 ) + low) + 1;
            printASM("RTS");
            return (byte)cycle;
        }
    });

    //-----------------------------------
    //RTI
    opcodes.put((byte)0x40, new Opcode((byte)6){
        @Override
        public byte execute(){
            cpu.statusRegister = cpu.stackPop();
            byte low = cpu.stackPop();
            byte high = cpu.stackPop();
            cpu.programCounter = (high << 8) + low;
            printASM("RTI");
            return (byte)cycle;
        }
    });

    //-----------------------
    //STA
    opcodes.put((byte)0x85,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Z "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0x95,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Zx "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0x8D,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA A "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0x9D,new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Ax "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0x99,new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteY();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Ay "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0x81,new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectX();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Ix "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0x91,new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectY();
            cpu.bus.cpuWrite(address,cpu.accumulator);
            printASM("STA Iy "+Integer.toString(address));
            return (byte)cycle;
        }
    });
    //-----------------------
    //STX
    opcodes.put((byte)0x86,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            cpu.bus.cpuWrite(address,cpu.indexX);
            printASM("STX Z "+Integer.toString(address));
            return (byte)cycle;
        }
    });    

    opcodes.put((byte)0x96,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageY();
            cpu.bus.cpuWrite(address,cpu.indexX);
            printASM("STX Zy "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0x8E,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.bus.cpuWrite(address,cpu.indexX);
            printASM("STX A "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    //-----------------------
    //STY
    opcodes.put((byte)0x84,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            cpu.bus.cpuWrite(address,cpu.indexY);
            printASM("STY Z "+Integer.toString(address));
            return (byte)cycle;
        }
    });    

    opcodes.put((byte)0x94,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            cpu.bus.cpuWrite(address,cpu.indexY);
            printASM("STY Zx "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0x8C,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            cpu.bus.cpuWrite(address,cpu.indexY);
            printASM("STY A "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    //-----------------------
    //LDA
    opcodes.put((byte)0XA9,new Opcode((byte)2) {
        @Override
        public byte execute(){
            byte value = cpu.bus.cpuRead(cpu.programCounter++);
            cpu.accumulator = value;
            updateZNFlags(cpu.accumulator);
            printASM("LDA "+Integer.toString(value));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xA5,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            LDA(address);
            printASM("LDA Z "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xB5,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            LDA(address);
            printASM("LDA Zx "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xAD,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            LDA(address);
            printASM("LDA A "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xBD,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            LDA(address);
            printASM("LDA Ax "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xB9,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteY();
            LDA(address);
            printASM("LDA Ay "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xA1,new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectX();
            LDA(address);
            printASM("LDA Ix "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xB1,new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectY();
            LDA(address);
            printASM("LDA Iy "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    //-----------------------
    //LDX
    opcodes.put((byte)0xA2,new Opcode((byte)2) {
        @Override
        public byte execute(){
            byte value = cpu.bus.cpuRead(cpu.programCounter++);
            cpu.indexX = value;
            updateZNFlags(cpu.indexX);
            printASM("LDX "+Integer.toString(value));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xA6,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            LDX(address);
            printASM("LDX Z "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xB6,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageY();
            LDX(address);
            printASM("LDX Zy "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xAE,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            LDX(address);
            printASM("LDX A "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xBE,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteY();
            LDX(address);
            printASM("LDX Ay "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    //-----------------------
    //LDY
    opcodes.put((byte)0xA0,new Opcode((byte)2) {
        @Override
        public byte execute(){
            byte value =  cpu.bus.cpuRead(cpu.programCounter++);
            cpu.indexY = value;
            updateZNFlags(cpu.indexY);
            printASM("LDY "+Integer.toString(value));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xA4,new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            LDY(address);
            printASM("LDY Z "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xB4,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            LDY(address);
            printASM("LDY Zx "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xAC,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            LDY(address);
            printASM("LDY A "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    opcodes.put((byte)0xBC,new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            LDY(address);
            printASM("LDY Ax "+Integer.toString(address));
            return (byte)cycle;
        }
    });

    //-------------------------
    //LSR
    opcodes.put((byte)0x4A, new Opcode((byte)2) {
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

    opcodes.put((byte)0x46, new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            LSR(address);
            printASM("LSR Z " +  Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x56, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            LSR(address);
            printASM("LSR Zx" + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x4E, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            LSR(address);
            printASM("LSR A" + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x5E, new Opcode((byte)7) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            LSR(address);
            printASM("LSR Ax" + Integer.toString(address));
            return (byte)cycle;
        }
    });
    //--------------------
    //ORA
    opcodes.put((byte)0x09, new Opcode((byte)2) {
        @Override
        public byte execute(){
            byte value = cpu.bus.cpuRead(cpu.programCounter++);
            cpu.accumulator |= value;
            updateZNFlags(cpu.accumulator);
            printASM("ORA " + Integer.toString(value));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x05, new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            ORA(address);
            printASM("ORA Z " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x15, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            ORA(address);
            printASM("ORA Zx " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x0D, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            ORA(address);
            printASM("ORA A " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x1D, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            ORA(address);
            printASM("ORA Ax " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x19, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteY();
            ORA(address);
            printASM("ORA Ay " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x01, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectX();
            ORA(address);
            printASM("ORA Ix " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x11, new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectY();
            ORA(address);
            printASM("ORA Iy " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    //------------------
    //PHA
    opcodes.put((byte)0x48, new Opcode((byte)3) {
        @Override
        public byte execute(){
            cpu.stackPush(cpu.accumulator);
            printASM("PHA");
            return (byte)cycle;
        }
    });
    //-------------------
    //PHP
    opcodes.put((byte)0x08, new Opcode((byte)3) {
        @Override
        public byte execute(){
            cpu.stackPush((byte)(cpu.statusRegister | 0x30));
            printASM("PHP");
            return (byte)cycle;
        }
    });
    //------------------
    //PLA
    opcodes.put((byte)0x68, new Opcode((byte)4) {
        @Override
        public byte execute(){
            cpu.accumulator = cpu.stackPop(); 
            updateZNFlags(cpu.accumulator);
            printASM("PLA");
            return (byte)cycle;
        }
    });
    //-------------------
    //PLP
    opcodes.put((byte)0x28, new Opcode((byte)4) {
        @Override
        public byte execute(){
            cpu.statusRegister = cpu.stackPop(); 
            cpu.updateFlag(Flag.D, true);
            cpu.updateFlag(Flag.B, false);
            updateZNFlags(cpu.statusRegister);
            printASM("PLP");
            return (byte)cycle;
        }
    });
    //-----------------
    //ROL
    opcodes.put((byte)0x2A, new Opcode((byte)2) {
        @Override
        public byte execute(){
            boolean carry = (cpu.accumulator & 0x80 ) == 0x80;
            int value = ((cpu.accumulator << 1) | cpu.getFlag(Flag.C) ) & 0xFF;
            cpu.accumulator = (byte)value;
            updateASFlags(cpu.accumulator, carry);
            printASM("ROL Accumulator");
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x26, new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            ROL(address);
            printASM("ROL Z " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x36, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            ROL(address);
            printASM("ROL Zx " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x2E, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            ROL(address);
            printASM("ROL A " + Integer.toString(address));
            return (byte)cycle;
        }

    });
    opcodes.put((byte)0x3E, new Opcode((byte)7) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            ROL(address);
            printASM("ROL Ax " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    //----------------------
    //ROR
    opcodes.put((byte)0x6A, new Opcode((byte)2) {
        @Override
        public byte execute(){
            boolean carry = (cpu.accumulator & 1 ) == 1;
            int value = ((cpu.accumulator >> 1) | (cpu.getFlag(Flag.C) << 8 ) ) & 0xFF;
            cpu.accumulator = (byte)value;
            updateASFlags(cpu.accumulator, carry);
            printASM("ROR Accumulator");
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x66, new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            ROR(address);
            printASM("ROR Z " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x76, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            ROR(address);
            printASM("ROR Zx " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x6E, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            ROR(address);
            printASM("ROR A " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0x7E, new Opcode((byte)7) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            ROR(address);
            printASM("ROR Ax " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    //-----------------
    //SBC
    opcodes.put((byte)0xE9, new Opcode((byte)2) {
        @Override
        public byte execute(){
            byte value = cpu.bus.cpuRead(cpu.programCounter++);
            SBC(value);
            printASM("SBC " + Integer.toString(value));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0xE5, new Opcode((byte)3) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPage();
            SBC(cpu.bus.cpuRead(address));
            printASM("SBC Z " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0xF5, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getZeroPageX();
            SBC(cpu.bus.cpuRead(address));
            printASM("SBC Zx " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0xED, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsolute();
            SBC(cpu.bus.cpuRead(address));
            printASM("SBC A " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0xFD, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteX();
            SBC(cpu.bus.cpuRead(address));
            printASM("SBC Ax " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0xF9, new Opcode((byte)4) {
        @Override
        public byte execute(){
            int address = cpu.getAbsoluteY();
            SBC(cpu.bus.cpuRead(address));
            printASM("SBC Ay " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0xE1, new Opcode((byte)6) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectX();
            SBC(cpu.bus.cpuRead(address));
            printASM("SBC Ix " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    opcodes.put((byte)0xF1, new Opcode((byte)5) {
        @Override
        public byte execute(){
            int address = cpu.getIndirectY();
            SBC(cpu.bus.cpuRead(address));
            printASM("SBC Iy " + Integer.toString(address));
            return (byte)cycle;
        }
    });
    //---------------------
    //SEC
    opcodes.put((byte)0x38, new Opcode((byte)2) {
        @Override
        public byte execute(){
            cpu.updateFlag(Flag.C, true);
            printASM("SEC");
            return (byte)cycle;
        }
    });
    //---------------------
    //SED
    opcodes.put((byte)0xF8, new Opcode((byte)2) {
        @Override
        public byte execute(){
            cpu.updateFlag(Flag.D, true);
            printASM("SED");
            return (byte)cycle;
        }
    });
    //---------------------
    //SEI
    opcodes.put((byte)0x78, new Opcode((byte)2) {
        @Override
        public byte execute(){
            cpu.updateFlag(Flag.I, true);
            printASM("SEI");
            return (byte)cycle;
        }
    }); 
    //--------------------
    //TAX
    opcodes.put((byte)0xAA, new Opcode((byte)2) {
        @Override
        public byte execute(){
            cpu.indexX = cpu.accumulator;
            updateZNFlags(cpu.indexX);
            printASM("TAX");
            return (byte)cycle;
        }
    });     
    //-------------------
    //TAY
    opcodes.put((byte)0xA8, new Opcode((byte)2) {
        @Override
        public byte execute(){
            cpu.indexY = cpu.accumulator;
            updateZNFlags(cpu.indexY);
            printASM("TAY");
            return (byte)cycle;
        }
    }); 
    //-------------------
    //TSX
    opcodes.put((byte)0xBA, new Opcode((byte)2) {
        @Override
        public byte execute(){
            cpu.indexX = cpu.stackPointer;
            updateZNFlags(cpu.indexX);
            printASM("TSX");
            return (byte)cycle;
        }
    }); 
    //-------------------
    //TXA
    opcodes.put((byte)0x8A, new Opcode((byte)2) {
        @Override
        public byte execute(){
            cpu.accumulator = cpu.indexX;
            updateZNFlags(cpu.accumulator);
            printASM("TXA");
            return (byte)cycle;
        }
    }); 

    //-------------------
    //TXS
    opcodes.put((byte)0x9A, new Opcode((byte)2) {
        @Override
        public byte execute(){
            cpu.stackPointer = cpu.indexX;
            printASM("TXS");
            return (byte)cycle;
        }
    }); 

    //-------------------
    //TYA
    opcodes.put((byte)0x98, new Opcode((byte)2) {
        @Override
        public byte execute(){
            cpu.accumulator = cpu.indexY;
            updateZNFlags(cpu.accumulator);
            printASM("TYA");
            return (byte)cycle;
        }
    }); 
    }
}
