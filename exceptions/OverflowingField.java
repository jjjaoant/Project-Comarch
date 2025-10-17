package exceptions;

public class OverflowingField extends RuntimeException {
    public OverflowingField(String field) {
        super("Field " + field + " is overflowed");
    }
}
