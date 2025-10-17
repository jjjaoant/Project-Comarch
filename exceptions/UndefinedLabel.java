package exceptions;

public class UndefinedLabel extends RuntimeException {
  public UndefinedLabel(String label) {
    super("Label '" + label + "' is undefined.");
  }
}
