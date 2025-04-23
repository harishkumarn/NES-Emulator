package com.nes8.components;

import com.nes8.components.bus.Bus;

public class Controller {
    Bus bus;

    private Controller(Bus bus){
        this.bus = bus;
    }

    public static void init(Bus bus){
        new Controller(bus);
    }
}
