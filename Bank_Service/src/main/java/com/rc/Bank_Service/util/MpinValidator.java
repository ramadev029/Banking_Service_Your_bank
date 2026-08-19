package com.rc.Bank_Service.util;

/**
 * Non-Trivial 6-Digit MPIN Validation Engine (SBI YONO Banking Standards).
 * Rejects predictable passcodes (sequential ascending, sequential descending, repetitive digits).
 */
public class MpinValidator {

    public static void validateNonTrivialMpin(String mpin) {
        if (mpin == null || !mpin.matches("^[0-9]{6}$")) {
            throw new IllegalArgumentException("MPIN must be exactly 6 numeric digits.");
        }

        // 1. Check for Repetitive Digits (e.g. 111111, 000000, 999999)
        char first = mpin.charAt(0);
        boolean allSame = true;
        for (int i = 1; i < 6; i++) {
            if (mpin.charAt(i) != first) {
                allSame = false;
                break;
            }
        }
        if (allSame) {
            throw new IllegalArgumentException("Trivial MPIN rejected! Repetitive digits (e.g. 111111, 000000) are not allowed.");
        }

        // 2. Check for Sequential Ascending (e.g. 123456, 234567, 012345)
        boolean seqAsc = true;
        for (int i = 0; i < 5; i++) {
            if (mpin.charAt(i + 1) - mpin.charAt(i) != 1) {
                seqAsc = false;
                break;
            }
        }
        if (seqAsc) {
            throw new IllegalArgumentException("Trivial MPIN rejected! Sequential numbers (e.g. 123456) are not allowed.");
        }

        // 3. Check for Sequential Descending (e.g. 654321, 765432)
        boolean seqDesc = true;
        for (int i = 0; i < 5; i++) {
            if (mpin.charAt(i) - mpin.charAt(i + 1) != 1) {
                seqDesc = false;
                break;
            }
        }
        if (seqDesc) {
            throw new IllegalArgumentException("Trivial MPIN rejected! Reverse sequential numbers (e.g. 654321) are not allowed.");
        }
    }
}
