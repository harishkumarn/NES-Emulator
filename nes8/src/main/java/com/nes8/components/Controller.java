package com.nes8.components;

import com.nes8.components.bus.Bus;
import com.nes8.components.helper.ControllerInterface;

public class Controller {
    Bus bus;
    public byte shiftregister1 = 0 , shiftregister2 = 0 ;
    byte shiftIndex1 = 0 , shiftIndex2 = 0 ;
    ControllerInterface controllers = new ControllerInterface();

    private Controller(Bus bus){
        this.bus = bus;
    }

    public static void init(Bus bus){
        new Controller(bus);
    }

    public void loadShiftRegisters(){
        this.shiftIndex1 = 0;
        this.shiftIndex2 = 0;
        byte[] arr = controllers.getKeyPressData();
        shiftregister1 = arr[0];
        shiftregister2 = arr[1];
    }

    public byte rightShiftRegister(int address){
        if(address == 0x4016){
            return (byte)((shiftIndex1 & ( 1 << shiftIndex1++)) > 0 ? 1 : 0);
        }else{// 0x4017
            return (byte)((shiftIndex2 & ( 1 << shiftIndex2++)) > 0 ? 1 : 0);
        }
    }
}
