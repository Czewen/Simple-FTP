
import java.lang.System;
import java.net.Socket;
import java.io.*;
import java.util.*;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program takes no arguments.
//


public class CSftp
{
    static final int MAX_LEN = 255;
    static Socket connection = null;
    static boolean cisco = false;
    public static void main(String [] args)
    {
		
		try {
			String hostname = "";
			
			PrintStream out =  null;
			BufferedReader in =  null;
			
		    for (int len = 1; len > 0;) {
		    	byte cmdString[] = new byte[MAX_LEN];
		
				System.out.print("csftp> ");
				len = System.in.read(cmdString);
				if (len <= 0) 
				    break;
				// Start processing the command here.

				
				String cmd = readableText(cmdString, 0, 255);
				String[] cmd_split = cmd.split(" ");
			
				switch(cmd_split[0]){
				case "open":
					if(connection == null){
						if(cmd_split.length==3){
							hostname = cmd_split[1];
							try{
								int portNumber = Integer.parseInt(cmd_split[2]);
								connection = openSocket(connection, hostname, portNumber);
							}
							catch(Exception e){
								System.out.println("802 Invalid Argument.");
								break;
							}
						}
						else if(cmd_split.length==2){
							hostname = cmd_split[1];
							int portNumber = 21;
							connection = openSocket(connection, hostname, portNumber);
						}
						else
							System.out.println("801 Incorrect number of arguments.");
					}
					else{
						System.out.println("803 Supplied command not expected at this time.");
					}
					break;
					
				case "user":
					if(connection!=null){
						if(cmd_split.length==2){
							String username = cmd_split[1];
							String server_response = sendToServer("USER "+username+"\r\n");
							if(server_response!=null){
								//if connection timed out
								if(server_response.startsWith("421 ")){
	                        		try{
	                        			connection.close();
	                        			connection = null;
	                        		}
	                        		catch(IOException e){
	                        			System.out.println("899 Processing error. "+e.getMessage());
	                        		}
	                        		break;
								}
								if(server_response!=null)
									System.out.println("<-- "+server_response);
								
								if(server_response.startsWith("331 ")){
									System.out.print("Password: ");
									Scanner scan = new Scanner(System.in);
									String pw = scan.nextLine();
									String password_response = "<-- "+ sendToServer("PASS "+pw+"\r\n");
									System.out.println(password_response);
									
									if(password_response.contains("230 ")){
										//switch server to binary mode
										String response = sendToServer("TYPE I"+"\r\n");
										System.out.println("<-- "+response);
									}
								}
							}
							else{
								System.out.println("803 Supplied command not expected at this time.");
							}
						}
						else{
							System.out.println("801 Incorrect number of arguments.");
						}
					}
					else{
						System.out.println("803 Supplied command not expected at this time.");
					}
					break;
					
				case "dir":
					if(connection!=null){
						String pasv_response = sendToServer("PASV"+"\r\n");
						if(pasv_response!=null){
							System.out.println("<-- "+pasv_response);
							
							if(pasv_response.startsWith("227 ")){
								
								int[] numbers = findNumbers(pasv_response);
								
								int portNumber = numbers[4]*256 + numbers[5];
								String ipaddress = formIP(numbers[0], numbers[1], numbers[2], numbers[3]);
								Socket data = null; 
								data = openDataSocket(data, ipaddress, portNumber);
								if(data!=null){
									String response = dir(data);
									if(response!=null && !response.equals("")){
										System.out.println("<-- "+response);
									}
								}
							}
							else if(pasv_response.startsWith("421 ")){
	                        	try{
	                        		connection.close();
	                        		connection = null;
	                        	}
	                        	catch(IOException e){
	                        		System.out.println("899 Processing error. "+e.getMessage());
	                        	}
							}
						}
						else{
							System.out.println("803 Supplied command not expected at this time.");
						}
					}
					else
						System.out.println("803 Supplied command not expected at this time.");
					break;
					
				case "get":
					if(connection!=null){
						if(cmd_split.length==2){
							String pasv_response = sendToServer("PASV"+"\r\n");
							if(pasv_response!=null){
								System.out.println("<-- "+pasv_response);
								
								if(pasv_response.startsWith("227 ")){
									
									String filename = cmd_split[1];
									
									int[] numbers = findNumbers(pasv_response);
									int portNumber = numbers[4]*256 + numbers[5];
									String ipaddress = formIP(numbers[0], numbers[1], numbers[2], numbers[3]);
									Socket data = null;
									data = openDataSocket(data, ipaddress, portNumber);
									String get_response = get(data, filename );
									if(get_response!=null){
										System.out.println("<-- "+get_response);
									}
								}
								
								else{
									System.out.println("<-- "+pasv_response);
									//if connection timed out
									if(pasv_response.startsWith("421 ")){
		                        		try{
		                        			connection.close();
		                        			connection = null;
		                        		}
		                        		catch(IOException e){
		                        			System.out.println("899 Processing error. "+e.getMessage());
		                        		}
									}
								}
							}
							else{
								System.out.println("803 Supplied command not expected at this time.");
							}
						}
						else{
							System.out.println("801 Incorrect number of arguments");
						}
					}
					else{
						System.out.println("803 Supplied command not expected at this time.");
					}
					break;
					
				case "stor":
					if(connection!=null){
						if(cmd_split.length==2){
							String pasv_response = sendToServer("PASV"+"\r\n");
							if(pasv_response!=null){
								System.out.println("<-- "+pasv_response);
								if(pasv_response.startsWith("227 ")){
									
									
									String filename = cmd_split[1];
									int[] numbers = findNumbers(pasv_response);
									int portNumber = numbers[4]*256 + numbers[5];
									String ipaddress = formIP(numbers[0], numbers[1], numbers[2], numbers[3]);
									Socket data = null;
									data = openDataSocket(data, ipaddress, portNumber);
									String stor_response = stor(filename, data);
									if(stor_response!=null){
										System.out.println("<-- "+stor_response);
										
									}
								}
								//if connection timed out
								else if(pasv_response.startsWith("421 ")){
	                        		try{
	                        			connection.close();
	                        			connection = null;
	                        		}
	                        		catch(IOException e){
	                        			System.out.println("899 Processing error. "+e.getMessage());
	                        		}
								}
							}
							else{
								System.out.println("803 Supplied command not expected at this time.");
							}
						}
						else{
							System.out.println("801 Incorrect number of arguments");
						}
					}
					else{
						System.out.println("803 Supplied command not expected at this time.");
					}
					break;
					
					
				case "close":
					if(connection==null)
						System.out.println("803 Supplied command not expected at this time.");
					else{
						System.out.println("Closing connections...");
						String server_response = sendToServer("QUIT"+"\r\n");
						if(server_response!=null)
							System.out.println("<-- "+server_response);
						try{
							connection.close();
							connection = null;
						}
						catch(IOException e){
							System.out.println("An error occured while trying to close the socket.");
						}
					}
					hostname = null;
					break;
				
				case "quit":	
					System.out.println("Closing socket.");
					if(in!=null)
						in.close();
					if(out!=null)
						out.close();
					if(connection!=null){
						try{
							connection.close();
							connection = null;
						}
						catch(IOException e){
							System.out.println("899 Processing error. "+e.getMessage());
						}
					}
					System.out.println("Terminating...");
					System.exit(-1);
					break;
                        
                        
                case "cd":
                        if(connection!=null){
                            if(cmd_split.length==2){
                                String DIRECTORY = cmd_split[1];
                                String cd_response = sendToServer("CWD "+DIRECTORY+"\r\n");
                                
                                if(cd_response!=null){
                                	System.out.println("<-- "+cd_response);
                                	//if connection timed out
                                	if(cd_response.startsWith("421 "))
                                		try{
                                			connection.close();
                                			connection = null;
                                		}
                                		catch(IOException e){
                                			System.out.println("899 Processing error. "+e.getMessage());
                                		}
                                }
                                else{
            						System.out.println("803 Supplied command not expected at this time.");
            					}
                            }
                            else{
                            	System.out.println("801 Incorrect number of arguments.");
                            }
                        }
                        else{
                            System.out.println("803 Supplied command not expected at this time.");
                        }
                        break;
				default:
					System.out.println("800 Invalid command.");
					break;
				}
					
		   }
		} 
		catch (IOException exception) {
		    System.err.println("898 Input error while reading commands, terminating.");
		    System.exit(-1);
		}
	}
    
    
    
