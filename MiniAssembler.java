import java.io.*;
import java.util.*;

public class MiniAssembler {

    // Opcode table
    private static final Map<String, Integer> OPCODES = new HashMap<>();
    static {
        OPCODES.put("add", 0);
        OPCODES.put("nand", 1);
        OPCODES.put("lw", 2);
        OPCODES.put("sw", 3);
        OPCODES.put("beq", 4);
        OPCODES.put("jalr", 5);
        OPCODES.put("halt", 6);
        OPCODES.put("noop", 7);
    }

    // store labels
    private static Map<String, Integer> labels = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java MiniAssembler <input.asm> <output.obj>");
            System.exit(1);
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            String line;
            while ((line = br.readLine()) != null) {
                String clean = line.split("#")[0].trim(); // remove comments
                if (!clean.isEmpty()) lines.add(clean);
            }
        }

        // pass1: collect labels
        int addr = 0;
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            if (!OPCODES.containsKey(parts[0]) && !parts[0].equals(".fill")) {
                labels.put(parts[0], addr);
            }
            addr++;
        }

        // pass2: encode
        List<Integer> machineCodes = new ArrayList<>();
        addr = 0;
        for (String line : lines) {
            String[] parts = line.split("\\s+");

            // if line starts with label, skip it
            if (!OPCODES.containsKey(parts[0]) && !parts[0].equals(".fill")) {
                parts = Arrays.copyOfRange(parts, 1, parts.length);
            }

            machineCodes.add(encode(parts, addr));
            addr++;
        }

        // write to .obj file
        try (PrintWriter pw = new PrintWriter(new FileWriter(args[1]))) {
            for (int code : machineCodes) {
                pw.println(code);
            }
        }
        System.out.println("Written machine code to " + args[1]);
    }

    private static int encode(String[] parts, int currentLine) {
        String instr = parts[0];

        // O-type
        if (instr.equals("halt") || instr.equals("noop")) {
            int opcode = OPCODES.get(instr);
            return opcode << 22;
        }

        // R-type
        if (instr.equals("add") || instr.equals("nand")) {
            int opcode = OPCODES.get(instr);
            int rs = Integer.parseInt(parts[1]);
            int rt = Integer.parseInt(parts[2]);
            int rd = Integer.parseInt(parts[3]);
            return (opcode << 22) | (rs << 19) | (rt << 16) | rd;
        }

        // I-type
        if (instr.equals("lw") || instr.equals("sw") || instr.equals("beq")) {
            int opcode = OPCODES.get(instr);
            int rs = Integer.parseInt(parts[1]);
            int rt = Integer.parseInt(parts[2]);
            int offset;
            try {
                offset = Integer.parseInt(parts[3]);
            } catch (NumberFormatException e) {
                offset = labels.get(parts[3]);
                if (instr.equals("beq")) {
                    offset = labels.get(parts[3]) - (currentLine + 1);
                }
            }
            return (opcode << 22) | (rs << 19) | (rt << 16) | (offset & 0xFFFF);
        }

        // J-type
        if (instr.equals("jalr")) {
            int opcode = OPCODES.get(instr);
            int rs = Integer.parseInt(parts[1]);
            int rd = Integer.parseInt(parts[2]);
            return (opcode << 22) | (rs << 19) | (rd << 16);
        }

        // .fill directive
        if (instr.equals(".fill")) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return labels.get(parts[1]);
            }
        }

        throw new RuntimeException("Unknown instruction: " + instr);
    }
}
