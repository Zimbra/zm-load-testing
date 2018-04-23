package com.zimbra.jmeter;

import java.util.Properties;

public abstract class Build {

  private Properties mProp;
  private String mProfile;

  public Build(Properties prop,String profile) {
    mProp=prop;
    mProfile=profile;
  }

  public Properties getProp() {
    return mProp;
  }

  public void setProp(Properties prop) {
    mProp=prop;
  }

  public String getProfile() {
    return mProfile;
  }

  public void setProfile(String profile) {
    mProfile=profile;
  }
}
