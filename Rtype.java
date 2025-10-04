public class Rtype {
    // Singleton instance
    private static Rtype instance;

    private Rtype() {}

    public static Rtype getInstance() {
        if (instance == null) {
            instance = new Rtype();
        }
        return instance;
    }

    /**
     * Execute R-type instruction (ADD, NAND).
     * @param machine The CPU state (registers, memory, PC).
     * @param opcode  Operation code (0=ADD, 1=NAND).
     * @param rs      Source register A.
     * @param rt      Source register B.
     * @param rd      Destination register.
     */
    public void execute(Machine machine, int opcode, int rs, int rt, int rd) {
        int[] reg = machine.getRegisters();

        // Register 0 must always stay 0
        if (rd == 0) return;

        switch (opcode) {
            case 0: // ADD
                reg[rd] = reg[rs] + reg[rt];
                break;

            case 1: // NAND
                reg[rd] = ~(reg[rs] & reg[rt]);
                break;

            default:
                System.err.println("Unknown R-type opcode: " + opcode);
                machine.halt();
                break;
        }
    }
}
