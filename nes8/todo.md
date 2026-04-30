# NES Emulator — Consolidated Bug Report

**Project:** `com.nes8`
**Files Reviewed:** `CPU.java`, `ISA.java`, `Bus.java`, `PPU.java`, `MMC0.java`

---

## Severity Legend

| Symbol | Meaning |
|--------|---------|
| 🔴 | Critical — causes incorrect execution, crashes, or completely wrong output |
| 🟠 | Medium — produces subtle but consistently wrong behaviour |
| 🟡 | Minor — fragile, dead code, or cosmetic |

---

## CPU.java

### 🔴 Bug 1 — Flag enum is entirely bit-reversed (Lines 28–36)

The real 6502 status register layout is `N V U B D I Z C` from bit 7 to bit 0. The enum assigns them in the exact opposite order.

```
Actual assignment vs correct:
  C = 1<<7 (0x80)   should be 0x01
  Z = 1<<6 (0x40)   should be 0x02
  I = 1<<5 (0x20)   should be 0x04
  D = 1<<4 (0x10)   should be 0x08
  B = 1<<3 (0x08)   should be 0x10
  U = 1<<2 (0x04)   should be 0x20
  V = 1<<1 (0x02)   should be 0x40
  N = 1    (0x01)   should be 0x80
```

Every flag test and update across the entire emulator — carry, zero, negative, overflow — operates on the wrong bit.

**Fix:**
```java
public enum Flag {
    N(1<<7), V(1<<6), U(1<<5), B(1<<4),
    D(1<<3), I(1<<2), Z(1<<1), C(1);
}
```

---

### 🔴 Bug 2 — `stackPop()` reads before incrementing SP (Line 130)

```java
public byte stackPop(){
    return bus.cpuRead(0x100 + stackPointer++);  // reads THEN increments
}
```

After a push, SP points to the next **empty** slot below the top. A pop must pre-increment SP to point to the top item, then read. The current code reads the empty slot, causing every pop to return garbage.

**Fix:**
```java
public byte stackPop(){
    return bus.cpuRead(0x100 + (++stackPointer & 0xFF));
}
```

---

### 🔴 Bug 3 — Stack address uses signed `stackPointer`, writes to zero page (Lines 126 & 130)

```java
bus.cpuWrite(0x100 + stackPointer--, value);
```

`stackPointer` is a `byte`. Java sign-extends it to int, so `SP = (byte)0xFD = -3`, and `0x100 + (-3) = 0x00FD` — **zero page** — instead of `0x01FD`. Every push/pop writes to the wrong memory region, breaking JSR, RTS, and all interrupt handling.

**Fix:** Mask SP with `& 0xFF` in both `stackPush` and `stackPop`:
```java
public void stackPush(byte value){
    bus.cpuWrite(0x100 + (stackPointer-- & 0xFF), value);
}
public byte stackPop(){
    return bus.cpuRead(0x100 + (++stackPointer & 0xFF));
}
```

---

### 🔴 Bug 4 — Low byte sign extension corrupts all 16-bit address construction (Lines 61, 75, 87, 158, 164, 171, 178, 183)

Every address built with `((high << 8) | low)` is wrong when `low >= 0x80`. `low` is a signed byte, so `(byte)0x80` becomes `0xFFFFFF80` before the OR, corrupting the high byte:

```
high = 0x01, low = (byte)0x80
(0x01 << 8)           = 0x00000100
0x00000100 | 0xFFFFFF80 = 0xFFFFFF80
0xFFFFFF80 & 0xFFFF     = 0xFF80   ← WRONG (should be 0x0180)
```

This affects **half of all possible addresses** and hits `reset()`, `NMI()`, `IRQ()`, `getAbsolute()`, `getAbsoluteX()`, `getAbsoluteY()`, `getIndirect()`.

**Fix:** Mask `low` before the OR everywhere:
```java
((high & 0xFF) << 8) | (low & 0xFF)
```

---

### 🟠 Bug 5 — `getZeroPage*` methods return `byte`, sign-extending addresses ≥ 0x80 in callers (Lines 141–153)

