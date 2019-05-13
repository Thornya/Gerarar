package Modele;


import java.net.*;
import java.io.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LocalClient implements Runnable {
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
    private DatagramSocket ds;

    public LocalClient(String address, String port) {
        try {
            server_address = InetAddress.getByName(address);
            ds = new DatagramSocket();
        } catch (UnknownHostException e) {
            //TODO gérer l'exception
            e.printStackTrace();
        } catch (SocketException e) {
            //TODO handle the exception
        }
        server_port = Integer.parseInt(port);
    }

    public void sendRequest(boolean requestMode, String filename_str) {
        byte[] opcode = new byte[2];
        if (requestMode) {  //RRQ corresponds to true
            opcode[1] = 1;
        }
        else {              //WRQ corresponds to false
            opcode[1] = 2;
        }

        byte[] filename = filename_str.getBytes();

        String mode_str = "octet";
        byte[] mode = mode_str.getBytes();

        byte nullbyte = 0;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(opcode);
            outputStream.write(filename);
            outputStream.write(nullbyte);
            outputStream.write(mode);
            outputStream.write(nullbyte);
        } catch (IOException e) {
            //TODO handle the exception
        }

        byte buffer[] = outputStream.toByteArray();

        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, server_address, server_port);
        try {
            ds.send(dp);
        } catch (IOException e) {
            //TODO handle the exception
        }
    }


    private void sendData(byte[]data,int size,DatagramSocket ds,short blockid){
        byte[] opcode = new byte[2];
        opcode[1]=3;

        byte[] blockids= new byte [2];
        blockids[1]=(byte)blockid;
        if (blockid>255){
            blockid= (short)(blockid&0x0000FF00);
            blockids[0]= (byte)(blockid/256);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(opcode);
            outputStream.write(blockid);
            outputStream.write(data);
        } catch (IOException e) {
            //TODO handle the exception
        }

        byte buffer[] = outputStream.toByteArray();
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, server_address, server_port);
        try {
            ds.send(dp);
        } catch (IOException e) {
            //TODO handle the exception
        }

    }


    private byte[] readFile(String Filename,int start){
        byte[] input= new byte[512];
        int i,b;
        try{
            FileInputStream fe= new FileInputStream(Filename);
            int size=fe.read(input,start,512);

            fe.close();
        }
        catch(IOException ex) {
            System.out.println("ReadFile : "+ex);
        }
        return input;
    }

}
