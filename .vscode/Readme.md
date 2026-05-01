# NES-8 — NES Emulator in Java

A work-in-progress Nintendo Entertainment System (NES) emulator written in Java, targeting cycle-accurate emulation of the **MOS 6502 CPU**, **PPU (Picture Processing Unit)**, **APU (Audio Processing Unit)**, and the **MMC0 (NROM) memory mapper**.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                        BUS                          │
│   (Central communication layer — CPU/PPU/APU/ROM)   │
└────┬──────────┬──────────┬──────────┬───────────────┘
     │          │          │          │
   CPU        PPU        APU      Controller
  (6502)   (2C02)                    
     │          │
    RAM       NameTable
              Palette
              OAM
     │
    ROM
     │
   MMC0
  (NROM Mapper)
```

---

## Components

### CPU — MOS 6502
- Full 56-instruction ISA implementation
- All official addressing modes: Immediate, Zero Page, Zero Page X/Y, Absolute, Absolute X/Y, Indirect, Indirect X (Indexed Indirect), Indirect Y (Indirect Indexed)
- Interrupt handling: **NMI**, **IRQ**, **BRK**, **RESET**
- Undocumented opcodes: SLO, and others partially implemented
- Cycle-accurate timing using `Thread.sleep`

### PPU — Picture Processing Unit
- 8 memory-mapped registers (`0x2000`–`0x2007`)
- Pattern Table rendering (background + sprite)
- Nametable and Attribute Table reads for background tile palette selection
- Palette memory with hardware-correct mirroring
- OAM (Object Attribute Memory) for sprite metadata
- V-Blank and NMI signalling
- Output via `OutputBuffer` (Java AWT)

### APU — Audio Processing Unit
- Status register mapped at `0x4015`
- Write handler at `0x4000`–`0x4017`
- *(Full channel emulation not yet implemented)*

### Bus
- CPU memory map:

| Address Range | Device |
|---|---|
| `0x0000`–`0x1FFF` | RAM (2KB, mirrored every 2KB) |
| `0x2000`–`0x3FFF` | PPU Registers (mirrored every 8 bytes) |
| `0x4000`–`0x4013`, `0x4015`, `0x4017` | APU |
| `0x4014` | OAM DMA |
| `0x4016`–`0x4017` | Controllers |
| `0x4020`–`0xFFFF` | Cartridge (mapper) |

- PPU memory map:

| Address Range | Device |
|---|---|
| `0x0000`–`0x1FFF` | Pattern Tables (CHR ROM via mapper) |
| `0x2000`–`0x3EFF` | Nametables (2KB, mirrored) |
| `0x3F00`–`0x3FFF` | Palette RAM (32 bytes, mirrored) |

### MMC0 — NROM Mapper (iNES Mapper 0)
- Supports 16KB (NROM-128) and 32KB (NROM-256) PRG-ROM
- CHR-RAM write support (`0x0000`–`0x1FFF`)
- SRAM at `0x6000`–`0x7FFF` with dirty-flag tracking
- PRG-ROM mirroring for 16KB ROMs at `0xC000`–`0xFFFF`

---

## Project Structure

```
com.nes8/
├── components/
│   ├── bus/
│   │   └── Bus.java              # Central bus — CPU/PPU/APU memory routing
│   ├── processor/
│   │   ├── CPU.java              # MOS 6502 CPU core
│   │   ├── ISA.java              # Instruction set + opcode dispatch table
│   │   ├── PPU.java              # Picture Processing Unit
│   │   └── APU.java              # Audio Processing Unit
│   ├── mappers/
│   │   ├── MMC0.java             # NROM mapper (Mapper 0)
│   │   └── MemoryMappingController.java
│   ├── helper/
│   │   └── RenderingUtils.java   # Tile rendering helpers
│   ├── Controller.java           # NES controller (shift register)
│   └── DMA.java                  # OAM DMA transfer
├── memory/
│   ├── RAM.java                  # 2KB CPU RAM
│   └── ROM.java                  # iNES ROM loader (PRG + CHR data)
├── graphics/
│   ├── PatternTable.java         # CHR tile data
│   ├── NameTable.java            # Background tile layout
│   ├── Pallete.java              # NES system palette + RAM
│   ├── ObjectAttributeMemory.java# Sprite attribute table
│   └── OutputBuffer.java         # AWT display output
├── Settings.java                 # Game speed, debug flags
└── Constants.java                # ONE_KB and other shared constants
```

---

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven (or any Java build tool)

### Build

```bash
mvn clean compile
```

### Run

```bash
mvn exec:java -Dexec.mainClass="com.nes8.Main"
```

Or run the compiled JAR:

```bash
java -jar nes8.jar path/to/rom.nes
```

### Supported ROM Format

- **iNES** (`.nes`) format
- **Mapper 0 (NROM)** only — covers games like *Donkey Kong*, *Super Mario Bros.*, *Balloon Fight*, *Ice Climber*

---

## Settings

Configurable via `Settings.java`:

| Field | Description |
|---|---|
| `GAME_SPEED` | Multiplier for CPU/PPU cycle timing (1.0 = real speed) |
| `DISASSEMBLE_ASM` | Print disassembly trace to stdout during execution |

---

## Status

| Component | Status |
|---|---|
| CPU (6502) core | ✅ Implemented — bugs being fixed |
| ISA (all official opcodes) | ✅ Implemented — several flag/opcode bugs known |
| PPU background rendering | 🚧 In progress |
| PPU sprite rendering | ❌ Not yet implemented |
| APU | 🚧 Stub only |
| Controller input | 🚧 Partial |
| MMC0 mapper | ✅ Implemented |
| Save RAM (SRAM) | ✅ Implemented |

---

## Known Issues

See [`NES_Emulator_Bug_Report.md`](./NES_Emulator_Bug_Report.md) for the full list of 30 identified bugs across all components. The highest priority items are:

1. **Flag enum bit-reversed** — every flag operation in the CPU uses the wrong bit
2. **Stack pointer sign-extension** — push/pop write to zero page instead of `0x01xx`
3. **16-bit address low-byte sign-extension** — corrupts half of all address reads
4. **JSR mapped to wrong opcode** (`0x29` instead of `0x20`) — overwrites AND Immediate
5. **`tileQuadrantMapping` is 1×4 instead of 2×2** — crashes on every second tile row during rendering

---

## References

- [NESdev Wiki](https://www.nesdev.org/wiki/NES_reference_guide) — comprehensive NES hardware reference
- [6502 Instruction Reference](https://www.nesdev.org/obelisk-6502-guide/reference.html) — opcode tables, addressing modes, flag behaviour
- [NES PPU internals](https://www.nesdev.org/wiki/PPU_rendering) — rendering pipeline documentation
- [iNES file format](https://www.nesdev.org/wiki/INES) — ROM header specification

---

## License

MIT License — see `LICENSE` for details.