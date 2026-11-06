import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * All clinic data: dentists, services and prices, user accounts and the appointments
 * (saved to data/appointments.tsv after every change).
 */
final class Store {
    private Store() {}

    // ------------------------------------------------------------ settings

    /** Put some sample appointments in the very first run so the screens are not empty. */
    static final boolean SEED_DEMO_DATA = true;

    static final String[] DENTISTS = {"Divan Berdeju", "Bernardo Dagatan", "Mira Dagatan"};

    /** Clinic hours in minutes since midnight (10:00 AM - 4:00 PM) and length of one visit. */
    static final int OPEN = 10 * 60;
    static final int CLOSE = 16 * 60;
    static final int SLOT = 30;
    static final int MAX_YEARS_AHEAD = 2;

    static String hoursText() {
        return Util.fmtTime(OPEN) + " - " + Util.fmtTime(CLOSE);
    }

    // ------------------------------------------------------------ services

    static final class Service {
        final String name;
        final int price;

        Service(String name, int price) {
            this.name = name;
            this.price = price;
        }

        @Override public String toString() { return name; }
    }

    /** Edit the prices (in pesos) here. */
    static final List<Service> SERVICES = Collections.unmodifiableList(Arrays.asList(
            new Service("Cleaning", 800),
            new Service("Dental Check-up", 500),
            new Service("Dental Filling", 1400),
            new Service("Tooth Extraction", 1000),
            new Service("Teeth Whitening", 3000),
            new Service("Teeth Brace", 25000),
            new Service("Brace Adjust", 1500),
            new Service("Root Canal", 5000),
            new Service("Veneer", 8000),
            new Service("Teeth X-ray", 600),
            new Service("Scaling", 900),
            new Service("Polishing", 700),
            new Service("Fluoride", 600)));

    static int priceOf(String serviceName) {
        for (Service s : SERVICES) if (s.name.equals(serviceName)) return s.price;
        return 0;
    }

    // --------------------------------------------------------------- users

    enum Role { ADMIN, STAFF, DENTIST }

    static final class User {
        final String username;
        final String password;
        final Role role;
        final String dentist;   // only for dentists

        User(String username, String password, Role role, String dentist) {
            this.username = username;
            this.password = password;
            this.role = role;
            this.dentist = dentist;
        }
    }

    /** Change the login names / passwords here. */
    private static final List<User> USERS = Collections.unmodifiableList(Arrays.asList(
            new User("admin", "admin123", Role.ADMIN, null),
            new User("staff", "staff123", Role.STAFF, null),
            new User("divan", "divan123", Role.DENTIST, DENTISTS[0]),
            new User("bernardo", "bernardo123", Role.DENTIST, DENTISTS[1]),
            new User("mira", "mira123", Role.DENTIST, DENTISTS[2])));

