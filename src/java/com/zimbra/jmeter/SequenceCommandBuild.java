package com.zimbra.jmeter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SequenceCommandBuild extends CommandBuild {

  HashMap<Integer,Command> mPercent = new HashMap<Integer,Command>();

  public SequenceCommandBuild(Properties prop,String profile) {
    super(prop,profile);
    buildmap();
  }

  public ArrayList<Command> commands() {
    ArrayList<Command> commands = new ArrayList<Command>();

    for (int i=1; i<=mPercent.size(); i++ ) {
      Command tmp = mPercent.get(i);
      commands.add(tmp);
      increment(tmp);
    }

    return commands;
  }

  private void buildmap() {
    Pattern seq = Pattern.compile("PROFILE."+getProfile()+".sequence.(.*)");
    Enumeration e = getProp().propertyNames();
    while (e.hasMoreElements()) {
      String k = (String) e.nextElement();
      Matcher mseq = seq.matcher(k); 
      if (mseq.find()) {
        String v = getProp().getProperty(k);
        Command tmp = new Command(v);
        mPercent.put(Integer.parseInt(mseq.group(1)),tmp);
      }
    }
  }

}