```java
public byte getZeroPage() {
    return bus.cpuRead(programCounter++);
}
```

Valid zero page addresses `0x80`–`0xFF` become `-128` to `-1` as signed bytes. Callers that assign the result to `int` (e.g., `getIndirectX()`) get sign-extended to `0xFFFFFF80`, causing reads from completely wrong addresses or array index crashes.

**Fix:** Return `int` and mask:
```java
public int getZeroPage()  { return bus.cpuRead(programCounter++) & 0xFF; }
public int getZeroPageX() { return (bus.cpuRead(programCounter++) + indexX) & 0xFF; }
public int getZeroPageY() { return (bus.cpuRead(programCounter++) + indexY) & 0xFF; }
```

---

### 🟠 Bug 6 — `IRQ()` spuriously increments PC before pushing (Line 81)

```java
programCounter++;           // ← wrong
pushAddressToStack(programCounter);
```

The `interpret()` loop already advances the PC after each instruction. This extra increment pushes PC+1, making RTI return one byte too far into the code.

**Fix:** Remove the `programCounter++` from `IRQ()`.

---

### 🟠 Bug 7 — `interpret()` loop terminates on a static address bound (Line 99)

```java
while(programCounter <= this.byteCodeLastAddress){
```

The PC legitimately jumps into RAM, the stack, PPU-mapped regions, NMI handlers — many of which may be at addresses outside the static bound. This can falsely halt execution mid-run.

**Fix:** Use `while(true)` with a `volatile boolean running` flag for clean shutdown.

---

### 🟡 Bug 8 — `cycle()` accepts a signed `byte` (Line 90)

Any cycle count > 127 would sign-extend to a negative nanosecond value in `Thread.sleep()`, throwing `IllegalArgumentException`. Not triggered by current opcodes, but fragile.

**Fix:** Change parameter to `int`.

---

## ISA.java

### 🔴 Bug 9 — JSR registered with wrong opcode, overwrites AND Immediate (Line 1015)

```java
opcodes.put((byte)0x29, new Opcode((byte)6){ // JSR — WRONG
```

JSR's opcode is `0x20`. `0x29` is AND Immediate (already registered at line 271). The JSR entry silently overwrites AND Immediate. Neither instruction works correctly.

**Fix:** Change `0x29` → `0x20`.

---

### 🔴 Bug 10 — ROR never rotates the carry bit in (Lines 171 & 1603)

```java
int result = ((value >> 1) | (cpu.getFlag(Flag.C) << 8)) & 0xFF;
```

`getFlag(C)` returns 0 or 1. `1 << 8 = 0x100`, which is entirely masked away by `& 0xFF`. The carry is never rotated into bit 7. ROR silently becomes a plain right shift.

**Fix:** Change `<< 8` → `<< 7` in both the memory variant (line 171) and the accumulator variant (line 1603).

---

### 🔴 Bug 11 — BRK pushes wrong bit for the B flag (Line 65)

```java
cpu.stackPush((byte)(cpu.statusRegister | 0x8)); // 0x8 = bit 3 = Decimal flag
```

The B flag is bit 4 (`0x10`). The 6502 convention pushes SR with both the B flag (bit 4) and the unused bit 5 set.

**Fix:** Change `0x8` → `0x30`.

---

### 🔴 Bug 12 — ADC flag calculations broken by signed byte promotion (Lines 38–43)

```java
cpu.updateFlag(Flag.C, (a + o + c) > 255);
```

Java sign-extends `byte` operands to `int` before arithmetic. `(byte)0xFF + (byte)0x01 = -1 + 1 = 0`, never `> 255`. Carry will never be set for high unsigned values.

**Fix:** Mask all operands before arithmetic:
```java
int sum = (a & 0xFF) + (o & 0xFF) + c;
cpu.updateFlag(Flag.C, sum > 255);
cpu.updateFlag(Flag.Z, (sum & 0xFF) == 0);
cpu.updateFlag(Flag.N, (sum & 0x80) != 0);
```

---

### 🟠 Bug 13 — ROR and ROL update flags from accumulator, not the result (Lines 172 & 180)