    static User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        for (User u : USERS) {
            if (u.username.equalsIgnoreCase(username.trim()) && u.password.equals(password)) return u;
        }
        return null;
    }

    // --------------------------------------------------------- appointments

    private static final List<Appointment> ALL = new ArrayList<>();
    private static int nextId = 1;
    private static final Path DIR = Paths.get("data");
    private static final Path FILE = DIR.resolve("appointments.tsv");
    private static String lastError = "";

    private static final Comparator<Appointment> BY_ID = Comparator.comparingInt((Appointment a) -> a.id);
    private static final Comparator<Appointment> BY_SCHEDULE = Comparator
            .comparing((Appointment a) -> a.date)
            .thenComparingInt((Appointment a) -> a.minutes)
            .thenComparingInt((Appointment a) -> a.id);

    static String lastError() { return lastError; }

    static Appointment add(Appointment a) {
        a.id = nextId++;
        ALL.add(a);
        return a;
    }

    static List<Appointment> query(Predicate<Appointment> filter, boolean sortById) {
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : ALL) if (filter.test(a)) out.add(a);
        out.sort(sortById ? BY_ID : BY_SCHEDULE);
        return out;
    }

    static int count(Predicate<Appointment> filter) {
        int n = 0;
        for (Appointment a : ALL) if (filter.test(a)) n++;
        return n;
    }

    // -------------------------------------------------------- slot checking

    /**
     * Returns a message when the dentist (or the same patient) already has an active
     * appointment overlapping this time, otherwise null.
     */
    static String findConflict(String dentist, LocalDate date, int minutes, String patientKey, int ignoreId) {
        for (Appointment o : ALL) {
            if (o.id == ignoreId || o.archived || !o.isActive() || !o.date.equals(date)) continue;
            if (Math.abs(o.minutes - minutes) >= SLOT) continue;
            if (o.dentist.equals(dentist)) {
                return dentist + " already has an appointment at " + o.timeText() + " on " + o.dateText()
                        + ".\nEach visit takes " + SLOT + " minutes - please pick another time or dentist.";
            }
            if (o.patientKey().equals(patientKey)) {
                return o.patientName() + " already has an appointment at " + o.timeText() + " on "
                        + o.dateText() + " with " + o.dentist + ".";
            }
        }
        return null;
    }

    /** Full check for a new / moved appointment. Returns an error message or null when fine. */
    static String validateSlot(LocalDate date, int minutes, String dentist, String patientKey, int ignoreId) {
        if (date == null) return "Please choose a valid appointment date (MM/dd/yyyy).";
        if (minutes < 0) return "Please enter a valid time, for example 1:30 PM.";
        if (minutes < OPEN || minutes > CLOSE) return "The clinic is open from " + hoursText() + ".";
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) return "The appointment date cannot be in the past.";
        if (date.isAfter(today.plusYears(MAX_YEARS_AHEAD))) {
            return "Appointments can only be booked up to " + MAX_YEARS_AHEAD + " years ahead.";
        }
        if (date.equals(today)) {
            LocalTime now = LocalTime.now();
            if (minutes <= now.getHour() * 60 + now.getMinute()) return "That time has already passed today.";
        }
        return findConflict(dentist, date, minutes, patientKey, ignoreId);
    }

    // ---------------------------------------------------------- persistence

    static void load() {
        ALL.clear();
        nextId = 1;
        if (!Files.exists(FILE)) {
            if (SEED_DEMO_DATA) seed();
            save();
            return;
        }
        try {
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) continue;
                try {
                    Appointment a = parse(line);
                    ALL.add(a);
                    nextId = Math.max(nextId, a.id + 1);
                } catch (RuntimeException e) {
                    System.err.println("Skipping unreadable line in " + FILE + ": " + line);
                }
            }
        } catch (IOException e) {
            lastError = e.toString();
            System.err.println("Could not read " + FILE + ": " + e);
        }
    }

    static boolean save() {
        try {
            Files.createDirectories(DIR);
            List<String> lines = new ArrayList<>();
            for (Appointment a : ALL) lines.add(format(a));
            Path tmp = DIR.resolve("appointments.tsv.tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, FILE, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            lastError = e.toString();
            return false;
        }
    }

    private static String format(Appointment a) {
        String[] f = {
                String.valueOf(a.id), a.firstName, a.lastName, a.gender, String.valueOf(a.age),
                a.birthDate == null ? "" : a.birthDate.toString(), a.contact, a.dentist,
                String.join("|", a.services), a.date.toString(), String.valueOf(a.minutes),
                a.note, a.medical, a.status, String.valueOf(a.archived)};
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < f.length; i++) {
            if (i > 0) b.append('\t');
            b.append(esc(f[i]));
        }
        return b.toString();
    }

    private static Appointment parse(String line) {
        String[] f = line.split("\t", -1);
        if (f.length < 15) throw new IllegalArgumentException("too few fields");
        for (int i = 0; i < f.length; i++) f[i] = unesc(f[i]);
        Appointment a = new Appointment();
        a.id = Integer.parseInt(f[0]);
        a.firstName = f[1];
        a.lastName = f[2];
        a.gender = f[3];
        a.age = Integer.parseInt(f[4]);
        a.birthDate = f[5].isEmpty() ? null : LocalDate.parse(f[5]);
        a.contact = f[6];
        a.dentist = f[7];
        for (String s : f[8].split("\\|")) if (!s.isEmpty()) a.services.add(s);
        a.date = LocalDate.parse(f[9]);
        a.minutes = Integer.parseInt(f[10]);
        a.note = f[11];
        a.medical = f[12];
        a.status = f[13];
        a.archived = Boolean.parseBoolean(f[14]);
        return a;
    }

    private static String esc(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': b.append("\\\\"); break;
                case '\t': b.append("\\t"); break;
                case '\n': b.append("\\n"); break;
                case '\r': break;
                default: b.append(c);
            }
        }
        return b.toString();
    }

    private static String unesc(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                b.append(n == 't' ? '\t' : n == 'n' ? '\n' : n);
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    // ------------------------------------------------------------ demo data

    private static void seed() {
        LocalDate t = LocalDate.now();
        demo("Maria", "Santos", "Female", 29, "09171234567", DENTISTS[0], t.plusDays(1), 10 * 60 + 30,
                Appointment.APPROVED, false, "First visit", "", "Cleaning", "Dental Check-up");
        demo("John", "Reyes", "Male", 34, "09181234567", DENTISTS[1], t.plusDays(1), 13 * 60,
                Appointment.PENDING, false, "", "", "Teeth Whitening");
        demo("Ana", "Cruz", "Female", 12, "09191234567", DENTISTS[2], t.plusDays(2), 11 * 60,
                Appointment.PENDING, false, "Accompanied by mother", "Allergic to penicillin", "Dental Check-up");
        demo("Paolo", "Lim", "Male", 41, "09201234567", DENTISTS[0], t, 15 * 60,
                Appointment.APPROVED, false, "", "High blood pressure", "Root Canal");
        demo("Liza", "Gomez", "Female", 52, "09211234567", DENTISTS[1], t.minusDays(3), 10 * 60 + 30,
                Appointment.DONE, true, "", "", "Dental Filling", "Polishing");
        demo("Mark", "Tan", "Male", 23, "09221234567", DENTISTS[2], t.minusDays(1), 14 * 60,
                Appointment.CANCELLED, true, "Patient called to cancel", "", "Tooth Extraction");
    }

    private static void demo(String first, String last, String gender, int age, String contact, String dentist,
                             LocalDate date, int minutes, String status, boolean archived,
                             String note, String medical, String... services) {
        Appointment a = new Appointment();
        a.firstName = first;
        a.lastName = last;
        a.gender = gender;
        a.age = age;
        a.birthDate = LocalDate.now().minusYears(age).minusDays(30);
        a.contact = contact;
        a.dentist = dentist;
        a.services = new ArrayList<>(Arrays.asList(services));
        a.date = date;
        a.minutes = minutes;
        a.note = note;
        a.medical = medical;
        a.status = status;
        a.archived = archived;
        add(a);
    }
}
