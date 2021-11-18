package ng.com.epump.efueling.models;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public static String parseDate(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static String parseDate(String csDate,  String pattern){
        if (csDate == null){
            return "";
        }
        String[] possibleDateFormats =
                {
                        "yyyy.MM.dd G 'at' HH:mm:ss z",
                        "EEE, MMM d, ''yy",
                        "h:mm a",
                        "hh 'o''clock' a, zzzz",
                        "K:mm a, z",
                        "yyyyy.MMMMM.dd GGG hh:mm aaa",
                        "EEE, d MMM yyyy HH:mm:ss Z",
                        "yyMMddHHmmssZ",
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                        "YYYY-'W'ww-u",
                        "EEE, dd MMM yyyy HH:mm:ss z",
                        "EEE, dd MMM yyyy HH:mm zzzz",
                        "yyyy-MM-dd'T'HH:mm:ssZ",
                        "yyyy-MM-dd'T'HH:mm:ss.SSSzzzz",
                        "yyyy-MM-dd'T'HH:mm:sszzzz",
                        "yyyy-MM-dd'T'HH:mm:ss z",
                        "yyyy-MM-dd'T'HH:mm:ssz",
                        "yyyy-MM-dd'T'HH:mm:ss",
                        "yyyy-MM-dd'T'HHmmss.SSSz",
                        "yyyy-MM-dd",
                        "yyyyMMdd",
                        "MM/dd/yy",
                        "MM/dd/yyyy"
                };
        for (String formatString : possibleDateFormats)
        {
            try
            {
                DateFormat inputFormat = new SimpleDateFormat(formatString);
                Date date = inputFormat.parse(csDate);
                return new SimpleDateFormat(pattern, Locale.US).format(date);
            }
            catch (ParseException | IllegalArgumentException e) {

            }
        }
        return "";
    }

    public static String lefPadZero(String data, int length){
        return String.format("%" + length + "s", data).replace(' ', '0');
    }

    public static String maskPan(String data){
        String mask = data.substring(6, data.length()-4);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < mask.length(); i++) {
            sb.append("*");
        }
        return data.replace(mask, sb.toString());
    }

    public static String getPrintHtmlFromAsset(Context context, String fileName){
        InputStream is = null;
        try {
            is = context.getAssets().open(fileName);
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getEpumpLogoBitmapFromAsset(Context context) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open("print_logo.png");
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
