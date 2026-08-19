package com.rc.Bank_Service.util;

/**
 * Jaro-Winkler String Distance Metric Algorithm.
 * Measures string similarity between customer entered name and PAN tax record name.
 */
public class JaroWinklerDistance {

    public static double compareNames(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        
        String str1 = s1.trim().toUpperCase().replaceAll("[^A-Z ]", "");
        String str2 = s2.trim().toUpperCase().replaceAll("[^A-Z ]", "");

        if (str1.equals(str2)) return 1.0;

        double jaro = jaroDistance(str1, str2);
        int prefix = 0;
        int maxPrefix = 4;

        for (int i = 0; i < Math.min(Math.min(str1.length(), str2.length()), maxPrefix); i++) {
            if (str1.charAt(i) == str2.charAt(i)) {
                prefix++;
            } else {
                break;
            }
        }

        return jaro + (prefix * 0.1 * (1.0 - jaro));
    }

    private static double jaroDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        if (len1 == 0 || len2 == 0) return 0.0;

        int matchDistance = Math.max(len1, len2) / 2 - 1;
        if (matchDistance < 0) matchDistance = 0;

        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];

        int matches = 0;
        int transpositions = 0;

        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);

            for (int j = start; j < end; j++) {
                if (s2Matches[j]) continue;
                if (s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }

        if (matches == 0) return 0.0;

        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }

        double m = matches;
        return (m / len1 + m / len2 + (m - transpositions / 2.0) / m) / 3.0;
    }
}
