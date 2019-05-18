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
    private static int retourFonction = 0;

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
                retourFonction = client.SendFile(IP, port, fichier);
                if (retourFonction != LocalClient.transfer_successful) {
                    newPopUp(erreurTitre, getErrorText(retourFonction), PopUpType.ERROR);
                    retourFonction = 0;
                }
            }
        });
    }

    private void setupButtonTelecharger(){
        buttonTelecharger.setOnAction(event ->{
            if (testReadyR()) {
                retourFonction = client.ReceiveFile(IP, port, fichier);
                if (retourFonction != LocalClient.transfer_successful) {
                    newPopUp(erreurTitre, getErrorText(retourFonction), PopUpType.ERROR);
                    retourFonction = 0;
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
            case LocalClient.error_unavailable_server: return "Serveur inaccessible";

            case LocalClient.error_server_undefined: return "Erreur serveur : Inconnue";
            case LocalClient.error_server_file_not_found: return "Erreur serveur : Fichier introuvable";
            case LocalClient.error_server_access_violation: return "Erreur serveur : Accès interdit au fichier";
            case LocalClient.error_server_disk_full: return "Erreur serveur : Disque serveur plein";
            case LocalClient.error_server_illegal_tftp_operation: return "Erreur serveur : Opération TFTP non autorisée";
            case LocalClient.error_server_unknown_transfer_id: return "Erreur serveur : L'ID de transfert ne correspond pas";
            case LocalClient.error_server_file_already_exists: return "Erreur serveur : Le fichier existe déjà";
            case LocalClient.error_server_unkown_user: return "Erreur serveur : Utilisateur inconnu";

            case LocalClient.error_file_creation: return "Erreur locale : Impossible de créer le fichier";
            case LocalClient.error_unable_to_send_packet: return "Erreur locale : Impossible d'envoyer des données au serveur";
            case LocalClient.error_creating_socket: return "Erreur locale : Impossible de créer le socket de communication";
            case LocalClient.error_merging_byte_arrays: return "Erreur locale : Impossible de créer un DatagramPacket";
            case LocalClient.error_no_valid_server_address: return "Erreur locale : Adresse serveur non-valide";
            case LocalClient.error_no_valid_server_port: return "Erreur locale : Port serveur non-valide";
            case LocalClient.error_while_dealing_exception: return "Erreur locale : Impossible de déterminer l'erreur";

            case LocalClient.error_client_undefined: return "Erreur locale : Inconnue";
            case LocalClient.error_client_file_not_found: return "Erreur locale : Fichier introuvable";
            case LocalClient.error_client_access_violation: return "Erreur locale : Accès interdit au fichier";
            case LocalClient.error_client_disk_full: return "Erreur locale : Disque client plein";
            case LocalClient.error_client_illegal_tftp_operation: return "Erreur locale : Opération TFTP non autorisée";
        }
        return "Erreur inconnue";
    }
}
