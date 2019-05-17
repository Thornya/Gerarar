package Controlleur;

import Modele.LocalClient;
import javafx.beans.binding.BooleanBinding;
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
    private static String IP, port, fichier;
    private LocalClient client;
    private static int codeErreur = 0;

    private final String erreurTitre = "Erreur!";
    private final String ipTitre = "Destination confirmée!";
    private final String ipTexte = "Adresse IP et ports enregistrés.";

    @FXML
    private void initialize() {
        client = LocalClient.getInstance();
        setupButtonValiderIP();
        setupButtonParcourir();
        setupButtonEnvoyer();
        setupButtonTelecharger();
        setupButtonQuit();
        setupTextField();
    }

    private void setupButtonValiderIP(){
        buttonValiderIP.setOnAction(event ->{
            if (!TFadresseIP.getText().equals("") && TFadresseIP != null && !TFport.getText().equals("") && TFport.getText() != null) {
                IP = TFadresseIP.getText();
                port = TFport.getText();
                newPopUp(ipTitre,ipTexte, PopUpType.INFO);
            }
        });

        buttonValiderIP.disableProperty().bind(new BooleanBinding()
        {
            {
                super.bind(TFadresseIP.textProperty(),
                           TFport.textProperty());
            }

            @Override
            protected boolean computeValue() {
                return (TFadresseIP.getText().isEmpty()
                        || TFport.getText().isEmpty());
            }
        });
    }

    private void setupButtonParcourir() {
        buttonParcourir.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir le fichier à transferer");
            File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
            if (file != null) {
                fichier = file.getAbsolutePath();
                labelNomFichier.setText(fichier);
                if (!IP.equals("") && !port.equals(""))
                    buttonEnvoyer.setDisable(false);
            }
        });
    }

    private void setupButtonEnvoyer(){
        buttonEnvoyer.setOnAction(event ->{
            if (testReadyE()) {
                codeErreur = client.SendFile(IP, port, fichier);
                if (codeErreur != 0) {
                    newPopUp(erreurTitre, getErrorText(codeErreur), PopUpType.ERROR);
                    codeErreur = 0;
                }
            }
        });
    }

    private void setupButtonTelecharger(){
        buttonTelecharger.setOnAction(event ->{
            if (testReadyR()) {
                codeErreur = client.ReceiveFile(IP, port, fichier);
                if (codeErreur != 0) {
                    newPopUp(erreurTitre, getErrorText(codeErreur), PopUpType.ERROR);
                    codeErreur = 0;
                }
            }
        });
    }

    private void setupButtonQuit() {
        buttonQuitter.setOnAction(event -> ((Stage) mainPane.getScene().getWindow()).close());
    }

    private void setupTextField(){
        buttonTelecharger.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(TFnomFicher.textProperty(),
                        TFadresseIP.textProperty(),
                        TFport.textProperty());
            }

            @Override
            protected boolean computeValue() {
                return (TFnomFicher.getText().isEmpty()
                        || TFadresseIP.getText().isEmpty()
                        || TFport.getText().isEmpty());
            }
        });
    }

    private void newPopUp(String nom, String text, PopUpType type){
        Stage stageNewWindow = new Stage();
        try {
            if (type == PopUpType.ERROR)
                PopUpController.setTextPopUp("Erreur: " + text);
            else
                PopUpController.setTextPopUp(text);
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

    private boolean testReadyE(){
        return (!IP.equals("") && !port.equals("") &&
                (fichier != null && !fichier.equals("")));
    }

    private boolean testReadyR(){
        return (!IP.equals("") && !port.equals("")) &&
                (TFnomFicher.getText() != null && !TFnomFicher.getText().equals(""));
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
