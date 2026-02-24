# Lockin - Smart Locker Management System

This is a client-server application built for managing automated smart lockers. It provides a digital platform for finding, booking, and managing secure storage units without needing physical keys. 

## Tech Stack
- **Backend:** Spring Boot (Java), REST APIs
- **Frontend:** JavaFX (Desktop GUI), CSS
- **Database:** MySQL
- **Build Tool:** Maven

## Main Features
- **User Authentication:** Secure registration and login to protect personal data.
- **Locker Discovery:** View locker locations and check the real-time availability of compartments.
- **Reservation Management:** Create, track, and cancel bookings for specific compartment sizes.
- **Admin Dashboard:** Monitor active reservations, transaction history, and usage costs.

## How to Run Locally

1. Clone the repository:
   ```bash
   git clone [https://github.com/stephanie-matasaru/Lockin-App.git](https://github.com/stephanie-matasaru/Lockin-App.git)
   cd Lockin-App
Set up a local MySQL database and update the connection credentials in the backend.

Install dependencies:

Bash
./mvnw clean install
Start the backend server and launch the JavaFX client:

Bash
./mvnw spring-boot:run
