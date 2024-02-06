package dev.lemonclient.utils.misc;

import java.util.Random;

public class RandomUtils {
    public static long randomDelay(final int minDelay, final int maxDelay) {
        return RandomUtils.nextInt(minDelay, maxDelay);
    }

    public static int nextInt(int startInclusive, int endExclusive) {
        return (endExclusive - startInclusive <= 0) ? startInclusive : startInclusive + new Random().nextInt(endExclusive - startInclusive);
    }

    public static double nextDouble(double startInclusive, double endInclusive) {
        return (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0) ? startInclusive : startInclusive + (endInclusive - startInclusive) * Math.random();
    }

    public static float nextFloat(double startInclusive, double endInclusive) {
        return (float) ((startInclusive == endInclusive || endInclusive - startInclusive <= 0f) ? startInclusive : (startInclusive + (endInclusive - startInclusive) * Math.random()));
    }

    public static boolean nextBoolean() {
        return new Random().nextBoolean();
    }

    public static String randomNumber(int length) {
        return random(length, "123456789");
    }

    public static String randomString(int length) {
        return random(length, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    }

    public static String random(int length, String chars) {
        return random(length, chars.toCharArray());
    }

    public static String random(int length, char[] chars) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(chars[(new Random().nextInt(chars.length))]);
        }
        return stringBuilder.toString();
    }
}
