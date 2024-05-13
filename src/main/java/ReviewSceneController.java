
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import javafx.scene.paint.Color;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class ReviewSceneController implements Initializable {
    @FXML
    private Label courseLabel;
    @FXML
    private Label courseLabel2;
    @FXML
    private Label averageRatingLabel;
    @FXML
    private Label message;
    @FXML
    private Label ratingFeedback;
    @FXML
    private Label reviewSubmitFeedback;
    @FXML
    private Label prevReview;
    @FXML
    private Label prevReview2;
    @FXML
    private ListView<Review> reviewList;
    @FXML
    private TextField rating;
    @FXML
    private TextArea comment;
    private SceneManager sceneManager;
    private DataManager dataManager;

    private String username;

    private int courseId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        try {
//            dataManager = new DataManager("database.sqlite");
//            dataManager.connect();
//            loadReviews(this.courseId);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }
    public void initializeReviewScene() {
        try {
            dataManager = new DataManager("database.sqlite");
            dataManager.connect();
            loadReviews(this.courseId);
            prevReview.setFont(Font.font("Open Sans", FontWeight.NORMAL, 12));
            prevReview.setMaxWidth(Double.MAX_VALUE);
            AnchorPane.setLeftAnchor(prevReview, 0.0);
            AnchorPane.setRightAnchor(prevReview, 0.0);
            prevReview.setAlignment(Pos.CENTER);
            prevReview2.setFont(Font.font("Open Sans", FontWeight.NORMAL, 12));
            prevReview2.setMaxWidth(Double.MAX_VALUE);
            AnchorPane.setLeftAnchor(prevReview2, 0.0);
            AnchorPane.setRightAnchor(prevReview2, 0.0);
            prevReview2.setAlignment(Pos.CENTER);
            if (dataManager.duplicateReview(this.username, this.courseId)) {
                prevReview.setText("You already reviewed this course.");
                prevReview2.setText("Edit it by clicking the Select User Review button to load your review, editing it, and clicking Update Review.");
            }
            else{
                prevReview.setText("Enter Review:");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void displayCourseName(Course course){
        courseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12.5));
        courseLabel.setMaxWidth(Double.MAX_VALUE);
        AnchorPane.setLeftAnchor(courseLabel, 0.0);
        AnchorPane.setRightAnchor(courseLabel, 0.0);
        courseLabel.setAlignment(Pos.CENTER);

        courseLabel2.setFont(Font.font("Arial", FontWeight.BOLD, 12.5));
        courseLabel2.setMaxWidth(Double.MAX_VALUE);
        AnchorPane.setLeftAnchor(courseLabel2, 0.0);
        AnchorPane.setRightAnchor(courseLabel2, 0.0);
        courseLabel2.setAlignment(Pos.CENTER);
        courseLabel2.setText("Reviews for:");
        courseLabel.setText(course.toString());
    }
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public void setCourseId(int courseId){
        this.courseId = courseId;
    }
    public void loadReviews(int courseID) {
        try {
            List<Review> reviews = dataManager.getReviews(courseID);
            if (reviews.isEmpty()) {
                message.setText("No reviews yet");
            } else {
                message.setText("Reviews: ");
                reviewList.getItems().clear();
                reviewList.getItems().addAll(reviews);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public void setCourseDetails(Course course) {
//        courseLabel.setText(course.getSubject() + " " +  ": " + course.getTitle());
//        try {
//            // Calculate and display average rating
//            List<Review> reviews = dataManager.getReviews(course.getId());
//            double averageRating = calculateAverageRating(reviews);
//            averageRatingLabel.setText("Average Rating: " + String.format("%.2f", averageRating));
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    private double calculateAverageRating(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return 0.0;
        }

        double totalRating = 0.0;
        for (Review review : reviews) {
            totalRating += review.getRating();
        }
        return totalRating / reviews.size();
    }

    @FXML
    public void handleReviewSubmission() {
        String ratingVal = rating.getText();
        String commentVal = comment.getText();
        try {
            if (!isValidRating(ratingVal)){
                throw new NumberFormatException();
            }
            int ratingInt = Integer.parseInt(ratingVal);
            if (dataManager.duplicateReview(this.username, this.courseId)) {
                reviewSubmitFeedback.setText("You already reviewed this course.");
                reviewSubmitFeedback.setTextFill(Color.RED);
            } else {
                var userId = dataManager.getUserIdByUsername(this.username);
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                dataManager.addReview(userId, this.courseId, ratingInt, timestamp, commentVal);
                reviewSubmitFeedback.setText("Review added successfully!");
                reviewSubmitFeedback.setTextFill(Color.GREEN);
                dataManager.commit();
                resetFields();
                updateAverageRating();
                loadReviews(this.courseId);
                prevReview.setText("You already reviewed this course.");
                prevReview2.setText("Edit it by clicking the Select User Review button to load your review, editing it, and clicking Update Review.");
            }
        }
        catch (NumberFormatException e){
            ratingFeedback.setText("Invalid rating. Rating should be an integer between 1 and 5.");
            ratingFeedback.setTextFill(Color.RED);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    public void handleReviewDeletion() {
        Review selectedReview = reviewList.getSelectionModel().getSelectedItem();
        if (selectedReview == null) {
            // Show a message indicating no review is selected for deletion
            reviewSubmitFeedback.setText("Please select a review to delete.");
            reviewSubmitFeedback.setTextFill(Color.RED);
            return;
        } else {
            try {
                int reviewId = selectedReview.getId();
                int reviewUserId = selectedReview.getUserID();

                int loggedInUserId = dataManager.getUserIdByUsername(username);

                if (reviewUserId == loggedInUserId) {
                    dataManager.deleteReview(reviewId);
                    reviewSubmitFeedback.setText("Review deleted successfully!");
                    reviewSubmitFeedback.setTextFill(Color.GREEN);
                    resetFields();
                    dataManager.commit();
                    List<Review> reviews = dataManager.getReviews(courseId);
                    reviewList.getItems().clear();
                    reviewList.getItems().addAll(reviews);
                    loadReviews(this.courseId);
                    updateAverageRating();
                    prevReview.setText("Enter Review:");
                    prevReview2.setText("");
                } else {
                    reviewSubmitFeedback.setText("You don't have permission to delete this review.");
                    reviewSubmitFeedback.setTextFill(Color.RED);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                reviewSubmitFeedback.setText("Error deleting review. Please try again.");
            }
        }
    }

    @FXML
    private Button selectUserReviewButton;

    @FXML
    public void handleSelectUserReview() {
        try {
            List<Review> userCourseReviews = dataManager.getReviewsByUserAndCourse(username, this.courseId);
            if (!userCourseReviews.isEmpty()) {
                reviewList.getSelectionModel().select(userCourseReviews.get(0));
                handleReviewSelection();
            } else {
                reviewSubmitFeedback.setText("You haven't reviewed this course yet.");
                reviewSubmitFeedback.setTextFill(Color.RED);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            reviewSubmitFeedback.setText("Error selecting user review. Please try again.");
        }
    }

    @FXML
    public void handleReviewUpdate() {
        try {
            Review selectedReview = reviewList.getSelectionModel().getSelectedItem();
            if (selectedReview == null) {
                reviewSubmitFeedback.setText("Please select a review to edit.");
                reviewSubmitFeedback.setTextFill(Color.RED);
                return;
            }

            int reviewUserId = selectedReview.getUserID();
            int loggedInUserId = dataManager.getUserIdByUsername(username);

            if (reviewUserId != loggedInUserId) {
                reviewSubmitFeedback.setText("You don't have permission to edit this review.");
                reviewSubmitFeedback.setTextFill(Color.RED);
                return;
            }
            if (rating.getText().isEmpty()) {
                throw new NumberFormatException();
            }
            int updatedRating = Integer.parseInt(rating.getText());
            if (updatedRating < 1 || updatedRating > 5) {
                throw new NumberFormatException();
            }

            String updatedComment = comment.getText();
            Timestamp updatedTimestamp = new Timestamp(System.currentTimeMillis());

            dataManager.updateReview(selectedReview.getId(), updatedRating, updatedTimestamp, updatedComment);
            reviewSubmitFeedback.setText("Review updated successfully!");
            reviewSubmitFeedback.setTextFill(Color.GREEN);
            resetFields();
            updateAverageRating();
            loadReviews(this.courseId);
        } catch (SQLException | NumberFormatException e) {
            //e.printStackTrace();
            reviewSubmitFeedback.setTextFill(Color.RED);
            reviewSubmitFeedback.setText("Invalid Rating. Please use a number between 1 and 5.");
        }
    }


    public void disconnectDataManager() {
        try {
            dataManager.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handleReviewSelection() throws SQLException {
        Review selectedReview = reviewList.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            int reviewUserId = selectedReview.getUserID();
            int loggedInUserId = dataManager.getUserIdByUsername(username);

            if (reviewUserId == loggedInUserId) {
                // Populate fields with selected review data
                rating.setText(String.valueOf(selectedReview.getRating()));
                comment.setText(selectedReview.getComment());
                reviewSubmitFeedback.setText("Editing review...");
                reviewSubmitFeedback.setTextFill(Color.BLACK);
            } else {
                reviewSubmitFeedback.setText("You don't have permission to edit this review.");
                reviewSubmitFeedback.setTextFill(Color.RED);
                rating.clear();
                comment.clear();
            }
        }
    }


    @FXML
    public void navigateToSearchScreen(ActionEvent event) throws SQLException {
        dataManager.disconnect();
        SceneManager sceneManager = new SceneManager();
        SearchScreenController searchPage = new SearchScreenController();
        sceneManager.loadSearchScene(event, "search-screen.fxml", "Search For A Course!", searchPage, this.username);
        // Logic to navigate back to the Course Search Screen
    }

    public boolean isValidRating(String rating){
        try{
            int ratingInt = Integer.parseInt(rating);
            if (ratingInt == 1 || ratingInt == 2 || ratingInt == 3 || ratingInt == 4 || ratingInt == 5){
                return true;
            }
            else{
                throw new NumberFormatException();
            }
        }
        catch (NumberFormatException e){
            return false;
        }
    }

    private void resetFields() {
        rating.setText("");
        comment.setText("");
        ratingFeedback.setText(""); // Clear any previous feedback on rating validation
        reviewList.getSelectionModel().clearSelection(); // Clear selection in the review list
        // Add any other field reset logic if needed
    }

    private void updateAverageRating() {
        try {
            List<Review> reviews = dataManager.getReviews(courseId);
            double averageRating = calculateAverageRating(reviews);
            averageRatingLabel.setId("Average Rating: " + String.format("%.2f", averageRating));
            displayCourseName(dataManager.getCourseByCourseID(courseId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
