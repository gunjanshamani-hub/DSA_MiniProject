import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class Reservation implements Comparable<Reservation> {
    private static int nextId = 1001;
    private final int id;
    private final String customerName;
    private final int partySize;
    private final LocalDateTime reservationTime; 
    private final LocalDateTime checkInTime;     
    private ReservationStatus status;            
    private final boolean isVIP;
    private final String phoneNumber;
    private final String notes;

    // Constructor for all reservations (walk-in or future)
    public Reservation(String name, int size, LocalDateTime time, boolean isVip, String phone, String notes) {
        this.id = nextId++;
        this.customerName = name;
        this.partySize = size;
        this.reservationTime = time;
        
        // If booking time is past or now, it's a walk-in/checked-in
        boolean isWalkInOrCurrent = !time.isAfter(LocalDateTime.now().plusMinutes(5));
        
        this.checkInTime = isWalkInOrCurrent ? time : null;
        this.status = isWalkInOrCurrent ? ReservationStatus.CHECKED_IN : ReservationStatus.PENDING;
        
        this.isVIP = isVip;
        this.phoneNumber = phone;
        this.notes = notes;
    }

    // --- Accessors and Mutators ---
    public int getId() { return id; }
    public String getName() { return customerName; }
    public int getPartySize() { return partySize; }
    public LocalDateTime getReservationTime() { return reservationTime; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public boolean isVIP() { return isVIP; }
    public String getNotes() { return notes; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public void checkIn() {
        // In a real application, you would create a NEW object with the current checkInTime
        // For simplicity, since the checkInTime is final, we only update the status.
        // The manager handles the PriorityQueue re-sort.
        if (this.status == ReservationStatus.PENDING) {
            this.status = ReservationStatus.CHECKED_IN;
        }
    }
    
    public long getMinutesWaiting() {
        if (status == ReservationStatus.CHECKED_IN && checkInTime != null) {
            return ChronoUnit.MINUTES.between(checkInTime, LocalDateTime.now());
        }
        return 0;
    }

    /**
     * Priority Logic:
     * 1. CHECKED_IN parties are higher priority than PENDING reservations.
     * 2. Among CHECKED_IN: VIPs > Larger Size > Longest Wait.
     * 3. Among PENDING: Earliest Reservation Time first.
     */
    @Override
    public int compareTo(Reservation other) {
        boolean thisIsWaiting = this.status == ReservationStatus.CHECKED_IN;
        boolean otherIsWaiting = other.status == ReservationStatus.CHECKED_IN;

        // 1. Status Filter
        if (thisIsWaiting && !otherIsWaiting) return -1; // This (waiting) has higher priority
        if (!thisIsWaiting && otherIsWaiting) return 1;  // Other (waiting) has higher priority
        
        // If both are PENDING or both are SEATED/CANCELED, sort by reservation time
        if (!thisIsWaiting && !otherIsWaiting) {
            return this.reservationTime.compareTo(other.reservationTime); // Earliest reservation first
        }

        // --- Active Waitlist Priority (Both are CHECKED_IN) ---

        // VIP Priority
        if (this.isVIP() && !other.isVIP()) return -1; 
        if (!this.isVIP() && other.isVIP()) return 1;

        // Party Size Priority (Larger parties first)
        int sizeComparison = Integer.compare(other.getPartySize(), this.getPartySize());
        if (sizeComparison != 0) return sizeComparison;

        // Time Priority (Longest wait time first - FIFO)
        return this.checkInTime.compareTo(other.checkInTime);
    }

    @Override
    public String toString() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String vipStar = this.isVIP ? " ⭐ VIP" : "";
        String notesIcon = this.notes != null && !this.notes.isEmpty() ? " 📝" : "";
        String statusIcon = this.status.icon;

        String timeInfo;
        if (this.status == ReservationStatus.PENDING) {
            timeInfo = String.format(" | Reserved: %s %s", 
                                      this.reservationTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                      this.reservationTime.format(timeFormatter));
        } else { // CHECKED_IN
            long waitTime = getMinutesWaiting();
            timeInfo = String.format(" | Waiting: %d min", waitTime);
        }

        return String.format("%s %s (%d guests)%s%s%s", 
                             statusIcon, this.customerName, this.partySize, vipStar, notesIcon, timeInfo);
    }
}
