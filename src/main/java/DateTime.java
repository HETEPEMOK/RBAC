import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTime {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SIMPLE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static String getCurrentSimpleDateTime() {
        return LocalDateTime.now().format(SIMPLE_DATETIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        return date1.compareTo(date2) > 0;
    }

    public static boolean isEqual(String date1, String date2) {
        return date1.compareTo(date2) == 0;
    }

    public static boolean isBeforeOrEqual(String date1, String date2) {
        return date1.compareTo(date2) <= 0;
    }

    public static boolean isAfterOrEqual(String date1, String date2) {
        return date1.compareTo(date2) >= 0;
    }

    public static String addDays(String date, int days) {
        try {
            if (date.length() == 10) {
                LocalDate d = LocalDate.parse(date, DATE_FORMATTER);
                return d.plusDays(days).format(DATE_FORMATTER);
            } else if (date.length() == 16) {
                LocalDateTime dt = LocalDateTime.parse(date, SIMPLE_DATETIME_FORMATTER);
                return dt.plusDays(days).format(SIMPLE_DATETIME_FORMATTER);
            } else {
                LocalDateTime dt = LocalDateTime.parse(date, DATETIME_FORMATTER);
                return dt.plusDays(days).format(DATETIME_FORMATTER);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + date);
        }
    }

    public static String formatRelativeTime(String date) {
        try {
            LocalDateTime target;
            if (date.length() == 10) {
                target = LocalDate.parse(date, DATE_FORMATTER).atStartOfDay();
            } else if (date.length() == 16) {
                target = LocalDateTime.parse(date, SIMPLE_DATETIME_FORMATTER);
            } else {
                target = LocalDateTime.parse(date, DATETIME_FORMATTER);
            }

            LocalDateTime now = LocalDateTime.now();

            if (target.isBefore(now)) {
                long days = ChronoUnit.DAYS.between(target, now);
                if (days == 0) {
                    long hours = ChronoUnit.HOURS.between(target, now);
                    if (hours == 0) {
                        long minutes = ChronoUnit.MINUTES.between(target, now);
                        return minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
                    }
                    return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
                }
                return days + " day" + (days != 1 ? "s" : "") + " ago";
            } else {
                long days = ChronoUnit.DAYS.between(now, target);
                if (days == 0) {
                    long hours = ChronoUnit.HOURS.between(now, target);
                    if (hours == 0) {
                        long minutes = ChronoUnit.MINUTES.between(now, target);
                        return "in " + minutes + " minute" + (minutes != 1 ? "s" : "");
                    }
                    return "in " + hours + " hour" + (hours != 1 ? "s" : "");
                }
                return "in " + days + " day" + (days != 1 ? "s" : "");
            }
        } catch (Exception e) {
            return "unknown date";
        }
    }

    public static long daysBetween(String date1, String date2) {
        try {
            LocalDate d1 = LocalDate.parse(date1.substring(0, 10), DATE_FORMATTER);
            LocalDate d2 = LocalDate.parse(date2.substring(0, 10), DATE_FORMATTER);
            return ChronoUnit.DAYS.between(d1, d2);
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isExpired(String expirationDate) {
        return isBefore(expirationDate, getCurrentSimpleDateTime());
    }

    public static String getRemainingTime(String expirationDate) {
        try {
            LocalDateTime exp = LocalDateTime.parse(expirationDate, SIMPLE_DATETIME_FORMATTER);
            LocalDateTime now = LocalDateTime.now();

            if (exp.isBefore(now)) {
                return "Expired";
            }

            long days = ChronoUnit.DAYS.between(now, exp);
            long hours = ChronoUnit.HOURS.between(now, exp) % 24;
            long minutes = ChronoUnit.MINUTES.between(now, exp) % 60;

            if (days > 0) {
                return String.format("%d days %d hours", days, hours);
            } else if (hours > 0) {
                return String.format("%d hours %d minutes", hours, minutes);
            } else {
                return String.format("%d minutes", minutes);
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }
}