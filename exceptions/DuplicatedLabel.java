package exceptions;

public class DuplicatedLabel extends RuntimeException {
    public DuplicatedLabel(String label) {
      super("Error: Label '" + label + "' is defined more than once.");
    }
}
