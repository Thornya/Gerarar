package Vue;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class startView extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        AnchorPane root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("Vue/view.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("GERARAR");
        primaryStage.setScene(new Scene(root,800, 500));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
