import java.io.*;
import java.util.HashMap;

public class Assembler {
    // Define 7 as a maximum register number available as per the given condition (tip)
    private static final int MAX_REG_NUM = 7;
    // Define offsetField range as -32768 to 32767, since offsetField is allowed 16-bit at most
    private static final int MAX_OFFSET_VALUE = 32767; // 16-bit signed
    private static final int MIN_OFFSET_VALUE = -32768; // 16-bit signed

    // Map opcode to the correspondant operator to be able to assemble using the correct opcode
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
        // .fill is mapped to -1, since .fill is a pseudo operation with no opcode
        opcodeMap.put(".fill", -1);
    }

    // Create a static symbolTable variable to map symbol address to an input field with symbol (labels/number as symbol/etc.)
    private static HashMap<String, Integer> symbolTable;

    public static void main(String[] args) {
        // Check for valid number of arguments (input file and output file) for running the assembler.
        if (args.length != 2) {
            System.out.println("[Error] Usage: Assembler <assembly-code-file> <machine-code-file>");
            System.exit(1);
        }

        // Define inputFile as args[0] (first argument) and outputFile as args[1] (second argument)
        // when running the assembler
        String inputFile = args[0];
        String outputFile = args[1];

        // Define symbolTable as a Hash Map to link symbol input to its symbol address
        // This is defined later as to not make symbolTable final
        symbolTable = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            createSymbolTable(reader);
        } catch (IOException e) {
            System.out.println("[Error] Unable to read <assembly-code-file>: " + e.getMessage());
            System.exit(1);
        }

        // Use FileReader and FileWriter functions to read inputFile (file) and write outputFile (file)
        // Then, calls convertMC function that accepts reader (a variable that stores lines read from the inputFile)
        // and writer (a variable that writes information into the outputFile)
        // Catch any IOException error, just in case the BufferedReader or Buffered Writer function(s)
        // couldn't read inputFile or write outputFile
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Calls convertMC function, to convert Assembly Language program to Machine Code
            convertMC(reader, writer);
        } catch (IOException e) {
            System.out.println("[Error] Unable to read/write <assembly-code-file>/<machine-code-file>: " + e.getMessage());
            // If there is an error, call exit(1) as per given condition
            System.exit(1);
        }
    }

    /** Create Symbol Table to link symbol assemble language input
     *  to its address (symbolic address)
     * @param reader lines read from inputFile
     * @throws IOException when there is a problem with reading inputFile
     */
    private static void createSymbolTable(BufferedReader reader) throws IOException {
        // Set the initial address as 0 (no data has been stored yet)
        int address = 0;
        // Define line as String to store a line from reader variable, which reads each lines individually from inputFile
        String line;

        // This code body will continue until all lines in the inputFile are read
        while ((line = reader.readLine()) != null) {
            // Parse each line for individual data fields as per the format:
            // label<white>instruction<white>field0<white>field1<white>field2<white>comments
            String[] fields = readAndParse(line);

            // If there is an empty data field, continue (no error detected)
            if (fields == null) continue;

            // Put label as the first field as per the format
            String label = fields[0];

            // If there is a label, check for errors related to the label
            if (!label.isEmpty()) {
                // If the symbol table already contains the label, then, stop the program,
                // and print out "[Error] Duplicate label: " with the said label
                if (symbolTable.containsKey(label)) {
                    throw new IllegalArgumentException("[Error] Duplicate label: " + label);
                }
                // If the label's length is more than 6 characters, then, stop the program,
                // and print out "[Error] Label is too long: " with the said label
                if (label.length() > 6) {
                    throw new IllegalArgumentException("[Error] Label is too long: " + label);
                }
                // If the label doesn't start with an alphabetic character or an alphanumeric character,
                // then, stop the program, and print out "[Error] The label starts with invalid character: "
                // with the said label
                if (!label.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
                    throw new IllegalArgumentException("[Error] The label starts with invalid character: " + label);
                }
                // If the label contains only number, then, stop the program,
                // and print out "[Error] Label is a number (invalid): " with the said label
                if (isNumeric(label)) {
                    throw new IllegalArgumentException("[Error] Label is a number (invalid): " + label);
                }
                // If the label matches any reserved opcode/operator, then stop the program,
                // and print out "[Error] Label matches a reserved opcode (operator): " with the said label
                if (opcodeMap.containsKey(label.toLowerCase())) {
                    throw new IllegalArgumentException("[Error] Label matches a reserved opcode (operator): " + label);
                }

                // If there is no error with the label, the add the label to the symbol table
                // with its corresponding address
                symbolTable.put(label, address);
            }
            // After adding the label into the table, add 1 to the address as to not store different labels
            // with the same address
            address++;
        }
    }

    /** Convert parsed assembly language code lines into
     * machine code
     * @param reader lines read from the inputFile
     * @param writer lines to be written into the outputFile
     * @throws IOException when there is a problem with reading inputFile or writing outputFile
     */
    public static void convertMC(BufferedReader reader, BufferedWriter writer) throws IOException {
        // Set the initial address as 0 (no data has been reached yet)
        int address = 0;
        // Define line as String to store a line from reader variable, which reads each lines individually from inputFile
        String line;

        // This code body will continue until all lines in the inputFile are read
        while ((line = reader.readLine()) != null) {
            // Parse each line for individual data fields as per the format:
            // label<white>instruction<white>field0<white>field1<white>field2<white>comments
            String[] fields = readAndParse(line);

            // If there is an empty data field, continue (no error detected)
            if (fields == null) continue;

            // Since the label is stored in the first field (field[0]), as per the format,
            // the rest of the data from the format are stored in the following order:
            // opcode (instruction) is stored in the second field (field[1])
            // arg0 (field0) is stored in the third field (field[2])
            // arg1 (field1) is stored in the fourth field (field[3])
            // arg2 (field2) is stored in the fifth field (field[4])
            // comments are not stored because they are not relevant to converting into machine code
            String opcode = fields[1];
            String arg0 = fields[2];
            String arg1 = fields[3];
            String arg2 = fields[4];

            // Initialize machine code as 0 because there is no machine code yet
            long machineCode = 0;

            // If there is no instruction/operator mapped to the opcode,
            // then, stop the program, and print out "[Error] Invalid instruction: " with the
            // said instruction/operator
            if (!opcodeMap.containsKey(opcode.toLowerCase())) {
                throw new IllegalArgumentException("[Error] Invalid instruction: " + opcode);
            }

            // Initialize op variable to store an integer associated with the instruction's opcode
            int op = opcodeMap.get(opcode.toLowerCase());

            // As per the condition:
            // Bit 31-25 always 0
            // Bit 24-22 are 3-bit Opcode
            // So, left-shift the machine code by 22 bits to start converting assembly language code
            // to machine code at bit 22
            machineCode |= ((long) op << 22);

            // Switch case function to choose what actions to take according to each instruction/operator
            switch (opcode.toLowerCase()) {
                // R-type instruction (requires regA, regB, and destReg)
                // If the instruction/operator is ADD or NAND (R-type instruction)
                // then, continue with their cases' actions
                case "add":
                case "nand":
                    // Initialize RT_regA (register A value) with parseRegister function
                    // to retrieve only register value from arg0 (field[1])
                    int RT_regA = parseRegister(arg0, "regA", address);
                    // Initialize RT_regB (register B value) with parseRegister function
                    // to retrieve only register value from arg1 (field[2])
                    int RT_regB = parseRegister(arg1, "regB", address);
                    // Initialize RT_destReg (destination register value) with parseRegister function
                    // to retrieve only register value from arg2 (field[3])
                    int RT_destReg = parseRegister(arg2, "destReg", address);

                    // Convert each register values into machine code
                    // For register A, this value is stored in bit 21-19, shift left 19 bits, of the machine code
                    machineCode |= ((long) RT_regA << 19); // Bit 21-19
                    // For register B, this value is stored in bit 18-16, shift left 16 bits, of the machine code
                    machineCode |= ((long) RT_regB << 16); // Bit 18-16
                    // For register A, this value is stored in bit 2-0, no need to shift because that's the only
                    // free bit space, of the machine code
                    machineCode |= RT_destReg; // Bit 2-0
                    // The process is finished
                    break;

                // I-type instruction (requires regA, regB, offSetField)
                // If the instruction/operator is LW or SW (I-type instruction)
                // then, continue with their cases' actions
                case "lw":
                case "sw":
                    // Initialize IT_regA (register A value) with parseRegister function
                    // to retrieve only register value from arg0 (field[1])
                    int IT_regA = parseRegister(arg0, "regA", address);
                    // Initialize IT_regB (register B value) with parseRegister function
                    // to retrieve only register value from arg1 (field[2])
                    int IT_regB = parseRegister(arg1, "regB", address);
                    // Initialize IT_offset (offset field value) with parseOffset function
                    // to retrieve only offset value from arg2 (field[3])
                    int IT_offset = parseOffset(arg2, address, false);

                    // Convert each register values into machine code
                    // For register A, this value is stored in bit 21-19, shift left 19 bits, of the machine code
                    machineCode |= ((long) IT_regA << 19); // Bit 21-19
                    // For register B, this value is stored in bit 18-16, shift left 16 bits, of the machine code
                    machineCode |= ((long) IT_regB << 16); // Bit 18-16
                    // For offset field, only takes the least significant bit(s), and add them to the rest
                    // of the free bits
                    machineCode |= (IT_offset & 0xFFFF);
                    // The process is finished
                    break;

                // I-type instruction [This case if for PC-Relative instruction]
                // If the instruction/operator is BEQ (I-type instruction)
                // then, continue with its case's actions
                case "beq":
                    // Initialize PC_regA (register A value) with parseRegister function
                    // to retrieve only register value from arg0 (field[1])
                    int PC_regA = parseRegister(arg0, "regA", address);
                    // Initialize PC_regB (register B value) with parseRegister function
                    // to retrieve only register value from arg1 (field[2])
                    int PC_regB = parseRegister(arg1, "regB", address);
                    // Define PC_offset as an integer first, then decide what to do below
                    int PC_offset;

                    // If field2 of the input is a number
                    if (isNumeric(arg2)) {
                        // Then, set PC_offset value to the number value of field2
                        PC_offset = Integer.parseInt(arg2);
                        // Check whether the value is in range with 16-bit offset value with
                        // checkOffsetBounds function
                        checkOffsetBounds(PC_offset, address);
                    } else {
                        // If field2 of the input is not a number
                        // So, that field must be the target PC that it should jump to,
                        // then, set the targetAddress value to a defined label from symbol table
                        int targetAddress = getAddressFromSymbol(arg2, address, "beq");
                        // Then, set PC_offset value to the target address minus address plus one
                        PC_offset = targetAddress - (address + 1);
                        // Check whether the value is in range with 16-bit offset value with
                        // checkOffsetBounds function
                        checkOffsetBounds(PC_offset, address);
                    }

                    // Convert each register values into machine code
                    // For register A, this value is stored in bit 21-19, shift left 19 bits, of the machine code
                    machineCode |= ((long) PC_regA << 19); // Bit 21-19
                    // For register B, this value is stored in bit 18-16, shift left 16 bits, of the machine code
                    machineCode |= ((long) PC_regB << 16); // Bit 18-16
                    // For offset field, only takes the least significant bit(s), and add them to the rest
                    // of the free bits
                    machineCode |= (PC_offset & 0xFFFF);
                    // The process is finished
                    break;

                // J-type instruction (requires regA, regB)
                // If the instruction/operator is BEQ (I-type instruction)
                // then, continue with its case's actions
                case "jalr":
                    // Initialize JT_regA (register A value) with parseRegister function
                    // to retrieve only register value from arg0 (field[1])
                    int JT_regA = parseRegister(arg0, "regA", address);
                    // Initialize PC_regB (register B value) with parseRegister function
                    // to retrieve only register value from arg1 (field[2])
                    int JT_regB = parseRegister(arg1, "regB", address);

                    // Convert each register values into machine code
                    // For register A, this value is stored in bit 21-19, shift left 19 bits, of the machine code
                    machineCode |= ((long) JT_regA << 19); // Bit 21-19
                    // For register B, this value is stored in bit 18-16, shift left 16 bits, of the machine code
                    machineCode |= ((long) JT_regB << 16); // Bit 18-16
                    // The process is finished
                    break;

                // O-type instruction (doesn't require any input)
                // HALT is for stopping the process of in the assembly language code
                // NOOP is for when there is no operation to be done in the assembly language code
                case "halt":
                case "noop":
                    // The process is finished
                    break;

                // .fill is a special pseudo instruction
                // for storing value straight into machine code with having to convert the value
                case ".fill":
                    // Initialize fillValue from parseOffset function
                    // that retrieves the fill value from field0 of the line
                    int fillValue = parseOffset(arg0, address, true);
                    // The machine code for the line of assembly language code is the value given
                    machineCode = fillValue;
                    // The process is finished
                    break;
            }

            // Write finished Machine Code to the outputFile
            writer.write(String.valueOf((int) machineCode));
            writer.newLine();

            // After converting one line of assembly language code into machine code,
            // add address by one to prevent different lines having the same address
            address++;
        }
    }

    /** Retrieve lines read from inputFile and
     * parsed the input data fields
     * @param line read lines from the inputFile
     * @return parsed string with each field separated for use
     */
    private static String[] readAndParse(String line) {
        // Initialize variable trimmed as trimmed line, for individual line of code
        String trimmed = line.trim();
        // If the trimmed line empty, then return null,
        // meaning, there is no assembly language code to be converted
        if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("#")) {
            return null;
        }

        // Initialize comment index to separate comments from the other fields
        int commentIndex = trimmed.indexOf("#");
        // If the index equals -1, meaning, there is no comment
        if (commentIndex == -1) {
            commentIndex = trimmed.indexOf("//");
        }
        // If the index doesn't equal -1, then, there is a comment to be separated from the
        // rest of the line
        if (commentIndex != -1) {
            trimmed = trimmed.substring(0, commentIndex).trim();
        }
        // Return null, as there is no line to be converted
        if (trimmed.isEmpty()) {
            return null;
        }

        // After parsing the comment, split the other parts, and put them in the parts array
        String[] parts = trimmed.split("\\s+");

        // Initialize each field with empty string
        String label = "";
        String opcode = "";
        String arg0 = "";
        String arg1 = "";
        String arg2 = "";

        // Initialize startIndex with zero to parse each field
        int startIndex = 0;

        // If the field is not empty, then continue with the
        // code body in the if statement
        if (parts.length > 0) {
            // If the label doesn't match defined instruction, the put the label in the first part (part[0])
            if (!opcodeMap.containsKey(parts[0].toLowerCase())) {
                label = parts[0];
                // Let the startIndex be 1, since there is already label in parts array
                startIndex = 1;
            } else {
                // If the label is empty, then let startIndex be 0
                label = "";
                startIndex = 0;
            }
        }

        // If the length of parts array is more than the startIndex, meaning the label is already parsed
        // then, put instruction/operator into parts[startIndex]
        if (parts.length > startIndex) {
            opcode = parts[startIndex];
        }
        // If the length of parts array is more than the startIndex + 1, meaning the label
        // and instruction/operator are already parsed
        // then, put field0 into parts[startIndex + 1]
        if (parts.length > startIndex + 1) {
            arg0 = parts[startIndex + 1];
        }
        // If the length of parts array is more than the startIndex + 2, meaning the label,
        // instruction/operator, and field0 are already parsed
        // then, put field1 into parts[startIndex + 2]
        if (parts.length > startIndex + 2) {
            arg1 = parts[startIndex + 2];
        }
        // If the length of parts array is more than the startIndex + 3, meaning the label,
        // instruction/operator, field0, and field1 are already parsed
        // then, put field2 into parts[startIndex + 3]
        if (parts.length > startIndex + 3) {
            arg2 = parts[startIndex + 3];
        }

        // Return the parsed string for further use
        return new String[]{label, opcode, arg0, arg1, arg2};
    }

    /** Retrieve and parse the register number
     * from input field
     * @param arg input register field
     * @param fieldName name of the input register field
     * @param address address assigned to the variable
     * @return register number
     * @throws IllegalArgumentException when the input register is illegal
     */
    private static int parseRegister(String arg, String fieldName, int address) throws IllegalArgumentException {
        // If the input argument is not a number, meaning,
        // the input register is invalid,
        // then, stop the program, and print out "[Error] Invalid register value: " with the said argument
        if (!isNumeric(arg)) {
            throw new IllegalArgumentException("[Error] Invalid register value: " + arg);
        }

        // If the input argument is a number
        try {
            // Initialize regNum as the integer of argument
            int regNum = Integer.parseInt(arg);
            // If the regNum is less than zero, or more than the defined maximum number of register available,
            // then, stop the program, and print out "[Error] Invalid register value: " with the said argument
            if (regNum < 0 || regNum > MAX_REG_NUM) {
                throw new IllegalArgumentException("[Error] Invalid register value: " + arg);
            }
            // Return the regNum value
            return regNum;
        } catch (NumberFormatException e) {
            // If there is a number format exception error, then,
            // stop the program, and print out "[Error] Invalid register value: " with the said argument
            throw new IllegalArgumentException("[Error] Invalid register value: " + arg);
        }
    }

    /** Retrieve and parse offset value
     * from the offSetField
     * @param arg input offset value
     * @param address address assigned to the line
     * @param isFill check if the offset is for .fill instruction or not
     * @return the argument integer value
     * @throws IllegalArgumentException when the input offset is illegal
     */
    private static int parseOffset(String arg, int address, boolean isFill) throws IllegalArgumentException {
        // If the offset value is a number
        if (isNumeric(arg)) {
            try {
                // Set the value to the integer of argument
                int value = Integer.parseInt(arg);
                // If the offset value is not for .fill instruction,
                // then, check the offset range with checkOffsetBounds function
                if (!isFill) {
                    checkOffsetBounds(value, address); // lw/sw offset
                }
                // Return offset value
                return value;
            } catch (NumberFormatException e) {
                // If there is a number format exception error, then,
                // stop the program, and print out "[Error] Invalid offset field value: " with the said argument
                throw new IllegalArgumentException("[Error] Invalid offset field value: " + arg);
            }
        } else {
            // The offset is a symbolic address
            // If the symbol table doesn't contain the input symbolic address,
            // meaning, the label is undefined, then,
            // stop the program, and print out "[Error] Undefined Label: " with the said argument
            if (!symbolTable.containsKey(arg)) {
                throw new IllegalArgumentException("[Error] Undefined Label: " + arg);
            }
            // If the label is defined, then return the targetAddress with the label's address
            // from the symbolTable
            int targetAddress = symbolTable.get(arg);
            // If the offset value is not for .fill instruction,
            // then, check the offset range with checkOffsetBounds function
            if (!isFill) {
                checkOffsetBounds(targetAddress, address);
            }
            return targetAddress;
        }
    }

    /** Get address from symbol table
     * @param label label that should be defined in the symbol table
     * @param address address of the input line
     * @param opcode instruction of the line
     * @return address of the label
     * @throws IllegalArgumentException if the label is undefined
     */
    private static int getAddressFromSymbol(String label, int address, String opcode) throws IllegalArgumentException {
        // If the symbol table doesn't contain the input label, meaning, the label is undefined,
        // then, stop the program, and print out "[Error] Undefined Label: " with the said label
        if (!symbolTable.containsKey(label)) {
            throw new IllegalArgumentException("[Error] Undefined Label: " + label);
        }
        // If the symbol table contains the label, then return the label's address
        return symbolTable.get(label);
    }

    /** Check the range of the input offset value
     * @param offset input offset field value
     * @param address address of the line
     * @throws IllegalArgumentException when the input offset field is illegal
     */
    private static void checkOffsetBounds(int offset, int address) throws IllegalArgumentException {
        // If the offset value is less than -32768 or more than 32767, meaning,
        // it is out of range, then, stop the program, and print out "[Error] Invalid offset value range (-32768 to 32767): "
        // with the said offset value
        if (offset < MIN_OFFSET_VALUE || offset > MAX_OFFSET_VALUE) {
            throw new IllegalArgumentException("[Error] Invalid offset value range (-32768 to 32767): " + offset);
        }
    }

    /** Check whether the input line is a number or not
     * @param str input string from line
     * @return whether the string is number or not
     */
    private static boolean isNumeric(String str) {
        // If there is no string, then return false
        // because empty string is not a number
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            // If the string is able to be parseInt, then it is a number, and return true
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            // If there is a number format exception, then stop the program,
            // and return false
            return false;
        }
    }
}