
import java.sql.SQLException;

public class Course {
    private String subject;
    private int courseNum;
    private String title;

    private int courseId;

    public Course(int courseId,String subject, int courseNum, String title) {
        this.courseId = courseId;
        this.subject = subject;
        this.courseNum = courseNum;
        this.title = title;
    }
    public int getId() {
        return courseId;
    }
    public String getSubject() {
        return subject;
    }

    public int getCourseNum() {
        return courseNum;
    }

    public String getTitle() {
        return title;
    }

    public double getAverageReviews() {
        try {
            DataManager dataManager = new DataManager("database.sqlite");
            dataManager.connect();
            var reviewsForCourse = dataManager.getReviews(getId());
            var average = 0.0;
            if (!reviewsForCourse.isEmpty()) {
                for (Review review : reviewsForCourse) {
                    average += review.getRating();
                }
                average = average / reviewsForCourse.size();
            }
            dataManager.disconnect();
            return average;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0.0;
    }

    //    @Override
//    public String toString() {
//        return "Course{" +
//                "subject='" + subject + '\'' +
//                ", courseNum=" + courseNum +
//                ", title='" + title + '\'' +
//                '}';
//    }
    @Override
    public String toString() {
        return String.format("%s %04d - %s" + " | Avg. Review: %.2f", subject, courseNum, title, getAverageReviews());
    }
    public String titleString() {
        return String.format("%s %04d - %s", subject, courseNum, title);
    }
}
