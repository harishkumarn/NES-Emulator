package com.nes8.components.helper.controller;

import com.nes8.Settings;

/*
 * This class holds the logic which connects the Controller to the emulator
 */
public class ControllerInterface {

    int controller1 = 0 , controller2 = 0;

    public enum Key{
        
    }

    public enum Type{
        Keyboard,
        Controller
    }

    public ControllerInterface(Type type){
        this.type = type;
        init();
    }

    public void init(){
        
    }

    Type type = Settings.CONTROLLER_TYPE;

    public byte[] getKeyPressData(){
        return new byte[]{0,0};
    }   
}
