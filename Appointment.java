import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** One appointment (booking) in the clinic. */
final class Appointment {
    static final String PENDING = "Pending";
    static final String APPROVED = "Approved";
    static final String CANCELLED = "Cancelled";
    static final String DONE = "Done";

    int id;
    String firstName = "";
    String lastName = "";
    String gender = "";
    int age;
    LocalDate birthDate;          // may be null
    String contact = "";
    String dentist = "";
    List<String> services = new ArrayList<>();
    LocalDate date;
    int minutes;                  // minutes since midnight
    String note = "";
    String medical = "";
    String status = PENDING;
    boolean archived;

    String patientName() {
        return (firstName + " " + lastName).trim();
    }

    /** Lower-case "first last", used to detect the same patient booked twice. */
    String patientKey() {
        return (firstName.trim() + " " + lastName.trim()).toLowerCase(java.util.Locale.ROOT);
    }

    String servicesText() {
        return String.join(", ", services);
    }

    int total() {
        int sum = 0;
        for (String s : services) sum += Store.priceOf(s);
        return sum;
    }

    String dateText() {
        return Util.fmtDate(date);
    }

    String timeText() {
        return Util.fmtTime(minutes);
    }

    /** Pending or Approved: still occupies the dentist's time slot. */
    boolean isActive() {
        return PENDING.equals(status) || APPROVED.equals(status);
    }

    /** "note | Medical alert: text" (empty string when there is neither). */
    String combinedNote() {
        StringBuilder b = new StringBuilder();
        if (!note.isBlank()) b.append(note.trim());
        if (!medical.isBlank()) {
            if (b.length() > 0) b.append(" | ");
            b.append("Medical alert: ").append(medical.trim());
        }
        return b.toString();
    }

    /** A fresh Pending copy of this appointment (used for "Rebook"). */
    Appointment copyFor(String newDentist, LocalDate newDate, int newMinutes) {
        Appointment a = new Appointment();
        a.firstName = firstName;
        a.lastName = lastName;
        a.gender = gender;
        a.age = age;
        a.birthDate = birthDate;
        a.contact = contact;
        a.dentist = newDentist;
        a.services = new ArrayList<>(services);
        a.date = newDate;
        a.minutes = newMinutes;
        a.note = note;
        a.medical = medical;
        a.status = PENDING;
        a.archived = false;
        return a;
    }
}
