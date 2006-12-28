package org.codehaus.dimple.misc;

import java.io.Serializable;
import java.util.Arrays;

import org.codehaus.dimple.AbstractTestCase;

public class SumTestCase extends AbstractTestCase {
  private interface DoubleValues{
    double balance();
    double rate();
  }
  private static class MyAccount implements Serializable {
    private final double balance;
    private final double rate;
    
    public MyAccount(double balance, double rate) {
      this.balance = balance;
      this.rate = rate;
    }
    public double balance() {
      return balance;
    }
    public double rate() {
      return rate;
    }
    
  }
  private static class Account extends MyAccount implements DoubleValues {
    public Account(double balance, double rate) {
      super(balance, rate);
    }
  }

  public void testParticipantsReturningPrimitiveDoubleThatImplementTargetInterface()
  throws Exception{
    DoubleValues sum = assertSerializable(getSimpleSum());
    assertEquals(3.0, sum.balance());
    assertEquals(5.0, sum.rate());
  }

  public void testParticipantsReturningPrimitiveDoubleThatDoNotImplementTargetInterface()
  throws Exception{
    MyAccount[] accts = {
        new MyAccount(1,2), null, new MyAccount(2,3)
    };
    DoubleValues sum = assertSerializable(All.sumOf(DoubleValues.class, accts));
    assertEquals(3.0, sum.balance());
    assertEquals(5.0, sum.rate());
  }
  public void testListOfParticipantsReturningPrimitiveDoubleThatDoNotImplementTargetInterface()
  throws Exception{
    DoubleValues sum = assertSerializable(getListSum());
    assertEquals(3.0, sum.balance());
    assertEquals(5.0, sum.rate());
  }

  interface DoubleRefs {
    Double balance();
    Double rate();
  }
  public void testInterfaceReturningDoubleAndParticipantsReturningPrimitiveDoubleThatDoNotImplementTargetInterface()
  throws Exception{
    MyAccount[] accts = {
        new MyAccount(1,2), null, new MyAccount(2,3)
    };
    DoubleRefs sum = assertSerializable(All.sumOf(DoubleRefs.class, accts));
    assertEquals(3.0, sum.balance().doubleValue());
    assertEquals(5.0, sum.rate().doubleValue());
  }
  private static class DoubleRefsImpl implements DoubleRefs, Serializable {
    private final Double balance;
    private final Double rate;
    public Double balance() {
      return balance;
    }
    public Double rate() {
      return rate;
    }
    public DoubleRefsImpl(Double balance, Double rate) {
      this.balance = balance;
      this.rate = rate;
    }
  }
  public void testParticipantsReturningDoubleThatImplementTargetInterface()
  throws Exception{
    DoubleRefsImpl[] accts = {
        new DoubleRefsImpl(1d,2d), null, new DoubleRefsImpl(1d,3d), new DoubleRefsImpl(1d, null)
    };
    DoubleRefs sum = assertSerializable(All.sumOf(DoubleRefs.class, accts));
    assertEquals(3.0, sum.balance().doubleValue());
    assertEquals(5.0, sum.rate().doubleValue());
  }
  public void testHashable(){
    super.assertHashable(getSimpleSum());
  }
  private DoubleValues getSimpleSum() {
    DoubleValues[] dvs = {
      new Account(1,2), null, new Account(2,3)
    };
    DoubleValues sum = All.sumOf(DoubleValues.class, dvs);
    return sum;
  }
  private DoubleValues getListSum(){
    MyAccount[] dvs = {
      new MyAccount(1,2), null, new MyAccount(2,3)
    };
    DoubleValues sum = All.sumOf(DoubleValues.class, Arrays.asList(dvs));
    return sum;
  }
}
