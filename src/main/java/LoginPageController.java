import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.util.Optional;

public class LoginPageController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label feedback;

    public void handleSignInButton(ActionEvent event) throws Exception {
        String username = usernameField.getText();
        String password = passwordField.getText();
        DataManager manager = new DataManager("database.sqlite");
        manager.connect();
        if(manager.containsUser(username) == false){
            clearFields();
            feedback.setText("The entered username does not belong to any existing account, please try again.");
            feedback.setTextFill(Color.RED);
            manager.disconnect();
            return;
        }
        else{
            Optional<String> correctPassword = manager.retrievePassword(username);
            String passkey = correctPassword.get();
            if(passkey.equals(password)){
                //load search screen
                manager.disconnect();
                SceneManager sceneManager = new SceneManager();
//                sceneManager.loadScene(event, "search-screen.fxml", "Search For A Course!");
//                SearchScreenController searchPage  = new SearchScreenController();
//                searchPage.loadCourses();
                SearchScreenController searchPage = new SearchScreenController();
                sceneManager.loadSearchScene(event, "search-screen.fxml", "Search For A Course!", searchPage, username);
            }
            else{
                clearFields();
                feedback.setText("The entered password was incorrect, please try again.");
                feedback.setTextFill(Color.RED);
                manager.disconnect();
                return;
            }
        }
    }

    public void handleCloseButton(ActionEvent event) throws Exception {
        Platform.exit();
    }

    public void handleCreateAccountButton(ActionEvent event) throws Exception {
        clearFields();
        //switch to create account scene
        SceneManager manager = new SceneManager();
        manager.loadScene(event, "create-account-page.fxml", "Create A New Account For Hooviews!");
    }


    private Scene getScene(String fxml) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
        Scene newScene = new Scene(fxmlLoader.load());
        return newScene;
    }


    private void clearFields(){
        usernameField.clear();
        passwordField.clear();
    }

}
