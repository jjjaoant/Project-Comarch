public class Machine {
    private static final int MEMORY_SIZE = 65536;
    private static final int REGISTER_SIZE = 8;
    private static final int HALT_CODE = 0x1C00000;

    private final int[] memory;
    private final int[] registers;
    private int pc;              // Program Counter
    private int nextPc;          // Next Program Counter
    private int executed;        // Number of executed instructions
    private int instructionCount;
    private boolean halted;

    private final Decoder decoder;

    public Machine() {
        memory = new int[MEMORY_SIZE];
        registers = new int[REGISTER_SIZE];
        pc = 0;
        nextPc = 0;
        executed = 0;
        halted = false;
        decoder = new Decoder();
    }

    /** Run until HALT */
    public void simulate() {
        while (!halted) {
            printState();
            step();
        }
        printState();
    }

    /** Execute one instruction */
    private void step() {
        nextPc = (pc + 1) % MEMORY_SIZE;
        decoder.decode(this);
        registers[0] = 0;   // register[0] must always remain 0
        pc = nextPc;
        executed++;
    }

    /** Read current instruction */
    public int getInstruction() {
        if (pc >= 0 && pc < MEMORY_SIZE) {
            return memory[pc];
        }
        return HALT_CODE;   // stop if accessing invalid memory
    }

    // Getters
    public int[] getMemory() { return memory; }
    public int[] getRegisters() { return registers; }
    public int getPc() { return pc; }

    // Setters
    public void setNextPc(int newPc) { nextPc = newPc; }
    public void halt() { halted = true; }
    public void setInstructionCount(int count) { instructionCount = count; }

    /** Print state of the machine */
    private void printState() {
        StringBuilder sb = new StringBuilder();

        if (halted) {
            sb.append("machine halted\n")
              .append("total of ").append(executed).append(" instructions executed\n")
              .append("final state of machine:\n");
        }

        sb.append("@@@\nstate:\n")
          .append("\tpc ").append(pc).append('\n')
          .append("\tmemory:\n");

        for (int i = 0; i < instructionCount; i++) {
            sb.append("\t\tmem[").append(i).append("] ").append(memory[i]).append('\n');
        }

        sb.append("\tregisters:\n");
        for (int i = 0; i < registers.length; i++) {
            sb.append("\t\treg[").append(i).append("] ").append(registers[i]).append('\n');
        }

        sb.append("end state\n");
        System.out.println(sb);
    }
}
