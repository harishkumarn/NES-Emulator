package com.nes8.graphics;

import java.awt.Color;


/**
 * Range : 0x3F00 to 0x3FFF 
 * 0x3F00 to 0x3F1F - 32 B
 * -> 0x3000 to 0x3F0F - Background palettes ( 4 sets of 4 colors)
 * -> 0x3F10 to 0x 0x3F1F - Foreground palettes ( 4 sets of 4 colors )
 * Mirrors of 0x3F00 to 0x3F1F : 0x3F20 to 0x3FFF 
 */
public class Pallete {
    public static final Color[] PATTERN_TABLE_COLORS = new Color[]{Color.BLACK, Color.WHITE, Color.BLUE, Color.GRAY};

    private static final Color[] pallete = new Color[]{
        new Color(124, 124, 124), new Color(0, 0, 252),     new Color(0, 0, 188),     new Color(68, 40, 188),
        new Color(148, 0, 132),   new Color(168, 0, 32),    new Color(168, 16, 0),    new Color(136, 20, 0),
        new Color(80, 48, 0),     new Color(0, 120, 0),     new Color(0, 104, 0),     new Color(0, 88, 0),
        new Color(0, 64, 88),     new Color(0, 0, 0),       new Color(0, 0, 0),       new Color(0, 0, 0),

        new Color(188, 188, 188), new Color(0, 120, 248),   new Color(0, 88, 248),    new Color(104, 68, 252),
        new Color(216, 0, 204),   new Color(228, 0, 88),    new Color(248, 56, 0),    new Color(228, 92, 16),
        new Color(172, 124, 0),   new Color(0, 184, 0),     new Color(0, 168, 0),     new Color(0, 168, 68),
        new Color(0, 136, 136),   new Color(0, 0, 0),       new Color(0, 0, 0),       new Color(0, 0, 0),

        new Color(248, 248, 248), new Color(60, 188, 252),  new Color(104, 136, 252), new Color(152, 120, 248),
        new Color(248, 120, 248), new Color(248, 88, 152),  new Color(248, 120, 88),  new Color(252, 160, 68),
        new Color(248, 184, 0),   new Color(184, 248, 24),  new Color(88, 216, 84),   new Color(88, 248, 152),
        new Color(0, 232, 216),   new Color(120, 120, 120), new Color(0, 0, 0),       new Color(0, 0, 0),

        new Color(252, 252, 252), new Color(164, 228, 252), new Color(184, 184, 248), new Color(216, 184, 248),
        new Color(248, 184, 248), new Color(248, 164, 192), new Color(240, 208, 176), new Color(252, 224, 168),
        new Color(248, 216, 120), new Color(216, 248, 120), new Color(184, 248, 184), new Color(184, 248, 216),
        new Color(0, 252, 252),   new Color(248, 216, 248), new Color(0, 0, 0),       new Color(0, 0, 0)
    };

    private byte[][] backGround = new byte[4][4];
    private byte[][] foreGround = new byte[4][4];

    public void updatePalette(int address, byte index){
        // TODO: handle mirrored pallete addresses
        if(address >= 0x3F00 && address <= 0x3F0F){
            int relative = address - 0x3F00;
            backGround[relative / 4][relative % 4 ] = index; 
        }else{
            int relative  = address - 0X3F10;
            foreGround[relative / 4][relative % 4] = index;
        }
    }
}
