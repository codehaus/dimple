package org.codehaus.dimple;

import java.io.Serializable;

public class AnnotationTestCase extends AbstractTestCase {
  public interface I1 {
    String f();
  }
  public static class My1 {
    public String f(){
      return "my1";
    }
    @Implement
    public void g(){}
  }
  public void testMethodsAnnotatedAsImplementHasToImplementSomething()
  throws Exception{
    try{
      Implementor.proxy(I1.class, new My1());
      fail("should have failed");
    }
    catch(UnusedMethodException e){
      assertEquals(My1.class.getMethod("g", new Class[0]), e.getMethod());
    }
  }
  
  @Implement
  public static class My2 {
    public String f(){
      return "my1";
    }
    public void g(){}
  }
  public void testMethodsInClassAnnotatedAsImplementHasToImplementSomething()
  throws Exception{
    try{
      Implementor.proxy(I1.class, new My2());
      fail("should have failed");
    }
    catch(UnusedMethodException e){
      assertEquals(My2.class.getMethod("g", new Class[0]), e.getMethod());
    }
  }
  
  public static class My3 extends My1 {
  }
  public void testMethodsAnnotatedInSuperClassIsAlsoChecked()
  throws Exception{
    try{
      Implementor.willImplement(My3.class, I1.class);
      fail("should have failed");
    }
    catch(UnusedMethodException e){
      assertEquals(My3.class.getMethod("g", new Class[0]), e.getMethod());
    }
    try{
      Implementor.proxy(I1.class, new My3());
      fail("should have failed");
    }
    catch(UnusedMethodException e){
      assertEquals(My3.class.getMethod("g", new Class[0]), e.getMethod());
    }
  }


  public static class My4 extends My1 implements Serializable {
    private static final long serialVersionUID = 1448592419307215583L;

    public void g(){
    }
  }
  public void testMethodsOverridenWithoutAnnotationIsOkToBeUnused()
  throws Exception{
    My4 m4 = assertSerializable(new My4());
    I1 i1 = assertSerializable(Implementor.proxy(I1.class, m4));
    assertEquals("my1", i1.f());
  }


  public class My5 {
    public String f(){
      return "my5";
    }
    @Implement
    public int hashCode(){
      return 5;
    }
  }  
  public void testObjectMethodsAreNotNecessaryToBeUsed(){
    My5 m5 = new My5();
    I1 i1 = Implementor.proxy(I1.class, m5);
    assertEquals("my5", i1.f());
  }
  @Implement
  public class My6 {
    public String f(){
      return "my6";
    }
    public int hashCode(){
      return 6;
    }
    public String toString(){return "";}
    public boolean equals(Object obj){return false;}
  }  
  public void testObjectMethodsInAnnotatedClassAreNotNecessaryToBeUsed(){
    My6 m6 = new My6();
    I1 i1 = Implementor.proxy(Implementor.implementedBy(I1.class, My6.class), m6);
    assertEquals("my6", i1.f());
    assertEquals(6, i1.hashCode());
  }
  public interface I2 {
    boolean compare(String s, Object obj);
  }
  @Implement
  public class BadReturnType {
    @SuppressWarnings("unchecked")
    public int compare(Comparable obj1, Object obj2){
      return obj1.compareTo(obj2);
    }
  }
  public void testBadReturnTypeShouldThrowException()
  throws Exception {
    verifyBadReturnType(new BadReturnType());
  }
  public class SubBadReturnType extends BadReturnType {
    
  }
  public void testSubBadReturnTypeShouldThrowException()
  throws Exception {
    verifyBadReturnType(new SubBadReturnType());
  }
  private void verifyBadReturnType(Object badReturnTypeObj) throws NoSuchMethodException {

    try{
      Implementor.willImplement(badReturnTypeObj.getClass(), I2.class);
      fail("should have failed");
    }
    catch(InvalidReturnTypeException e){
      verifyError(e);
    }
    
    try{
      Implementor.proxy(I2.class, badReturnTypeObj);
      fail("should have failed");
    }
    catch(InvalidReturnTypeException e){
      verifyError(e);
    }
  }
  private void verifyError(InvalidReturnTypeException e) throws NoSuchMethodException {
    assertEquals(BadReturnType.class.getMethod("compare", new Class[]{Comparable.class, Object.class}),
        e.getImplementingMethod());
    assertEquals(I2.class.getMethod("compare", new Class[]{String.class, Object.class}), 
        e.getImplementedMethod());
  }
}
