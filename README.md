# Dental Clinic Appointment System (Java Swing)

Complete desktop app: login, staff booking, admin approval, dentist dashboard, archive.
No database or external libraries needed - data is saved in `data/appointments.tsv`.

## Run

Needs **JDK 11 or newer**.

* Windows: double-click `run.bat`
* Mac / Linux: `./run.sh`
* Or by hand: `javac -d out src/*.java` then `java -cp out Main`

NetBeans / Eclipse / VS Code: create a new Java project, copy everything in `src/` into it
(all classes use the default package) and run `Main`.

## Logins (Name / Password)

| Role | Name | Password |
|------|------|----------|
| Admin | `admin` | `admin123` |
| Staff | `staff` | `staff123` |
| Dentist - Divan Berdeju | `divan` | `divan123` |
| Dentist - Bernardo Dagatan | `bernardo` | `bernardo123` |
| Dentist - Mira Dagatan | `mira` | `mira123` |

Change names, passwords, dentists and prices at the top of `src/Store.java`.

## How it works

1. **Staff** -> *Appointment*: fills the booking board (personal information, one or more
   services with a live total, note, optional medical alert, dentist) and presses **BOOK**.
   The appointment starts as **Pending**. *View List* shows every appointment with search and filter.
2. **Admin** -> **APPROVE** (shows the patient note + medical alert), **CANCEL** (moves it to the
   archive), **RESCHEDULE**, **APPROVE VIEW** (service / note / medical details),
   **CANCEL VIEW**, **ARCHIVE** (Unarchive, Rebook).
3. **Dentist** sees only his / her own approved appointments, the note and an itemised receipt.
   **Done** marks the visit finished and moves it to the archive.

## Rules the program enforces

* Clinic hours 10:00 AM - 4:00 PM, every visit takes 30 minutes.
* A dentist cannot be double-booked, and the same patient cannot overlap with themselves.
* No past dates, no dates more than 2 years ahead; age is calculated from the birthdate.
* Names must be letters, phone numbers 7-15 digits.
* A dentist can only mark an appointment **Done** on or after its date.
* Unarchive: a Done visit returns as Approved, a Cancelled one returns as Pending.
  Rebook creates a brand-new Pending appointment (the archived one is kept).

## Pictures

The login and staff screens show a drawn tooth illustration. To use your own photos, put
`login.jpg` and `staff.jpg` (JPG or PNG content) into an `images/` folder next to where you run the app.

## Sample data

The first run adds a few demo appointments. To start empty set `SEED_DEMO_DATA = false` in
`Store.java` and delete the `data/` folder. Deleting `data/` always resets the system.
