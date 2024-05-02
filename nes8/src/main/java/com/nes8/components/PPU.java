package com.nes8.components;

import com.nes8.components.Bus;

public class PPU {
    Bus bus;

    public PPU(Bus bus){
        this.bus = bus;
    }

    public void start(){
        
    }

    private void cycle(byte cycles) throws InterruptedException{
        // 5.32 MHz is roughly  188 nano sec per cycle
        Thread.sleep(0,cycles *  188);
    }
}
