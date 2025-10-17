package exceptions;

public class IllegalLabel extends RuntimeException {
    public IllegalLabel(String label) {
        super("Label " + label + " is illegal");
    }
}