    public static boolean isPrintableAscii(byte value)
    {
        return (value >= 32 ) && (value < 127);
    }

    public static String readableText(byte[] buffer, int offset, int bufferSize)
    {
        StringBuilder builder = new StringBuilder();
        for( int index = 0; index < bufferSize; ++index)
        {
            byte current = buffer[offset+index];
            if( isPrintableAscii(current))
            {
                builder.append((char)current);
            }
        }
        return builder.toString();
    }
    
    public static String sendToServer(String request){
    	
    	try{
    		PrintStream out = new PrintStream(connection.getOutputStream());
    		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    		
    		out.print(request);
    		String s = in.readLine();
    		
    		if(request.contains("PASS ") && s.contains("microsoft")){
    			System.out.println("<-- "+s);
    			s = in.readLine();
    		}
    		
    		return s;
    	}
    	catch(IOException e){
    		System.out.println("825 Control connection I/O error, closing control connection.");
    		System.out.println(e.getMessage());
    		try{
    			connection.close();
    			connection = null;
    			return null;
    		}
    		catch(IOException ie){
    			System.out.println("899 Processing error. "+ie.getMessage());
    			connection = null;
    			return null;
    		}
    	}
    }
    
    
    
   public static Socket openSocket(Socket socket, String hostname, int portNumber){
	   try{
		   socket = new Socket(hostname, portNumber);
		   BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		   if(in!=null)
			   System.out.println("<-- " + in.readLine());
		  if(hostname.contains("cisco")){
			  String line = null;
			  for(int i=0; i<116; i++)
				  System.out.println(in.readLine());	  
		  }
			  
		   
		   return socket;
	   }
	   catch(IOException e){
		   System.out.println("820 Control connection to "+hostname+ " on port "+Integer.toString(portNumber)
					+" failed to open.");
	   }
	   return null;
   }
   
