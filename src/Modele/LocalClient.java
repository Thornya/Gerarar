package Modele;


import java.net.InetAddress;

public class LocalClient {
    public static final int max_trial_transfert = 3;
    public static final int wait_time_transfert_ms = 5000;

    public static final int error_unavailable_server = 10;
    public static final int error_no_server_response = 20;
    public static final int error_sending = 30;
    public static final int error_receiving = 40;

    public static final int error_server_undefined = 100;
    public static final int error_server_file_not_found = 110;
    public static final int error_server_access_violation = 120;
    public static final int error_server_disk_full = 130;
    public static final int error_server_illegal_tftp_opereation = 140;
    public static final int error_server_unknown_transfer_id = 150;
    public static final int error_server_file_already_exists = 160;
    public static final int error_server_unkown_user = 170;

    public static final int error_file_creation = - 10;
    public static final int error_file_writing = -20;
    public static final int error_creating_socket = - 30;

    private InetAddress server_address;
    private int server_port;

    public LocalClient(String address, String port) {
        server_address = InetAddress.getByName(address);
        server_port = Integer.parseInt(port);
    }

    public void sendRequest(boolean requestMode, String filename) {

    }
}
