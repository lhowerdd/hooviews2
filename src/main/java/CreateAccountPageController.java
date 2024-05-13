

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

public class CreateAccountPageController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label feedback;

    public void createAccount(ActionEvent event) throws Exception{
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty()) {
            feedback.setText("Username must be at least 1 character, and can be any combination of numbers, letters, and symbols");
            feedback.setTextFill(Color.RED);
            clearFields();
            return;
        }
        if(password.length() < 8){
            feedback.setText("Invalid password");
            feedback.setTextFill(Color.RED);
            clearFields();
            return;
        }
        DataManager dataManager = new DataManager("database.sqlite");
        dataManager.connect();
        if(dataManager.containsUser(username)){
            clearFields();
            feedback.setText("The entered username already exists, please try again.");
            feedback.setTextFill(Color.RED);
            dataManager.commit();
            dataManager.disconnect();
            return;
        }
        clearFields();
        feedback.setText("Account Created Successfully!");
        feedback.setTextFill(Color.GREEN);
        dataManager.addUser(username, password);
        dataManager.commit();
        dataManager.disconnect();
    }

    public void handleReturnToSignIn(ActionEvent event) throws Exception {
        SceneManager manager = new SceneManager();
        Scene scene = manager.getScene("login-page.fxml");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Welcome to Hooviews");
    }

    private void clearFields(){
        usernameField.clear();
        passwordField.clear();
    }
}
