import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The appointment table used on every screen (same columns as the original design). */
final class ApptTable extends JTable {
    private static final String[] COLUMNS = {
            "Appointment ID", "Dentist", "Patient Name", "Gender", "Age", "Service", "Date", "Time", "Status"};
    private static final int[] WIDTHS = {110, 150, 150, 70, 45, 200, 100, 85, 85};
    private static final int[] MIN_WIDTHS = {105, 110, 100, 60, 40, 110, 92, 78, 78};

    private final List<Appointment> rows = new ArrayList<>();
    private final DefaultTableModel model;

    ApptTable() {
        this(new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }

            @Override public Class<?> getColumnClass(int c) {
                return (c == 0 || c == 4) ? Integer.class : String.class;
            }
        });
    }

    private ApptTable(DefaultTableModel model) {
        super(model);
        this.model = model;
        Theme.styleTable(this);
        for (int i = 0; i < WIDTHS.length; i++) {
            getColumnModel().getColumn(i).setPreferredWidth(WIDTHS[i]);
            getColumnModel().getColumn(i).setMinWidth(MIN_WIDTHS[i]);
        }
    }

    /** Replaces the rows, keeping the same appointments selected when they are still listed. */
    void setData(List<Appointment> data) {
        Set<Integer> keep = new HashSet<>();
        for (Appointment a : selected()) keep.add(a.id);
        rows.clear();
        rows.addAll(data);
        model.setRowCount(0);
        for (Appointment a : data) {
            model.addRow(new Object[]{a.id, a.dentist, a.patientName(), a.gender, a.age,
                    a.servicesText(), a.dateText(), a.timeText(), a.status});
        }
        clearSelection();
        for (int i = 0; i < rows.size(); i++) {
            if (keep.contains(rows.get(i).id)) addRowSelectionInterval(i, i);
        }
    }

    List<Appointment> selected() {
        List<Appointment> out = new ArrayList<>();
        for (int i : getSelectedRows()) {
            if (i >= 0 && i < rows.size()) out.add(rows.get(i));
        }
        return out;
    }

    Appointment selectedOne() {
        List<Appointment> s = selected();
        return s.isEmpty() ? null : s.get(0);
    }
}
