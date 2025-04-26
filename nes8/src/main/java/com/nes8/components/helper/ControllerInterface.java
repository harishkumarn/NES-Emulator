package com.nes8.components.helper;

import com.nes8.Settings;

/*
 * This class holds the logic which connects the Controller to the emulator
 */
public class ControllerInterface {
    public enum Type{
        Keyboard,
        Controller
    }

    public ControllerInterface(){
        init();
    }

    public void init(){
        
    }

    Type type = Settings.CONTROLLER_TYPE;

    public byte[] getKeyPressData(){
        return new byte[]{0,0};
    }   
}
