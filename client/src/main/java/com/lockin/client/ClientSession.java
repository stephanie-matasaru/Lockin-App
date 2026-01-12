package com.lockin.client;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientSession {
    private static ClientSession instance;
    private final List<ActiveBooking> bookings;

    // --- Store the current user's name ---
    private String currentUsername;

    private ClientSession() {
        bookings = new ArrayList<>();
    }

    public static ClientSession getInstance() {
        if (instance == null) {
            instance = new ClientSession();
        }
        return instance;
    }

    // --- Getter & Setter for Username ---
    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    public String getCurrentUsername() {
        return (currentUsername != null) ? currentUsername : "Guest";
    }
    // -----------------------------------------

    // --- Helper Methods for DashboardView ---

    public boolean hasActiveBooking() {
        return !bookings.isEmpty();
    }

    public void clearSession() {
        bookings.clear();
        currentUsername = null; // Clear name on logout
    }

    // ---------------------------------------------

    public void addBooking(ActiveBooking booking) {
        bookings.add(booking);
    }

    public void removeBooking(ActiveBooking booking) {
        bookings.remove(booking);
    }

    public List<ActiveBooking> getBookings() {
        return bookings;
    }

    // Just gets the first booking (useful for checking status)
    public ActiveBooking getBooking() {
        if (bookings.isEmpty()) return null;
        return bookings.get(0);
    }

    public boolean isTaken(String location, String boxName) {
        for (ActiveBooking b : bookings) {
            if (b.location.equals(location) && b.boxInfo.equals(boxName)) {
                return true;
            }
        }
        return false;
    }

    // Inner class for Booking Data
    public static class ActiveBooking {
        public String location;
        public String boxInfo;
        public LocalDateTime expiryTime;

        public ActiveBooking(String location, String boxInfo) {
            this.location = location;
            this.boxInfo = boxInfo;
            this.expiryTime = LocalDateTime.now().plusHours(1);
        }

        public String getLockerLocation() {
            return location + " " + boxInfo;
        }
    }
}