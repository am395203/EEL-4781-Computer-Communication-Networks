===================================== Title =====================================
EEL4781_CMB-21Spring 00396 Programming Project: Simple File Server

===================================== Description =====================================

Given sample code in java, 2 programs are created: (1) a simple file server that responds to client requests
and (2) a client that requests the file(s) from the server. The client can request a file from the server,
send a file to the server, or request a byte range from the server. The server will be able to respond accordingly
 and run in debug mode when debug 1 is used in the command. 


************************************* IMPORTANT *************************************
*Port number must be specified first in string[0] for both Server and Client 
	ex: java Server 8900
	ex: java Client 8900
*Filename must be specified in string[1]
	ex: java Client 8900 filename.txt

Note - there are multiple issues with error detection
===================================== SERVER COMMANDS =======================================
To run the server use the following commands:
#To run the server normally:
	java Server <Port Number>
ex: 	java Server 8900

#To run the server in debug mode:
	java Server <Port Number> [debug 1] 
ex:	java Server 8900 debug 1


===================================== CLIENT COMMANDS =======================================
To run the client use the following commands:
#To run the client normally:
	java Client <Port Number> <filename>
ex:	java Client 8900 test.txt

#To specifly a byte range from server:
	java Client <Port Number> <filename> [-s <bytes>] [-e <bytes>]
ex:	java Client 8900 test.txt -s 50 -e 90

#To send a file to the server:
	java Client <Port Number> <filename> [-w]
ex:	java Client 8900 test.txt -w







