package com.zimbra.jmeter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.util.Map;

public class TestCommand {
  @Test
  public void name() {
    Command a = new Command("SearchRequest");
    assertEquals(a.getName(),"SearchRequest");
  }

  @Test
  public void nameArgSimple() {
    Command a = new Command("SearchReqeust(SEARCHTYPE=message)");
    assertEquals(a.getName(),"SearchReqeust");
    assertEquals(a.getArg("SEARCHTYPE"),"message");
  }

  @Test
  public void nameArgComplex() {
    Command a = new Command("SearchReqeust(SEARCHTYPE=message,SEARCH=\"is:invite not from:${A},${USER}\")");
    assertEquals(a.getName(),"SearchReqeust");
    assertEquals(a.getArg("SEARCHTYPE"),"message");
    assertEquals(a.getArg("SEARCH"),"is:invite not from:${A},${USER}");
  }

  @Test
  public void argsSet() {
    Command a = new Command("AuthRequest(A=1,B=2,C=3,D=4)");
    assertEquals(a.getName(),"AuthRequest");
    assertEquals(a.argsSet().size(),4);
    for (Map.Entry<String,String> m: a.argsSet()) {
      assertEquals(m.getValue(),a.getArg(m.getKey()));
    }
  }

  @Test
  public void equals() {
    Command a = new Command("SearchRequest");
    Command b = new Command("SearchRequest");
    assertTrue(a.equals(b));
  }
}
