public class Jtype {
    // Singleton instance: only one instance of Jtype is ever created.
    // This ensures all J-type instructions use the same shared object.
    private static Jtype instance;

    private Jtype() {
    }

    /**
     * Provides access to the single Jtype instance.
     * If it does not exist yet, create a new one.
     *
     * @return The shared Jtype instance.
     */
    public static Jtype getInstance() {
        if (instance == null) {
            instance = new Jtype();
        }
        return instance;
    }

    /**
     * Execute J-type instruction (JALR).
     * 
     * @param machine CPU state
     * @param opcode  Operation code (5 = JALR)
     * @param rs      Source register (jump target)
     * @param rd      Destination register (store return address)
     */

    public void execute(Machine machine, int opcode, int rs, int rd) {
        // Retrieve the register array from the machine.
        int[] reg = machine.getRegisters();

        // Check if the opcode is JALR (opcode 5)

        if (opcode == 5) { // JALR: Jump And Link Register
            // Step 1: Save (PC + 1) into rd as a return address.
            // If rd == 0, skip because register 0 must always remain 0.
            if (rd != 0) {
                reg[rd] = machine.getPc() + 1;
            }

            // Step 2: Jump to the address stored in register rs.
            // This changes the next program counter (nextPc).
            // If rs == rd, do not jump immediately to avoid conflicts.
            if (rs != rd) {
                machine.setNextPc(reg[rs]);
            }
        } else {
            // Unknown or invalid opcode: print error message and halt the CPU.
            System.err.println("Unknown J-type opcode: " + opcode);
            machine.halt();
        }
    }
}
