public class Itype {
    // Singleton instance: ensures that only one Itype object exists.
    private static Itype instance;

    private Itype() {
    }

    /**
     * Provides access to the single Itype instance.
     * Creates the instance only once if it doesn’t exist yet.
     *
     * @return The shared Itype instance.
     */
    public static Itype getInstance() {
        if (instance == null) {
            instance = new Itype();
        }
        return instance;
    }

    /**
     * Sign-extends a 16-bit number into a 32-bit integer.
     * If the 16th bit (bit 15) is 1, it means the number is negative.
     * Therefore, extend the sign bit by subtracting 2^16.
     *
     * Example:
     * 0000 0000 0000 1010 → +10 (no change)
     * 1111 1111 1111 1010 → -6 (after sign extension)
     *
     * @param num The original 16-bit value.
     * @return The sign-extended 32-bit value.
     */
    private int signExtend(int num) {
        if ((num & (1 << 15)) != 0) {
            num -= (1 << 16);
        }
        return num;
    }

    /**
     * Execute I-type instruction (LW, SW, BEQ).
     * 
     * @param machine CPU state
     * @param opcode  Operation code (2=LW, 3=SW, 4=BEQ)
     * @param rs      Source register A
     * @param rt      Target register B
     * @param offset  16-bit offset field
     */
    public void execute(Machine machine, int opcode, int rs, int rt, int offset) {
        // Get references to the CPU's registers and memory
        int[] reg = machine.getRegisters();
        int[] mem = machine.getMemory();

        // Convert 16-bit offset to 32-bit and calculate memory address
        int offsetField = signExtend(offset);
        int memAddress = offsetField + reg[rs];

        switch (opcode) {
            case 2: // LW: Load from memory into register rt
                if (rt != 0) { // register 0 must remain 0
                    // Check for valid memory address range
                    if (memAddress < 0 || memAddress >= mem.length) {
                        System.err.println("Invalid memory access at " + memAddress);
                        machine.halt();
                        return;
                    }
                    // Perform load: rt = memory[rs + offset]
                    reg[rt] = mem[memAddress];
                }
                break;

            case 3: // SW: Store from register rt into memory
                // Check for valid memory address range
                if (memAddress < 0 || memAddress >= mem.length) {
                    System.err.println("Invalid memory access at " + memAddress);
                    machine.halt();
                    return;
                }
                // Perform store: memory[rs + offset] = rt
                mem[memAddress] = reg[rt];
                break;

            case 4: // BEQ: Branch if reg[rs] == reg[rt]
                if (reg[rs] == reg[rt]) {
                    // New PC = current PC + 1 + offsetField
                    int newPc = machine.getPc() + 1 + offsetField;
                    machine.setNextPc(newPc);
                }
                break;

            default:
                // Invalid or unknown opcode — print an error and halt the CPU
                System.err.println("Unknown I-type opcode: " + opcode);
                machine.halt();
                break;
        }
    }
}
