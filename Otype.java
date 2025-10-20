public class Otype {
    // Singleton instance: ensures only one instance of Otype exists.
    private static Otype instance;

    private Otype() {
    }

    /**
     * Provides access to the single shared instance of Otype.
     * If it doesn’t exist yet, create a new one.
     *
     * @return The shared Otype instance.
     */
    public static Otype getInstance() {
        if (instance == null) {
            instance = new Otype();
        }
        return instance;
    }

    /**
     * Execute O-type instruction (HALT, NOOP).
     * 
     * @param machine CPU state
     * @param opcode  Operation code (6=HALT, 7=NOOP)
     */
    public void execute(Machine machine, int opcode) {
        switch (opcode) {
            case 6: // HALT: Stop the simulation
                // Immediately stop the CPU by setting the halted flag.
                machine.halt();
                break;

            case 7: // NOOP: Do nothing, move to next instruction
                // Do nothing — proceed to the next instruction.
                break;

            default:
                // Invalid opcode: print an error message and stop execution.
                System.err.println("Unknown O-type opcode: " + opcode);
                machine.halt();
                break;
        }
    }
}
