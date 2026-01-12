package com.lockin.client.controllers;

import com.lockin.client.ApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    private final ApiService apiService = new ApiService();

    @FXML
    protected void onRegisterClick() {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();

        if (user.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        statusLabel.setText("Registering...");

        new Thread(() -> {
            String response = apiService.register(user, pass, name, phone, email);
            Platform.runLater(() -> {
                if (response.contains("success")) {
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Success! Returning to login...");
                    // Wait 1.5 seconds then go back to login automatically
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override public void run() {
                                    Platform.runLater(() -> onBackClick());
                                }
                            },
                            1500
                    );
                } else {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Registration Failed: " + response);
                }
            });
        }).start();
    }

    @FXML
    protected void onBackClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lockin/client/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Lockin - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}