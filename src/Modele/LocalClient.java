package Modele;

import Exceptions.*;

import java.net.*;
import java.io.*;
import java.util.*;

public class LocalClient  {
    private static LocalClient instance;
    public static final int max_trial_transfert = 3;
    public static final int wait_time_transfert_ms = 5000;

    public static final int opcode_RRQ = 1;
    public static final int opcode_WRQ = 2;
    public static final int opcode_DATA = 3;
    public static final int opcode_ACK = 4;
    public static final int opcode_ERR = 5;

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
    public static final int error_access_denied_file = -20;
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

    public static LocalClient getInstance(){
        return instance;
    }

    public int ReceiveFile(String server_address_str, String server_port_str, String filename) {
    	byte[] buff = null;
    	FileOutputStream fo = null;
        try {        	
        	fo = new FileOutputStream(filename);
            server_address = InetAddress.getByName(server_address_str);
            ds = new DatagramSocket();
        	sendRequest(opcode_RRQ, filename);
            int size = receiveDATA(buff);
            byte[] blockId = { buff[2], buff[3] } ;
            sendACK((short) 1);
            fo.write(buff, 0, size);
            boolean finTransfert = (size != 512);
            short nPacket = 1;
            while(!finTransfert) {
            	size = receiveDATA(buff);
            	blockId[0] = buff[2];
            	blockId[1] = buff[3];
            	short nPackShort = convertisseurByteToShort(blockId);
            	if(nPackShort != nPacket) {
            		//TODO gérer le cas "réception du mauvais paquet
            		sendACK((short) (nPackShort-1));
            	}
            	else {
            		sendACK(nPackShort);
            		nPacket ++;
            	}
            	finTransfert = (size != 512);
            	fo.write(buff, nPacket*512, buff.length);
            }
            
        } catch (UnknownHostException e) {
            //TODO gérer l'exception
            e.printStackTrace();
        } catch (SocketException e) {
            //TODO handle the exception
        } catch (ServerIllegalTFTPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        fo.close();
        server_port = Integer.parseInt(server_port_str);
        return transfer_successful;
    }


    private void checkRequestPayload(DatagramPacket dp) throws ServerIllegalTFTPException {
        byte[] data = dp.getData();
        if (data[0] != 0) {
            sendError(4, "First byte is not null", dp.getAddress(), dp.getPort());
            throw (new ServerIllegalTFTPException("First byte is not null"));
        }

        String filename = "";
        int[] opcodes = {opcode_RRQ, opcode_WRQ};
        if (!Arrays.asList(opcodes).contains(data[1])) {
            sendError(4, "Unknown opcode", dp.getAddress(), dp.getPort());
            throw (new ServerIllegalTFTPException("Unknown opcode"));
        }
        int i;
        for (i = 2; i < data.length; i++) {
            if (data[i] == 0) {
                break;
            }
            filename += (char) data[i];
        }
        if (filename.length() == 0) {
            sendError(4, "Filename length is null", dp.getAddress(), dp.getPort());
            throw (new ServerIllegalTFTPException("Filename length is null"));
        }

        String mode = "";
        if (i == data.length - 1) {
            sendError(4, "Data payload has been cut", dp.getAddress(), dp.getPort());
            throw (new ServerIllegalTFTPException("Data payload has been cut"));
        }
        for(int j = i + 1; j < data.length; j++) {
            if (data[j] == 0) {
                break;
            }
            mode += (char) data[j];
        }
        if (mode.length() == 0) {
            sendError(4, "Mode length is null", dp.getAddress(), dp.getPort());
            throw (new ServerIllegalTFTPException("Mode length is null"));
        }
        String[] modes = {"netascii", "octet"};
        if (!Arrays.asList(modes).contains(mode.toLowerCase())) {
            sendError(4, "Unknown mode length", dp.getAddress(), dp.getPort());
            throw (new ServerIllegalTFTPException("Unknown mode length"));
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

    private void checkDataPayload(DatagramPacket dp) throws ServerIllegalTFTPException {
        byte[] data = dp.getData();
        if (data[0] != 0) {
            sendError(4, "First byte is not null", dp.getAddress(), dp.getPort());
            throw (new ServerIllegalTFTPException("First byte is not null"));
        }

        if (! (data[1] == opcode_DATA) ) {
            sendError(4, "Expecting DATA, received different opcode", dp.getAddress(), dp.getPort());
            throw (new ServerIllegalTFTPException("Expecting DATA, received different opcode"));
        }
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

            int trial_transfert=0;
            boolean received=false;
            while(!received && trial_transfert<max_trial_transfert) {
                sendRequest(opcode_RRQ,filename);
                if(receiveACK((short)0)){
                    received=true;
                }
                trial_transfert++;
            }

            short blockid=1;
            boolean finTransfert=false;
            while(!finTransfert){
                finTransfert=size!=512;
                trial_transfert=0;
                received=false;
                while(!received && trial_transfert<max_trial_transfert) {
                    sendData(input, size, blockid);
                    if(receiveACK(blockid)){
                        received=true;
                    }
                    trial_transfert++;
                }
                blockid++;
                if(!finTransfert)
                    size = fe.read(input, blockid * 512, 512);

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

    private void exceptionOccurred(Exception e) throws AccesDeniedException {
        if ( ( e.getMessage().contains("Access") || e.getMessage().contains("access") ) && e.getMessage().contains("denied")) {
            sendError(2, e.getMessage(), server_address, server_port);
            throw (new AccesDeniedException("Access denied to the file"));
        }
        else if ( e.getMessage().contains("space") && e.getMessage().contains("disk")) {
            //TODO throw an exception
            sendError(3, e.getMessage(), server_address, server_port);
        }
        else if (e instanceof SocketException) {
            //TODO throw an exception
            sendError(0, e.getMessage(), server_address, server_port);
        }
    }

    private void sendError(int error_number, String message, InetAddress adr, int port) {
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

        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, adr, port);
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
    private void sendACK(short nPacket) {
    	byte[] payloadACK = new byte[4];
    	payloadACK[1] = opcode_ACK;
    	
    	if(nPacket>255) {
    		payloadACK[2] = (255&0x0000FF00);
    		payloadACK[3] = (byte) ((nPacket-255)&0x0000FF00);
    	}
    	DatagramPacket dp = new DatagramPacket(payloadACK, payloadACK.length, server_address, server_port);
    	try {
			ds.send(dp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    private boolean receiveACK(short nPacket) {
    	byte[] buff = new byte[4];



        DatagramPacket dp = new DatagramPacket(buff,buff.length);
    	try {
            ds.setSoTimeout(wait_time_transfert_ms);
			ds.receive(dp);
		}catch(SocketTimeoutException e) {
            return false;
        } catch (IOException e) {
            exceptionOccurred(e);
        }
        if(dp.getPort()!=server_port) {
    		sendError(5, "TID doesn't match actual TID", dp.getAddress(),dp.getPort());
    	}
        return true;
    }

    private int receiveDATA(byte[] data) throws ServerIllegalTFTPException {
        DatagramPacket dp = new DatagramPacket(data,data.length);
        try {
            ds.receive(dp);
        } catch (IOException e) {
            exceptionOccurred(e);
        }
        try {
            checkDataPayload(dp);
        } catch (ServerIllegalTFTPException e) {
            throw e;
        }
        data = dp.getData();
        return dp.getLength();
    }




}
