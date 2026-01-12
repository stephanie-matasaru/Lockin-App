package com.lockin.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lockin.client.model.Customer;
import com.lockin.client.model.Locker;
import com.lockin.client.model.Operator;
import com.lockin.client.model.Reservation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ApiService {

    // Main Base URL
    private final String BASE_URL = "http://localhost:8080/api";

    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public ApiService() {
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        // Crucial for handling LocalDateTime from the server
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // ==========================================
    // 1. AUTHENTICATION (Login & Register)
    // ==========================================

    public String login(String username, String password) {
        try {
            String formData = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                    + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login?" + formData))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            // Clean up whitespace/quotes
            if (responseBody != null) {
                responseBody = responseBody.trim().replace("\"", "");
            }

            // ⚠️ STRICT CHECKING ONLY
            if ("ADMIN".equals(responseBody)) return "ADMIN";
            if ("CLIENT".equals(responseBody)) return "CLIENT";

            // Return the specific error (WRONG_PASS or NOT_FOUND)
            return responseBody;

        } catch (Exception e) {
            e.printStackTrace();
            return "CONNECTION_ERROR";
        }
    }

    public String register(String username, String password, String name, String phone, String email) {
        try {
            // 1. URL
            URL url = new URL("http://localhost:8080/api/customers/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 2.Parameters
            String params = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                    "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8) +
                    "&name=" + URLEncoder.encode(name, StandardCharsets.UTF_8) +
                    "&phone=" + URLEncoder.encode(phone, StandardCharsets.UTF_8) +
                    "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = params.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 3. Get Response
            int code = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result = reader.readLine();

            if (code == 200) {
                return result;
            } else {
                return "Error: " + result;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public boolean loginOperator(String username, String password) {
        try {
            Operator op = new Operator();
            op.setUsername(username);
            op.setPassword(password);

            String response = sendRequest("/operators/login", "POST", op);
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            System.out.println("Operator Login failed: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // 2. LOCKER & RESERVATION (User Side)
    // ==========================================

    public List<Locker> getLockers() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/lockers"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), new TypeReference<List<Locker>>(){});
        } else {
            throw new RuntimeException("Failed to load lockers: " + response.statusCode());
        }
    }

    public boolean createReservation(String location, String compartment, String size, double price) {
        try {
            String currentUser = ClientSession.getInstance().getCurrentUsername();

            String urlStr = "http://localhost:8080/api/reservations/create" +
                    "?location=" + URLEncoder.encode(location, StandardCharsets.UTF_8) +
                    "&compartment=" + URLEncoder.encode(compartment, StandardCharsets.UTF_8) +
                    "&size=" + size +
                    "&price=" + price +
                    "&username=" + currentUser;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Reservation getMyActiveReservation() {
        try {
            String currentUser = ClientSession.getInstance().getCurrentUsername();
            String urlStr = "http://localhost:8080/api/reservations/my-active?username=" + currentUser;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = reader.readLine(); // "Loc|Box|Size"
                if (line != null && !line.isEmpty()) {
                    String[] parts = line.split("\\|");
                    if(parts.length >= 3) {
                        Reservation r = new Reservation();
                        r.setFullLocation(parts[0]);
                        r.setLockerId(0L);
                        r.setSize(parts[2]);
                        return r;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean endReservation(String location, String compartment) {
        try {
            String urlStr = "http://localhost:8080/api/reservations/cancel" +
                    "?location=" + URLEncoder.encode(location, StandardCharsets.UTF_8) +
                    "&compartment=" + URLEncoder.encode(compartment, StandardCharsets.UTF_8);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            return conn.getResponseCode() == 200;
        } catch (Exception e) { return false; }
    }

    public boolean extendReservation(String location, String compartment, double amount) {
        try {
            String urlStr = "http://localhost:8080/api/reservations/extend" +
                    "?location=" + URLEncoder.encode(location, StandardCharsets.UTF_8) +
                    "&compartment=" + URLEncoder.encode(compartment, StandardCharsets.UTF_8) +
                    "&extraAmount=" + amount;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            return conn.getResponseCode() == 200;
        } catch (Exception e) { return false; }
    }

    public List<String> getTakenBoxes(String location) {
        List<String> list = new ArrayList<>();
        try {
            String urlStr = "http://localhost:8080/api/reservations/taken?location=" +
                    URLEncoder.encode(location, StandardCharsets.UTF_8);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = reader.readLine();
                if(line != null) {
                    line = line.replace("[", "").replace("]", "").replace("\"", "");
                    if(!line.isEmpty()) {
                        String[] parts = line.split(",");
                        for(String p : parts) list.add(p.trim());
                    }
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    public String[] getUserDetails(String username) {
        try {
            String urlStr = "http://localhost:8080/api/customers/details?username=" + username;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if(conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = reader.readLine(); // "Name|Email|Phone"
                if(line != null && line.contains("|")) {
                    return line.split("\\|");
                }
            }
        } catch (Exception e) {}
        return new String[]{"(Loading...)", "---", "---"};
    }

    // ==========================================
    // 3. ADMIN / OPERATOR METHODS
    // ==========================================

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        try {
            URL url = new URL("http://localhost:8080/api/customers/all");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                // Expecting server to send: ID|Username|FullName|Email|Phone
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        Customer c = new Customer(
                                Integer.parseInt(parts[0]), // ID
                                parts[1],                   // Username
                                "",                         // Password hidden
                                parts[2],                   // Full Name
                                parts[4],                   // Phone
                                parts[3]                    // Email
                        );
                        list.add(c);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        try {
            URL url = new URL("http://localhost:8080/api/reservations/all");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split("\\|");

                    if (parts.length >= 7) {
                        Reservation r = new Reservation();

                        try { r.setId(Long.parseLong(parts[0])); } catch (Exception e) { r.setId(0L); }

                        r.setFullLocation(parts[1] + " (" + parts[2] + ")");
                        r.setSize(parts[3]);

                        try { r.setEstimatedPrice(Double.parseDouble(parts[4])); } catch (Exception e) { r.setEstimatedPrice(0.0); }

                        r.setStatus(parts[5]);

                        try { r.setCustomerId(Long.parseLong(parts[6])); } catch (Exception e) { r.setCustomerId(0L); }

                        list.add(r);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public String[] getAdminStats() {
        try {
            URL url = new URL("http://localhost:8080/api/reservations/stats");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                Scanner scanner = new Scanner(conn.getInputStream());
                if (scanner.hasNext()) {
                    String response = scanner.next();
                    return response.split(":");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{"0.0", "0"};
    }

    // ==========================================
    // 4. UTILS
    // ==========================================

    private String sendRequest(String endpoint, String method, Object bodyObject) throws Exception {
        String fullUrl = BASE_URL + endpoint;

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Content-Type", "application/json");

        if (method.equalsIgnoreCase("POST")) {
            String jsonBody = objectMapper.writeValueAsString(bodyObject);
            builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            builder.GET();
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            System.err.println("API Error [" + response.statusCode() + "] at " + endpoint);
            return null;
        }
    }
}