```java
updateASFlags(cpu.accumulator, carry); // should be (byte)result
```

The memory-variant ROR/ROL compute a `result` but pass `cpu.accumulator` to the flag update, so N and Z flags reflect the accumulator's old state rather than the rotated value.

**Fix:** Change both to `updateASFlags((byte)result, carry)`.

---

### 🟠 Bug 14 — SBC Z flag checks raw signed result instead of masked byte result (Line 76)

```java
cpu.updateFlag(Flag.Z, result == 0);
```

`result` can be negative (e.g., `-1`) even when the byte-masked value wraps to `0x00`. Zero flag must check the masked result.

**Fix:** `cpu.updateFlag(Flag.Z, (result & 0xFF) == 0)`.

---

### 🟠 Bug 15 — PLP corrupts the status register (Lines 1539–1542)

```java
cpu.statusRegister = cpu.stackPop();
cpu.updateFlag(Flag.D, true);        // forces Decimal unconditionally
cpu.updateFlag(Flag.B, false);
updateZNFlags(cpu.statusRegister);   // treats SR as a data value
```

PLP should restore SR as pushed. Forcing `D = true` is wrong. Calling `updateZNFlags` on the SR itself corrupts N and Z.

**Fix:**
```java
byte sr = cpu.stackPop();
cpu.statusRegister = (byte)((sr & ~0x10) | 0x20); // clear B (bit4), set unused (bit5)
```

---

### 🟠 Bug 16 — Zero page address stored in `byte`, sign-extends in bus calls (Lines 779, 873, 925)

```java
byte address = cpu.getZeroPage(); // DEC Z, INC Z, EOR Z
```

Zero page addresses ≥ `0x80` sign-extend to negative ints when passed to `bus.cpuRead()` / `bus.cpuWrite()`, reading/writing from addresses like `0xFFFFFF80` instead of `0x0080`.

**Fix:** Declare all three as `int address`.

---

### 🟠 Bug 17 — ADC reads memory twice, breaks hardware register side effects (Lines 143–144)

```java
int temp = cpu.accumulator + cpu.bus.cpuRead(address) + cpu.getFlag(Flag.C);
updateADCFlags(cpu.accumulator, cpu.bus.cpuRead(address), ...); // second read
```

Double-reading RAM is harmless, but reading a PPU or APU register twice will trigger unintended side effects.

**Fix:** Cache the read: `byte val = cpu.bus.cpuRead(address);` and use `val` in both places.

---

### 🟠 Bug 18 — ROR uses arithmetic right shift on a signed byte (Lines 171 & 1603)

```java
int result = ((value >> 1) | ...) & 0xFF;
```

`value` is a `byte`. Java's `>>` sign-extends it first, so for values ≥ `0x80`, upper bits fill with 1s before the `& 0xFF` mask, which can corrupt the carry-OR result.

**Fix:** Use logical shift: `((value & 0xFF) >> 1)`.

---

### 🟡 Bug 19 — Duplicate INX (0xE8) and INY (0xC8) registrations (Lines 558–583)

`0xE8` and `0xC8` are each registered twice. The second registration silently overwrites the first. Not a functional bug but confusing dead code.

---

### 🟡 Bug 20 — Dead debug code in DEY (Lines 834–836)

```java
if(cpu.indexY == 1){ int x = 2; } // does nothing
```

Leftover breakpoint placeholder. Safe to delete.

---

### 🟡 Bug 21 — SLO (undocumented) missing write-back to memory (Lines 1855–1860)

The ASL result is ORed into the accumulator but never written back to the source memory address. SLO = ASL memory + ORA; the memory write is absent.

---

## Bus.java

### 🔴 Bug 22 — Cartridge address range uses decimal `4020` instead of `0x4020` (Lines 37 & 74)

```java
else if(address >= 4020 && address <= 0xFFFF){ // WRONG: 4020 decimal = 0x0FB4
```

The cartridge ROM range should begin at `0x4020`. Using decimal `4020` (`0x0FB4`) means the condition fires inside the RAM region. Unmapped I/O addresses (`0x4018`–`0x401F`) are also incorrectly routed to the ROM mapper. Appears in both `cpuRead()` and `cpuWrite()`.

