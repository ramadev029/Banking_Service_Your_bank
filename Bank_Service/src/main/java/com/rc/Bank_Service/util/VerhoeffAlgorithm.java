package com.rc.Bank_Service.util;

/**
 * Mathematical Verhoeff Checksum Algorithm (Dihedral Group D5).
 * Validates 12-digit Indian Aadhaar Numbers in real-time.
 */
public class VerhoeffAlgorithm {

    private static final int[][] d = {
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
        {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
        {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
        {3, 4, 0, 1, 2, 8, 9, 5, 6, 7},
        {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
        {5, 9, 8, 7, 6, 0, 1, 2, 3, 4},
        {6, 5, 9, 8, 7, 1, 2, 3, 4, 0},
        {7, 6, 5, 9, 8, 2, 3, 4, 0, 1},
        {8, 7, 6, 5, 9, 3, 4, 0, 1, 2},
        {9, 8, 7, 6, 5, 4, 0, 1, 2, 3}
    };

    private static final int[][] p = {
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
        {1, 5, 7, 6, 2, 8, 3, 0, 9, 4},
        {5, 8, 0, 3, 7, 9, 6, 1, 4, 2},
        {8, 9, 1, 6, 0, 4, 3, 5, 2, 7},
        {9, 4, 5, 3, 1, 2, 6, 8, 7, 0},
        {4, 2, 8, 6, 5, 7, 3, 9, 0, 1},
        {2, 7, 9, 3, 8, 0, 6, 4, 1, 5},
        {7, 0, 4, 6, 9, 1, 3, 2, 5, 8}
    };

    private static final int[] inv = {0, 4, 3, 2, 1, 5, 6, 7, 8, 9};

    public static boolean validateAadhaar(String aadhaar) {
        if (aadhaar == null || !aadhaar.matches("^[2-9]{1}[0-9]{11}$")) {
            return false;
        }

        int c = 0;
        int[] myArray = new int[aadhaar.length()];
        for (int i = 0; i < aadhaar.length(); i++) {
            myArray[i] = Integer.parseInt(String.valueOf(aadhaar.charAt(i)));
        }

        for (int i = 0; i < myArray.length; i++) {
            c = d[c][p[(i % 8)][myArray[myArray.length - 1 - i]]];
        }

        return c == 0;
    }

    public static String generateValidAadhaar() {
        String base11 = String.valueOf((long)(Math.random() * 70000000000L) + 20000000000L);
        for (int k = 0; k <= 9; k++) {
            String candidate = base11 + k;
            if (validateAadhaar(candidate)) {
                return candidate;
            }
        }
        return base11 + "0";
    }
}
