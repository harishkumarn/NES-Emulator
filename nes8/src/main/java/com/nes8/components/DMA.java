package com.nes8.components;

import com.nes8.memory.RAM;
import com.nes8.components.processor.PPU;

/* When CPU writes to 0x4014, it triggers 256 byte DMA transfer from 
 * the address written to 0x4014 to OAM
 */
public class DMA {
    /*TODO: How long is DMA transfer supposed to take?
    * Takes 513 - 514 cycles
    * CPU is possibly forzen during this time
    */
    public static void startDMATransfer(int address, RAM ram, PPU ppu){
        System.out.println("DMA Transfer");
        for(int i = 0; i < 256; ++i){
            ppu.oam.write(i , ram.read(address + i ));
        }
        // TODO : Add sleep
    }
}
