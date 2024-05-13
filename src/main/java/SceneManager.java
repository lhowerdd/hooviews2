
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;


public class SceneManager {

    public Scene getScene(String fileName) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fileName));
        Scene newScene = new Scene(fxmlLoader.load());
        return newScene;
    }

    //from https://stackoverflow.com/questions/12804664/how-to-swap-screens-in-a-javafx-application-in-the-controller-class
    public void loadScene(ActionEvent event, String fileName, String titleMessage) throws Exception{
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = getScene(fileName);
        stage.setScene(scene);
        stage.show();
        stage.setTitle(titleMessage);
    }

    public SceneManager(){
    }
    public void loadSearchScene(ActionEvent event, String fxmlFileName, String title, SearchScreenController searchScreenController, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent root = loader.load();

            SearchScreenController controller = loader.getController();

            controller.initialize(null, null);
            controller.setUsername(username);
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //public void loadReviewScene(MouseEvent event, String fxmlFileName, String title, int courseID) {
    public void loadReviewScene(MouseEvent event, String fxmlFileName, Course course, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent root = loader.load();

            ReviewSceneController reviewScreenController = loader.getController();
            reviewScreenController.setCourseId(course.getId());
            reviewScreenController.setUsername(username);
            reviewScreenController.initializeReviewScene();
            //reviewScreenController.loadReviews(course.getId());
            reviewScreenController.displayCourseName(course);

            //reviewScreenController.setCourseId(course.getId());
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(course.titleString());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    public void LoadMyReviewsScene(ActionEvent event, String fxmlFileName, String username) throws SQLException{
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent root = loader.load();
            MyProfileController controller = loader.getController();
            controller.setUsername(username);
            controller.setReviews();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Your Reviews");
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }




    }



