import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Simulator {

    public static void main(String[] args) {
        // ตรวจสอบจำนวน arguments
        if (args.length != 1) {
            System.err.println("Usage: java Simulator <machine_code_file>");
            System.exit(1);
        }

        String filename = args[0];
        File inputFile = new File(filename);

        if (!inputFile.exists()) {
            System.err.println("Error: file '" + filename + "' not found.");
            System.exit(1);
        }

        Machine machine = new Machine();
        int[] memory = machine.getMemory();
        int pc = 0;

        // โหลด machine code เข้าสู่ memory
        try {
            List<String> lines = Files.readAllLines(inputFile.toPath());
            for (String line : lines) {
                try {
                    memory[pc++] = Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid machine code at line " + (pc + 1));
                    System.exit(1);
                }
            }
            machine.setInstructionCount(pc);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }

        // เริ่มจำลองการทำงาน
        machine.simulate();

        System.exit(0);
    }
}
