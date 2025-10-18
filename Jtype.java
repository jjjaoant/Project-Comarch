public class Jtype {
    // Singleton instance
    private static Jtype instance;

    private Jtype() {}

    public static Jtype getInstance() {
        if (instance == null) {
            instance = new Jtype();
        }
        return instance;
    }

    /**
     * Execute J-type instruction (JALR).
     * @param machine CPU state
     * @param opcode  Operation code (5 = JALR)
     * @param rs      Source register (jump target)
     * @param rd      Destination register (store return address)
     */

    public void execute(Machine machine, int opcode, int rs, int rd) {
        int[] reg = machine.getRegisters();

        if (opcode == 5) { // JALR: Jump And Link Register
            // Save PC+1 into rd (unless rd == 0)
            if (rd != 0) {
                reg[rd] = machine.getPc() + 1;
            }

            // Jump to address in rs (only if rs != rd)
            if (rs != rd) {
                machine.setNextPc(reg[rs]);
            }
        } else {
            // Invalid or unknown opcode — stop the machine
            System.err.println("Unknown J-type opcode: " + opcode);
            machine.halt();
        }
    }
}
