package org.codehaus.dimple;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;

public class ImplementorTestCase extends AbstractTestCase {
  public void test_compareParameterTypes(){
    assertEquals(-1, TypingUtils.compareParameterTypes(new Class[1], 0, new Class[0], 0));
    assertEquals(1, TypingUtils.compareParameterTypes(new Class[0], 0, new Class[1], 0));
    assertEquals(0, TypingUtils.compareParameterTypes(new Class[1], 0, new Class[1], 0));
    assertEquals(-1, TypingUtils.compareParameterTypes(new Class[1], 1, new Class[1], 0));
    assertEquals(1, TypingUtils.compareParameterTypes(new Class[1], 1, new Class[1], 2));
  }
  public void test_compareMethodParameterTypes(){
    assertComparison(0, new Class[]{int.class, String.class}, new Class[]{int.class, String.class});
    assertComparison(-1, new Class[]{Object.class, Integer.class}, new Class[]{Object.class, Number.class});
    assertComparison(1, new Class[]{Number.class, Number.class}, new Class[]{Float.class, Number.class});
  }
  public void testImplementorSerializable()
  throws Exception {
    Implementor<ImplementorTestCase> implementor = Implementor.instance(ImplementorTestCase.class);
    assertEquals(implementor, Implementor.instance(ImplementorTestCase.class));
    Implementor<ImplementorTestCase> cloned = assertSerializable(implementor);
    assertEquals(implementor, cloned);
  }
  public void testImplementorHashable(){
    assertHashable(Implementor.instance(ImplementorTestCase.class));
  }
  public void testConnectionImplementor()
  throws Exception {
    final String TEST = "test";
    Connection conn = Implementor.proxy(Connection.class, new Object(){
      @SuppressWarnings("unused")
      public boolean close(){
        return true;
      }
      public String toString(){
        return TEST;
      }
    });
    conn.close();
    try{
      conn.commit();
      fail("exception expected");
    }
    catch(UnsupportedOperationException e){}
    assertSame(TEST, conn.toString());
  }
  private interface TestInterface {
    void close();
    int getAge();
    void setAge(int age);
    String getName(String a, Number b);
  }
  private static class Test1 implements Serializable {
    private static final long serialVersionUID = -1614818421008004989L;
    boolean closed = false;
    int age;
    public void close(){
      closed = true;
    }
    public void setAge(int age){
      this.age = age;
    }
    public int getAge(){
      return age;
    }
    public String getName(Object a, Object b){
      return ""+age;
    }
  }
  private static class Test2 extends Test1 {
    private static final long serialVersionUID = 1L;

    public String getName(String a, Number b){
      return a;
    }
  }
  private static class Test3 extends Test2 {
    private static final long serialVersionUID = 1L;

