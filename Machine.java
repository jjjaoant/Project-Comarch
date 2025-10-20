/**
 * The Machine class represents the LC-2K CPU simulator.
 * It contains:
 *  - Memory (65536 words)
 *  - Registers (8 general-purpose)
 *  - Program Counter (PC)
 *  - Instruction counter
 *  - The Decoder (used to interpret instructions)
 */

public class Machine {
    private static final int MEMORY_SIZE = 65536;  // 16-bit address space
    private static final int REGISTER_SIZE = 8;    // 8 registers (R0–R7)
    private static final int HALT_CODE = 0x1C00000; // used for invalid access safety

    private final int[] memory;     // main memory array
    private final int[] registers;  // register file
    private int pc;                 // current program counter
    private int nextPc;             // current program counter
    private int executed;           // number of executed instructions
    private int instructionCount;   // number of instructions loaded from file
    private boolean halted;         // flag that tells if the machine stopped

    private final Decoder decoder;  // instruction decoder
    
    /** Constructor: initializes memory, registers, and decoder */
    public Machine() {
        memory = new int[MEMORY_SIZE];
        registers = new int[REGISTER_SIZE];
        pc = 0;
        nextPc = 0;
        executed = 0;
        halted = false;
        decoder = new Decoder();
    }
  
    /**
     * Main simulation loop.
     * Executes instructions until a HALT instruction is encountered.
    */
    public void simulate() {
        while (!halted) {
            // Print the state before executing current instruction
            printState();

            // Execute one instruction (Fetch–Decode–Execute)
            step();
        }

        // After HALT, print the final state
        printState();
    }

     /**
     * Executes one step of simulation (one instruction).
     * This function performs:
     *  1. Fetch: get current instruction from memory[pc]
     *  2. Decode: interpret instruction via Decoder
     *  3. Execute: perform the operation
     */
    private void step() {
        // Prepare next PC (normally PC+1)
        nextPc = (pc + 1) % MEMORY_SIZE;

        // Decode and execute the current instruction
        decoder.decode(this);

        // Register 0 must always stay zero (hardware constraint)
        registers[0] = 0; 

        // Move to the next PC
        pc = nextPc;

        // Count executed instructions
        executed++;
    }

    /** Fetch the instruction currently pointed by PC */
    public int getInstruction() {
        if (pc >= 0 && pc < MEMORY_SIZE) {
            return memory[pc];
        }

        // If PC out of range, return HALT to stop safely
        return HALT_CODE;  
    }

    // --- Getters and Setters ---
    public int[] getMemory() { return memory; }
    public int[] getRegisters() { return registers; }
    public int getPc() { return pc; }
    public void setNextPc(int newPc) { nextPc = newPc; }
    public void halt() { halted = true; }
    public void setInstructionCount(int count) { instructionCount = count; }

    /**
     * Prints the current state of the entire machine.
     * This is required by the project spec (print before each instruction).
     */
    private void printState() {
        StringBuilder sb = new StringBuilder();
        
        // If machine halted, print summary first
        if (halted) {
            sb.append("machine halted\n")
              .append("total of ").append(executed).append(" instructions executed\n")
              .append("final state of machine:\n");
        }
       
        // Print PC, memory, and registers
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
