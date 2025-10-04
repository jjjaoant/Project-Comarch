public class Otype {
    // Singleton instance
    private static Otype instance;

    private Otype() {}

    public static Otype getInstance() {
        if (instance == null) {
            instance = new Otype();
        }
        return instance;
    }

    /**
     * Execute O-type instruction (HALT, NOOP).
     * @param machine CPU state
     * @param opcode  Operation code (6=HALT, 7=NOOP)
     */
    public void execute(Machine machine, int opcode) {
        switch (opcode) {
            case 6: // HALT
                machine.halt();
                break;

            case 7: // NOOP
                // Do nothing
                break;

            default:
                System.err.println("Unknown O-type opcode: " + opcode);
                machine.halt();
                break;
        }
    }
}