**Fix:** Both occurrences: `4020` → `0x4020`.

---

### 🟠 Bug 23 — Palette mirror mask misses transparent color mirrors (Line 51)

```java
return ppu.pallete.readPallete(address & 0x3F1F);
```

The NES hardware mirrors `0x3F10`, `0x3F14`, `0x3F18`, `0x3F1C` back to `0x3F00`, `0x3F04`, `0x3F08`, `0x3F0C`. The `& 0x3F1F` mask does not handle this, causing wrong sprite palette transparent colours.

**Fix:**
```java
int idx = address & 0x1F;
if (idx == 0x10 || idx == 0x14 || idx == 0x18 || idx == 0x1C) idx &= 0x0F;
return ppu.pallete.readPallete(idx);
```

---

## PPU.java

### 🔴 Bug 24 — `getVRAMOffset()` reads PPUSTATUS instead of PPUCTRL (Line 134)

```java
switch((registers[2] & 3)){ // register[2] is PPUSTATUS
```

The nametable select bits (0–1) are in **PPUCTRL (register 0)**, not PPUSTATUS. This selects the wrong nametable on every frame.

**Fix:** `registers[2]` → `registers[0]`.

---

### 🔴 Bug 25 — `tileQuadrantMapping` is 1×4, not 2×2 — crashes on every second tile row (Line 17)

```java
static int[][] tileQuadrantMapping = new int[][]{{0,1,2,3}};
```

Accessed as `tileQuadrantMapping[(i % 4) / 2][(j % 4) / 2]`. Row index can be 0 or 1, but there is only one row — `tileQuadrantMapping[1][...]` throws `ArrayIndexOutOfBoundsException` on every second tile row.

**Fix:**
```java
static int[][] tileQuadrantMapping = new int[][]{{0, 1}, {2, 3}};
```

---

### 🔴 Bug 26 — Palette color written to `c[i]` instead of `c[k]` (Line ~165)

```java
for(int k = 0; k < 4; ++k) c[i] = Pallete.pallete[pallete.backGround[pIndex][k]];
```

`i` is the scanline (0–239). `c` is `Color[4]`. For any `i >= 4` this throws `ArrayIndexOutOfBoundsException`. Even when `i < 4`, only the last iteration's value survives — all four palette entries collapse to one colour.

**Fix:** `c[i]` → `c[k]`.

---

### 🔴 Bug 27 — Attribute table offset uses running cursor and wrong index formula (Lines ~157–159)

```java
int attributeTableOffset = vramOffset + 960;
byte data = bus.ppuRead(attributeTableOffset + i * 8 + j * 8);
```

Two problems:

**A)** `vramOffset` is the running tile cursor (incremented per tile), not the nametable base — the attribute table pointer drifts with each tile read.

**B)** `i * 8 + j * 8` is wrong. The attribute table is 64 bytes (8×8 entries), each covering a 32×32-pixel area. The correct index is `(i / 32) * 8 + (j / 32)`.

**Fix:** Capture the nametable base once:
```java
int ntBase = getVRAMOffset();
int vramOffset = ntBase;
// ...inside tile loop:
int attrIdx = (i / 32) * 8 + (j / 32);
byte data = bus.ppuRead(ntBase + 0x3C0 + attrIdx);
```

---

### 🟠 Bug 28 — PPUDATA write uses raw `register[6]` as an 8-bit address (Line 125)

```java
case 7: nt.write(registers[6], data);
```

The real PPU maintains a 15-bit internal address latch populated by **two sequential writes** to PPUADDR (register 6). Using just `registers[6]` (the last 8-bit write) gives a garbage address for any nametable initialisation.

**Fix:** Implement a proper PPUADDR latch:
```java
private int ppuAddr = 0;
private boolean ppuAddrLatch = false; // false = expecting high byte

// In write(), case 6:
if (!ppuAddrLatch) { ppuAddr = (data & 0x3F) << 8; }
else               { ppuAddr |= (data & 0xFF); }
ppuAddrLatch = !ppuAddrLatch;
break;

// In write(), case 7:
bus.ppuWrite(ppuAddr, data);
ppuAddr += ((registers[0] & 0x04) != 0) ? 32 : 1;
break;
```

