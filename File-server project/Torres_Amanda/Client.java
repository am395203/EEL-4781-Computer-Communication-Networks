import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;


public class Client {
    private final int BUFFER_SIZE = 4096;
    private Socket connection;
    private DataInputStream socketIn;
    private DataOutputStream socketOut;
    private int bytes;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private int startByte = 0;
    private int endByte = 0;

    public Client(String host, int port, String[] args) throws IOException {        
        int choiceNum;
        try {
            connection = new Socket(host, port);
            
            choiceNum = clientChoice(args); //get client choice 
            
            if(choiceNum == 1) { //1: receive file from server
            	receiveFileClient(args[1]);
            }
            if(choiceNum == 2) { //2 receive byte range from server
            	receiveByteRangeClient(args[1]);
            }
            if(choiceNum == 3) { //send file to server
            	sendFileClient(args[1]);
            }
            if(choiceNum == 4) {
            	System.out.println("ERROR: Wrong Format Entered.");
            	System.out.println("Proper Formats: ");
            	System.out.println("Receive File: <Client> <Port Number> <filename>");
            	System.out.println("Receive Byte Range: <Client> <Port Number> <filename> [-s <bytes>] [-e <bytes>]");
            	System.out.println("Write File to Server: <Client> <Port Number> <filename> [-w]");       	
            }
            
            connection.close();
        } catch (Exception ex) {
            System.out.println("Error: " + ex);
        }
    }
   
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
    
    public static void main(String[] args) throws IOException {
    	int portNum = Integer.parseInt(args[0]);  	
		Client client = new Client("127.0.0.1", portNum, args);

    }  
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
    
    public void receiveFileClient(String file) throws IOException { //Used to receive and save file from server Xx
        socketIn = new DataInputStream(connection.getInputStream()); // Read data from server
        socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to server
    	socketOut.writeInt(1);
        socketOut.writeUTF(file); // Write filename to server 
                      
        FileOutputStream fos = new FileOutputStream(new File(file)); //new FileOutputStream to write contents to file
        
        int readBytes = 0; //bytes read
        while((readBytes = socketIn.read(buffer, 0, buffer.length)) != -1) { //continue reading until end of stream	
        	fos.write(buffer, 0, readBytes); //write read contents to buffer
        }
        System.out.print("File " + file + " has been saved.\n"); //notify user file has been saved 
        fos.close();
    }
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
    
    public void receiveByteRangeClient(String file) throws IOException { //Used to receive and save byte range Xx
        socketIn = new DataInputStream(connection.getInputStream()); // Read data from server
        socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to server
    	socketOut.writeInt(2);       
    	socketOut.writeUTF(file); // Write filename to server 
    	socketOut.writeInt(startByte); //write start bytes to server
    	socketOut.writeInt(endByte); //write end bytes to server
            	
        FileOutputStream fos = new FileOutputStream(new File(file)); //new FileOutputStream to write contents to file
                    
        int readBytes = 0; //bytes read
        while((readBytes = socketIn.read(buffer, 0, buffer.length)) != -1) { //continue reading until end of stream	
        	fos.write(buffer, 0, readBytes); //write read contents to buffer
        }
        System.out.print("File " + file + " has been saved.\n"); //notify user file has been saved 
        fos.close();
    }
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void sendFileClient(String file) throws IOException { //Used to send file from client to server Xx
    	socketIn = new DataInputStream(connection.getInputStream()); // Read data from server
        socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to server
    	socketOut.writeInt(3);
        socketOut.writeUTF(file); // Write filename to server
    	         
        FileInputStream fileIn = new FileInputStream(file); //New FileInputStream to read contents of file                   
        
        // Write file contents to server
        int count = 0; //keep track of bytes read
        while ((count = fileIn.read(buffer)) >= 0) { //continue to read from file till EOF is reached
            socketOut.write(buffer, 0, count); // Write bytes to socket
        }   	
        fileIn.close();
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public int clientChoice(String[] args) throws IOException {
    	boolean wFlag = false; //Write flag
    	boolean sFlag = false; //Starting byte flag
    	boolean eFlag = false; //End byte flag
    	int j;
    	
    	for(int i = 0; i < args.length; i++) { //checking which flags have been included 
    		if(args[i].equals("-w")) {
    			wFlag = true;
    		}
    		if(args[i].equals("-s")) {
    			j = i;
    			j++;
    			startByte = Integer.parseInt(args[j]); //extract value from string 
    			sFlag = true;
    		}
    		if(args[i].equals("-e")) {  			
    			j = i;
    			j++;
    			endByte = Integer.parseInt(args[j]); //extract value from string 
    			eFlag = true;
    		}	
    	}
    	    	
    	if(wFlag == false && sFlag == false && eFlag == false) { //receive file normally <Client> <filename>
    		return 1;  		   		
    	}
    	if(wFlag == false && sFlag == true && eFlag == true) { //receive only byte range <Client> <filename> [-s bytes] [-e bytes]
    		return 2;  		   		
    	}	
    	if(wFlag == true && sFlag == false && eFlag == false) { //send file to server <Client> <filename> [-w]
    		return 3;  		   		
    	}
    	else {
    		return 4; //none of the choices matched - wrong format 
    	}
    	
    }
        
}
