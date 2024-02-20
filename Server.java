/*
   Server program for Hw2.

   This program provides the input stream
   of data needed by the client program.
*/

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Random;
import java.nio.ByteBuffer;

class Server
{
   public static void main(String[] args)
   {
      final Random random = new Random();

      // Open the log file.
      final File logFile = new File("log_file.txt");
      PrintWriter log = null;
      try
      {
         log = new PrintWriter(
                   new BufferedWriter(
                       new FileWriter(logFile)));
      }
      catch (IOException e)
      {
         System.err.printf("ERROR! Could not open log file: %s\n", logFile);
         e.printStackTrace(System.err);
         System.exit(-1);
      }

      final int messages;  // Number of messages to send.
      if (0 < args.length) // Check for a command line argument.
      {
         // Send args[0] number of messages instead.
         messages = Integer.parseInt( args[0] );
      }
      else
      {
         // Random number of messages to send.
         messages = random.nextInt(1_000);
      }

      // Send the messages.
      for (int m = 0; m < messages; ++m)
      {
         // "Flip" a coin to determine the message type,
         // either a numeric message or a character message.
         if ( random.nextBoolean() ) // Send a numeric message.
         {
            // Choose a bit pattern for ints and doubles.
            int messageHeader = random.nextInt(128);
            // Send the message header.
            System.out.write(messageHeader);
            System.out.flush();
            log.printf("%s\n", toBinary(messageHeader));
            log.flush();

            // Send the numbers.
            for (int i = 0; i < 7; ++i, messageHeader >>= 1)
            {
               if ((0x01 & messageHeader) == 0x01) // send a double
               {
                  // Generate a random double.
                  final double d = random.nextInt() + random.nextDouble();

                  // Convert the double into its array of bytes.
                  final byte[] bytes = ByteBuffer.allocate(Double.BYTES).putDouble(d).array();

                  // Record the double in the log file.
                  log.printf("Sending double = %.12f (0x%02x%02x%02x%02x%02x%02x%02x%02x)\n",
                               d,
                               bytes[0], bytes[1], bytes[2], bytes[3],
                               bytes[4], bytes[5], bytes[6], bytes[7] );

                  // Send the double in little-endian byte order.
                  // NOTE: The JVM stores its doubles in big-endian order.
                  for (int j = 7; j >= 0; --j)
                  {
                     System.out.write(bytes[j]);
                            log.printf("%02x  ", bytes[j]);
                  }
                  System.out.flush();
                         log.printf("\n");
                         log.flush();
               }
               else // Send an int in weird-endian byte order.
               {
                  // Generate a random int.
                  final int k = random.nextInt();
                  log.printf("Sending int = %d (%#010x)\n", k, k);

                  // Convert the int into its array of bytes.
                  final byte[] bytes = ByteBuffer.allocate(Integer.BYTES).putInt(k).array();

                  // Send the int in weird-endian byte order.
                  // NOTE: The JVM stores its ints in big-endian order.
                  System.out.write(          bytes[2]); // 3rd most sig byte
                         log.printf("%02x ", bytes[2]);
                  System.out.write(          bytes[1]); // 2nd most sig byte
                         log.printf("%02x ", bytes[1]);
                  System.out.write(          bytes[0]); // most sig byte
                         log.printf("%02x ", bytes[0]);
                  System.out.write(          bytes[3]); // least sig byte
                         log.printf("%02x",  bytes[3]);

                  System.out.flush();
                         log.printf("\n");
                         log.flush();
               }
            }
            log.printf("\n");
            log.flush();
         }
         else // send a character message
         {
            // Number of characters in this message.
            final int count = random.nextInt(128);
            // Make the "message header" byte.
            final int messageHeader = 0x80 | count;
            // Send the message header byte.
            System.out.write(messageHeader);
            System.out.flush();
            log.printf("%s (%d characters)\n", toBinary(messageHeader), count);
            log.flush();
            // Send the characters.
            for (int i = 0; i < count; ++i)
            {
               // A random printable character (see an ascii table).
               int rndChar = 32 + random.nextInt(95);
               System.out.write(rndChar);
               System.out.flush();
               log.printf("%c", rndChar);
               log.flush();
            }
            log.printf("\n\n");
            log.flush();
         }
      }

      // Send an "end of data" message.
      System.out.write(0x80);
      System.out.flush();
      log.printf("%s\n", toBinary(0x80));
      log.flush();

      log.close();
   }


   /**
      @param c  an int whose least significant 8 bits will be converted to a String
      @return the binary String representation of the least significant 8 bits of c
   */
   public static String toBinary(int c)
   {
      String bits = "";
      for (int i = 0; i < 8; ++i)
      {
         if ( (c & 0x80) == 0x80 )
         {
            bits += "1";
         }
         else
         {
            bits += "0";
         }
         c <<= 1;
      }
      return bits;
   }
}
