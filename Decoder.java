public class Decoder {

    // Opcode constants
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
     * Decode and execute one instruction from the machine state.
     */
    public void decode(Machine machine) {
        int instruction = machine.getInstruction();
        int opcode = (instruction >>> 22) & 0b111;

        switch (opcode) {
            case ADD:
            case NAND: {
                // R-type: opcode, rs (21-19), rt (18-16), rd (2-0)
                int rs = (instruction >>> 19) & 0b111;
                int rt = (instruction >>> 16) & 0b111;
                int rd = instruction & 0b111;
                Rtype.getInstance().execute(machine, opcode, rs, rt, rd);
                break;
            }

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

            case JALR: {
                // J-type: opcode, rs (21-19), rd (18-16)
                int rs = (instruction >>> 19) & 0b111;
                int rd = (instruction >>> 16) & 0b111;
                Jtype.getInstance().execute(machine, opcode, rs, rd);
                break;
            }

            case HALT:
            case NOOP: {
                // O-type: opcode only
               Otype.getInstance().execute(machine, opcode);
                break;
            }

            default:
                System.err.println("Unknown opcode: " + opcode);
                machine.halt();
                break;
        }
    }
}
