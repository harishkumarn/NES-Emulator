package com.nes8.memory;

import com.nes8.Constants;

public class RAM {
    byte[] memory = new byte[2 * Constants.ONE_KB];// 2 KB

    public byte read(int i){
        return memory[i];
    }

    public void write(int i, byte val){
        memory[i] = val;
    }
}
