import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;


/**
 * The Simulator class is the main entry point for the Behavioral Simulator.
 * It is responsible for:
 *  - Reading a machine code file (.obj)
 *  - Loading the program into memory
 *  - Creating and running the Machine simulation
 */
public class Simulator {
    
     /**
     * Program entry point.
     * Reads the input .obj file, loads machine code into memory,
     * and starts the simulation.
     *
     * @param args Command-line arguments (expects one filename argument)
     */
    public static void main(String[] args) {
        // Check if a file argument is provided                          
        if (args.length != 1) {                             
            System.err.println("Usage: java Simulator <machine_code_file>");
            System.exit(1);
        }

        String filename = args[0];
        File inputFile = new File(filename);
        
        // Verify that the file exists
        if (!inputFile.exists()) {
            System.err.println("Error: file '" + filename + "' not found.");
            System.exit(1);
        }
        

        // Create a Machine object (represents CPU + Memory)
        Machine machine = new Machine();
        int[] memory = machine.getMemory();
        int pc = 0;    // program counter for loading memory

        // Load machine code from file into memory line by line
        try {
            List<String> lines = Files.readAllLines(inputFile.toPath());
            for (String line : lines) {
                try {
                    // Each line should contain one integer instruction
                    memory[pc++] = Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid machine code at line " + (pc + 1));
                    System.exit(1);
                }
            }
            // Save the number of loaded instructions into the machine
            machine.setInstructionCount(pc);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }

        // Start simulating the machine
        machine.simulate();

        System.exit(0);
    }
}