    public String getName(String a, Object b){
      return "not this";
    }
  }
  public void testImplementorSupportsContravariantParameters(){
    Implementor<Test1> impl1 = Implementor.instance(Implementor.willImplement(Test1.class, TestInterface.class));
    TestInterface test1 = impl1.implement(Implementor.implementedBy(TestInterface.class, Test1.class), new Test1());
    test1.close();
    test1.setAge(10);
    assertEquals(10, test1.getAge());
    assertEquals("10", test1.getName("x", new Integer(1)));
    
    Implementor<Test2> impl2 = Implementor.instance(Test2.class);
    TestInterface test2 = impl2.implement(TestInterface.class, new Test2());
    assertTest2(test2);
    

    Implementor<Test3> impl3 = Implementor.instance(Test3.class);
    TestInterface test3 = impl3.implement(TestInterface.class, new Test3());
    assertTest2(test3);
    
  }
  public void testImplementorProxyIsSerializable()
  throws Exception {
    TestInterface test = assertSerializable(Implementor.proxy(TestInterface.class, new Test1()));
    assertSerializable(Implementor.proxy(TestInterface.class, new Test1(), test));
  }
  public void testImplementorWithDefaultHandler(){
    TestInterface test = Implementor.proxy(TestInterface.class, new InvocationHandler(){
      private int age;
      @SuppressWarnings("unused")
      public int getAge(){
        return age;
      }
      @SuppressWarnings("unused")
      public String getName(String a, Object b){
        return a+b;
      }
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        assertEquals("setAge", method.getName());
        assertEquals(1, args.length);
        assertEquals(new Integer(10), args[0]);
        this.age = 10;
        return null;
      }
    });
    assertEquals("x1", test.getName("x",new Integer(1)));
    test.setAge(10);
    assertEquals(10, test.getAge());
  }
  public void testImplementorWithDefaultDelegate()
  throws Exception {
    final TestInterface defaultTest = Implementor.instance(Test1.class).implement(TestInterface.class, new Test1());
    TestInterface test = Implementor.proxy(TestInterface.class, new Object(){
      @SuppressWarnings("unused")
      public int getAge(){
        return defaultTest.getAge()+1;
      }
    }, defaultTest);
    test.close();
    test.setAge(10);
    assertEquals(11, test.getAge());
    assertEquals("10", test.getName("x", new Integer(1)));
  }
  public void testDefaultObjectMethodsUsed(){
    final TestInterface test = Implementor.instance(Test1.class)
      .implement(TestInterface.class, new Test1());
    test.hashCode();
    test.toString();
    assertEquals(test, test);
  }
  public void testObjectMethodsDelegated(){
    final int HASHCODE = 31415926;
    final String STR = "TEST";
    TestInterface defaultTest = Implementor.proxy(TestInterface.class, new Object(){
      public int hashCode(){
        return HASHCODE; 
      }
      public String toString(){
        return STR;
      }
      public boolean equals(Object obj){
        if(obj instanceof String){
          return true;
        }
        return super.equals(obj);
      }
    });
    assertEquals(HASHCODE, defaultTest.hashCode());
    assertEquals(STR, defaultTest.toString());
    assertEquals(defaultTest, "doesnt matter");
    assertEquals(defaultTest, defaultTest);
    TestInterface test = Implementor.proxy(TestInterface.class, new Object(){
      public int hashCode(){
        return HASHCODE*10;
      }
    }, defaultTest);
    assertEquals(HASHCODE*10, test.hashCode());
    assertEquals(STR, test.toString());
    assertEquals(test, "doesnt matter");
    assertEquals(test, test);
    assertEquals(test, defaultTest);
  }
  interface I1{
    String f();
  }
  interface I2{
    String g();
  }
  interface I3 {
    String h();
  }
  class A implements I1, I2 {
    public String f() {
      return "f";
    }

    public String g() {
      return "g";
    }
  };
  abstract class B extends A implements I3{}
  public void test_getAllInterfaces(){
    assertEquals(3, TypingUtils.getAllInterfaces(B.class).length);
  }
  public void testOverride(){
    Object orig = new B(){
      public String h(){return "h";}
    };
    Object proxy = Implementor.overrideObject(orig, new Object(){
      @SuppressWarnings("unused")
      public String f(){
        return "f'";
      }
    });
    Class[] itfs = TypingUtils.getAllInterfaces(orig.getClass());
    assertEquals(3, itfs.length);
    I1 i1 = (I1)proxy;
    assertEquals("f'", i1.f());
    I2 i2 = (I2)proxy;
    assertEquals("g",  i2.g());
    I3 i3 = (I3)proxy;
    assertEquals("h", i3.h());
  }
  public class ImplWithExtraMethod {
    public String f(){
      return "my6";
    }
    public String extra(){return null;}
  }
  public void testImplementedByWillThrowExceptionForExtraMethod(){
    try{
      Class<I1> ret = Implementor.implementedBy(I1.class, ImplWithExtraMethod.class);
      fail("should have failed");
    }
    catch(UnusedMethodException e){}
    try{
      Class<ImplWithExtraMethod> ret = Implementor.willImplement(ImplWithExtraMethod.class, I1.class);
      fail("should have failed");
    }
    catch(UnusedMethodException e){}
  }
  public class ImplWithBadReturnType {
    public int f(){return 1;}
  }

  public void testImplementedByWillThrowExceptionForInvalidReturnType(){
    try{
      Implementor.implementedBy(I1.class, ImplWithBadReturnType.class);
      fail("should have failed");
    }
    catch(InvalidReturnTypeException e){}
    try{
      Implementor.willImplement(ImplWithBadReturnType.class, I1.class);
      fail("should have failed");
    }
    catch(InvalidReturnTypeException e){}
  }
  

  private void assertTest2(TestInterface test2) {
    test2.close();
    test2.setAge(10);
    assertEquals(10, test2.getAge());
    assertEquals("x", test2.getName("x", new Integer(1)));
  }
  private static void assertComparison(int expectedResult, Class[] types1, Class[] types2){
    assertEquals(expectedResult, compareTypes(types1, types2));
  }
  private static int compareTypes(Class[] types1, Class[] types2){
    return TypingUtils.compareParameterTypes(types1, TypingUtils.getHierarchyDepthSum(types1), 
        types2, TypingUtils.getHierarchyDepthSum(types2));
  }
}
