package ng.com.epump.epumpconnection;

import java.security.SecureRandom;
import java.util.Random;

public class RandomGenerator {

    /**
     * Generate a random string.
     */
    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

    private static final String digits = "0123456789";

    private final Random random;

    private final char[] symbols;

    private final char[] buf;

    public RandomGenerator(int length, Random random, String symbols) {
        if (length < 1) throw new IllegalArgumentException();
        if (symbols.length() < 2) throw new IllegalArgumentException();
        this.random = random;
        this.symbols = symbols.toCharArray();
        this.buf = new char[length];
    }

    /**
     * Create an numeric string generator.
     */
    public RandomGenerator(int length, Random random) {
        this(length, random, digits);
    }

    /**
     * Create an numeric strings from a secure generator.
     */
    public RandomGenerator(int length) {
        this(length, new SecureRandom());
    }

    /**
     * Create session identifiers.
     */
    public RandomGenerator() {
        this(21);
    }

}
