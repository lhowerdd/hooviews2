

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Review {
    private int id;
    private int courseID;
    private int rating;
    private LocalDateTime timestamp;
    private String comment;
    private int userID;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public Review(int id, int userID, int courseID, int rating, LocalDateTime timestamp, String comment) {
        this.id = id;
        this.userID = userID;
        this.courseID = courseID;
        this.rating = rating;
        this.timestamp = timestamp;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }
    public int getUserID(){
        return userID;
    }
    public int getCourseID() {
        return courseID;
    }

    public int getRating() {
        return rating;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        String formattedTimestamp = timestamp.format(formatter);
        String commentText = comment.isEmpty() ? "" :  " | Comment: "+ comment;

        return formattedTimestamp + " | Rating: " + rating  + commentText;
    }

    public Course getCourseFromId() throws SQLException {
        DataManager manager = new DataManager("database.sqlite");
        manager.connect();
        Course course = manager.getCourseByCourseID(this.courseID);
        manager.disconnect();
        return course;
    }

}