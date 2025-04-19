package com.nes8.components;

public class Controller {
    Bus bus;

    private Controller(Bus bus){
        this.bus = bus;
    }

    public static void init(Bus bus){
        new Controller(bus);
    }
}