   public static Socket openDataSocket(Socket socket, String hostname, int portNumber){
	   try{
		   socket = new Socket(hostname, portNumber);
		   return socket;
	   }
	   catch(IOException e){
		   System.out.println("830 Data transfer connection to "+hostname+" on port "+portNumber +" failed to open.");
	   }
	   return null;
   }
   
   public static int[] findNumbers(String pasv_response){
	   int[] numbers_formatted = new int[6];
	   String[] pasv_split = pasv_response.split(" ");
	   String[] numbers_unformatted = pasv_split[4].split(",");
	   
	   //ip address
	   numbers_formatted[0] = Integer.parseInt(numbers_unformatted[0].replaceAll("[^\\d.]", ""));
	   numbers_formatted[1] = Integer.parseInt(numbers_unformatted[1]);
	   numbers_formatted[2] = Integer.parseInt(numbers_unformatted[2]);
	   numbers_formatted[3] = Integer.parseInt(numbers_unformatted[3]);
	   
	   //p1 and p2 
	   numbers_formatted[4] = Integer.parseInt(numbers_unformatted[4]);
	   String s = numbers_unformatted[5].replaceAll("[^\\d.]", "");
	   String d = s.replaceAll("[.]", "");
	  
	   numbers_formatted[5] = Integer.parseInt(d);
	   
	   return numbers_formatted;
   }
   
   public static String dir(Socket datasocket){
	   try{
		   BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		   PrintStream out = new PrintStream(connection.getOutputStream());
		   try{
			   BufferedReader data_in = new BufferedReader(new InputStreamReader(datasocket.getInputStream()));
			   out.print("LIST"+"\r\n");
			   System.out.println(in.readLine());
			   
			   String line = null;
			   while((line=data_in.readLine())!=null)
				   System.out.println(line);
			  
			   line = null;
			   if((line=in.readLine())!=null)
				   System.out.println("<-- "+line);
			   
			   datasocket.close();
			   //System.out.println("dir close socket");
			   return null;
			  
		   }
		   catch(IOException e){
			   System.out.println("835 Data transfer connection I/O error, closing data connection.");
				 datasocket.close();
		   }
	   }
	   catch(IOException e){
		   System.out.println("825 Control connection I/O error, closing control connection.");
		   try{
			   
			   connection.close();
			   connection = null;
			 
		   }
		   catch(IOException ce){
			   System.out.println("899 Processing error. "+ce.getMessage());
			   connection = null;
		   }
	   }
	   return null;
   }
   
   public static String get( Socket datasocket, String filename) throws IOException{
	   try{
		   BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		   PrintStream out = new PrintStream(connection.getOutputStream());
		   String s = null;
		   try{
			   InputStream data_in = datasocket.getInputStream();
			   try{
				   out.print("RETR "+filename+"\r\n");
				   String filepath = System.getProperty("user.dir");
				   
				   filepath+= "\\" +filename;
				   //for unix i hate
				   //curent windows
				   filepath+= "\\"+filename;
				   String control_response = in.readLine();
				   if(control_response.startsWith("150 ") || control_response.startsWith("125 ")){
					  boolean success = writeToDisk(filepath, data_in);
					  if(!success){
						  s = "899 Processing error. File transfer was unsuccesful, closing data connection.";
						  datasocket.close();
						  return s;
					  }
					  else{
						  s = in.readLine();
					  }
				   }
				   else{
					   return control_response;
				   }
			   }
			   catch(IOException e){
				   System.out.println("835 Data transfer connection I/O error, closing data connection.");
				   datasocket.close();
			   }
		   }
		   catch(IOException e){
			   System.out.println("835 Data transfer connection I/O error, closing data connection.");
			   datasocket.close();
		   }
		   return s;
	   }
	   catch(IOException e){
		   System.out.println("825 Control connection I/O error, closing control connection.");
		   connection.close();
		   connection = null;
	   }
	   return null;
   }
   
