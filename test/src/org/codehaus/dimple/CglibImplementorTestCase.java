package org.codehaus.dimple;

import java.io.Serializable;

public class CglibImplementorTestCase extends AbstractTestCase {
  public static class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int age;
    public Person(){}
    public Person(String name, int age) {
      this.name = name;
      this.age = age;
    }
    public int getAge() {
      return age;
    }
    public void setAge(int age) {
      this.age = age;
    }
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
  }
  public void testOverride(){
    Person tom = new Person("tom", 10);
    Person tommy = (Person)Implementor.proxy(Person.class, new Object(){
      public String getName(){
        return "tommy";
      }
    }, tom);
    tommy.setAge(15);
    assertEquals("tommy", tommy.getName());
    assertEquals(15, tom.getAge());
  }
  public static class Tommy implements Serializable {
    private static final long serialVersionUID = 1L;

    public String getName(){
      return "tommy";
    }
  }
  public void testCglibProxyIsSerializable()
  throws Exception{
    Person tom = new Person("tom", 10);
    Person tommy = (Person)Implementor.proxy(Person.class, new Tommy(), tom);
    assertSerializable(tommy);
    assertSerializable(Implementor.proxy(Person.class, new Tommy()));
  }
  public void testDefaultObjectMethodsUsed(){
    final Person test = (Person)new Implementor(Tommy.class)
    .implement(Person.class, new Tommy());
    test.hashCode();
    test.toString();
    //this is because cglib proxy not unwrapped.
    assertEquals(test, test);
  }
  public void testObjectMethodsDelegated(){
    final int HASHCODE = 31415926;
    final String STR = "TEST";
    Person defaultTest = (Person)Implementor.proxy(Person.class, new Object(){
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
    Person test = (Person)Implementor.proxy(Person.class, new Object(){
      public int hashCode(){
        return HASHCODE*10;
      }
    }, defaultTest);
    assertEquals(HASHCODE*10, test.hashCode());
    assertEquals(STR, test.toString());
    assertEquals(test, "doesnt matter");
    //how to unwrap enhanced class? 
    //assertEquals(test, test);
  }
}
