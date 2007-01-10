package org.codehaus.dimple;

import junit.framework.TestCase;

public class CornerCaseTest extends TestCase {
  interface Base {
    int getAge();
  }
  interface Sub extends Base {
    String getName();
  }
  class SubClass {
    public String getName(){
      return "x";
    }
  }
  public void test1(){
    Base base = new Base(){
      public int getAge(){
        return 1;
      }
    };
    SubClass subobj = new SubClass();
    Class subtype = Sub.class;
    Sub sub = (Sub)Implementor.proxy(subtype, subobj, base);
    assertEquals("x", sub.getName());
    assertEquals(1, sub.getAge());
  }
}
