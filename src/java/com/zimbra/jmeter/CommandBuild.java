package com.zimbra.jmeter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

public abstract class CommandBuild extends Build {

  private HashMap<String,Integer> mHcount = new HashMap<String,Integer>();
  private Integer total = 0;

  public CommandBuild(Properties prop, String profile) {
    super(prop,profile);
  }

  public abstract ArrayList<Command> commands() throws Exception;

  public void increment(Command cmd) {
    if (mHcount.containsKey(cmd.getName())) {
      mHcount.put(cmd.getName(),mHcount.get(cmd.getName())+1);
    } else {
      mHcount.put(cmd.getName(),1);
    }
    total=total+1;
  }

  public void stats() {
    Iterator<String> i = mHcount.keySet().iterator();    
    while (i.hasNext()) {
      String key = i.next();
      System.out.println(key+" "+mHcount.get(key));
    }
  }

}
