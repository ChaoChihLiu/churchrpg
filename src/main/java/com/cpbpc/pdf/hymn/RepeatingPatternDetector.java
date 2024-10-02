package com.cpbpc.pdf.hymn;

public class RepeatingPatternDetector {
    public static boolean hasRepeatingPattern(String str) {
        int n = str.length();

        // Loop over possible lengths of the repeating substring
        for (int len = 1; len <= n / 2; len++) {
            // Check if the string length is divisible by the candidate substring length
            if (n % len == 0) {
                // Get the candidate substring
                String substring = str.substring(0, len);

                // Build a new string by repeating the candidate substring
                StringBuilder repeated = new StringBuilder();
                for (int i = 0; i < n / len; i++) {
                    repeated.append(substring);
                }

                // Check if the built string matches the original string
                if (repeated.toString().equals(str)) {
                    return true; // Found a repeating pattern
                }
            }
        }

        return false; // No repeating pattern found
    }

    public static String getRepeatedWord(String str) {
        int n = str.length();

        // Loop over possible lengths of the repeating substring
        for (int len = 1; len <= n / 2; len++) {
            // Check if the string length is divisible by the candidate substring length
            if (n % len == 0) {
                // Get the candidate substring
                String substring = str.substring(0, len);

                // Build a new string by repeating the candidate substring
                StringBuilder repeated = new StringBuilder();
                for (int i = 0; i < n / len; i++) {
                    repeated.append(substring);
                }

                // Check if the built string matches the original string
                if (repeated.toString().equals(str)) {
                    return substring; // Found the repeating pattern
                }
            }
        }

        return ""; // No repeating pattern found
    }

    public static void main(String[] args) {
//        String str1 = "abababab";  // True
//        String str2 = "abcabcabc"; // True
//        String str3 = "abcd";      // False
//
//        System.out.println(hasRepeatingPattern(str1));  // Output: true
//        System.out.println(hasRepeatingPattern(str2));  // Output: true
//        System.out.println(hasRepeatingPattern(str3));  // Output: false
        System.out.println(getRepeatedWord("The Lord Is KingThe Lord Is KingThe Lord Is KingThe Lord Is KingThe Lord Is KingThe Lord Is King"));
    }
}

