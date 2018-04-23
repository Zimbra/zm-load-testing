package com.zimbra.jmeter;

import java.util.ArrayList;
import java.util.Properties;

public class Zimbra {

  private Properties mProp;
  private String mProfile;
  private CommandBuild mCb;

  public Zimbra(Properties prop,String profile) throws Exception {
    mProp=prop;
    mProfile=profile;
    String type = getProp().getProperty("PROFILE."+getProfile()+".type");
    if (type != null) {
      if (type.equals("sequence")) {
        mCb = new SequenceCommandBuild(getProp(),getProfile());
      } else {
        throw new Exception("Failed to find supported profile type");
      }
    } else {
      System.err.println("warning: PROFILE."+getProfile()+
                         ".type not found in property file");
    }
  }

  public Properties getProp() {
    return mProp;
  }

  public String getProfile() {
    return mProfile;
  }

  public ArrayList<Command> getcommands() throws Exception {
    return mCb.commands();
  }

  public void stats() {
    mCb.stats();
  }

}
