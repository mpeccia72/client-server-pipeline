/*
      Course: CS 33600
      Name: Michael Peccia
      Email: mpeccia@pnw.edu
      Assignment: 2
*/

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder; // orders endian

class Client
{
   public static void main(String[] args)
   {

      final BufferedInputStream in = new BufferedInputStream(System.in);
      
      final int size = 4096; // can increase array size if needed
      byte[] bytes = new byte[size]; 
      int bytesRead;
      int byteCounter = 0; // total number of bytes

      // while loop to read bytes
      while(true) {
         try {
            bytesRead = in.read(bytes);
            if(bytesRead != -1) 
               byteCounter += bytesRead;
            else  
               break;      
         }
         catch(IOException e) {
            break;
         }
      }

      // the bytes have all been read

      ByteBuffer buffer = ByteBuffer.wrap(bytes); // wrap
      buffer.order(ByteOrder.LITTLE_ENDIAN); // orders little endian

      /*
         The annoying thing about the ByteBuffer class is there is no function that allows access
         to all 8 bits at an index because if leading zeros exist, they are cut off. So 00010101 will
         be accessed as 10101. Measures are taken for these special cases below.
      */

      String instruction = ""; // current instruction 
      int leadingZeros = -1; // leading zeros in instruction (not visible)
      boolean readyForNewInstruction = true; // tells program if the next byte is a new instruction
      int type = -1; // this is the type of instruction, 0 = numeric, 1 = string
      int localIndex = -1; // index for scanning through each element of instruction

      int i = 0; // represents 0 bytes

      // algorithm
      while(true) {

         // new instruction is assigned
         if(readyForNewInstruction) {

            // terminates (0x80)
            if((buffer.getInt(i) & 0xFF) == 128)
               break;

            readyForNewInstruction = false; // new instruction begins 
            instruction = Integer.toBinaryString(buffer.get(i) & 0xFF); // assings binary instruction to string
            leadingZeros = 8 - instruction.length() - 1; // leading zero bits (not including most significant)
            localIndex = instruction.length() -  1;
            i++; // move up 1 byte (instruction takes up 1 byte)

            if(leadingZeros != -1) 
               type = 0; // message contains leading 0, it's numeric
            else  
               type = 1; // message is string (leadingZeros == -1 means that message begins with 1)
         
         }

         // executes for numeric
         if(type == 0) {

            // executes if current instruction bit represents an int
            if((localIndex >= 0 && instruction.charAt(localIndex) == '0') || (leadingZeros > 0 && localIndex < 0)) {

               // weird endian format
               byte[] weirdEndian = new byte[4];
               weirdEndian[0] = buffer.get(i + 2);
               weirdEndian[1] = buffer.get(i + 1);
               weirdEndian[2] = buffer.get(i);
               weirdEndian[3] = buffer.get(i + 3);

               // print
               System.out.println(ByteBuffer.wrap(weirdEndian).getInt(0));
               i += 4; // move up 4 bytes (int byte size)

               // for visible bits that are 0 (see line 45 - 49 for more information)
               if(localIndex >= 0 && instruction.charAt(localIndex) == '0')
                  localIndex--;
               // for invisible bits that are 0
               else  
                  leadingZeros--;

            }

            // executes if current instruction bit represents a double
            else if (localIndex >= 0 && instruction.charAt(localIndex) == '1') {
               System.out.printf("%.12f%n", buffer.getDouble(i));
               i += 8; // move up 8 bytes (double byte size)
               localIndex--;
            }

            // current instruction has finished
            else {
               readyForNewInstruction = true; // current instruction finished
            }            

         }

         // executes for strings
         else if(type == 1) {

            int j = 0;

            // this loop can run a maximum of 127 times depending on the value of the 7 least significant digits
            while(j < (buffer.getInt(i - 1) & 0xFF) - 128) {
               System.out.print((char)(buffer.getInt(i + j) & 0xFF)); // prints character at byte index
               j++;
            }

            i += j; // updates current byte index

            System.out.println();
            readyForNewInstruction = true; // current instruction finished
            
         }
         
      }

      // prints bytes
      System.out.printf("\nRead %d bytes from standard input.\n", byteCounter);

   }

}
