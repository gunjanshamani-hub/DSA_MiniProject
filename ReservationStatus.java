public enum ReservationStatus {
    // PENDING: Reserved for a future time, not yet arrived.
    PENDING("⏳"), 
    
    // CHECKED_IN: Walk-in or future reservation that has arrived and is actively waiting.
    CHECKED_IN("✅"), 
    
    // SEATED: Party has been assigned a table.
    SEATED("🍽️"), 
    
    // CANCELED: Reservation was canceled.
    CANCELED("❌");

    public final String icon;

    ReservationStatus(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return icon + " " + this.name().replace("_", " ");
    }
}
