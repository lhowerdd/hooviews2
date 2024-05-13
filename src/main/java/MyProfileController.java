
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyProfileController {
    @FXML
    Label usernameLabel;
    @FXML
    ListView<ProfileReview> myReviews;
    @FXML

    private String username;

    public void setUsername(String username) {
        this.username = username;
        usernameLabel.setText(" Logged in as: " + username);
        //userID.setText(getUserID(username).toString());
    }

    public void setReviews() throws SQLException {
        List<Review> reviews = getReviews();
        List<ProfileReview> profileReviews = convertReviewsToProfileReviews(reviews);
        myReviews.getItems().clear();
        myReviews.getItems().addAll(profileReviews);
    }

    public void handleCourseSearchButton(ActionEvent event) throws Exception {
        SceneManager manager = new SceneManager();
        SearchScreenController searchPage = new SearchScreenController();
        manager.loadSearchScene(event, "search-screen.fxml", "Search For A Course!", searchPage, this.username);
    }


    public void handleReviewSelection (MouseEvent event) {
        if (event.getClickCount() == 1) {
            ProfileReview selectedReview = myReviews.getSelectionModel().getSelectedItem();
            //DataManager manager = new DataManager("database.sqlite");
            Course course = selectedReview.getCourse();
            SceneManager scenemanager = new SceneManager();
            //manager.loadReviewScene(event, "review-scene.fxml", "Reviews for " + selectedCourse.getTitle(), courseID);
            scenemanager.loadReviewScene(event, "review-scene.fxml", course, this.username);
        }

    }

    public List<Review> getReviews() throws SQLException{
        DataManager manager = new DataManager("database.sqlite");
        manager.connect();
        int userID = manager.getUserIdByUsername(this.username);
        List<Review> reviews= manager.getReviewsByUserID(userID);
        manager.disconnect();
        return reviews;
    }

    private List<ProfileReview> convertReviewsToProfileReviews(List<Review> reviews) throws SQLException {
        List<ProfileReview> profileReviews = new ArrayList<>();
        for(int i = 0; i < reviews.size(); i++){
            Review currentReview = reviews.get(i);
            Course reviewedCourse = currentReview.getCourseFromId();
            ProfileReview profileReview = new ProfileReview(reviewedCourse, currentReview.getRating());
            profileReviews.add(profileReview);
        }
        return profileReviews;
    }




}
