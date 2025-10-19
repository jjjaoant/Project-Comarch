import java.io.*;
import java.util.HashMap;

public class Assembler {
    // Define max/min number for each value
    private static final int MAX_REG_NUM = 7;
    private static final int MAX_OFFSET_VALUE = 32767; // 16-bit signed
    private static final int MIN_OFFSET_VALUE = -32768; // 16-bit signed

    // Opcode Mapping
    private static final HashMap<String, Integer> opcodeMap = new HashMap<>();
    static {
        opcodeMap.put("add", 0);
        opcodeMap.put("nand", 1);
        opcodeMap.put("lw", 2);
        opcodeMap.put("sw", 3);
        opcodeMap.put("beq", 4);
        opcodeMap.put("jalr", 5);
        opcodeMap.put("halt", 6);
        opcodeMap.put("noop", 7);
        opcodeMap.put(".fill", -1); // pseudo operator
    }

    private static HashMap<String, Integer> symbolTable;

    public static void main(String[] args) {
        // Check for valid number of arguments
        if (args.length != 2) {
            System.out.println("[Error] Usage: Assembler <assembly-code-file> <machine-code-file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        // Create Symbol Table
        symbolTable = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            createSymbolTable(reader);
        } catch (IOException e) {
            System.out.println("[Error] Unable to read <assembly-code-file>: " + e.getMessage());
            System.exit(1);
        }

        // Convert into machine code
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            convertMC(reader, writer);
        } catch (IOException e) {
            System.out.println("[Error] Unable to read/write <assembly-code-file>/<machine-code-file>: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void createSymbolTable(BufferedReader reader) throws IOException {
        int address = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            String[] fields = readAndParse(line);

            // If there is an empty, continue first
            if (fields == null) continue;

            String label = fields[0];

            // Check label
            if (!label.isEmpty()) {
                if (symbolTable.containsKey(label)) {
                    throw new IllegalArgumentException("[Error] Duplicate label: " + label);
                }
                if (label.length() > 6) {
                    throw new IllegalArgumentException("[Error] Label too long: " + label);
                }
                if (!label.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
                    throw new IllegalArgumentException("[Error] Invalid label: " + label);
                }
                if (isNumeric(label)) {
                    throw new IllegalArgumentException("[Error] Label is a number (invalid): " + label);
                }
                if (opcodeMap.containsKey(label.toLowerCase())) {
                    throw new IllegalArgumentException("[Error] Label matches a reserved opcode: " + label);
                }

                symbolTable.put(label, address);
            }
            address++;
        }
    }

    public static void convertMC(BufferedReader reader, BufferedWriter writer) throws IOException {
        int address = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            String[] fields = readAndParse(line);
            if (fields == null) continue;

            String opcode = fields[1];
            String arg0 = fields[2];
            String arg1 = fields[3];
            String arg2 = fields[4];

            long machineCode = 0;

            if (!opcodeMap.containsKey(opcode.toLowerCase())) {
                throw new IllegalArgumentException("[Error] Invalid opcode: " + opcode);
            }

            int op = opcodeMap.get(opcode.toLowerCase());

            // Bit 31-25 always 0
            // Bit 24-22 are 3-bit Opcode
            machineCode |= ((long) op << 22);

            switch (opcode.toLowerCase()) {
                // R-type instruction (regA, regB, destReg)
                case "add":
                case "nand":
                    int RT_regA = parseRegister(arg0, "regA", address);
                    int RT_regB = parseRegister(arg1, "regB", address);
                    int RT_destReg = parseRegister(arg2, "destReg", address);

                    machineCode |= ((long) RT_regA << 19); // Bit 21-19
                    machineCode |= ((long) RT_regB << 16); // Bit 18-16
                    machineCode |= RT_destReg; // Bit 2-0
                    break;

                // I-type instruction (regA, regB, offSetField)
                case "lw":
                case "sw":
                    int IT_regA = parseRegister(arg0, "regA", address);
                    int IT_regB = parseRegister(arg1, "regB", address);
                    int IT_offset = parseOffset(arg2, address, false);

                    machineCode |= ((long) IT_regA << 19); // Bit 21-19
                    machineCode |= ((long) IT_regB << 16); // Bit 18-16
                    machineCode |= (IT_offset & 0xFFFF);
                    break;

                // I-type instruction [PC-Relative]
                case "beq":
                    int PC_regA = parseRegister(arg0, "regA", address);
                    int PC_regB = parseRegister(arg1, "regB", address);
                    int PC_offset;

                    if (isNumeric(arg2)) {
                        PC_offset = Integer.parseInt(arg2);
                        checkOffsetBounds(PC_offset, address);
                    } else {
                        int targetAddress = getAddressFromSymbol(arg2, address, "beq");
                        PC_offset = targetAddress - (address + 1);
                        checkOffsetBounds(PC_offset, address);
                    }

                    machineCode |= ((long) PC_regA << 19); // Bit 21-19
                    machineCode |= ((long) PC_regB << 16); // Bit 18-16
                    machineCode |= (PC_offset & 0xFFFF);
                    break;

                // J-type instruction (regA, regB)
                case "jalr":
                    int JT_regA = parseRegister(arg0, "regA", address);
                    int JT_regB = parseRegister(arg1, "regB", address);

                    machineCode |= ((long) JT_regA << 19); // Bit 21-19
                    machineCode |= ((long) JT_regB << 16); // Bit 18-16
                    break;

                case "halt":
                case "noop":
                    break;

                case ".fill":
                    int fillValue = parseOffset(arg0, address, true);
                    machineCode = fillValue;
                    break;
            }

            // Write Machine Code
            writer.write(String.valueOf((int) machineCode));
            writer.newLine();

            address++;
        }
    }

