import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RestaurantAppGUI extends JFrame {

    // Core Logic Manager
    private final WaitlistManager manager = new WaitlistManager();

    // GUI Components
    private JList<Reservation> waitlistJList;
    private DefaultListModel<Reservation> listModel;

    // Input Fields
    private JTextField nameField, sizeField, phoneField;
    private JCheckBox vipCheckBox;
    private JTextArea notesArea;

    // New Reservation/Booking Fields
    private JComboBox<Integer> dayCombo, hourCombo, minuteCombo;
    private JCheckBox isReservationCheckBox;

    // EWT Display
    private JLabel ewtLabel;

    public RestaurantAppGUI() {
        super("Restaurant Waitlist System (DS Queue Project)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        add(createInputPanel());
        add(createWaitlistPanel());
        add(createActionPanel());

        // Timer to refresh the list every 15 seconds (to update live wait times)
        new Timer(15000, e -> updateWaitlistDisplay()).start();

        updateWaitlistDisplay();

        pack();
        setSize(700, 750); // Increased size for new fields
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add Walk-in/Reservation (Enqueue)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(20);
        sizeField = new JTextField("2", 5);
        phoneField = new JTextField(20);
        vipCheckBox = new JCheckBox("VIP Customer");
        notesArea = new JTextArea(2, 20);
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        // --- New: Reservation Time Controls Setup ---
        LocalDateTime now = LocalDateTime.now();
        // Populate days for today, tomorrow, and day after
        Integer[] days = { now.getDayOfMonth(), now.plusDays(1).getDayOfMonth(), now.plusDays(2).getDayOfMonth() };
        dayCombo = new JComboBox<>(days);
        // Populate hours (e.g., from current hour up to 23)
        int startHour = now.getHour(); // e.g., 8

        Integer[] hours = IntStream.range(0, 24)
                .map(i -> (startHour + i) % 24) // Calculate (current hour + offset) modulo 24
                .boxed()
                .toArray(Integer[]::new);
        hourCombo = new JComboBox<>(hours);
        Integer[] minutes = { 0, 15, 30, 45 };
        minuteCombo = new JComboBox<>(minutes);
        isReservationCheckBox = new JCheckBox("Future Booking");

        isReservationCheckBox.addActionListener(e -> {
            boolean enabled = isReservationCheckBox.isSelected();
            dayCombo.setEnabled(enabled);
            hourCombo.setEnabled(enabled);
            minuteCombo.setEnabled(enabled);
            if (enabled)
                hourCombo.setSelectedIndex(0); // Select current hour by default
        });

        dayCombo.setEnabled(false);
        hourCombo.setEnabled(false);
        minuteCombo.setEnabled(false);
        // --- End New Controls Setup ---

        // --- UI Layout ---
        // Row 1: Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        panel.add(nameField, gbc);
        gbc.gridwidth = 1;

        // Row 2: Size & Phone
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Party Size:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panel.add(sizeField, gbc);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panel.add(phoneField, gbc);

        // Row 3: Notes
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Notes/Requests:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.ipady = 30; // Make the notes area taller
        panel.add(notesScrollPane, gbc);
        gbc.ipady = 0;
        gbc.gridwidth = 1;

        // Row 4 (NEW): Reservation Controls
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(isReservationCheckBox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        panel.add(new JLabel("Day:"), gbc);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        panel.add(dayCombo, gbc);

        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        panel.add(new JLabel("Time:"), gbc);
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        timePanel.add(hourCombo);
        timePanel.add(new JLabel(":"));
        timePanel.add(minuteCombo);
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(timePanel, gbc);
        gbc.gridwidth = 1;

        // Row 5: VIP & Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(vipCheckBox, gbc);

        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(createAddButton(), gbc);

        return panel;
    }

    private JButton createAddButton() {
        JButton addButton = new JButton("Add to Waitlist (Enqueue)");
        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                int size = Integer.parseInt(sizeField.getText().trim());
                boolean isVip = vipCheckBox.isSelected();
                String phone = phoneField.getText().trim();
                String notes = notesArea.getText().trim();

                if (name.isEmpty() || size <= 0) {
                    JOptionPane.showMessageDialog(this, "Name and Party Size are required.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalDateTime bookingTime;
                if (isReservationCheckBox.isSelected()) {
                    // Calculate the correct date based on the day combo box selection
                    LocalDate date = LocalDate.now();
                    if (dayCombo.getSelectedIndex() == 1)
                        date = date.plusDays(1);
                    if (dayCombo.getSelectedIndex() == 2)
                        date = date.plusDays(2);

                    LocalTime time = LocalTime.of((int) hourCombo.getSelectedItem(),
                            (int) minuteCombo.getSelectedItem());
                    bookingTime = LocalDateTime.of(date, time);

                    if (bookingTime.isBefore(LocalDateTime.now().minusMinutes(1))) {
                        JOptionPane.showMessageDialog(this, "Booking time cannot be in the past.", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    // Walk-in (current time)
                    bookingTime = LocalDateTime.now();
                }

                Reservation newRes = new Reservation(name, size, bookingTime, isVip, phone, notes);
                manager.addReservation(newRes);

                // Clear input fields after adding
                nameField.setText("");
                sizeField.setText("2");
                phoneField.setText("");
                notesArea.setText("");
                vipCheckBox.setSelected(false);
                isReservationCheckBox.setSelected(false);
                dayCombo.setEnabled(false);
                hourCombo.setEnabled(false);
                minuteCombo.setEnabled(false);

                updateWaitlistDisplay();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Party size must be a valid number.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        return addButton;
    }

    private JPanel createWaitlistPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Current Waiting List (PriorityQueue)"));

        listModel = new DefaultListModel<>();
        waitlistJList = new JList<>(listModel);
        waitlistJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        waitlistJList.setCellRenderer(new ReservationListCellRenderer());

        // --- Right-click Menu for Actions (Check-In) ---
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem checkInItem = new JMenuItem("Check-In Party");
        checkInItem.addActionListener(this::checkInAction);
        popupMenu.add(checkInItem);

        waitlistJList.setComponentPopupMenu(popupMenu);

        panel.add(new JScrollPane(waitlistJList), BorderLayout.CENTER);

        // Add EWT Display at the bottom of the Waitlist Panel
        ewtLabel = new JLabel(" Estimated Wait Time: N/A", SwingConstants.CENTER);
        ewtLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(ewtLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton seatButton = new JButton("Seat Next Party (Dequeue)");

        seatButton.addActionListener(this::seatNextAction);
        panel.add(seatButton);
        return panel;
    }

    private void checkInAction(ActionEvent e) {
        int index = waitlistJList.getSelectedIndex();
        if (index == -1)
            return;

        Reservation selected = listModel.getElementAt(index);

        if (selected.getStatus() == ReservationStatus.PENDING) {
            // Must remove and re-add to re-sort the PriorityQueue
            manager.updateReservationStatus(selected, ReservationStatus.CHECKED_IN);
            JOptionPane.showMessageDialog(this, selected.getName() + " has been checked in! (Now in Waitlist)",
                    "Checked In", JOptionPane.INFORMATION_MESSAGE);
            updateWaitlistDisplay();
        } else if (selected.getStatus() == ReservationStatus.CHECKED_IN) {
            JOptionPane.showMessageDialog(this, selected.getName() + " is already checked in and waiting.", "Status",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, selected.getName() + " is already " + selected.getStatus() + ".",
                    "Status", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void seatNextAction(ActionEvent e) {
        Reservation seatedParty = manager.seatNext();

        if (seatedParty != null) {
            String message = String.format("%s (ID:%d, Size:%d) has been seated!",
                    seatedParty.getName(), seatedParty.getId(), seatedParty.getPartySize());
            if (!seatedParty.getNotes().isEmpty()) {
                message += "\nNotes: " + seatedParty.getNotes();
            }
            JOptionPane.showMessageDialog(this, message, "Party Seated", JOptionPane.INFORMATION_MESSAGE);
            updateWaitlistDisplay();
        } else {
            JOptionPane.showMessageDialog(this,
                    "The waitlist is currently empty (or only contains PENDING reservations).", "Empty Queue",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateWaitlistDisplay() {
        listModel.clear();
        Reservation[] waiting = manager.getAllWaiting();
        for (Reservation res : waiting) {
            listModel.addElement(res);
        }
        setTitle(String.format("Restaurant Waitlist System | Total Waiting: %d", manager.getQueueSize()));

        // --- EWT Calculation (Simple Placeholder Logic) ---
        // Only count parties that are actively waiting (CHECKED_IN)
        long waitingCount = Arrays.stream(waiting)
                .filter(r -> r.getStatus() == ReservationStatus.CHECKED_IN)
                .count();

        // Find the longest wait time in the queue to inform the EWT
        long longestWait = Arrays.stream(waiting)
                .filter(r -> r.getStatus() == ReservationStatus.CHECKED_IN)
                .mapToLong(Reservation::getMinutesWaiting)
                .max().orElse(0);

        // Simple EWT: Longest wait time + a buffer (e.g., 10 minutes)
        // A production EWT would be based on table turnover rate.
        long ewt = longestWait + 10;

        ewtLabel.setText(String.format(" Estimated Wait Time for Walk-ins: ~%d minutes (%d actively waiting)", ewt,
                waitingCount));
    }

    /**
     * The essential main method that starts the application.
     */
    public static void main(String[] args) {
        // Ensure the GUI is created and updated on the Event-Dispatching Thread (EDT)
        SwingUtilities.invokeLater(RestaurantAppGUI::new);
    }
}

// --- 2. Custom Renderer for Visual Priority ---
// This is the class that handles the visual display (colors, bold text) in the
// list.
class ReservationListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof Reservation) {
            Reservation res = (Reservation) value;

            // Set background color based on status/priority
            Color bgColor;
            if (res.getStatus() == ReservationStatus.PENDING) {
                bgColor = new Color(220, 240, 255); // Light Blue for PENDING Reservation
            } else if (res.isVIP()) {
                bgColor = new Color(255, 230, 230); // Light Pink for VIP CHECKED_IN
            } else {
                bgColor = list.getBackground();
            }
            label.setBackground(bgColor);

            // Bold text for parties with notes
            if (res.getNotes() != null && !res.getNotes().isEmpty()) {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            } else {
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            }
        }

        // Handle selection coloring correctly
        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        }

        return label;
    }
}
