import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import exceptions.*;

public class Assembler {
    public enum Operator {
        ADD, NAND, LW, SW, BEQ, JALR, HALT, NOOP, FILL
    }

    // A map storing each label and its correlated address
    static Map<String, Integer> labels = new HashMap<>();
    // A list storing each lines in the assembly code
    static List<String> lines;

    public static void main(String[] args) {
        // Check whether there is an invalid number of arguments
        if (args.length != 2) {
            System.out.println("Only 2 Arguments Required. <assembly-code-file> <machine-code-file>");
            System.exit(1);
        }

        File inputFile = new File("src\\" +args[0]);
        File outputFile = new File("src\\" +args[1]);
        try {
            assemble(inputFile, outputFile); // Compile Assembly Code
        } catch (FileNotFoundException | DuplicatedLabel | UnknownInstruction | UndefinedLabel | OverflowingField | IllegalLabel e) {
            System.out.println("error: " + e.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }

    public static void assemble(File inputFile, File outputFile) throws DuplicatedLabel, FileNotFoundException, UnknownInstruction, UndefinedLabel, OverflowingField, IllegalLabel {
        Charset charset = StandardCharsets.UTF_8;
        Path inputPath;
        try {
            inputPath = Paths.get(inputFile.getAbsolutePath()).normalize();
        } catch (InvalidPathException e) {
            throw new FileNotFoundException(e.getMessage() + " cannot be converted to a valid path.");
        } catch (SecurityException e) {
            throw new FileNotFoundException(e.getMessage() + " cannot be accessed.");
        }

        String code;
        try {
            code = Files.readString(inputPath, charset);
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage() + " cannot be found.");
        }

        lines = code.lines().toList();

        int lineCount = 0;
        for (String line : lines) {
            lineCount++;
            String label = line.split("\t")[0].strip();
            if (label.isBlank()) continue;
            if (label.length() > 6) throw new IllegalLabel("Illegal label \"" + label + "\" at line " + lineCount + ".\n" + "Labels must be 6 characters or less.");
            if (!Character.isLetter(label.charAt(0))) throw new IllegalLabel("Illegal label \"" + label + "\" at line " + lineCount + ".\n" + "The first character in a label must be a letter.");
            if (labels.put(label, lineCount - 1) != null) throw new DuplicatedLabel("Duplicated label \"" + label + "\" at line " + lineCount + ".");
        }

        StringBuilder sb = new StringBuilder();
        lineCount = 0;
        for (String line : lines) {
            String[] tokens = line.split("\t");
            int tokensLength = Math.min(tokens.length, 5);
            lineCount++;
            if (tokensLength <= 1) throw new UnknownInstruction("No operator found at line " + lineCount + ".");
            int opcodeShift = 22;
            int field0Shift = 19;
            int field1Shift = 16;
            int field2Shift = 0;
            Operator operator = getOperator(tokens[1], lineCount);

            // O-type
            if (operator == Operator.HALT || operator == Operator.NOOP) {
                checkExcessTokens(tokens, 2, tokensLength, lineCount);
                sb.append(operator.ordinal() << opcodeShift).append("\n");
                continue;
            }

            // Retrieve the 0th field
            int i_field0 = variableInstance(tokens, 2, lineCount);

            // Store .fill assignment
            // No need to check for overflow since .fill is for storing 32 bits
            if (operator == Operator.FILL) {
                checkExcessTokens(tokens, 3, tokensLength, lineCount);
                sb.append(i_field0).append("\n");
                continue;
            }

            // Retrieve the first field
            int i_field1 = variableInstance(tokens, 3, lineCount);

            // Check for overflowing fields
            checkOverflow(i_field0, 0, 7, tokens[2], lineCount);
            checkOverflow(i_field1, 0, 7, tokens[3], lineCount);
            short field0 = (short) i_field0;
            short field1 = (short) i_field1;

            // Concat opcode, field0, and field1
            int basicFields = (operator.ordinal() << opcodeShift) | (field0 << field0Shift) | (field1 << field1Shift);

            // Store J-type instruction
            if (operator == Operator.JALR) {
                checkExcessTokens(tokens, 4, tokensLength, lineCount);
                sb.append(basicFields).append("\n");
                continue;
            }

            // Store I-type instruction
            if (operator == Operator.LW || operator == Operator.SW || operator == Operator.BEQ) {
                int filter = 65535;
                int field2 = variableInstance(tokens, 4, lineCount, (operator == Operator.BEQ));
                checkOverflow(field2, -32768, 32767, tokens[4], lineCount);
                sb.append(basicFields | (field2 << field2Shift)).append("\n");
                continue;
            }

            // Store R-type instruction
            if (operator == Operator.ADD || operator == Operator.NAND) {
                int i_field2 = variableInstance(tokens, 4, lineCount);
                checkOverflow(i_field2, 0, 7, tokens[4], lineCount);
                short field2 = (short) i_field2;
                sb.append(basicFields | (field2 << field2Shift)).append("\n");
                continue;
            }

            // If opcode is not decoded properly
            throw new UnknownInstruction("Unexpected error has occurred during compilation.");
        }

        // Remove last \n
        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);
        }

        // Write machine code onto output file
        try {
            FileWriter outputWriter = new FileWriter(outputFile, false);
            outputWriter.write(sb.toString());
            outputWriter.close();
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage() + " cannot be written.");
        }
    }

    private static int variableInstance(String[] tokens, int index, int lineCount, boolean isLabelRelative) throws UndefinedLabel, UnknownInstruction {
        String str;

        // Array out of bound
        try {
            str = tokens[index].strip();
        } catch (ArrayIndexOutOfBoundsException ignore) {
            throw new UnknownInstruction("Missing operand at line " + lineCount + ".");
        }

        // Whitespace
        if (str.isBlank()) throw new UnknownInstruction("Missing operand at line " + lineCount + ".");

        // Parse integer
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ignore) {}

        // Get label address
        Integer parsedLabel = labels.get(str);
        if (parsedLabel != null) {
            return parsedLabel - (isLabelRelative ? lineCount : 0);
        }

        // Undefined label
        throw new UndefinedLabel("Undefined label \"" + str + "\" at line " + lineCount + ".");
    }

    private static int variableInstance(String[] tokens, int index, int lineCount) throws UndefinedLabel, UnknownInstruction {
        return variableInstance(tokens, index, lineCount, false);
    }

    private static Operator getOperator(String str, int lineCount) throws UnknownInstruction {
        String strOperator = str.strip().toUpperCase();

        if (strOperator.isBlank()) throw new UnknownInstruction("No operator found at line " + lineCount + ".");
        if (strOperator.equals(".FILL")) return Operator.FILL;

        try {
            return Operator.valueOf(strOperator);
        } catch (IllegalArgumentException ignore) {
            throw new UnknownInstruction("Unknown operator \"" + strOperator + "\" at line " + lineCount + ".");
        }
    }

    private static void checkExcessTokens(String[] tokens, int startIndex, int endIndex, int lineCount) throws UnknownInstruction {
        for (int i = startIndex; i < endIndex; i++) {
            try {
                if (!tokens[i].isBlank()) throw new UnknownInstruction("Excess operand " + tokens[i] + " at line " + lineCount + ".");
            } catch (ArrayIndexOutOfBoundsException ignore) {
                return;
            }
        }
    }

    private static void checkOverflow(int number, int min, int max, String token, int lineCount) throws OverflowingField {
        if (number < min || number > max) {
            throw new OverflowingField("Overflowing field \"" + token + "\" at line " + lineCount + "\n" + "the field requires between " + min + " and " + max + ".");
        }
    }
}