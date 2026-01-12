package com.lockin.client;

import com.lockin.client.model.Customer;
import com.lockin.client.model.Reservation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AdminDashboardView {

    private final ApiService apiService;
    private BorderPane root;
    private Button btnDash, btnUsers, btnReservations, btnLogout;
    private Stage stage; // Keep reference to stage for logout

    // DATA STORAGE (Real data goes here)
    private ObservableList<Customer> customerList;
    private ObservableList<Reservation> reservationList;

    public AdminDashboardView() {
        this.apiService = new ApiService();
        this.customerList = FXCollections.observableArrayList();
        this.reservationList = FXCollections.observableArrayList();
    }

    public void show(Stage stage) {
        this.stage = stage;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0b162a;");

        // 1. Create Sidebar
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // 2. Load Data from Database
        refreshData();

        // 3. Show Default Page (Overview)
        showOverview();

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("LockIn - Admin Panel");
    }

    // --- LOGIC: FETCH REAL DATA ---
    private void refreshData() {
        System.out.println("🔄 Fetching data from MySQL...");

        // 1. Get Customers
        List<Customer> customers = apiService.getAllCustomers();
        customerList.setAll(customers);

        // 2. Get Reservations (We will add this to ApiService next)
        List<Reservation> reservations = apiService.getAllReservations();
        if(reservations != null) reservationList.setAll(reservations);

        System.out.println("✅ Loaded " + customerList.size() + " customers.");
    }

    // --- GUI: SIDEBAR ---
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #050910; -fx-border-color: #00e5ff; -fx-border-width: 0 1 0 0;");

        Label title = new Label("ADMIN");
        title.setStyle("-fx-text-fill: #00e5ff; -fx-font-size: 24px; -fx-font-weight: bold;");

        btnDash = createNavButton("Overview", "📊");
        btnUsers = createNavButton("Customers", "👥");
        btnReservations = createNavButton("Reservations", "📅");
        btnLogout = createNavButton("Logout", "🚪");

        // Actions
        btnDash.setOnAction(e -> showOverview());
        btnUsers.setOnAction(e -> showCustomersTable());
        btnReservations.setOnAction(e -> showReservationsTable());
        btnLogout.setOnAction(e -> logout());

        sidebar.getChildren().addAll(title, new Separator(), btnDash, btnUsers, btnReservations, new Region(), btnLogout);
        return sidebar;
    }

    // --- GUI: PAGES ---

    private void showOverview() {
        // 1. Calculate Revenue (Safe Check)
        double totalRev = reservationList.stream()
                .mapToDouble(r -> {
                    try {
                        return r.getEstimatedPrice();
                    } catch (Exception e) {
                        return 0.0;
                    }
                })
                .sum();

        long activeCount = reservationList.stream()
                .filter(r -> "ACTIVE".equalsIgnoreCase(r.getStatus()))
                .count();

        VBox content = new VBox(30);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: #1a1a1a;"); // Dark BG

        Label header = new Label("BUSINESS OVERVIEW");
        header.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox statsGrid = new HBox(20);
        statsGrid.getChildren().addAll(
                createStatCard("Total Revenue", totalRev + " RON", "💰"),
                createStatCard("Active Rentals", String.valueOf(activeCount), "🔓"),
                createStatCard("Total Customers", String.valueOf(customerList.size()), "👥"),
                createStatCard("System Status", "ONLINE", "⚡")
        );

        Button btnRefresh = new Button("Sync Database");
        btnRefresh.setStyle("-fx-background-color: #00e5ff; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnRefresh.setOnAction(e -> {
            refreshData();
            showOverview();
        });

        content.getChildren().addAll(header, statsGrid, btnRefresh);
        root.setCenter(content);
        updateActiveButton(btnDash);
    }
    private void showCustomersTable() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1a1a1a;");

        Label header = new Label("REGISTERED CUSTOMERS");
        header.setStyle("-fx-font-size: 24px; -fx-text-fill: #00e5ff; -fx-font-weight: bold;");

        TableView<Customer> table = new TableView<>();
        table.setItems(customerList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-control-inner-background: #2b2b2b;" +
                        "-fx-base: #1a1a1a;" +
                        "-fx-table-cell-border-color: transparent;" +
                        "-fx-text-fill: white;"
        );

        // --- 1. ID ---
        TableColumn<Customer, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setStyle("-fx-alignment: CENTER; -fx-text-fill: white;");

        // --- 2. USERNAME ---
        TableColumn<Customer, String> colUser = new TableColumn<>("Username");
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUser.setStyle("-fx-alignment: CENTER; -fx-text-fill: #00e5ff;"); // Cyan for usernames

        // --- 3. NAME ---
        TableColumn<Customer, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colName.setStyle("-fx-alignment: CENTER; -fx-text-fill: white;");

        // --- 4. PHONE ---
        TableColumn<Customer, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colPhone.setStyle("-fx-alignment: CENTER; -fx-text-fill: white;");

        // --- 5. EMAIL ---
        TableColumn<Customer, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setStyle("-fx-alignment: CENTER; -fx-text-fill: white;");

        table.getColumns().addAll(colId, colUser, colName, colPhone, colEmail);

        content.getChildren().addAll(header, table);
        root.setCenter(content);

        updateActiveButton(btnUsers);
    }

    private void showReservationsTable() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #1a1a1a;");

        HBox headerBox = new HBox(20);
        Label header = new Label("ALL RESERVATIONS");
        header.setStyle("-fx-font-size: 24px; -fx-text-fill: #00e5ff; -fx-font-weight: bold;");

        // Manual Refresh Button
        Button btnRefresh = new Button("↻ Refresh Data");
        btnRefresh.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-border-color: #555;");
        btnRefresh.setOnAction(e -> {
            refreshData(); // Reloads list from Server
            showReservationsTable(); // Re-draws table
        });

        headerBox.getChildren().addAll(header, btnRefresh);

        TableView<Reservation> table = new TableView<>();
        table.setItems(reservationList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setStyle(
                "-fx-background-color: #2b2b2b;" +
                        "-fx-control-inner-background: #2b2b2b;" +
                        "-fx-base: #1a1a1a;" +
                        "-fx-table-cell-border-color: transparent;" +
                        "-fx-text-fill: white;"
        );

        // --- 1. ID ---
        TableColumn<Reservation, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setStyle("-fx-alignment: CENTER; -fx-text-fill: white;");

        // --- 2. CUSTOMER ID (NEW!) ---
        TableColumn<Reservation, Number> colCustId = new TableColumn<>("Cust. ID");
        colCustId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colCustId.setStyle("-fx-alignment: CENTER; -fx-text-fill: #ffff00;"); // Yellow text

        // --- 3. LOCATION ---
        TableColumn<Reservation, String> colLoc = new TableColumn<>("Location");
        colLoc.setCellValueFactory(new PropertyValueFactory<>("fullLocation"));
        colLoc.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: white;");

        // --- 4. SIZE ---
        TableColumn<Reservation, String> colSize = new TableColumn<>("Size");
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colSize.setStyle("-fx-alignment: CENTER; -fx-text-fill: white;");

        // --- 5. PRICE ---
        TableColumn<Reservation, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("estimatedPrice"));
        colPrice.setStyle("-fx-alignment: CENTER; -fx-text-fill: #00ff00;");

        // --- 6. STATUS ---
        TableColumn<Reservation, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    if ("ACTIVE".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-background-color: #2b2b2b;");
                    } else {
                        setStyle("-fx-text-fill: #ff4444; -fx-alignment: CENTER; -fx-background-color: #2b2b2b;");
                    }
                }
            }
        });

        table.getColumns().addAll(colId, colCustId, colLoc, colSize, colPrice, colStatus);

        content.getChildren().addAll(headerBox, table);
        root.setCenter(content);
        updateActiveButton(btnReservations);
    }

    // --- HELPERS ---

    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Button createNavButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT;");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnMouseEntered(e -> { if(btn.getStyle().contains("#888")) btn.setStyle("-fx-background-color: rgba(0,229,255,0.1); -fx-text-fill: white; -fx-alignment: CENTER_LEFT;"); });
        btn.setOnMouseExited(e -> { if(!btn.getStyle().contains("#0b162a")) btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #888; -fx-alignment: CENTER_LEFT;"); });
        return btn;
    }

    private void updateActiveButton(Button active) {
        // Reset all
        String idle = "-fx-background-color: transparent; -fx-text-fill: #888; -fx-alignment: CENTER_LEFT;";
        btnDash.setStyle(idle);
        btnUsers.setStyle(idle);
        btnReservations.setStyle(idle);
        // Set active
        active.setStyle("-fx-background-color: #00e5ff; -fx-text-fill: #0b162a; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT;");
    }

    private VBox createStatCard(String title, String value, String icon) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setStyle("-fx-border-color: #00e5ff; -fx-border-radius: 5; -fx-background-color: rgba(0,229,255,0.05);");

        Label lblIcon = new Label(icon); lblIcon.setStyle("-fx-font-size: 24px;");
        Label lblVal = new Label(value); lblVal.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label lblTitle = new Label(title); lblTitle.setStyle("-fx-text-fill: #00e5ff; -fx-font-size: 12px;");

        card.getChildren().addAll(lblIcon, lblVal, lblTitle);
        return card;
    }

    private <S, T> TableColumn<S, T> createCol(String title, String property) {
        TableColumn<S, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }
}