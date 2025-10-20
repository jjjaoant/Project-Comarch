public class Rtype {
    // Singleton instance: only one instance of this class should exist.
    // This ensures all R-type operations share the same object in memory.
    private static Rtype instance;

    private Rtype() {
    }

    public static Rtype getInstance() {
        if (instance == null) {
            instance = new Rtype();
        }
        return instance;
    }

    /**
     * Execute R-type instruction (ADD, NAND).
     * 
     * @param machine The CPU state (registers, memory, PC).
     * @param opcode  Operation code (0=ADD, 1=NAND).
     * @param rs      Source register A.
     * @param rt      Source register B.
     * @param rd      Destination register.
     */
    public void execute(Machine machine, int opcode, int rs, int rt, int rd) {
        // Get a reference to the CPU's register array.
        int[] reg = machine.getRegisters();

        // Register 0 must always stay 0
        if (rd == 0)
            return;

        switch (opcode) {
            case 0: // ADD operation
                // Perform integer addition: reg[rd] = reg[rs] + reg[rt]
                reg[rd] = reg[rs] + reg[rt];
                break;

            case 1: // NAND operation
                // Perform bitwise NAND: reg[rd] = ~(reg[rs] & reg[rt])
                reg[rd] = ~(reg[rs] & reg[rt]);
                break;

            default:
                // Invalid opcode: print error message and stop the machine.
                System.err.println("Unknown R-type opcode: " + opcode);
                machine.halt();
                break;
        }
    }
}
