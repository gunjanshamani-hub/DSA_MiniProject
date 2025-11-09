import java.util.PriorityQueue;
import java.util.Arrays;

public class WaitlistManager {
    // PriorityQueue handles the sorting based on the Reservation's compareTo() method.
    private PriorityQueue<Reservation> waitlist = new PriorityQueue<>();

    public void addReservation(Reservation reservation) {
        // Enqueue: Add a new reservation/walk-in to the priority queue.
        waitlist.offer(reservation); 
    }

    public Reservation seatNext() {
        // Dequeue: Remove and return the highest priority reservation.
        // The Reservation.compareTo() logic ensures that only CHECKED_IN parties 
        // will be at the front of the queue if any exist.
        Reservation next = waitlist.poll();
        if (next != null) {
            next.setStatus(ReservationStatus.SEATED); // Update status upon seating
        }
        return next;
    }

    public int getQueueSize() {
        return waitlist.size();
    }

    /**
     * Retrieves all reservations from the queue, sorted by priority.
     * This is essential for correctly displaying the list in the GUI.
     */
    public Reservation[] getAllWaiting() {
        // PriorityQueue.toArray() does NOT guarantee the priority order.
        // We must convert it to an array, then sort the array using the Reservation's compareTo logic.
        Reservation[] waitingArray = waitlist.toArray(new Reservation[0]);
        Arrays.sort(waitingArray);
        return waitingArray;
    }
    
    /**
     * NEW FEATURE: Allows the GUI to find and update a reservation object (e.g., Check-In).
     * Since PriorityQueue doesn't allow direct element mutation, we remove and re-add.
     */
    public boolean updateReservationStatus(Reservation reservationToUpdate, ReservationStatus newStatus) {
        // Remove the old object (if it exists)
        if (waitlist.remove(reservationToUpdate)) {
            // Create a temporary reference to update the status
            reservationToUpdate.setStatus(newStatus); 
            // If the status is CHECKED_IN, the priority might change, so we re-add it.
            // If it's CANCELED or another terminal status, we don't re-add it to the active queue.
            if (newStatus == ReservationStatus.CHECKED_IN || newStatus == ReservationStatus.PENDING) {
                waitlist.offer(reservationToUpdate); // Re-add, which re-sorts the priority
            }
            return true;
        }
        return false; // Reservation not found
    }
}
