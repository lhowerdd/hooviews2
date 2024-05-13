


//this class is solely to make life easier for displaying the reviews in the myprofile page
public class ProfileReview {

    private String subject;
    private String title;
    private int number;
    private Course course;
    private int rating;
    //private String comment;

    public ProfileReview(Course course, int rating){
        this.subject = course.getSubject();
        this.title = course.getTitle();
        this.number = course.getCourseNum();
        this.course = course;
        this.rating = rating;
    }

    public Course getCourse(){
        return this.course;
    }

    public String toString(){
        return this.subject + " " + this.number + " | " + this.title + " | Your Rating: " + this.rating;
    }



}
