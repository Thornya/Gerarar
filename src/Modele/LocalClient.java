package Modele;

import Exceptions.*;

import java.net.*;
import java.io.*;

public class LocalClient  {
    public static final int max_trial_transfert = 3;
    public static final int wait_time_transfert_ms = 10000;

    public static final int transfer_successful = 0;

    public static final int error_unavailable_server = 10;

    public static final int error_server_undefined = 100;
    public static final int error_server_file_not_found = 110;
    public static final int error_server_access_violation = 120;
    public static final int error_server_disk_full = 130;
    public static final int error_server_illegal_tftp_operation = 140;
    public static final int error_server_unknown_transfer_id = 150;
    public static final int error_server_file_already_exists = 160;
    public static final int error_server_unkown_user = 170;

    public static final int error_file_creation = - 10;
    public static final int error_unable_to_send_packet = -20;
    public static final int error_creating_socket = - 30;
    public static final int error_merging_byte_arrays = - 40;
    public static final int error_no_valid_server_address = - 50;
    public static final int error_no_valid_server_port = - 60;
    public static final int error_while_dealing_exception = -70;

    public static final int error_client_undefined = -100;
    public static final int error_client_file_not_found = -110;
    public static final int error_client_access_violation = -120;
    public static final int error_client_disk_full = -130;
    public static final int error_client_illegal_tftp_operation = -140;
    //public static final int error_client_unknown_transfer_id = -150;
    //public static final int error_client_file_already_exists = -160;
    //public static final int error_client_unkown_user = -170;

    private static final int opcode_RRQ = 1, opcode_WRQ = 2, opcode_DATA = 3, opcode_ACK = 4, opcode_ERR = 5;

    private static final short error_code_undefined = 0, error_code_file_not_found = 1, error_code_access_violation = 2, error_code_disk_full = 3, error_code_illegal_tftp_operation = 4, error_code_unknown_transfer_id = 5, error_code_file_already_exists = 6,error_code_unkown_user = 7;

    private static InetAddress server_address;
    private static int server_port;
    private static DatagramSocket ds;


    public static int ReceiveFile(String server_address_str, String server_port_str, String filepath) {
        try {
            try {
                server_port = Integer.parseInt(server_port_str);
                server_address = InetAddress.getByName(server_address_str);
                ds = new DatagramSocket();
            } catch (NumberFormatException e) {
                System.err.println("NumberFormatException occurred while initializing in 'ReceiveFile' method : ");
                e.printStackTrace();
                return error_no_valid_server_port;
            }  catch (UnknownHostException e) {
                System.err.println("UnknownHostException occurred while initializing in 'ReceiveFile' method : ");
                e.printStackTrace();
                return error_no_valid_server_address;
            } catch (SocketException e) {
                System.err.println("SocketException occurred while initializing in 'ReceiveFile' method : ");
                e.printStackTrace();
                return error_creating_socket;
            }

            byte[] buff = new byte[8192];
            File file = new File(filepath);
            FileOutputStream fo = new FileOutputStream(file);

            int nPacket = 1;
            int size = -1;
            int trial_transfert=0;
            boolean received=false;

            while(!received && trial_transfert<max_trial_transfert) {
                sendRequest(opcode_RRQ,file.getName());
                size = receiveDATA(buff, nPacket, true);
                if(size != -1)
                    received=true;
                else
                    trial_transfert++;
            }if(trial_transfert==max_trial_transfert)
                return error_unavailable_server;
            fo.write(buff, 4, size);
            boolean finTransfert = (size != 512);

            while(!finTransfert) {

                trial_transfert=0;
                received=false;
                while(!received && trial_transfert<max_trial_transfert) {
                    sendACK(nPacket);
                    size = receiveDATA(buff, nPacket + 1, false);
                    if(size != -1) {
                        nPacket++;
                        received = true;
                    }
                    else
                        trial_transfert++;
                }if(trial_transfert==max_trial_transfert)
                    return error_unavailable_server;
                finTransfert = (size != 512);
                fo.write(buff, 4, size);
            }
            sendACK(nPacket);
            fo.close();
            return transfer_successful;
        } catch (NullPointerException e) {
            System.err.println("NullPointerException occurred while initializing in 'SendFile' method : ");
            e.printStackTrace();
            return error_client_file_not_found;
        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException occurred while initializing in 'ReceiveFile' method : ");
            e.printStackTrace();
            if ( e.getMessage().toLowerCase().contains("access") && e.getMessage().toLowerCase().contains("denied")) {
                return error_client_access_violation;
            } else if (e.getMessage().toLowerCase().contains("space") && e.getMessage().toLowerCase().contains("disk")) {
                return error_client_disk_full;
            }
            return error_file_creation;
        } catch (Exception e) {
            return exceptionOccurred(e);
		}
    }

