package com.nes8;

import com.nes8.components.helper.ControllerInterface;

public class Settings {
    public static final String BASE_PATH = "E:\\Git Repos\\NES-Emulator";
    public static final String ROM_PATH = Settings.BASE_PATH + "\\ROMS\\mario.nes";

    public static final int PT_SCALE = 2;
    public static final int DISPLAY_SCALE = 5; 

    public static ControllerInterface.Type CONTROLLER_TYPE = ControllerInterface.Type.Controller;

    public static boolean RENDER_PATTERN_TABLE = true;

    public static float GAME_SPEED = 1f;
}
