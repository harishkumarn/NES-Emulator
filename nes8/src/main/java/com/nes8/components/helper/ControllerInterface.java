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

    Type type = Settings.CONTROLLER_TYPE;
}