    public static int SendFile(String server_address_str, String server_port_str, String filepath) {
        try {
            server_port = Integer.parseInt(server_port_str);
            server_address = InetAddress.getByName(server_address_str);
            ds = new DatagramSocket();
        } catch (NumberFormatException e) {
            System.err.println("NumberFormatException occurred while initializing in 'SendFile' method : ");
            e.printStackTrace();
            return error_no_valid_server_port;
        } catch (UnknownHostException e) {
            System.err.println("UnknownHostException occurred while initializing in 'SendFile' method : ");
            e.printStackTrace();
            return error_no_valid_server_address;
        } catch (SocketException e) {
            System.err.println("SocketException occurred while initializing in 'SendFile' method : ");
            e.printStackTrace();
            return error_creating_socket;
        }
        try {
            File file = new File(filepath);
            FileInputStream fe = new FileInputStream(file);
            byte[] input = new byte[512];
            int size = fe.read(input, 0, 512);
            int blockid = 0;

            int trial_transfert = 0;
            boolean received = false;
            while (!received && (trial_transfert < max_trial_transfert) ) {
                sendRequest(opcode_WRQ, file.getName());
                if (receiveACK(blockid, true))
                    received = true;
                else
                    trial_transfert++;
            }
            if (trial_transfert == max_trial_transfert)
                return error_unavailable_server;

            blockid ++;
            boolean finTransfert = false;
            while (!finTransfert) {
                finTransfert = (size != 512);
                trial_transfert = 0;
                received = false;
                while (!received && (trial_transfert < max_trial_transfert) ) {
                    sendData(input, blockid, size);
                    if (receiveACK(blockid, false))
                        received = true;
                    else
                        trial_transfert++;
                }
                if (trial_transfert == max_trial_transfert)
                    return error_unavailable_server;
                blockid++;
                if (!finTransfert) {
                    size = fe.read(input, 0, 512);
                }
            }
            fe.close();
            return transfer_successful;
        } catch (NullPointerException e) {
            System.err.println("NullPointerException occurred while initializing in 'SendFile' method : ");
            e.printStackTrace();
            return error_client_file_not_found;
        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException occurred while initializing in 'SendFile' method : ");
            e.printStackTrace();
            if ( e.getMessage().toLowerCase().contains("access") && e.getMessage().toLowerCase().contains("denied")) {
                return error_client_access_violation;
            } else if (e.getMessage().toLowerCase().contains("space") && e.getMessage().toLowerCase().contains("disk")) {
                return error_client_disk_full;
            } else {
                return error_client_file_not_found;
            }
        } catch (Exception e) {
            return exceptionOccurred(e);
        }
    }



