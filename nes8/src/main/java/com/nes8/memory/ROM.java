package com.nes8.memory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.nes8.components.mappers.MMC0;
import com.nes8.components.mappers.MMC1;
import com.nes8.components.mappers.MemoryMappingController;

public class ROM {


    public enum NameTableArrangeMent{
        HORIZONTAL, // 32x60 -> Games with vertical scroll
        VERTICAL // 64x30 -> Games with horizontal scroll
    }

    public byte[] pgr_ROM , chr_ROM ;
    public MemoryMappingController mmc ;
    NameTableArrangeMent nTableArrangeMent;
    BufferedInputStream br = null;
    public int pgr_rom_size = 0;
    public int chr_rom_size =0;
    public ROM(String romPath)  throws IOException{
        initStream(romPath);
    }

    private void initStream(String file) throws IOException{
        br = new BufferedInputStream(new FileInputStream(new File(file)));
    } 

    public boolean initROM() throws IOException{
        System.out.println("\n");
        boolean status = true;
        try{
            byte[] header = new byte[16];
            br.read(header);
            int fileType = (header[7] & 12) == 8 ? 2 : 1;
            nTableArrangeMent = (header[6] & 1 ) == 0  ? NameTableArrangeMent.VERTICAL : NameTableArrangeMent.HORIZONTAL;
            boolean alternativeNameTables = (header[6] & 8 ) == 1;
            if((header[6] & 4) >  0 ){
                br.skip(256l);
            }
            int mapper = 0, hintScreenData = 0 ;
            if(fileType == 1){
                pgr_rom_size = header[4] & 0xFF;
                chr_rom_size = header[5]  & 0xFF;
                mapper = (header[7] & 0xF0 ) | ((header[6] & 0xF0) >> 4 ) ;
                hintScreenData = (header[7] & 2) == 2 ? 8192 : 0 ;
            }else{// bytes 8-15 in NES 2 format
                pgr_rom_size = ((header[9] & 0xF) << 8) |  (header[4] & 0xFF);
                chr_rom_size = ((header[9] & 0xF0) << 4) |  (header[5]  & 0xFF);
                mapper = ((header[6] & 0xF) << 8 ) | (header[7] & 0xF0 ) | ((header[6] & 0xF0) >> 4 ) ;
            }
            System.out.println("iNES File Format : " + fileType + "\n\n");
            pgr_rom_size *= 16384;
            chr_rom_size = chr_rom_size * 8192 + hintScreenData;
            System.out.println("Pgr ROM size  : " +  pgr_rom_size + " Bytes");
            System.out.println("Chr size  : " + chr_rom_size + " Bytes\n\n");
            pgr_ROM = new byte[pgr_rom_size ];
            chr_ROM = new byte[chr_rom_size ];
            System.out.println("Pgr ROM bytes read : " +  br.read(pgr_ROM));
            System.out.println("Chr ROM bytes read : " +  br.read(chr_ROM));
            status = setMapper(mapper);
        }catch(Exception e){
            System.err.println(e);
        }finally{
            br.close();
        }
        System.out.println("\n");
        return status;
    }

    boolean setMapper(int mapper){
        System.out.println("\n\nMapper id " +  mapper);
        switch(mapper){
            case 0:
            this.mmc = new MMC0();
            break;
            case 1:
            this.mmc = new MMC1();
            break;
            case 2:
            break;
            case 3:
            break;
            case 4:
            break;
            case 5:
            break;
            case 6:
            break;
            case 9:
            break;
            case 10:
            break;
            case 37:
            break;
            case 47:
            break;
            case 66:
            break;
            case 99:
            break;
            case 105:
            break;
            case 118:
            break;
            case 155:
            break;
            case 185:
            break;
            default:
                System.out.println("Unknown mapper!!");
                return false;
        }
        return true;
    }

}
