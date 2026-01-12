package com.lockin.client.controllers;

import com.lockin.client.AdminDashboardView;
import com.lockin.client.ApiService;
import com.lockin.client.ClientSession;
import com.lockin.client.DashboardView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private CheckBox chkOperator;

    private final ApiService apiService = new ApiService();

    @FXML
    protected void onLoginButtonClick() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Please fill all fields");
            return;
        }

        try {
            String response = apiService.login(user, pass);

            if (response == null) response = "";
            response = response.trim().replace("\"", "");

            System.out.println("DEBUG Client: Server said [" + response + "]");

            if ("ADMIN".equals(response)) {
                // SUCCESS ADMIN
                try {
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    new AdminDashboardView().show(stage);
                } catch (Exception e) {
                    e.printStackTrace();
                    messageLabel.setText("Error opening Admin Panel");
                }

            } else if ("CLIENT".equals(response)) {
                // SUCCESS CLIENT
                ClientSession.getInstance().setCurrentUsername(user);
                try {
                    Stage currentStage = (Stage) loginButton.getScene().getWindow();
                    currentStage.close();
                    DashboardView dashboard = new DashboardView();
                    dashboard.show(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                    messageLabel.setText("Error opening dashboard.");
                }

            } else if ("WRONG_PASS".equals(response)) {
                messageLabel.setText("Incorrect Password!");
            } else if ("NOT_FOUND".equals(response)) {
                messageLabel.setText("User does not exist.");
            } else {
                messageLabel.setText("Login Failed. (Error: " + response + ")");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Connection Failed!");
        }
    }

    @FXML
    protected void onRegisterLinkClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lockin/client/register-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 550));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error: Could not find register-view.fxml");
        }
    }
}