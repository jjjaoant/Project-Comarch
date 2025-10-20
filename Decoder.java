/**
 * The Decoder class is responsible for interpreting (decoding)
 * the 32-bit machine instruction into opcode and operands.
 * Then, it delegates execution to the correct instruction handler:
 * Rtype, Itype, Jtype, or Otype.
 */
public class Decoder {

    // Opcode constants (3-bit binary)
    public static final int ADD  = 0b000;
    public static final int NAND = 0b001;
    public static final int LW   = 0b010;
    public static final int SW   = 0b011;
    public static final int BEQ  = 0b100;
    public static final int JALR = 0b101;
    public static final int HALT = 0b110;
    public static final int NOOP = 0b111;

    public Decoder() {}

      /**
     * Decode and execute one instruction.
     * This method separates the instruction into fields (opcode, registers, offset)
     * and dispatches execution to the appropriate type handler.
     */
    public void decode(Machine machine) {
        // --- FETCH current instruction ---
        int instruction = machine.getInstruction();

         // --- Extract opcode ---
        // Move bits 24–22 to the rightmost position (shift right 22 bits)
        // Then mask with 0b111 to keep only 3 bits
        int opcode = (instruction >>> 22) & 0b111;

        switch (opcode) {

            // --- R-type instructions: ADD, NAND ---
            case ADD:
            case NAND: {
                // R-type: opcode, rs (21-19), rt (18-16), rd (2-0)
                int rs = (instruction >>> 19) & 0b111;
                int rt = (instruction >>> 16) & 0b111;
                int rd = instruction & 0b111;
                Rtype.getInstance().execute(machine, opcode, rs, rt, rd);
                break;
            }
            
            // --- I-type instructions: LW, SW, BEQ ---
            case LW:
            case SW:
            case BEQ: {

                // I-type: opcode, rs (21-19), rt (18-16), offset (15-0)
                int rs = (instruction >>> 19) & 0b111;
                int rt = (instruction >>> 16) & 0b111;
                int offset = instruction & 0xFFFF;
                Itype.getInstance().execute(machine, opcode, rs, rt, offset);
                break;
            }
            
            // --- J-type instruction: JALR ---
            case JALR: {
                
                // J-type: opcode, rs (21-19), rd (18-16)
                int rs = (instruction >>> 19) & 0b111;
                int rd = (instruction >>> 16) & 0b111;
                Jtype.getInstance().execute(machine, opcode, rs, rd);
                break;
            }
            
            // --- O-type instructions: HALT, NOOP ---
            case HALT:
            case NOOP: {

                // O-type: opcode only
               Otype.getInstance().execute(machine, opcode);
                break;
            }

            // --- Invalid opcode ---
            default:
                System.err.println("Unknown opcode: " + opcode);
                machine.halt();
                break;
        }
    }
}
