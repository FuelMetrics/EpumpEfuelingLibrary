package ng.com.epump.efueling.models;

import java.security.SecureRandom;

public class Utility {
    public static boolean ConnectionStarted;
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

    public static String bytesToHexString(byte[] source){
        StringBuilder stringBuilder = new StringBuilder("");
        if (source == null || source.length <= 0){
            return  null;
        }
        char[] buffer = new char[2];
        for (byte src : source) {
            buffer[0] = Character.forDigit((src >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit((src) & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }
}
