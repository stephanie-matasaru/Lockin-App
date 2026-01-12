package com.lockin.client;

import com.lockin.client.model.Locker;
import com.lockin.client.model.Compartment;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardView {

    private final ApiService apiService;
    private BorderPane root;
    private Button btnMap, btnProfile, btnLogout;

    // --- COORDINATES ---
    private final double[][] PIN_COORDS = {
            {700, 600},  // 1. Universitatea de Vest
            {675, 350},  // 2. Piata Unirii
            {670, 150},  // 3. Iulius Town
            {900, 250}   // 4. ISHO
    };

    private final String[] PIN_NAMES = {
            "Universitatea de Vest",
            "Piata Unirii",
            "Iulius Town",
            "ISHO"
    };

    public DashboardView() {
        this.apiService = new ApiService();
    }

    public void show(Stage stage) {
        root = new BorderPane();
        root.getStyleClass().add("root");

        // --- Restore Session from Server ---
        if (!ClientSession.getInstance().hasActiveBooking()) {
            com.lockin.client.model.Reservation serverRes = apiService.getMyActiveReservation();

            if (serverRes != null) {
                ClientSession.getInstance().addBooking(
                        new ClientSession.ActiveBooking(serverRes.getFullLocation(), serverRes.getSize())
                );
                System.out.println("🔄 Session Restored from Server: " + serverRes.getFullLocation());
            }
        }

        // --- SIDEBAR ---
        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(260);
        sidebar.setPadding(new Insets(40, 20, 20, 20));

        Label logo = new Label("LOCK-IN");
        logo.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #00e5ff; -fx-effect: dropshadow(three-pass-box, #00e5ff, 15, 0, 0, 0);");

        btnMap = createNavButton("🗺  Map View");
        btnProfile = createNavButton("👤  My Account");
        btnLogout = createNavButton("🚪  Logout");

        btnMap.setOnAction(e -> showMapView());
        btnProfile.setOnAction(e -> showProfileView());
        btnLogout.setOnAction(e -> {
            try {
                ClientSession.getInstance().clearSession();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lockin/client/login-view.fxml"));
                Parent loginRoot = loader.load();
                stage.setScene(new Scene(loginRoot, 400, 450));
            } catch (IOException ex) { ex.printStackTrace(); }
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(logo, new Separator(), btnMap, btnProfile, spacer, new Separator(), btnLogout);
        root.setLeft(sidebar);

        if (ClientSession.getInstance().hasActiveBooking()) {
            showProfileView();
        } else {
            showMapView();
        }

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private void highlightNav(Button activeBtn) {
        if(btnMap != null) btnMap.getStyleClass().remove("nav-button-active");
        if(btnProfile != null) btnProfile.getStyleClass().remove("nav-button-active");
        activeBtn.getStyleClass().add("nav-button-active");
    }

    // ================= MAP VIEW =================
    private void showMapView() {
        highlightNav(btnMap);
        Pane mapContainer = new Pane();
        mapContainer.setStyle("-fx-background-color: #0b162a;");

        URL mapUrl = getClass().getResource("/map.jpg");
        if (mapUrl == null) {
            Label errorLabel = new Label("Map Image Not Found\n(Check src/main/resources/map.jpg)");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 20px;");
            errorLabel.setLayoutX(500); errorLabel.setLayoutY(300);
            mapContainer.getChildren().add(errorLabel);
        } else {
            ImageView mapView = new ImageView(new Image(mapUrl.toExternalForm()));
            mapView.setFitWidth(1400);
            mapView.setPreserveRatio(true);
            mapContainer.getChildren().add(mapView);
        }

        for (int i = 0; i < PIN_NAMES.length; i++) {
            Locker locker = new Locker();
            locker.setLocation(PIN_NAMES[i]);
            List<Compartment> compartments = new ArrayList<>();

            for(int k=1; k<=6; k++) {
                Compartment c = new Compartment();
                c.setId((long)(i*10 + k));
                c.setPosition(k);
                if (k <= 2) c.setSize("Small");
                else if (k <= 4) c.setSize("Medium");
                else c.setSize("Large");
                compartments.add(c);
            }
            locker.setCompartments(compartments);

            Button pin = createMapPin(locker);
            pin.setLayoutX(PIN_COORDS[i][0]);
            pin.setLayoutY(PIN_COORDS[i][1]);
            mapContainer.getChildren().add(pin);
        }

        ScrollPane scroll = new ScrollPane(mapContainer);
        scroll.setStyle("-fx-background: #0b162a; -fx-background-color: #0b162a;");
        scroll.setPannable(true);
        scroll.setHvalue(0.48);
        scroll.setVvalue(0.4);
        root.setCenter(scroll);
    }

    private Button createMapPin(Locker locker) {
        Button pin = new Button("📍");
        pin.setStyle("-fx-background-color: rgba(0, 229, 255, 0.2); -fx-text-fill: #00e5ff; -fx-font-size: 24px; -fx-background-radius: 50; -fx-border-color: #00e5ff; -fx-border-radius: 50; -fx-min-width: 50px; -fx-min-height: 50px; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, #00e5ff, 15, 0.4, 0, 0);");
        pin.setTooltip(new Tooltip(locker.getLocation()));
        pin.setOnAction(e -> showLockerPopup(locker));
        return pin;
    }

    // ================= LOCKER POPUP =================
    private void showLockerPopup(Locker locker) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(locker.getLocation());

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #0b162a; -fx-border-color: #00e5ff; -fx-border-width: 2;");

        Label title = new Label(locker.getLocation());
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: #00e5ff; -fx-font-weight: 900;");

        FlowPane grid = new FlowPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setPrefWidth(400);

        List<String> takenBoxes = apiService.getTakenBoxes(locker.getLocation());

        for (Compartment c : locker.getCompartments()) {
            String boxName = "Box #" + c.getPosition();

            boolean isTaken = ClientSession.getInstance().isTaken(locker.getLocation(), boxName)
                    || takenBoxes.contains(boxName);

            Button btn = createCompartmentGraphic(c, isTaken);

            if (!isTaken) {
                btn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Rental");
                    confirm.setHeaderText("Rent " + boxName + " (" + c.getSize() + ")");
                    confirm.setContentText("Rate: 5 RON / hour");

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            double finalPrice = 5.00;
                            boolean serverSuccess = apiService.createReservation(locker.getLocation(), boxName, c.getSize(), finalPrice);

                            if (serverSuccess) {
                                ClientSession.getInstance().addBooking(
                                        new ClientSession.ActiveBooking(locker.getLocation(), boxName)
                                );
                                btn.setStyle("-fx-background-color: rgba(255, 68, 68, 0.15); -fx-border-color: #ff4444; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5; -fx-min-width: 80; -fx-min-height: 100;");
                                btn.setDisable(true);
                                popup.close();
                                showProfileView();
                                System.out.println("Reservation saved with price: " + finalPrice);
                            } else {
                                Alert error = new Alert(Alert.AlertType.ERROR);
                                error.setContentText("Failed to connect to server.");
                                error.show();
                            }
                        }
                    });
                });
            }
            grid.getChildren().add(btn);
        }

        Button closeBtn = new Button("CANCEL");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00e5ff; -fx-border-color: #00e5ff; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> popup.close());

        layout.getChildren().addAll(title, new Separator(), grid, new Separator(), closeBtn);
        popup.setScene(new Scene(layout, 460, 600));
        popup.show();
    }

    private Button createCompartmentGraphic(Compartment c, boolean isTaken) {
        Button btn = new Button();
        String normalStyle = "-fx-background-color: transparent; -fx-border-color: #00e5ff; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5; -fx-cursor: hand; -fx-alignment: center;";
        String takenStyle = "-fx-background-color: rgba(255, 68, 68, 0.15); -fx-border-color: #ff4444; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5; -fx-opacity: 0.7;";

        btn.setStyle(isTaken ? takenStyle : normalStyle);
        btn.setPrefSize(80, 100);

        if (!isTaken) {
            btn.setOnMouseEntered(e -> btn.setStyle(normalStyle + "-fx-background-color: rgba(0, 229, 255, 0.1); -fx-effect: dropshadow(three-pass-box, #00e5ff, 10, 0.4, 0, 0);"));
            btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        }

        VBox graphics = new VBox(5);
        graphics.setAlignment(Pos.CENTER);
        Label num = new Label(String.valueOf(c.getPosition()));
        num.setStyle("-fx-text-fill: #00e5ff; -fx-font-size: 22px; -fx-font-weight: bold;");
        Rectangle handle = new Rectangle(20, 3, javafx.scene.paint.Color.web("#00e5ff"));
        handle.setArcWidth(2); handle.setArcHeight(2);
        Label size = new Label(c.getSize().toUpperCase());
        size.setStyle("-fx-text-fill: #00e5ff; -fx-font-size: 10px; -fx-opacity: 0.8;");

        graphics.getChildren().addAll(num, handle, size);
        btn.setGraphic(graphics);
        if (isTaken) btn.setDisable(true);
        return btn;
    }

    // ================= PROFILE VIEW =================
    private void showProfileView() {
        highlightNav(btnProfile);

        VBox content = new VBox(30);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);

        Label header = new Label("MY ACCOUNT");
        header.setStyle("-fx-font-size: 30px; -fx-text-fill: #00e5ff; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, #00e5ff, 10, 0, 0, 0);");

        VBox profileCard = new VBox(15);
        profileCard.setMaxWidth(600);
        profileCard.setStyle("-fx-background-color: rgba(0, 229, 255, 0.05); -fx-border-color: #00e5ff; -fx-border-width: 0 0 0 4; -fx-padding: 20;");

        Label detailsHeader = new Label("Personal Details");
        detailsHeader.setStyle("-fx-text-fill: #00e5ff; -fx-font-size: 18px; -fx-font-weight: bold;");

        // --- FETCH DYNAMIC USERNAME ---
        String currentUser = ClientSession.getInstance().getCurrentUsername();
        String[] details = apiService.getUserDetails(currentUser); // Returns [Name, Email, Phone]

        String realName = details.length > 0 ? details[0] : currentUser;
        String realEmail = details.length > 1 ? details[1] : "---";
        String realPhone = details.length > 2 ? details[2] : "---";

        profileCard.getChildren().addAll(
                detailsHeader, new Separator(),
                createDetailRow("Username:", currentUser),
                createDetailRow("Name:", realName),
                createDetailRow("Email:", realEmail),
                createDetailRow("Phone:", realPhone)
        );

        Label bookingsHeader = new Label("Active Reservations");
        bookingsHeader.setStyle("-fx-text-fill: #00e5ff; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        VBox bookingsContainer = new VBox(15);
        bookingsContainer.setAlignment(Pos.TOP_CENTER);
        bookingsContainer.setMaxWidth(600);

        List<ClientSession.ActiveBooking> myBookings = ClientSession.getInstance().getBookings();

        if (myBookings.isEmpty()) {
            Label empty = new Label("No active rentals.");
            empty.setStyle("-fx-text-fill: #777; -fx-font-size: 16px; -fx-padding: 20;");
            bookingsContainer.getChildren().add(empty);
        } else {
            for (ClientSession.ActiveBooking booking : new java.util.ArrayList<>(myBookings)) {
                bookingsContainer.getChildren().add(createBookingCard(booking));
            }
        }

        VBox mainLayout = new VBox(30);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.getChildren().addAll(header, profileCard, bookingsHeader, bookingsContainer);

        ScrollPane scroll = new ScrollPane(mainLayout);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPadding(new Insets(20));

        root.setCenter(scroll);
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(15);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14px; -fx-min-width: 100;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        row.getChildren().addAll(lbl, val);
        return row;
    }

    private VBox createBookingCard(ClientSession.ActiveBooking booking) {
        VBox card = new VBox(10);
        card.setMaxWidth(600);
        card.setStyle("-fx-background-color: rgba(0, 229, 255, 0.1); -fx-border-color: #00e5ff; -fx-border-radius: 10; -fx-padding: 20;");

        HBox topRow = new HBox(20);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label location = new Label(booking.location);
        location.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label boxInfo = new Label(booking.boxInfo);
        boxInfo.setStyle("-fx-text-fill: #00e5ff; -fx-font-size: 18px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().addAll(location, spacer, boxInfo);

        Label timerLabel = new Label();
        timerLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Consolas', monospace; -fx-font-size: 28px;");
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            java.time.Duration remaining = java.time.Duration.between(LocalDateTime.now(), booking.expiryTime);
            if (remaining.isNegative()) {
                timerLabel.setText("EXPIRED");
                timerLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 28px;");
            } else {
                long min = remaining.toMinutes();
                long sec = remaining.minusMinutes(min).getSeconds();
                timerLabel.setText(String.format("%02d:%02d remaining", min, sec));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        HBox buttons = new HBox(10);
        // --- 1. EXTEND BUTTON (Connects to Server) ---
        Button extendBtn = new Button("Extend (+1h)");
        extendBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #00e5ff; -fx-border-color: #00e5ff; -fx-cursor: hand;");
        extendBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Extend Rental Time");
            confirm.setContentText("Cost: 5 RON. Proceed?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    boolean success = apiService.extendReservation(booking.location, booking.boxInfo, 5.0);
                    if (success) {
                        booking.expiryTime = booking.expiryTime.plusHours(1);
                        System.out.println("✅ Extended successfully (Price updated in DB)");
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setContentText("Could not connect to server.");
                        error.show();
                    }
                }
            });
        });

        // --- 2. END RENTAL BUTTON (Connects to Server) ---
        Button endBtn = new Button("End Rental");
        endBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4444; -fx-border-color: #ff4444; -fx-cursor: hand;");
        endBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Checkout");
            confirm.setContentText("Are you sure you want to end this rental?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    boolean success = apiService.endReservation(booking.location, booking.boxInfo);
                    if (success) {
                        timeline.stop();
                        ClientSession.getInstance().removeBooking(booking);
                        showProfileView(); // Refresh screen
                        System.out.println("✅ Rental Ended (Status updated to COMPLETED in DB)");
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setContentText("Could not connect to server.");
                        error.show();
                    }
                }
            });
        });

        buttons.getChildren().addAll(extendBtn, endBtn);
        HBox bottomRow = new HBox(20);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.getChildren().addAll(timerLabel, spacer, buttons);
        card.getChildren().addAll(topRow, new Separator(), bottomRow);
        return card;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }
}