    private static int exceptionOccurred(Exception e)  {
        System.err.println("Exception occured : ");
        e.printStackTrace();
        try {
            if (e instanceof SocketException) {
                sendError(error_code_undefined, e.getMessage(), server_address, server_port);
                return error_client_undefined;
            } else if (e instanceof UnknownHostException) {
                return error_no_valid_server_address;
            } else if (e instanceof MergingByteArraysException) {
                sendError(error_code_undefined, e.getMessage(), server_address, server_port);
                return error_merging_byte_arrays;
            } else if (e instanceof ClientIllegalTFTPOperationException) {
                sendError(error_code_illegal_tftp_operation, e.getMessage(), server_address, server_port);
                return error_client_illegal_tftp_operation;
            } else if (e instanceof UnableToSendPacketException) {
                return error_unable_to_send_packet;
            } else if (e instanceof ServerUndefinedException) {
                return error_server_undefined;
            } else if (e instanceof ServerFileNotFoundException) {
                return error_server_file_not_found;
            } else if (e instanceof ServerAccessViolationException) {
                return error_server_access_violation;
            } else if (e instanceof ServerDiskFullException) {
                return error_server_disk_full;
            } else if (e instanceof ServerIllegalTFTPOperationException) {
                return error_server_illegal_tftp_operation;
            } else if (e instanceof ServerUnkownTransferIDException) {
                return error_server_unknown_transfer_id;
            } else if (e instanceof ServerFileAlreadyExistsException) {
                return error_server_file_already_exists;
            } else if (e instanceof ServerUnkownUserException) {
                return error_server_unkown_user;
            } else if (e.getMessage().toLowerCase().contains("access") && e.getMessage().toLowerCase().contains("denied")) {
                 sendError(error_code_access_violation, e.getMessage(), server_address, server_port);
                 return error_client_access_violation;
            } else if (e.getMessage().toLowerCase().contains("space") && e.getMessage().toLowerCase().contains("disk")) {
                 sendError(error_code_disk_full, e.getMessage(), server_address, server_port);
                 return error_client_disk_full;
            }
        } catch (Exception e1) {
            System.err.println("Exception in exceptionOccured method : ");
            e1.printStackTrace();
            return error_while_dealing_exception;
        }
        return error_client_undefined;
    }



    private static boolean receiveACK(int nPacket, boolean overrideTID) throws Exception  {
        byte[] buff = new byte[4];

        DatagramPacket dp = new DatagramPacket(buff,buff.length);
        try {
            ds.setSoTimeout(wait_time_transfert_ms);
            ds.receive(dp);
        }catch(SocketTimeoutException e) {
            return false;
        }
        if (overrideTID) {
            server_port = dp.getPort();
        } else if (dp.getPort()!=server_port) {
            sendError(error_code_unknown_transfer_id, "TID doesn't match actual TID", dp.getAddress(),dp.getPort());
            return receiveACK(nPacket, overrideTID);
        }
        byte[] opCode = {dp.getData()[0],dp.getData()[1]};
        if (opcode_ACK != convertisseurByteInt(opCode)) {
            if (opcode_ERR == convertisseurByteInt(opCode)) {
                receivedError(dp.getData());
            }
            else {
                sendError(error_code_illegal_tftp_operation, "Expected ACK paquet, received something else", dp.getAddress(), dp.getPort());
                throw new ClientIllegalTFTPOperationException("Expected ACK paquet, received something else : ");
            }
        }
        byte[] packetNumber = {dp.getData()[2],dp.getData()[3]};
        if (nPacket != convertisseurByteInt(packetNumber)) {
            sendError(error_code_illegal_tftp_operation, "Acquitted packet number doesn't match", dp.getAddress(),dp.getPort());
            throw  new ClientIllegalTFTPOperationException("Acquitted packet number doesn't match");
        }
        return true;
    }

    private static int receiveDATA(byte[] data, int nPacket, boolean overrideTID) throws Exception {
        DatagramPacket dp = new DatagramPacket(data,data.length);
        try {
            ds.setSoTimeout(wait_time_transfert_ms);
            ds.receive(dp);
        }catch(SocketTimeoutException e) {
            return -1;
        }
        if (overrideTID) {
            server_port = dp.getPort();
        } else if (dp.getPort()!=server_port) {
            sendError(error_code_unknown_transfer_id, "TID doesn't match actual TID", dp.getAddress(),dp.getPort());
            return receiveDATA(data, nPacket, overrideTID);
        }
        byte[] opCode = {dp.getData()[0],dp.getData()[1]};
        if (opcode_DATA != convertisseurByteInt(opCode)) {
            if (opcode_ERR == convertisseurByteInt(opCode)) {
                receivedError(dp.getData());
            }
            else {
                sendError(error_code_illegal_tftp_operation, "Expected DATA paquet, received something else", dp.getAddress(), dp.getPort());
                throw new ClientIllegalTFTPOperationException("Expected DATA paquet, received something else");
            }
        }
        byte[] packetNumber = {dp.getData()[2],dp.getData()[3]};
        if (nPacket != convertisseurByteInt(packetNumber)) {
            sendError(error_code_illegal_tftp_operation, "Data packet number doesn't match expected packet number", dp.getAddress(),dp.getPort());
            throw  new ClientIllegalTFTPOperationException("Data packet number doesn't match expected packet number");
        }
        return dp.getLength() - 4;

    }

