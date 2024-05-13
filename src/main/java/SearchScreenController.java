
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class SearchScreenController implements Initializable{
    @FXML
    private TextField courseSubject;
    @FXML
    private TextField courseNumber;
    @FXML
    private TextField courseTitle;
    @FXML
    private Label messageLabel;
    @FXML
    private ListView<Course> courseList;
    @FXML
    private Label loadCourseFeedback;
    private SceneManager sceneManager;

    private String username;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            DataManager dataManager= new DataManager("database.sqlite");
            dataManager.connect();
            loadCourses();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    public void setUsername(String username){
        this.username = username;
    }
    public String getUsername(){
        return this.username;
    }
    public void loadCourses() {
        try {
            if (courseList != null) {
                DataManager dataManager = new DataManager("database.sqlite");
                dataManager.connect();
                List<Course> courses = dataManager.getAllCourses();
                courseList.getItems().clear();
                courseList.getItems().addAll(courses);

                dataManager.disconnect();
            } else {
                loadCourseFeedback.setText("Unable to load courses. List of courses is null.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleResetButton(ActionEvent event) throws Exception {
        courseSubject.clear();
        courseNumber.clear();
        courseTitle.clear();
        messageLabel.setText("Search fields cleared, displaying all courses.");
        messageLabel.setTextFill(Color.BLACK);
        var dataManager = new DataManager("database.sqlite");
        dataManager.connect();
        List<Course> courses = dataManager.getAllCourses();
        courseList.getItems().setAll(courses);
        dataManager.disconnect();
    }

    public void handleMyProfileButton(ActionEvent event) throws Exception {
        SceneManager manager = new SceneManager();
        //manager.loadScene(event, "my-profile.fxml", "My Profile");
        manager.LoadMyReviewsScene(event, "my-profile.fxml", this.username);
    }

    public void handleSearchButton(ActionEvent event) throws Exception {
        String subject = courseSubject.getText();
        String courseNum= courseNumber.getText();
        String title = courseTitle.getText();
        //messageLabel.setText("Results with fields: Subject = "+isEmpty(subject)+" | Number = "+isEmpty(courseNum)+" | Title = "+isEmpty(title));
        messageLabel.setText("Course search results with given fields:");
        messageLabel.setTextFill(Color.BLACK);
        try {
            if (isInvalidSearchSubject(subject)) {
                throw new IllegalArgumentException("Invalid search query for Course Subject mnemonic. It should be 2-4 letters.");
            }
            if (isInvalidSearchCourseNumber(courseNum)) {
                throw new IllegalArgumentException("Invalid search query for Course Number. It should be exactly 4 digits.");
            }
            if (isInvalidSearchTitle(title)) {
                throw new IllegalArgumentException("Invalid search for Course Title. It must 1-50 characters long and can include letters, numbers, and symbols.");
            }
            DataManager dataManager = new DataManager("database.sqlite");
            dataManager.connect();
            List<Course> courses = dataManager.getCoursesBySearch(subject, courseNum, title);
            courseList.getItems().setAll(courses);
            dataManager.disconnect();
        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setTextFill(Color.RED);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void handleAddCourseButton(ActionEvent event) throws Exception{
        String subject = courseSubject.getText();
        String courseNumText= courseNumber.getText();
        String title = courseTitle.getText();
        try {
            if (!isValidAddSubject(subject)) {
                throw new IllegalArgumentException("Invalid Subject mnemonic. It should be 2-4 letters.");
            }
            if (!isValidAddCourseNumber(courseNumText)) {
                throw new IllegalArgumentException("Invalid course number. It should be exactly 4 digits.");
            }
            if (!isValidAddTitle(title)) {
                throw new IllegalArgumentException("Invalid title. It should be 1-50 characters long and can include letters, numbers, and symbols.");
            }
            int courseNum = Integer.parseInt(courseNumText);

            DataManager dataManager = new DataManager("database.sqlite");
            dataManager.connect();

            if (dataManager.containsCourse(subject, courseNum, title)) {
                messageLabel.setText("Course already exists!");
                messageLabel.setTextFill(Color.RED);
            } else {
                var subjectUpper = subject.toUpperCase();
                dataManager.addCourse(subjectUpper, courseNum, title);
                messageLabel.setText("Course added successfully!");
                messageLabel.setTextFill(Color.GREEN);
                dataManager.commit();
                dataManager.disconnect();
                loadCourses();
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Invalid course number. Please enter a valid integer, that is 4 digits long.");
            messageLabel.setTextFill(Color.RED);
        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setTextFill(Color.RED);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleCourseSelection(MouseEvent event) {
            if (event.getClickCount() == 1) {
                Course selectedCourse = courseList.getSelectionModel().getSelectedItem();
                //int courseID = selectedCourse.getId();
                SceneManager manager = new SceneManager();
                //manager.loadReviewScene(event, "review-scene.fxml", "Reviews for " + selectedCourse.getTitle(), courseID);
                manager.loadReviewScene(event, "review-scene.fxml", selectedCourse, this.username);
                }
            }


    public void handleSignOut(ActionEvent event) throws Exception{
        SceneManager manager = new SceneManager();
        manager.loadScene(event, "login-page.fxml", "Welcome to Hooviews");
    }
    private boolean isValidAddSubject(String subject) {
        return subject != null && subject.matches("^[A-Za-z]{2,4}$");
    }
    private boolean isValidAddCourseNumber(String courseNumText) {
        return courseNumText != null && courseNumText.matches("^\\d{4}$");
    }
    private boolean isValidAddTitle(String title) {
        return title != null && title.length() >= 1 && title.length() <= 50;
    }
    private boolean isInvalidSearchSubject(String subject) {
        return !subject.isEmpty() && (subject.length() < 2 || subject.length() > 4);
    }
//    private boolean isInvalidSearchCourseNumber(String courseNum) {
//        return !courseNum.isEmpty() && (Integer.parseInt(courseNum) < 1000 || Integer.parseInt(courseNum) > 9999);
//    }
    private boolean isInvalidSearchCourseNumber(String courseNum) {
        try {
            if (!courseNum.isEmpty()) {
                if (courseNum.length() != 4){
                    throw new IllegalArgumentException("Invalid course number. It should be exactly 4 digits.");
                }
                int num = Integer.parseInt(courseNum);
                if (num < 0 || num > 9999) {
                    throw new IllegalArgumentException("Invalid course number. It should be exactly 4 digits.");
                }
            }
            return false;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid course number. It should be a valid integer.");
        }
    }

    private boolean isInvalidSearchTitle(String title) {
        return title.length() > 50;
    }

    private String isEmpty(String field) {
        if (field != null && !field.isEmpty()) {
            return field;
        }
        return ("Empty");
    }
}
