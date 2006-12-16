package org.codehaus.dimple;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;

public class ImplementorTestCase extends AbstractTestCase {
  public void test_compareParameterTypes(){
    assertEquals(-1, Implementor.compareParameterTypes(new Class[1], 0, new Class[0], 0));
    assertEquals(1, Implementor.compareParameterTypes(new Class[0], 0, new Class[1], 0));
    assertEquals(0, Implementor.compareParameterTypes(new Class[1], 0, new Class[1], 0));
    assertEquals(-1, Implementor.compareParameterTypes(new Class[1], 1, new Class[1], 0));
    assertEquals(1, Implementor.compareParameterTypes(new Class[1], 1, new Class[1], 2));
  }
  public void test_compareMethodParameterTypes(){
    assertComparison(0, new Class[]{int.class, String.class}, new Class[]{int.class, String.class});
    assertComparison(-1, new Class[]{Object.class, Integer.class}, new Class[]{Object.class, Number.class});
    assertComparison(1, new Class[]{Number.class, Number.class}, new Class[]{Float.class, Number.class});
  }
  public void testImplementorSerializable()
  throws Exception {
    Implementor Implementor = new Implementor(ImplementorTestCase.class);
    assertEquals(Implementor, new Implementor(ImplementorTestCase.class));
    Implementor cloned = (Implementor)assertSerializable(Implementor);
    assertEquals(Implementor, cloned);
  }
  public void testImplementorHashable(){
    assertHashable(new Implementor(ImplementorTestCase.class));
  }
  public void testConnectionImplementor()
  throws Exception {
    final String TEST = "test";
    Connection conn = (Connection)Implementor.proxy(Connection.class, new Object(){
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
    Implementor impl1 = new Implementor(Test1.class);
    TestInterface test1 = (TestInterface)impl1.implement(TestInterface.class, new Test1());
    test1.close();
    test1.setAge(10);
    assertEquals(10, test1.getAge());
    assertEquals("10", test1.getName("x", new Integer(1)));
    
    Implementor impl2 = new Implementor(Test2.class);
    TestInterface test2 = (TestInterface)impl2.implement(TestInterface.class, new Test2());
    assertTest2(test2);
    

    Implementor impl3 = new Implementor(Test3.class);
    TestInterface test3 = (TestInterface)impl3.implement(TestInterface.class, new Test3());
    assertTest2(test3);
    
  }
  public void testImplementorProxyIsSerializable()
  throws Exception {
    TestInterface test = (TestInterface)assertSerializable(Implementor.proxy(TestInterface.class, new Test1()));
    assertSerializable(Implementor.proxy(TestInterface.class, new Test1(), test));
  }
  public void testImplementorWithDefaultHandler(){
    TestInterface test = (TestInterface)Implementor.proxy(TestInterface.class, new InvocationHandler(){
      private int age;
      public int getAge(){
        return age;
      }
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
    final TestInterface defaultTest = (TestInterface)new Implementor(Test1.class).implement(TestInterface.class, new Test1());
    TestInterface test = (TestInterface)Implementor.proxy(TestInterface.class, new Object(){
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
    final TestInterface test = (TestInterface)new Implementor(Test1.class)
    .implement(TestInterface.class, new Test1());
    test.hashCode();
    test.toString();
    assertEquals(test, test);
  }
  public void testObjectMethodsDelegated(){
    final int HASHCODE = 31415926;
    final String STR = "TEST";
    TestInterface defaultTest = (TestInterface)Implementor.proxy(TestInterface.class, new Object(){
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
    TestInterface test = (TestInterface)Implementor.proxy(TestInterface.class, new Object(){
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
    return Implementor.compareParameterTypes(types1, Implementor.getHierarchyDepthSum(types1), 
        types2, Implementor.getHierarchyDepthSum(types2));
  }
}
