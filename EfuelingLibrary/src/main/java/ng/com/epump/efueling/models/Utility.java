package ng.com.epump.efueling.models;

import java.security.SecureRandom;

public class Utility {
    public static String convert2DecimalString(double value, boolean groupThousands) {
        if (groupThousands) {
            return String.format("%,.2f", value);
        } else {
            return String.format("%.2f", value);
        }
    }

    public static String padPassword(String password){
        RandomGenerator rdg = new RandomGenerator(1, new SecureRandom());
        StringBuilder pass = new StringBuilder(rdg.nextString());

        String[] passSplit = password.split("(?<=\\G.{2})");
        for (String s: passSplit) {
            pass.append(s).append(rdg.nextString());
        }
        return pass.toString();
    }
}
