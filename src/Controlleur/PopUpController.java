package Controlleur;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PopUpController {

    public Button buttonQuitter;
    private static String text = "";
    public Label label;

    @FXML
    private void initialize() {
        setupTexte();
        setupButtonQuit();
    }

    private void setupTexte() {
        label.setText(text);
    }

    private void setupButtonQuit() {
        buttonQuitter.setOnAction(event -> ((Stage) buttonQuitter.getScene().getWindow()).close());
    }

    static void setTextPopUp(String s){
        text = s;
    }

}
