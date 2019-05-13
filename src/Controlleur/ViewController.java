package Controlleur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ViewController {

    public AnchorPane mainPane;
    public Button buttonValiderIP, buttonQuitter, buttonParcourir, buttonEnvoyer, buttonTelecharger;
    public TextField TFadresseIP, TFport, TFnomFicher;
    public Label labelNomFichier;
    private String IP, port;
    private File fichier;
    private boolean erreur;

    private final String erreurTitre = "Erreur!";
    private final String ipTitre = "Destination confirmée!";
    private final String ipTexte = "Adresse IP et ports enregistrés.";



    @FXML
    private void initialize() {
        setupButtonValiderIP();
        setupButtonParcourir();
        setupButtonEnvoyer();
        setupButtonTelecharger();
        setupButtonQuit();

    }

    private void setupButtonValiderIP(){
        buttonValiderIP.setOnAction(event ->{
            if (TFadresseIP.getText() != "" && TFadresseIP != null && TFport.getText() != "" && TFport.getText() != null) {
                IP = TFadresseIP.getText();
                port = TFport.getText();
                newPopUp(ipTitre,ipTexte);
            }
        });
    }

    private void setupButtonParcourir() {
        buttonParcourir.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir le fichier à transferer");
            fichier = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
            if (fichier != null) {
                labelNomFichier.setText(fichier.getAbsolutePath());
            }
        });
    }

    private void setupButtonEnvoyer(){
        buttonEnvoyer.setOnAction(event ->{
            //TODO: Ajouter l'envoi
            int erreurCode = 100;
            if(erreur) {
                newPopUp(erreurTitre, getErrorText(erreurCode));
            }
        });
    }

    private void setupButtonTelecharger(){
        buttonTelecharger.setOnAction(event ->{
            //TODO: Ajouter le receive
            int erreurCode = 10;
            if(erreur) {
                newPopUp(erreurTitre, getErrorText(erreurCode));
            }
        });
    }

    private void setupButtonQuit() {
        buttonQuitter.setOnAction(event -> ((Stage) mainPane.getScene().getWindow()).close());
    }

    private void newPopUp(String nom, String text){
        Stage stageNewWindow = new Stage();
        try {
            PopUpController.setTextPopUp("Erreur: " + text);
            AnchorPane root = FXMLLoader.load(getClass().getResource("../Vue/popUp.fxml"));
            setupNewWindow(stageNewWindow, root,nom);
            stageNewWindow.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setupNewWindow(Stage stage, AnchorPane mainPane, String title){
        stage.setTitle(title);
        stage.setScene(new Scene(mainPane, 300, 150));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
    }

    private String getErrorText(int errorCode){
        switch (errorCode){
            case 10: return "Serveur inaccessible";
            case 20: return "Pas de réponse du serveur";
            case 30: return "Echec de l'envoi";
            case 40: return "Echec de la reception";

            case 100: return "Interne au serveur";
            case 110: return "Le fichier n'existe pas";
            case 120: return "Autorisation serveur";
            case 130: return "Serveur plein";
            case 140: return "Operation TFTP non autorisée";
            case 150: return "Paquet tier reçu";
            case 160: return "Le fichier existe déjà";
            case 170: return "Utilisateur inconnu";

            case -10: return "Création du fichier";
            case -20: return "Ecriture";
            case -30: return "Création de la socket";
        }
        return null;
    }
}