---

### 🟠 Bug 29 — Writing to PPUSTATUS fires spurious NMIs (Line 107)

```java
case 2: // PPUSTATUS
    if((data & 0x80) == 0x80) bus.cpu.NMI();
```

PPUSTATUS (`0x2002`) is **read-only** on real hardware. This fires an NMI any time code writes to `0x2002`, which some games do incidentally.

**Fix:** Remove the `case 2` body entirely (leave just `break`).

---

## MMC0.java

### 🟡 Bug 30 — PRG-ROM address not base-subtracted before masking (Line 21)

```java
return rom.pgr_ROM[address & (... ? 0x7FFF : 0x3FFF)];
```

Correct only because `0x8000 & 0x3FFF = 0x0000` and `0x8000 & 0x7FFF = 0x0000` happen to give correct array offsets for NROM. This is coincidental and will break for any other mapper layout.

**Suggested fix (idiomatic):**
```java
return rom.pgr_ROM[(address - 0x8000) & (pgr_ROM.length > 0x4000 ? 0x7FFF : 0x3FFF)];
```

---

## Consolidated Priority Order

### Fix Immediately (will prevent any correct execution)

| # | File | Issue |
|---|------|-------|
| 1 | CPU | Flag enum entirely bit-reversed |
| 3 | CPU | Signed SP sign-extends in stack address — writes to zero page |
| 4 | CPU | Low byte sign extension corrupts all 16-bit address construction |
| 9 | ISA | JSR opcode `0x29` overwrites AND Immediate |
| 22 | Bus | `4020` decimal used instead of `0x4020` |

### Fix Next (incorrect output, wrong rendering)

| # | File | Issue |
|---|------|-------|
| 2 | CPU | `stackPop` reads before pre-increment |
| 5 | CPU | `getZeroPage*` return `byte`, sign-extends in callers |
| 6 | CPU | IRQ spurious `programCounter++` |
| 10 | ISA | ROR carry bit shifted by 8 instead of 7 — carry never rotated in |
| 11 | ISA | BRK pushes `0x8` (D flag) instead of `0x30` (B + unused) |
| 12 | ISA | ADC flags use signed byte arithmetic — carry/overflow broken |
| 24 | PPU | `getVRAMOffset()` reads PPUSTATUS instead of PPUCTRL |
| 25 | PPU | `tileQuadrantMapping` is 1×4 — crashes every second tile row |
| 26 | PPU | Palette written to `c[i]` instead of `c[k]` |
| 27 | PPU | Attribute table offset uses running cursor + wrong formula |

### Fix After (subtle correctness issues)

| # | File | Issue |
|---|------|-------|
| 7 | CPU | `interpret()` loop terminates on static address bound |
| 13 | ISA | ROR/ROL flag update uses accumulator, not result |
| 14 | ISA | SBC Z flag checks raw result not masked byte |
| 15 | ISA | PLP forces D=true and calls `updateZNFlags` on SR |
| 16 | ISA | Zero page address in `byte` sign-extends in bus calls |
| 17 | ISA | ADC double-reads memory — breaks PPU/APU register reads |
| 18 | ISA | ROR uses arithmetic right shift on signed byte |
| 23 | Bus | Palette mirror mask misses transparent color mirrors |
| 28 | PPU | PPUDATA write uses raw 8-bit `register[6]`, needs PPUADDR latch |
| 29 | PPU | Write to PPUSTATUS fires spurious NMIs |

### Clean Up (minor / cosmetic)

| # | File | Issue |
|---|------|-------|
| 8 | CPU | `cycle(byte)` — fragile signed byte parameter |
| 19 | ISA | Duplicate INX / INY registrations |
| 20 | ISA | Dead debug code `int x = 2` in DEY |
| 21 | ISA | SLO undocumented op missing memory write-back |
| 30 | MMC0 | PRG-ROM address works by coincidence, not by design |

---

*Total bugs found: 30 across 5 files — 9 critical, 14 medium, 7 minor.*