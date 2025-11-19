package com.ieltsgrading.ielts_evaluator.util;

/**
 * Utility class for rounding IELTS scores according to official IELTS rules
 * 
 * IELTS Band Score Scale: 0.0, 0.5, 1.0, 1.5, 2.0, ..., 8.5, 9.0
 * 
 * Rounding Rules:
 * - Decimal < 0.25: round DOWN to .0
 * - Decimal 0.25 - 0.74: round to .5
 * - Decimal >= 0.75: round UP to next .0
 * 
 * Examples:
 * - 6.1 → 6.0
 * - 6.24 → 6.0
 * - 6.25 → 6.5
 * - 6.5 → 6.5
 * - 6.74 → 6.5
 * - 6.75 → 7.0
 * - 6.8 → 7.0
 */
public class IeltsScoreRounder {

    /**
     * Round score to nearest IELTS band (0.5 increments)
     * 
     * @param score Raw score
     * @return Rounded score (e.g., 6.0, 6.5, 7.0)
     */
    public static Double roundToIeltsBand(Double score) {
        if (score == null) {
            return null;
        }

        // Ensure score is within valid range [0.0, 9.0]
        if (score < 0.0) {
            return 0.0;
        }
        if (score > 9.0) {
            return 9.0;
        }

        // Get integer part and decimal part
        int integerPart = (int) Math.floor(score);
        double decimalPart = score - integerPart;

        // Apply IELTS rounding rules
        if (decimalPart < 0.25) {
            // Round down to .0
            return (double) integerPart;
        } else if (decimalPart < 0.75) {
            // Round to .5
            return integerPart + 0.5;
        } else {
            // Round up to next .0
            return (double) (integerPart + 1);
        }
    }

    /**
     * Calculate average of multiple IELTS scores and round the result
     * 
     * @param scores Array of scores
     * @return Rounded average
     */
    public static Double calculateAverageIeltsScore(Double... scores) {
        if (scores == null || scores.length == 0) {
            return null;
        }

        double sum = 0;
        int count = 0;
        
        for (Double score : scores) {
            if (score != null) {
                sum += score;
                count++;
            }
        }

        if (count == 0) {
            return null;
        }

        double average = sum / count;
        return roundToIeltsBand(average);
    }

    /**
     * Format score for display (e.g., "6.5", "7.0")
     * 
     * @param score Score to format
     * @return Formatted string
     */
    public static String formatIeltsScore(Double score) {
        if (score == null) {
            return "N/A";
        }
        return String.format("%.1f", score);
    }

    /**
     * Get band descriptor for a given score
     * 
     * @param score IELTS band score
     * @return Band descriptor
     */
    public static String getBandDescriptor(Double score) {
        if (score == null) {
            return "Not Available";
        }
        
        if (score >= 9.0) return "Expert User";
        if (score >= 8.0) return "Very Good User";
        if (score >= 7.0) return "Good User";
        if (score >= 6.0) return "Competent User";
        if (score >= 5.0) return "Modest User";
        if (score >= 4.0) return "Limited User";
        if (score >= 3.0) return "Extremely Limited User";
        if (score >= 2.0) return "Intermittent User";
        if (score >= 1.0) return "Non User";
        return "Did Not Attempt";
    }

    /**
     * Check if a score is a valid IELTS band score
     * 
     * @param score Score to validate
     * @return true if valid (0.0, 0.5, 1.0, ..., 9.0), false otherwise
     */
    public static boolean isValidIeltsBand(Double score) {
        if (score == null) {
            return false;
        }
        
        // Must be between 0.0 and 9.0
        if (score < 0.0 || score > 9.0) {
            return false;
        }
        
        // Must be a multiple of 0.5
        double decimal = score - Math.floor(score);
        return decimal == 0.0 || decimal == 0.5;
    }

    /**
     * Round up to the next half band
     * (Useful for minimum requirements)
     * 
     * @param score Raw score
     * @return Rounded up score
     */
    public static Double roundUpToNextHalfBand(Double score) {
        if (score == null) {
            return null;
        }
        
        if (score < 0.0) return 0.0;
        if (score > 9.0) return 9.0;
        
        double rounded = Math.ceil(score * 2) / 2.0;
        return Math.min(rounded, 9.0);
    }

    /**
     * Round down to the previous half band
     * 
     * @param score Raw score
     * @return Rounded down score
     */
    public static Double roundDownToHalfBand(Double score) {
        if (score == null) {
            return null;
        }
        
        if (score < 0.0) return 0.0;
        if (score > 9.0) return 9.0;
        
        return Math.floor(score * 2) / 2.0;
    }
}