import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;

public class Server {
    private final int BUFFER_SIZE = 4096;
    private Socket connection;
    private ServerSocket socket;
    private DataInputStream socketIn;
    private DataOutputStream socketOut;
    private FileInputStream fileIn;
    private String filename;
    private int bytes;
    private int start = 0;
    private int end = 0;
    private boolean debug = false;
    private byte[] buffer = new byte[BUFFER_SIZE];

    public Server(int port, String[] fi) {
        try {
            socket = new ServerSocket(port);
            
            // Wait for connection and process it
            while (true) {
                try {
                    connection = socket.accept(); // Block for connection request
                    socketIn = new DataInputStream(connection.getInputStream()); // Read data from client
                    socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to client
                    serverChoice(fi); //check if server is in debug mode 
                    
                    int clientChoice = socketIn.readInt(); //1 -> receive file, 2 -> receive byte range, 3 -> send file to server
                    
///////////REGULAR OPTIONS//////////////////////////////////////////////////////////////////////
                    if(clientChoice == 1 && debug == false) { //send file to client X
                    	sendFileServer();
                    }
                    if(clientChoice == 2 && debug == false) { //send byte range to client 
                    	sendByteServer();
                    }
                    if(clientChoice == 3 && debug == false) { //receive file from client X
                    	 receiveFileServer();
                    }
                    
///////////DEBUG OPTIONS///////////////////////////////////////////////////////////////////////////////
                    if(clientChoice == 1 && debug == true) { //send file to client in debug mode X
                    	sendFileDebug();
                    }
                    if(clientChoice == 2 && debug == true) {//send byte range to client in debug mode 
                    	sendByteDebug();
                    }
                    if(clientChoice == 3 && debug == true) { //writing to server while debug is on 
                    	System.out.println("ERROR: Can Not Send File to Server While Server is in Debug Mode.");
                    }
                               
                } catch (Exception ex) {
                    System.out.println("Error: " + ex);
                } finally {
                    // Clean up socket and file streams
                    if (connection != null) {
                        connection.close();
                    }
                }
            }
        } catch (IOException i) {
            System.out.println("Error: " + i);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
    
    public static void main(String[] args) {
    	int portNum = Integer.parseInt(args[0]);  	
        Server server = new Server(portNum, args);
    }  
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void sendFileServer() throws IOException { //Send file to client  Xx
        socketIn = new DataInputStream(connection.getInputStream()); // Read data from client
        socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to client
        filename = socketIn.readUTF(); // Read filename from client
        
        File tempFile = new File(filename);//Cjeck if file exists before sending 
        if(!tempFile.exists()) {
        	System.out.println("ERROR:" + " File: " + filename + " Does Not Exist");
          
        	return;
        }
        FileInputStream fileIn = new FileInputStream(filename); //New FileInputStream to read contents of file                   
        
        // Write file contents to client
        int count = 0; //keep track of bytes read
        while ((count = fileIn.read(buffer)) >= 0) { //continue to read till end of stream
            socketOut.write(buffer, 0, count); // Write bytes to socket
        }
        fileIn.close();
    }
         
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
        
    public void receiveFileServer() throws IOException { //read and save file from client  Xx
        socketIn = new DataInputStream(connection.getInputStream()); // Read data from client
        socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to client
        filename = socketIn.readUTF(); // Read filename from client
                 
        File tempFile = new File(filename);//Check if file exists before sending 
        if(tempFile.exists()) {
        	System.out.println("ERROR:" + " File: " + filename + " Already Saved to Server.");
        	return;
        }
          
        FileOutputStream fos = new FileOutputStream(new File(filename)); //new FileOutputStream to write contents to new file
        
        int readBytes = 0; //bytes read
        while((readBytes = socketIn.read(buffer, 0, buffer.length)) != -1) { //continue reading until end of file has been reached	
        	fos.write(buffer, 0, readBytes); //write read contents to buffer
        }
        System.out.print("File " + filename + " has been saved.\n"); //notify server file has been saved 
        fos.close();
    }
      
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
    public void sendFileDebug() throws IOException { //send file in the debug mode  Xx
        socketIn = new DataInputStream(connection.getInputStream()); // Read data from client
        socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to client
        filename = socketIn.readUTF(); // Read filename from client       
                
        File tempFile = new File(filename);//Check if file exists before sending 
        if(!tempFile.exists()) {
        	System.out.println("ERROR:" + " File: " + filename + " Does Not Exist");
          
        	return;
        }
                      
        FileInputStream fileIn = new FileInputStream(filename); //new FileOutputStream to read from file 
            
        System.out.println("Sending " + filename + " to " + connection.getInetAddress()); //Print out before printing progress      
        byte[] fileBytes; //store read bytes from file
        long fileSize = new File(filename).length(); //Obtain the length of the file to be sent 
        
        long current = 0; //keep track of how many bytes have been read
        while(current!=fileSize){ //continue till fileSize has been reached 
        int size = (int) java.lang.Math.ceil((fileSize / 10.0)); //size of bytes that will be read for 10% progression (/by 10)
        if(fileSize - current >= size) { //if there are still bytes 
        	current += size; //add size of bytes to current
        }
       else{ //if statement fails	   
	       size = (int)(fileSize - current);
	       current = fileSize; //set bytes read to file size
        }
        fileBytes = new byte[size]; //initialize array to size of bytes 
        fileIn.read(fileBytes, 0, size); //read from 0 to size and store in fileBytes
        socketOut.write(fileBytes); //write bytes to client 
        
        double progress = ((double)current/(double)fileSize) *100; //keep track of progress 
        progress = java.lang.Math.floor(progress); //get the floor of the value
        
    	if((progress % 10) != 0) { //if there are excess numbers ex: 91, 92..
    		int excess = (int) (progress %10); //get that excess ex: 1,2,...
    		progress -= excess; //and subtract to get tens, ex: now have 90
    	}
        
        System.out.println("Sent "+ progress +" % of " + filename); //print our progress 
        }
        System.out.println("Finished sending " + filename + " to " + connection.getInetAddress());
        fileIn.close();

    }
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
        
    public void sendByteServer() throws IOException { //Send requested bytes to client XX
        socketIn = new DataInputStream(connection.getInputStream()); // Read data from client
        socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to client
        filename = socketIn.readUTF(); // Read filename from client        
        start = socketIn.readInt(); //read start bytes from client 
        end = socketIn.readInt(); //read end bytes from server 
        
        File tempFile = new File(filename);//Check if file exists before sending 
        if(!tempFile.exists()) {
        	System.out.println("ERROR:" + " File: " + filename + " Does Not Exist");
          
        	return;
        }
        
        long fileSize = new File(filename).length(); //Obtain the length of the file to be sent 
        
        if(start < 0 || end < 0) { //checking if start or end bytes are negative
        	System.out.println("ERROR: Bytes Can Not be Less Than 0");
        	return;
        }
        if(end > fileSize) { //checking if end bytes exceed the file size
        	System.out.println("ERROR: End Byte Larger Than File Size");
        	return;
        }
        if(end < start) { //checking if end bytes are smaller than start
        	System.out.println("ERROR: End Byte is Smaller Than Starting Byte");
        	return;
        }
              
        FileInputStream fileIn = new FileInputStream(filename); //New FileInputStream to read contents of file  
        fileIn.skip((start-1)); //skip by start-1 bytes (start included)
        
        long bytesToRead = end - start+1; //how many bytes we need to read
        while(bytesToRead > 0) { //continue while there are still bytes to be read
        	int read = fileIn.read(buffer, 0, (int)Math.min(BUFFER_SIZE, bytesToRead));
        	socketOut.write(buffer, 0, read); //write to socket 
        	bytesToRead -= read; //subract number of bytes read from bytesToRead
        } 
        fileIn.close();
    }
        
        
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
     
    public void sendByteDebug() throws IOException { //send bytes in the debug mode XX
        socketIn = new DataInputStream(connection.getInputStream()); // Read data from client
        socketOut = new DataOutputStream(connection.getOutputStream()); // Write data to client
        filename = socketIn.readUTF(); // Read filename from client    
        start = socketIn.readInt(); //read start bytes from client 
        end = socketIn.readInt(); //read end bytes from server 

        File tempFile = new File(filename);//Check if file exists before sending 
        if(!tempFile.exists()) {
        	System.out.println("ERROR:" + " File: " + filename + " Does Not Exist");       
        	return;
        } 
        
        FileInputStream fileIn = new FileInputStream(filename); //new FileOutputStream to read from file 
        long fileSize = new File(filename).length(); //Obtain the length of the file to be sent 
        
        if(start < 0 || end < 0) { //checking if start or end bytes are negative
        	System.out.println("ERROR: Bytes Can Not be Less Than 0");
        	fileIn.close();
        	return;
        }
        if(end > fileSize) { //checking if end bytes exceed the file size
        	System.out.println("ERROR: End Byte Larger Than File Size");
        	fileIn.close();
        	return;
        }
        if(end < start) { //checking if end bytes are smaller than start
        	System.out.println("ERROR: End Byte is Smaller Than Starting Byte");
        	fileIn.close();
        	return;
        }
          
        System.out.println("Sending " + filename + " to " + connection.getInetAddress()); //Print out before printing progress      
        byte[] fileBytes; //store read bytes from file
        long bytesToRead = (end-start+1); //length of bytes to read: start - end + 1
        fileIn.skip((start-1)); //skip intial bytes, start included so -1

        long current = 0; //keep track of how many bytes have been read
        while(current != bytesToRead){ //continue till end byte has been reached 
        int size = (int) java.lang.Math.ceil((bytesToRead / 10.0)); //size of bytes that will be read for 10% progression (/by 10)
        if(bytesToRead - current >= size) { //if there are still bytes 
        	current += size; //add size of bytes to current
        }
       else{ //if statement fails	   
	       size = (int)(bytesToRead - current);
	       current = bytesToRead; //set bytes read to bytesRead
        }
        fileBytes = new byte[size]; //initialize array to size of bytes 
        fileIn.read(fileBytes, 0, size); //read from 0 to size and store in fileBytes
        socketOut.write(fileBytes); //write bytes to client 
        
        double progress = ((double)current/(double)bytesToRead) *100; //keep track of progress 
        progress = java.lang.Math.floor(progress); //get the floor of the value
        
    	if((progress % 10) != 0) { //if there are excess numbers ex: 91, 92..
    		int excess = (int) (progress %10); //get that excess ex: 1,2,...
    		progress -= excess; //and subtract to get tens, ex: now have 90
    	}
        
        System.out.println("Sent "+ progress +" % of " + filename); //print our progress 
        }
        System.out.println("Finished sending " + filename + " to " + connection.getInetAddress());
        fileIn.close();

    }    
     
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void serverChoice(String args[]) {
    	boolean debugFlag =false;
    	boolean setFlag = false;
    	int i;
    	
    	for(i = 0; i < args.length; i++) {
        	if(args[i].equals("\0")) {
        		return;
        	}
        	if(args[i].equals("debug")) {
        		debugFlag = true;
        	}   	
        	if(args[i].equals("1")) {
        		setFlag = true;
        	} 	
    	}
    	
    	if(debugFlag == true && setFlag == true) {
    		debug = true;
    	}
    	else {
    		debug = false;
    	}
    	return; 	
    }
     
    
}
