import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class HelloWorld extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        //create a Label to say Hello World
        Label helloLabel = new Label("Hello World!");

        //create a pane to act as the root Pane
        Pane root = new FlowPane();

        //add the Label to the root Pane
        root.getChildren().add(helloLabel);

        //Create a Scene containing our root Pane
        Scene scene = new Scene(root);

        //Put our scene on stage
        primaryStage.setScene(scene);

        //display the stage
        primaryStage.show();
    }
}