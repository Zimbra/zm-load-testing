package com.zimbra.jmeter;

import java.io.*;
import com.zimbra.jmeter.Zimbra;
import java.util.ArrayList;
import java.util.Properties;

public class test {
  public static void main(String[] args) throws Exception {
    // args[0] action command
    // args[1] protocol SOAP|IMAP|POP|LMTP|SMTP|...
    // args[2] property file
    // args[3] iterations defaults to 1
    
    // Get properties file
    Properties p = new Properties();
    FileInputStream in = new FileInputStream(args[2]);
    p.load(in);
    in.close();
    
    // Initialize Zimbra jmeter module 
    Zimbra z = new Zimbra(p,args[1]);

    if (args[0].equals("command")) {
  
      // Get commands for desire iterations
      int max = 1;
      if (args.length>3) { max = Integer.parseInt(args[3]); }
      for (int i=1;i<=max;i++) {
        if (i > 1) { System.out.println(); }
        System.out.println("Iteration "+i);
        ArrayList<Command> test=z.getcommands();
        while (test.size() > 0) {
          Command tmp = test.remove(0);
          System.out.println(tmp.getName());
        }
      }
  
      // generate stats from usage
      System.out.println();
      z.stats();
    }
  }
}
