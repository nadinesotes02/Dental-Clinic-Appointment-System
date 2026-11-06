import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small formatting / parsing / validation helpers. */
final class Util {
    private Util() {}

    private static final Pattern DATE_RX = Pattern.compile("^(\\d{1,2})/(\\d{1,2})/(\\d{4})$");
    private static final Pattern TIME_RX = Pattern.compile("^(\\d{1,2})(?:[:.](\\d{2}))?\\s*([AaPp][Mm])?$");
    private static final Pattern NAME_RX = Pattern.compile("^\\p{L}[\\p{L} .'\\-]*$");

    /** Parses MM/dd/yyyy (1 or 2 digit month/day). Returns null when invalid. */
    static LocalDate parseDate(String s) {
        if (s == null) return null;
        Matcher m = DATE_RX.matcher(s.trim());
        if (!m.matches()) return null;
        try {
            return LocalDate.of(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        } catch (DateTimeException e) {
            return null;
        }
    }

    static String fmtDate(LocalDate d) {
        return d == null ? "" : String.format(Locale.US, "%02d/%02d/%04d", d.getMonthValue(), d.getDayOfMonth(), d.getYear());
    }

    /** 810 -> "1:30 PM" (written by hand: JDK date formatters use odd spaces before AM/PM). */
    static String fmtTime(int minutes) {
        int h = minutes / 60, m = minutes % 60;
        int h12 = (h % 12 == 0) ? 12 : h % 12;
        return String.format(Locale.US, "%d:%02d %s", h12, m, h < 12 ? "AM" : "PM");
    }

    /**
     * Parses a time such as "1:30 PM", "13:30" or "1:30". Returns minutes since midnight or -1.
     * Without AM/PM (and without the explicit {@code ampm} hint) clinic hours are assumed:
     * 10 and 11 are morning, 12 is noon and 1-9 are afternoon.
     */
    static int parseTime(String s, String ampm) {
        if (s == null) return -1;
        Matcher m = TIME_RX.matcher(s.trim());
        if (!m.matches()) return -1;
        int h = Integer.parseInt(m.group(1));
        int min = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
        if (min > 59) return -1;
        String ap = m.group(3) != null ? m.group(3).toUpperCase(Locale.ROOT) : (ampm != null ? ampm.toUpperCase(Locale.ROOT) : null);
        if (ap != null) {
            if (h < 1 || h > 12) return -1;
            if (h == 12) h = 0;
            if (ap.equals("PM")) h += 12;
        } else {
            if (h > 23) return -1;
            if (h >= 1 && h <= 9) h += 12;      // 1:30 -> 1:30 PM
            else if (h == 0) return -1;
        }
        return h * 60 + min;
    }

    static boolean validName(String s) {
        return s != null && NAME_RX.matcher(s.trim()).matches();
    }

    static boolean validPhone(String s) {
        if (s == null) return false;
        return s.replaceAll("[\\s\\-()]", "").matches("^\\+?\\d{7,15}$");
    }

    static int ageOn(LocalDate birth, LocalDate on) {
        return Period.between(birth, on).getYears();
    }

    /** "PHP 1,400.00" */
    static String money(int pesos) {
        return String.format(Locale.US, "PHP %,.2f", (double) pesos);
    }

    static String html(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Multi-line summary of one appointment. */
    static String details(Appointment a) {
        StringBuilder b = new StringBuilder();
        b.append('#').append(a.id).append("  ").append(a.patientName())
                .append("  (").append(a.gender).append(", ").append(a.age).append(")\n");
        b.append("Contact: ").append(a.contact).append("    Dentist: ").append(a.dentist).append('\n');
        b.append("Schedule: ").append(a.dateText()).append(' ').append(a.timeText())
                .append("    Status: ").append(a.status).append('\n');
        b.append("Services: ").append(a.servicesText()).append("    Total: ").append(money(a.total())).append('\n');
        if (!a.note.isBlank()) b.append("Note: ").append(a.note.trim()).append('\n');
        if (!a.medical.isBlank()) b.append("Medical alert: ").append(a.medical.trim()).append('\n');
        return b.toString();
    }

    /** Services with prices and total, for text areas. */
    static String itemised(Appointment a) {
        StringBuilder b = new StringBuilder();
        for (String s : a.services) {
            b.append(s).append("  -  ").append(money(Store.priceOf(s))).append('\n');
        }
        b.append("\nTOTAL: ").append(money(a.total()));
        return b.toString();
    }

    /** Plain-text receipt (fixed width, shown in a monospaced text area). */
    static String receipt(Appointment a) {
        String line = "-".repeat(36) + "\n";
        StringBuilder b = new StringBuilder();
        b.append("APPOINTMENT RECEIPT\n").append(line);
        b.append("Receipt No: ").append(a.id).append('\n');
        b.append("Name: ").append(a.patientName()).append('\n');
        b.append("Gender: ").append(a.gender).append('\n');
        b.append("Age: ").append(a.age).append('\n');
        b.append("Dentist: ").append(a.dentist).append('\n');
        b.append("Date: ").append(a.dateText()).append('\n');
        b.append("Time: ").append(a.timeText()).append('\n');
        b.append("Status: ").append(a.status).append('\n');
        b.append(line).append("SERVICES\n");
        for (String s : a.services) {
            b.append(String.format(Locale.US, "%-22s%14s\n", s, money(Store.priceOf(s))));
        }
        b.append(line);
        b.append(String.format(Locale.US, "%-22s%14s\n", "TOTAL", money(a.total())));
        b.append(line).append("Thank you for choosing our clinic!\n");
        return b.toString();
    }
}
