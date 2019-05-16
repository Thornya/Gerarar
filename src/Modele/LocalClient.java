package Modele;


import java.net.*;
import java.io.*;
import java.util.*;

public class LocalClient  {
    public static final int max_trial_transfert = 3;
    public static final int wait_time_transfert_ms = 5000;

    public static final int opcode_RRQ = 1;
    public static final int opcode_WRQ = 2;
    public static final int opcode_DATA = 3;
    public static final int opcode_ACK = 4;
    public static final int opcode_ERR = 5;

    public static final int transfer_successful = 0;

    public static final int error_unavailable_server = 10;
    public static final int error_no_server_response = 20;
    public static final int error_sending = 30;
    public static final int error_receiving = 40;

    public static final int error_server_undefined = 100;
    public static final int error_server_file_not_found = 110;
    public static final int error_server_access_violation = 120;
    public static final int error_server_disk_full = 130;
    public static final int error_server_illegal_tftp_operation = 140;
    public static final int error_server_unknown_transfer_id = 150;
    public static final int error_server_file_already_exists = 160;
    public static final int error_server_unkown_user = 170;

    public static final int error_file_creation = - 10;
    public static final int error_file_writing = -20;
    public static final int error_creating_socket = - 30;

    public static final int error_client_undefined = -100;
    public static final int error_client_file_not_found = -110;
    public static final int error_client_access_violation = -120;
    public static final int error_client_disk_full = -130;
    public static final int error_client_illegal_tftp_operation = -140;
    public static final int error_client_unknown_transfer_id = -150;
    public static final int error_client_file_already_exists = -160;
    public static final int error_client_unkown_user = -170;

    private InetAddress server_address;
    private int server_port;
    private DatagramSocket ds;



    public int ReceiveFile(String server_address_str, String server_port_str, String filename) {
        try {
            server_address = InetAddress.getByName(server_address_str);
            ds = new DatagramSocket();
        } catch (UnknownHostException e) {
            //TODO gérer l'exception
            e.printStackTrace();
        } catch (SocketException e) {
            //TODO handle the exception
        }
        server_port = Integer.parseInt(server_port_str);
        return transfer_successful;
    }


    public void receivePacket() {
        byte[] buffer = new byte[8192];
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
        try {
            ds.receive(dp);
        } catch (IOException e) {
            //TODO deal with the exception
        }
        byte[] payload = dp.getData();
        switch (payload[1]) {
            case opcode_RRQ:
                checkRequestPayload(dp.getData());
                String file = extractFileName(dp.getData());
                break;
            case opcode_WRQ:
                break;
            case opcode_DATA:
                break;
            case opcode_ACK:
                break;
            case opcode_ERR:
                break;
            default:
                break;
        }
    }

    private boolean checkRequestPayload(byte[] data) {
        if (data[0] != 0) {
            //TODO send illegal tftp
            return false;
        }
        String filename = "";
        int[] opcodes = {opcode_RRQ, opcode_WRQ, opcode_DATA, opcode_ACK, opcode_ERR};
        if (!Arrays.asList(opcodes).contains(data[0])) {
            //TODO send illegal tftp
            return false;
        }
        int i;
        for (i = 2; i < data.length; i++) {
            if (data[i] == 0) {
                break;
            }
            filename += (char) data[i];
        }
        if (i == data.length - 1) {
            //TODO send illegal tftp
            return false;
        }

    }

    private String extractFileName(byte[] data) {
        String res = "";
        for (int i = 2; i < data.length; i++) {
            if (data[i] == 0) {
                return res;
            }
            res += (char) data[i];
        }
        return res;
    }





    public int SendFile(String server_address_str, String server_port_str, String filename) {
        try {
            server_address = InetAddress.getByName(server_address_str);
            ds = new DatagramSocket();
        } catch (UnknownHostException e) {
            //TODO gérer l'exception
            e.printStackTrace();
        } catch (SocketException e) {
            System.err.println("Socket Exception occurred while initializing in 'SendFile' method : ");
            e.printStackTrace();
        }
        FileInputStream fe= null;
        try {
            fe = new FileInputStream(filename);
            byte[] input= new byte[512];
            int size=fe.read(input,0,512);
            short blockid=0;
            sendRequest(opcode_RRQ,filename);
            while(size==512){
                sendData(input,size,blockid);

                blockid++;
                size=fe.read(input,blockid*512,512);

            }
        } catch (FileNotFoundException e) {
            //TODO gérer exception
        } catch (IOException e) {
            //fe.read
            //TODO gérer exception
        }


        try {
            fe.close();
        } catch (IOException e) {
            //TODO gérer exception
        }

        server_port = Integer.parseInt(server_port_str);
        return transfer_successful;
    }

    private void sendRequest(int opnumber, String filename_str) {
        byte[] opcode = new byte[2];
        if (opnumber != opcode_RRQ && opnumber != opcode_WRQ) {
            return;
        }
        else {
            opcode[1] = ((byte) opnumber);
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

    private void exceptionOccurred(Exception e) {
        if ( ( e.getMessage().contains("Access") || e.getMessage().contains("access") ) && e.getMessage().contains("denied")) {
            //TODO send access denied error code (code :2)
            sendError(2, e.getMessage());
        }
        else if ( e.getMessage().contains("space") && e.getMessage().contains("disk")) {
            //TODO send disk full error code (code :3)
            sendError(3, e.getMessage());
        }
    }

    private void sendError(int error_number, String message) {
        byte[] opcode = new byte[2];
        opcode[1]=opcode_ERR;

        byte[] error_code = new byte[2];
        error_code[2] = ((byte) error_number);

        byte[] error_msg = message.getBytes();

        byte nullbyte = 0;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(opcode);
            outputStream.write(error_code);
            outputStream.write(error_msg);
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


    private void sendData(byte[]data,int size,short blockid){
        byte[] opcode = new byte[2];
        opcode[1]=opcode_DATA;

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
        DatagramPacket dp = new DatagramPacket(buffer, size, server_address, server_port);
        try {
            ds.send(dp);
        } catch (IOException e) {
            //TODO handle the exception
        }

    }




}
