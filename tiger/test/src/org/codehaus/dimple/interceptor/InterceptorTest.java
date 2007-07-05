package org.codehaus.dimple.interceptor;


import org.codehaus.dimple.Implement;
import org.codehaus.dimple.Implementor;
import org.codehaus.dimple.Interceptor;
import org.codehaus.dimple.UnusedMethodException;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Test;
import static org.easymock.EasyMock.*;
import static junit.framework.Assert.*;

public class InterceptorTest {
  public interface Call {
    void f();
    char call1(boolean yesno, boolean ok, boolean reall, 
            char c, byte b, short s, double i, float f, int l, int l2);

    void call2(boolean yesno, char c, byte b, short s, 
            int i, float f, double d, String msg);
    
    String translate(String s);
    String toString();
  }
  public interface Impl {
    int call2(boolean yesno, char c, byte b, short s, int i, 
            float f, double d, CharSequence msg);
  }
  private final IMocksControl mocker = EasyMock.createStrictControl();
  private int mockCount = 0;
  private <T> T mock(Class<T> type) {
    mockCount ++;
    return mocker.createMock(type);
  }
  @After public void verifyAll() {
    if(mockCount > 0) {
      mocker.verify();
    }
  }
  public void testInterfaceMethod() {
    Call interceptee = mock(Call.class);
    Impl overrider = mock(Impl.class);
    expect(interceptee.call1(true, true, false, 'a', 
            (byte)0, (short)1, 2, 4, 3, 6)).andReturn('o');
    expect(overrider.call2(false, 'a', (byte)0, (short)1, 2, 4, 5, "hello"))
      .andReturn(1).times(2);
    mocker.replay();
    Interceptor<Call, Impl> interceptor = 
      Implementor.generateInterceptor(Call.class, Impl.class);
    Call call = interceptor.intercept(interceptee, overrider);
    assertEquals('o', 
            call.call1(true, true, false, 'a', (byte)0, (short)1, 2, 4, 3, 6));
    call.call2(false, 'a', (byte)0, (short)1, 2, 4, 5, "hello");
    interceptor.stub(overrider)
      .call2(false, 'a', (byte)0, (short)1, 2, 4, 5, "hello");
    
  }
  @Implement
  static class MyInterceptor {
    public String translate(String s) {
      return s;
    }
    public String toString() {
      return "my interceptor";
    }
  }

  @Test public void testVirtualMethod() {
    Call interceptee = mock(Call.class);
    String msg = "hello";
    mocker.replay();
    assertEquals(msg, 
            Implementor.generateInterceptor(Call.class, MyInterceptor.class)
        .intercept(interceptee, new MyInterceptor()).translate(msg));
  }
  

  static class MyStaticInterceptor {
    public static String translate(String s) {
      return s;
    }
  }
  @Test public void testStaticMethod() {
    Call interceptee = mock(Call.class);
    String msg = "hello";
    mocker.replay();
    assertEquals(msg, Implementor.generateInterceptor(
            Call.class, MyStaticInterceptor.class)
        .intercept(interceptee, null).translate(msg));
  }

  @Test public void testAnonymousClassInterceptor() {
    Call interceptee = mock(Call.class);
    mocker.replay();
    assertEquals("test", Implementor.intercept(Call.class, interceptee, new Object(){
      @SuppressWarnings("unused")
      public String translate(Object obj) {
        return "test";
      }
    }).translate("hello"));
  }
  @Test public void testAnonymousClassStub() {
    assertEquals("test", Implementor.stub(Call.class, new Object(){
      @SuppressWarnings("unused")
      @Implement
      public String translate(Object obj) {
        return "test";
      }
    }).translate("hello"));
  }
  @Test(expected=UnusedMethodException.class)
  public void testUnusedMethod() {
    Implementor.stub(Call.class, new Object(){
      @SuppressWarnings("unused")
      @Implement
      public String translateNotUsed(Object obj) {
        return "test";
      }
    });
  }
  @Test(expected=UnsupportedOperationException.class)
  public void testUnsupportedMethod() {
    Implementor.generateInterceptor(Call.class, MyStaticInterceptor.class)
        .stub(null).f();
  }
  @Test
  public void testToString() {
    assertEquals("my interceptor", 
            Implementor.generateInterceptor(Call.class, MyInterceptor.class)
        .stub(new MyInterceptor()).toString());
  }
  @Test public void testHashcode() {
    Call stub = Implementor.generateInterceptor(Call.class, MyInterceptor.class)
        .stub(new MyInterceptor());
    assertEquals(System.identityHashCode(stub), stub.hashCode());
  }
  @Test public void testEquals() {
    Call stub = Implementor.generateInterceptor(Call.class, MyInterceptor.class)
        .stub(new MyInterceptor());
    assertEquals(stub, stub);
  }
}
