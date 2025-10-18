public class Itype {
    // Singleton instance
    private static Itype instance;

    private Itype() {}

    public static Itype getInstance() {
        if (instance == null) {
            instance = new Itype();
        }
        return instance;
    }

    /** Sign-extend a 16-bit number to 32-bit */
    private int signExtend(int num) {
        if ((num & (1 << 15)) != 0) {
            num -= (1 << 16);
        }
        return num;
    }

    /**
     * Execute I-type instruction (LW, SW, BEQ).
     * @param machine CPU state
     * @param opcode  Operation code (2=LW, 3=SW, 4=BEQ)
     * @param rs      Source register A
     * @param rt      Target register B
     * @param offset  16-bit offset field
     */
    public void execute(Machine machine, int opcode, int rs, int rt, int offset) {
        int[] reg = machine.getRegisters();
        int[] mem = machine.getMemory();

        // Convert 16-bit offset to 32-bit and calculate memory address
        int offsetField = signExtend(offset);
        int memAddress = offsetField + reg[rs];

        switch (opcode) {
            case 2: // LW: Load from memory into register rt
                if (rt != 0) { // register 0 must remain 0
                    if (memAddress < 0 || memAddress >= mem.length) {
                        System.err.println("Invalid memory access at " + memAddress);
                        machine.halt();
                        return;
                    }
                    reg[rt] = mem[memAddress];
                }
                break;

            case 3: // SW: Store from register rt into memory
                if (memAddress < 0 || memAddress >= mem.length) {
                    System.err.println("Invalid memory access at " + memAddress);
                    machine.halt();
                    return;
                }
                mem[memAddress] = reg[rt];
                break;

            case 4: // BEQ: Branch if reg[rs] == reg[rt]
                if (reg[rs] == reg[rt]) {
                    int newPc = machine.getPc() + 1 + offsetField;
                    machine.setNextPc(newPc);
                }
                break;

            default:
                // Invalid or unknown opcode — stop the machine
                System.err.println("Unknown I-type opcode: " + opcode);
                machine.halt();
                break;
        }
    }
}