   private static boolean writeToDisk(String filepath, InputStream stream){
	   
	   try{
		   File destination = new File(filepath);
		   FileOutputStream file_out = new FileOutputStream(destination);
		   int numberofbytes = 0;
		   while(numberofbytes>-1){
			   byte[] buffer = new byte[1024];
			   
			   try{
				 //store byte data into buffer	
			   		numberofbytes = stream.read(buffer);
			   }
			   catch(IOException e){
				   System.out.println("835 Data transfer connection I/O error, closing data connection. ");
			   }
			   try{
				   //write byte data into the destination file
				   if(numberofbytes>-1)
					   file_out.write(buffer, 0, numberofbytes);
			   }
			   catch(IOException e){
				   System.out.println("899 Processing error. Error writing to "+filepath);
			   }
			   
		   }
		   file_out.flush();
		   file_out.close();
		   return true;
	   }
	   catch(FileNotFoundException fe){
		   System.out.println("810 Access to local file " +filepath+" denied");
	   }
	   catch(IOException e){
		   System.out.println("899 Processing error. Error creating file at "+filepath);
	   }
	   return false;
   }
   
   private static String stor(String filename, Socket datasocket) throws IOException{
	   try{
		   BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		   PrintStream out = new PrintStream(connection.getOutputStream());
		   String s = null;
		   try{
			   OutputStream data_out = datasocket.getOutputStream();
			   String filepath = System.getProperty("user.dir");
			   //filepath += "\\"+filename;
			   //for unix i hate 
			   //current windows
			   filepath+= "\\"+filename;
			   try{
				  
				   File source = new File(filepath);
				   FileInputStream source_data = new FileInputStream(source);
				   
				   out.print("STOR "+filename+"\r\n");
				   String control_response = in.readLine();
				   
				   if(control_response.startsWith("150 ")){
					   System.out.println("<-- "+control_response);
					   try{
					  
						   int numberofbytes = 0;
						   
						   while(numberofbytes>-1){
							   byte[] buffer = new byte[1024];
							   try{
								   numberofbytes = source_data.read(buffer);
							   }
							   catch(IOException readerror){
								   System.out.println("899 Processing Error. "+readerror.getMessage());
								   source_data.close();
								   data_out.close();
								   data_out = null;
								   return null;
							   }
							   if(numberofbytes>-1){
								   try{
									   data_out.write(buffer, 0, numberofbytes);
								   }
								   catch(IOException e){
									   System.out.println("835 Data transfer connection I/O error, closing data connection.");
									   source_data.close();
									   data_out.close();
									   data_out = null;
									   return null;
								   }
							   } 
						   }
						   source_data.close();
					   }
					   
					   catch(IOException ioe){
						   System.out.println("899 Processing error. "+ioe.getMessage());
						   source_data.close();
						   datasocket.close();
						   return null;
					   }	  
				   }
				   else{
					   try{
						   source_data.close();
					   }
					   catch(IOException ioe){
						   System.out.println("899 Processing error. "+ioe.getMessage());
					   }
					   return control_response;
				   }
			   }
			   catch(FileNotFoundException fe){
				   System.out.println("810 Access to local file " +filepath+" denied");
				   datasocket.close();
				   return null;
			   }
			   catch(IOException e){
				   System.out.println("835 Data transfer connection I/O error, closing data connection.");
				   datasocket.close();
			   }
			   datasocket.close();
			   s = in.readLine();
			  return s;
		   }
		   catch(IOException e){
			   System.out.println("835 Data transfer connection I/O error, closing data connection.");
			   datasocket.close();
		   }
		   return null;
	   }
	   catch(IOException e){
		   System.out.println("825 Control connection I/O error, closing control connection.");
		   connection.close();
		   connection = null;
		   return null;
	   }
	   
   }
   
   
   
   private static String formIP(int number1, int number2, int number3, int number4){
	   String ipaddress = Integer.toString(number1)+"."+Integer.toString(number2)+"."+Integer.toString(number3)+"."+Integer.toString(number4);
	   return ipaddress;
   }
   
   

}
