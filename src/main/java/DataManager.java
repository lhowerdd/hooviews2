

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataManager {

    private final String fileName;
    private Connection connection;

    public DataManager(String fileName){
        this.fileName = fileName;
    }

    public void connect() throws SQLException{
        if (connection != null && !connection.isClosed()) {
            throw new IllegalStateException("The connection is already opened");
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + fileName);
        //the next line enables foreign key enforcement - do not delete/comment out
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        //the next line disables auto-commit - do not delete/comment out
        connection.setAutoCommit(false);
    }


    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Rollback to the last commit, or when the connection was opened
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * Ends the connection to the database
     */
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    public void createUsersTable() throws SQLException {
        String createUsersTableQuery = "create table if not exists Users (" +
                "ID INTEGER PRIMARY KEY," +
                "Username TEXT NOT NULL UNIQUE," +
                "Password TEXT NOT NULL)  ";
        Statement s1 = connection.createStatement();
        s1.execute(createUsersTableQuery);
    }


    public void addUser(String username, String password) throws SQLException {
        try{
            createUsersTable();

            String addUserQuery = "INSERT INTO USERS (ID, Username, Password) " +
                    "VALUES(?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(addUserQuery);
            statement.setString(2, username);
            statement.setString(3, password);
            statement.executeUpdate();
        }
        catch(SQLException e){
            rollback();
            throw e;
        }
    }

    public boolean containsUser(String username) throws SQLException {
        try {
            createUsersTable();

            String findUserQuery = "SELECT * FROM Users WHERE Username = ? LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(findUserQuery);
            statement.setString(1, username);
            ResultSet user = statement.executeQuery();
            if(user.next() == false){
                return false;
            }
            else {
                return true;
            }
        }
        //this is sus, could probably handle better
        catch(SQLException e){
            System.out.println("error\n");
            return true;
        }
    }



    /**
     * *ensure username exists before calling
     * @param username
     */
    public Optional<String> retrievePassword(String username) throws SQLException {
        try{
            createUsersTable();

            String passwordQuery = "SELECT Password FROM Users where Username = ?";
            PreparedStatement statement = connection.prepareStatement(passwordQuery);
            statement.setString(1, username);
            ResultSet user = statement.executeQuery();
            //account doesn't exist
            if(user.next() == false){
                return Optional.empty();
            }
            else{
                String password = user.getString("Password");
                return Optional.of(password);
            }
        }
        catch (SQLException e){
            return Optional.empty();
        }
    }


    public void clearTables() throws SQLException {
        String query1 = "DELETE FROM Users";
        Statement s1 = connection.createStatement();
        s1.execute(query1);
    }
    public void createCoursesTable() throws SQLException {
        String createCoursesTableQuery = "create table if not exists Courses (" +
                "ID INTEGER PRIMARY KEY," +
                "Subject TEXT NOT NULL," +
                "CourseNum INTEGER NOT NULL," +
                "Title TEXT NOT NULL) " ;
        Statement s1 = connection.createStatement();
        s1.execute(createCoursesTableQuery);
    }

    public void addCourse(String subject, int courseNum, String title) throws SQLException {
        try{
            createCoursesTable();

            String addUserQuery = "INSERT INTO COURSES (ID, Subject, CourseNum, Title) " +
                    "VALUES(?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(addUserQuery);
            statement.setString(2, subject);
            statement.setInt(3, courseNum);
            statement.setString(4, title);
            statement.executeUpdate();
        }
        catch(SQLException e){
            rollback();
            throw e;
        }
    }

    public boolean containsCourse(String subject, int courseNum, String title) throws SQLException {
        try {
            createCoursesTable();

            String containsCourseQuery = "SELECT COUNT(*) FROM Courses WHERE LOWER(Subject) = LOWER(?) AND CourseNum = ? AND LOWER(Title) = LOWER(?)";
            PreparedStatement statement = connection.prepareStatement(containsCourseQuery);
            statement.setString(1, subject);
            statement.setInt(2, courseNum);
            statement.setString(3, title);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
            return false;
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    /*
    public Course getCourseBySubject(String subject) throws SQLException {
        try{
            String courseQuery = "SELECT"
        }
    }
    */



    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();

        try {
            createCoursesTable();

            String getAllCoursesQuery = "SELECT * FROM Courses";
            PreparedStatement statement = connection.prepareStatement(getAllCoursesQuery);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int courseId = resultSet.getInt("ID");
                String subject = resultSet.getString("Subject");
                int courseNum = resultSet.getInt("CourseNum");
                String title = resultSet.getString("Title");

                Course course = new Course(courseId, subject, courseNum, title);
                courses.add(course);
            }
        } catch (SQLException e) {
            rollback();
            throw e;
        }

        return courses;
    }

    public List<Course> getCoursesBySearch(String subject, String courseNum, String title) throws SQLException {
        List<Course> courses = new ArrayList<>();
        try {
            createCoursesTable();

            StringBuilder courseQuery = new StringBuilder("SELECT * FROM Courses where 1=1");
            var subjectIndex = -1;
            var courseNumIndex = -1;
            var titleIndex = -1;
            var index = 1;
            if (subject != null && !subject.isEmpty()) {
                courseQuery.append(" AND LOWER(Subject) = LOWER(?)");
                subjectIndex = index;
                index ++;
            }
            if (courseNum != null && !courseNum.isEmpty()) {
                courseQuery.append(" AND CourseNum = ?");
                courseNumIndex = index;
                index ++;
            }
            if (title != null && !title.isEmpty()) {
                courseQuery.append(" AND Title LIKE ?");
                titleIndex = index;
                index ++;
            }
            PreparedStatement statement = connection.prepareStatement(courseQuery.toString());
            if (subjectIndex != -1) {
                statement.setString(subjectIndex, subject);
            }
            if (courseNumIndex != -1) {
                statement.setInt(courseNumIndex, Integer.parseInt(courseNum));
            }
            if (titleIndex != -1) {
                statement.setString(titleIndex, "%" + title + "%");
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                var tempID = resultSet.getInt("ID");
                var tempSubject = resultSet.getString("Subject");
                var tempCourseNum = resultSet.getInt("CourseNum");
                var tempTitle = resultSet.getString("Title");

                Course course = new Course(tempID, tempSubject, tempCourseNum, tempTitle);
                courses.add(course);
            }
            return courses;
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }
    public void createReviewsTable() throws SQLException {
        String createReviewsTableQuery = "CREATE TABLE IF NOT EXISTS Reviews (" +
                "ReviewID INTEGER PRIMARY KEY," +
                "UserID INTEGER," +
                "CourseID INTEGER," +
                "Rating INTEGER," +
                "Timestamp TIMESTAMP," +
                "Comment TEXT," +
                "FOREIGN KEY (UserID) REFERENCES Users(ID)," +
                "FOREIGN KEY (CourseID) REFERENCES Courses(ID)," +
                "UNIQUE(UserID, CourseID))";
        Statement statement = connection.createStatement();
        statement.execute(createReviewsTableQuery);
    }
    public void addReview(int userID, int courseID, int rating, Timestamp timestamp, String comment) throws SQLException {
        try {
            createReviewsTable();
            String addReviewQuery = "INSERT INTO Reviews (ReviewID, UserID, CourseID, Rating, Timestamp, Comment) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(addReviewQuery);
            statement.setInt(2, userID);
            statement.setInt(3, courseID);
            statement.setInt(4, rating);
            statement.setTimestamp(5, timestamp);
            statement.setString(6, comment);

            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    public void deleteReview(int reviewId) throws SQLException {
        String deleteReviewQuery = "DELETE FROM Reviews WHERE ReviewID = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteReviewQuery)) {
            statement.setInt(1, reviewId);
            statement.executeUpdate();
            connection.commit();
        }
    }

    public void updateReview(int reviewId, int updatedRating, Timestamp updatedTimestamp, String updatedComment) throws SQLException {
        String updateReviewQuery = "UPDATE Reviews SET Rating = ?, Timestamp = ?, Comment = ? WHERE ReviewID = ?";
        try (PreparedStatement statement = connection.prepareStatement(updateReviewQuery)) {
            statement.setInt(1, updatedRating);
            statement.setTimestamp(2, updatedTimestamp);
            statement.setString(3, updatedComment);
            statement.setInt(4, reviewId);
            statement.executeUpdate();
            connection.commit();
        }
    }




    public List<Review> getReviews(int courseId) throws SQLException{
        List<Review> reviews = new ArrayList<>();
        try {
            createReviewsTable();

            String getAllCoursesQuery = "SELECT * FROM Reviews WHERE CourseID = ?";
            PreparedStatement statement = connection.prepareStatement(getAllCoursesQuery);
            statement.setInt(1, courseId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("ReviewID");
                int userId = resultSet.getInt("UserID");
                int rating = resultSet.getInt("Rating");
                var timestamp = resultSet.getTimestamp("Timestamp");
                var comment = resultSet.getString("Comment");

                Review review= new Review(id, userId, courseId, rating, timestamp.toLocalDateTime(), comment);
                reviews.add(review);
            }

        } catch (SQLException e) {
            rollback();
            throw e;
        }
        return reviews;
    }

    public List<Review> getReviewsByUserAndCourse(String username, int courseId) throws SQLException {
        try {
            createReviewsTable();
            createUsersTable();

            String getUserIdQuery = "SELECT ID FROM Users WHERE Username = ?";
            PreparedStatement getUserIdStatement = connection.prepareStatement(getUserIdQuery);
            getUserIdStatement.setString(1, username);
            ResultSet userResult = getUserIdStatement.executeQuery();

            int userId = -1;
            if (userResult.next()) {
                userId = userResult.getInt("ID");
            } else {
                throw new SQLException("User not found");
            }

            String getReviewsQuery = "SELECT * FROM Reviews WHERE UserID = ? AND CourseID = ?";
            PreparedStatement statement = connection.prepareStatement(getReviewsQuery);
            statement.setInt(1, userId);
            statement.setInt(2, courseId);
            ResultSet reviewsSet = statement.executeQuery();

            List<Review> reviews = new ArrayList<>();
            while (reviewsSet.next()) {
                int reviewID = reviewsSet.getInt("ReviewID");
                int rating = reviewsSet.getInt("Rating");
                Timestamp timestamp = reviewsSet.getTimestamp("Timestamp");
                String comment = reviewsSet.getString("Comment");

                Review review = new Review(reviewID, userId, courseId, rating, timestamp.toLocalDateTime(), comment);
                reviews.add(review);
            }

            return reviews;
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }



    public List<Review> getReviewsByUserID(int id) throws SQLException{
        try{
            createReviewsTable();
            String getReviewsQuery = "SELECT * FROM Reviews Where UserID = ?";
            PreparedStatement statement = connection.prepareStatement(getReviewsQuery);
            statement.setInt(1, id);
            ResultSet reviewsSet = statement.executeQuery();

            List<Review> reviews = new ArrayList<>();
            while (reviewsSet.next()) {
                int reviewID = reviewsSet.getInt("ReviewID");
                int courseId = reviewsSet.getInt("CourseID");
                //int userId = reviewsSet.getInt("UserID");
                int rating = reviewsSet.getInt("Rating");
                var timestamp = reviewsSet.getTimestamp("Timestamp");
                var comment = reviewsSet.getString("Comment");

                Review review= new Review(reviewID, id, courseId, rating, timestamp.toLocalDateTime(), comment);
                reviews.add(review);
            }

            return reviews;

        }
        catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    public boolean duplicateReview(String username, int courseId) throws SQLException {
        try {
            createReviewsTable();
            createUsersTable();

            String getUserIdQuery = "SELECT ID FROM Users WHERE Username = ?";
            PreparedStatement getUserIdStatement = connection.prepareStatement(getUserIdQuery);
            getUserIdStatement.setString(1, username);
            ResultSet userResult = getUserIdStatement.executeQuery();

            int userId = userResult.getInt("ID");

            String checkReviewQuery = "SELECT COUNT(*) FROM Reviews WHERE UserID = ? AND CourseID = ?";
            PreparedStatement checkReviewStatement = connection.prepareStatement(checkReviewQuery);
            checkReviewStatement.setInt(1, userId);
            checkReviewStatement.setInt(2, courseId);

            ResultSet resultSet = checkReviewStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
            return false;
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }
    public int getUserIdByUsername(String username) throws SQLException {
        try {
            createUsersTable();

            String getUserIdQuery = "SELECT ID FROM Users WHERE Username = ?";
            PreparedStatement getUserIdStatement = connection.prepareStatement(getUserIdQuery);
            getUserIdStatement.setString(1, username);
            ResultSet userResult = getUserIdStatement.executeQuery();
            int userId = userResult.getInt("ID");
            return userId;

        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    public Course getCourseByCourseID(int courseID) throws SQLException{

        try {
            createCoursesTable();
            String getCourseQuery = "SELECT * from Courses where ID = ?";
            PreparedStatement statement = connection.prepareStatement(getCourseQuery);
            statement.setInt(1, courseID);
            ResultSet courseSet = statement.executeQuery();
            //int courseId = courseSet.getInt("ID");
            String subject = courseSet.getString("Subject");
            int courseNum = courseSet.getInt("CourseNum");
            String title = courseSet.getString("Title");

            Course course = new Course(courseID, subject, courseNum, title);

            return course;
        }

        catch (SQLException e) {
            rollback();
            throw e;
        }

    }



}


    /*
    public static void main(String[] args) throws Exception{
        DataManager manager = new DataManager("database.sqlite");
        manager.connect();
        manager.createUsersTable();
        //manager.addUser("Bob", "hellobrehs");
        //boolean test = manager.containsUser("Bob");
        //System.out.println(test);
        Optional<String> password = manager.retrievePassword("jeff");
        System.out.println(password.get());
        //manager.clearTables();
        manager.commit();
    }
    */