    private static String[] readAndParse(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("#")) {
            return null;
        }

        int commentIndex = trimmed.indexOf("#");
        if (commentIndex == -1) {
            commentIndex = trimmed.indexOf("//");
        }
        if (commentIndex != -1) {
            trimmed = trimmed.substring(0, commentIndex).trim();
        }
        if (trimmed.isEmpty()) {
            return null;
        }

        String[] parts = trimmed.split("\\s+");

        String label = "";
        String opcode = "";
        String arg0 = "";
        String arg1 = "";
        String arg2 = "";

        int startIndex = 0;

        if (parts.length > 0) {
            if (!opcodeMap.containsKey(parts[0].toLowerCase())) {
                label = parts[0];
                startIndex = 1;
            } else {
                label = "";
                startIndex = 0;
            }
        }

        if (parts.length > startIndex) {
            opcode = parts[startIndex];
        }
        if (parts.length > startIndex + 1) {
            arg0 = parts[startIndex + 1];
        }
        if (parts.length > startIndex + 2) {
            arg1 = parts[startIndex + 2];
        }
        if (parts.length > startIndex + 3) {
            arg2 = parts[startIndex + 3];
        }

        return new String[]{label, opcode, arg0, arg1, arg2};
    }

    private static int parseRegister(String arg, String fieldName, int address) throws IllegalArgumentException {
        if (!isNumeric(arg)) {
            throw new IllegalArgumentException("[Error] Invalid register value: " + arg);
        }

        try {
            int regNum = Integer.parseInt(arg);
            if (regNum < 0 || regNum > MAX_REG_NUM) {
                throw new IllegalArgumentException("[Error] Invalid register value: " + arg);
            }
            return regNum;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("[Error] Invalid register value: " + arg);
        }
    }

    private static int parseOffset(String arg, int address, boolean isFill) throws IllegalArgumentException {
        if (isNumeric(arg)) {
            try {
                int value = Integer.parseInt(arg);
                if (!isFill) {
                    checkOffsetBounds(value, address); // lw/sw offset
                }
                return value;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("[Error] Invalid offset field value: " + arg);
            }
        } else {
            // Symbolic address (label)
            if (!symbolTable.containsKey(arg)) {
                throw new IllegalArgumentException("[Error] Undefined Label: " + arg);
            }
            int targetAddress = symbolTable.get(arg);
            if (!isFill) {
                checkOffsetBounds(targetAddress, address);
            }
            return targetAddress;
        }
    }

    private static int getAddressFromSymbol(String label, int address, String opcode) throws IllegalArgumentException {
        if (!symbolTable.containsKey(label)) {
            throw new IllegalArgumentException("[Error] Undefined Label: " + label);
        }
        return symbolTable.get(label);
    }

    private static void checkOffsetBounds(int offset, int address) throws IllegalArgumentException {
        if (offset < MIN_OFFSET_VALUE || offset > MAX_OFFSET_VALUE) {
            throw new IllegalArgumentException("[Error] Invalid offset value range (-32768 to 32767): " + offset);
        }
    }

    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}