    private static void receivedError(byte[] data) throws Exception {
        byte[] errorCode = {data[2],data[3]};
        String errorMessage = "";
        for (int i = 2; i < data.length; i++) {
            if (data[i] == 0) {
                break;
            }
            errorMessage += (char) data[i];
        }
        switch (convertisseurByteInt(errorCode)) {
            case error_code_file_not_found:
                throw new ServerFileNotFoundException(errorMessage);
            case error_code_access_violation:
                throw new ServerAccessViolationException(errorMessage);
            case error_code_disk_full:
                throw new ServerDiskFullException(errorMessage);
            case error_code_illegal_tftp_operation:
                throw new ServerIllegalTFTPOperationException(errorMessage);
            case error_code_unknown_transfer_id:
                throw new ServerUnkownTransferIDException(errorMessage);
            case error_code_file_already_exists:
                throw new ServerFileAlreadyExistsException(errorMessage);
            case error_code_unkown_user:
                throw new ServerUnkownUserException(errorMessage);
            default:
                throw new ServerUndefinedException(errorMessage);
        }
    }



    private static void sendRequest(int opnumber, String filename_str) throws Exception {
        byte[] opcode = new byte[2];
        if (opnumber != opcode_RRQ && opnumber != opcode_WRQ) {
            throw new Exception("Opnumber not valid in sendRequest");
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
            throw new MergingByteArraysException("Unable to merge byte arrays in sendRequest");
        }

        byte[] buffer = outputStream.toByteArray();

        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, server_address, server_port);
        try {
            ds.send(dp);
        } catch (IOException e) {
            throw new UnableToSendPacketException("Unable to send request packet in sendRequest");
        }
    }

    private static void sendError(short error_number, String message, InetAddress adr, int port) throws Exception {
        byte[] opcode = new byte[2];
        opcode[1]=opcode_ERR;

        byte[] error_code = new byte[2];
        error_code[1] = ((byte) error_number);

        byte[] error_msg = message.getBytes();

        byte nullbyte = 0;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(opcode);
            outputStream.write(error_code);
            outputStream.write(error_msg);
            outputStream.write(nullbyte);
        } catch (IOException e) {
            throw new MergingByteArraysException("Unable to merge byte arrays in sendError");
        }

        byte[] buffer = outputStream.toByteArray();

        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, adr, port);
        try {
            ds.send(dp);
        } catch (IOException e) {
            throw new UnableToSendPacketException("Unable to send DatagramPacket in sendError");
        }
    }

    private static void sendData(byte[] data, int blockid, int size) throws Exception {
        byte[] opcode = new byte[2];
        opcode[1]=opcode_DATA;

        byte[] blockids= new byte [2];
        blockids[1]=(byte)blockid;
        if (blockid>255){
            blockid= blockid&0x0000FF00;
            blockids[0]= (byte)(blockid/256);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(opcode);
            outputStream.write(blockids);
            outputStream.write(data);
        } catch (IOException e) {
            throw new MergingByteArraysException("Unable to merge byte arrays in sendData");
        }

        byte[] buffer = outputStream.toByteArray();
        DatagramPacket dp = new DatagramPacket(buffer, size + 4, server_address, server_port);
        try {
            ds.send(dp);
        } catch (IOException e) {
            throw new UnableToSendPacketException("Unable to send DatagramPacket in sendData");
        }

    }

    private static void sendACK(int nPacket) throws Exception {
    	byte[] payloadACK = new byte[4];
    	payloadACK[1] = opcode_ACK;


        payloadACK[3] = (byte) nPacket;
    	if(nPacket>255) {
    	    nPacket = nPacket&0x0000FF00;
    		payloadACK[2] = (byte)(nPacket/256);
    	}


    	DatagramPacket dp = new DatagramPacket(payloadACK, payloadACK.length, server_address, server_port);
    	try {
			ds.send(dp);
		} catch (IOException e) {
            throw new UnableToSendPacketException("Unable to send DatagramPacket in sendACK");
		}
    	
    }



    private static int convertisseurByteInt(byte[] data){
        int petit=data[1];
        int grand=data[0];
        if(data[0]<0)
            grand=data[0]+256;
        if(data[1]<0)
            petit=data[1]+256;

        return grand*256+petit;
    }

}
