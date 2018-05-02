package com.zimbra.jmeter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;

public class Command {

  private String mName=null;
  private HashMap<String,String> mArgs=new HashMap<String,String>();

  public Command(String s) {
    // <command>(<arg1>=<value1>,<arg2>=<value2>,...)
    // (...) is optional
    // <value> is either \w or in ".."
    Pattern cmd       = Pattern.compile("^([^\\(]+)\\(([^\\)]+)\\)$");
    Pattern value     = Pattern.compile("^\"(.*)\"$");
    Matcher mcmd      = cmd.matcher(s);
    if (mcmd.find()) {
      setName(mcmd.group(1));
      String[] tokens = mcmd.group(2).split(
                           ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
      for (String t: tokens) {
        String[] kv=t.split("=");
        Matcher mvalue = value.matcher(kv[1]);
        if (mvalue.find()) {
          kv[1]=mvalue.group(1);
        }
        addArg(kv[0],kv[1]);
      }
    } else {
      setName(s);
    }
  }

  public String getName() {
    return mName;
  }

  public void setName(String name) {
    mName=name;
  }

  public void addArg(String key,String value) {
    mArgs.put(key,value);
  }

  public void removeArg(String key) {
    if (mArgs.containsKey(key)) {
      mArgs.remove(key);
    }
  }

  public String getArg(String key) {
    return mArgs.get(key);
  }

  public Set<Map.Entry<String,String>> argsSet() {
    return mArgs.entrySet();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    Command cmd = (Command) o;
    return getName().equals(cmd.getName()) &&
           argsSet().equals(cmd.argsSet());
  }

}
