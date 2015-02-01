
import java.lang.System;
import java.io.IOException;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program takes no arguments.
//


public class CSftp
{
    static final int MAX_LEN = 255;

    public static void main(String [] args)
    {
		byte cmdString[] = new byte[MAX_LEN];
		try {
		    for (int len = 1; len > 0;) {
				System.out.print("csftp> ");
				len = System.in.read(cmdString);
				if (len <= 0) 
				    break;
				// Start processing the command here.
				String cmd = readableText(cmdString, 0, 255);
				if(!checkCmdString(cmd)){
					System.out.println("800 Invalid command.");
					break;
				}
					
		   }
		} 
		catch (IOException exception) {
		    System.err.println("998 Input error while reading commands, terminating.");
		}
	}
    
    public static boolean checkCmdString(String cmdString){
    	boolean correct = false;
    	
    	String[] accepted_inputs = {"open", "user", "close", "quit", "get", "put", "cd", "dir"};
    	for(int i=0; i<accepted_inputs.length; i++){
    		if(cmdString.equals(accepted_inputs[i])){
    			correct = true;
    			System.out.println("match");
    		}
    	}
    	return correct;
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
    
}
