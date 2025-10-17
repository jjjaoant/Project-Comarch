package exceptions;

public class UnknownInstruction extends RuntimeException {
  public UnknownInstruction(String instruction) {
    super("Unknown instruction '" + instruction + "'.");
  }
}
