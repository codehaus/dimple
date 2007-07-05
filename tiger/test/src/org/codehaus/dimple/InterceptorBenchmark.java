package org.codehaus.dimple;

import org.junit.Test;
import static org.junit.Assert.*;
public class InterceptorBenchmark {
  public interface Call {
    String f(String s);
    int f(int i);
  }
  public static class Impl {
    public int f(int i) {
      return i+1;
    }
  }
  private Call call = new Call() {
    public String f(String s) {
      return s;
    }
    public int f(int i) {
      return i;
    }
  };
  private int times = 1000000;
  @Test public void testInterceptorDelegate() {
    Call mycall = Implementor.generateInterceptor(Call.class, Object.class).intercept(call, null);
    verifyDelegate("test", mycall);
    String title = "interceptor delegating";
    runBenchmark(mycall, title);
  }

  @Test public void testProxyDelegate() {
    Call mycall = Implementor.instance(Object.class).implement(Call.class, new Object(), call);
    verifyDelegate("test", mycall);
    String title = "proxy delegating";
    runBenchmark(mycall, title);
  }

  @Test public void testInterceptorOverride() {
    Call mycall = Implementor.generateInterceptor(Call.class, Impl.class).intercept(call, new Impl());
    verifyOverride("test", mycall);
    String title = "interceptor override";
    runBenchmark(mycall, title);
  }

  @Test public void testProxyOverride() {
    Call mycall = Implementor.instance(Impl.class).implement(Call.class, new Impl(), call);
    verifyOverride("test", mycall);
    String title = "proxy override";
    runBenchmark(mycall, title);
  }
  private void verifyDelegate(String msg, Call mycall) {
    assertEquals(1, mycall.f(1));
    assertSame(msg, mycall.f(msg));
  }
  private void verifyOverride(String msg, Call mycall) {
    assertEquals(2, mycall.f(1));
    assertSame(msg, mycall.f(msg));
  }
  private void runBenchmark(Call mycall, String title) {
    long instant = System.currentTimeMillis();
    for(int i=0; i<times; i++) {
      mycall.f(1);
    }
    System.out.println(title+": "+(System.currentTimeMillis()-instant));
  }
}
