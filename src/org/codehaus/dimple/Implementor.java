
/*
 *  Copyright 2006 Ben Yu
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  Unless required by applicable law or agreed to in writing, 
 *  software distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions 
 *  and limitations under the License.
 *  
 */

package org.codehaus.dimple;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * This class is used to create implementation of interface(s) dynamically.
 * <p>
 * This class is ideal for creating stub or interceptor for interfaces where only a few methods are of interest while
 * most other methods are either ignored or delegated.
 * </p>
 * <p>
 * For example:
 * <pre> 
 * Connection realConn = ...;
 * Connection nonCloseableConnection = (Connection)Implementor.proxy(Connection.class, new Object(){
 *   public void close() {
 *     //we intercept close() call and do nothing.
 *   }
 * }, realConn);
 * nonCloseableConnection.close();// no-op. realConn is not closed.
 * </pre>
 * </p>
 * @author Ben Yu
 * Dec 9, 2006 11:27:44 PM
 */
public class Implementor implements Serializable {
  /**
   * Equivalent as new Implementor(with.getClass()).implementWithDefaultHandler(itf, with, defaultHandler)
   * <p>
   * This method is a convenience shortcut.
   * As the constructor of Implementor may be expensive, it is recommended to create an Implementor object once
   * and then use it repeatedly.
   */
  public static Object proxyWithDefaultHandler(Class asType, Object with, InvocationHandler defaultHandler) {
    return new Implementor(with.getClass()).implementWithDefaultHandler(asType, with, defaultHandler);
  }
  /**
   * Equivalent as new Implementor(with.getClass()).implement(asType, with, defaultDelegate)
   * <p>
   * This method is a convenience shortcut.
   * As the constructor of Implementor may be expensive, it is recommended to create an Implementor object once
   * and then use it repeatedly.
   */
  public static Object proxy(Class asType, Object with, Object defaultDelegate){
    if(!asType.isInstance(defaultDelegate)){
      throw new IllegalArgumentException("default delegate of type "+asType.getName() + " expected, "
          + ((defaultDelegate==null)?null:defaultDelegate.getClass().getName())+" encountered");
    }
    return new Implementor(with.getClass()).implement(asType, with, defaultDelegate);
  }
  
  /**
   * Equivalent as new Implementor(with.getClass()).implement(asType)
   * <p>
   * This method is a convenience shortcut.
   * As the constructor of Implementor may be expensive, it is recommended to create an Implementor object once
   * and then use it repeatedly. 
   */
  public static Object proxy(Class asType, Object with) {
    return new Implementor(with.getClass()).implement(asType, with);
  }

  /**
   * create a dynamic proxy that implements <i>asType</i> by calling <i>with</i>
   * if a method is implemented by the impl class.
   * Otherwise delegate call to defaultDelegate 
   * or throw UnsupportedOperationException if defaultDelegate is null.
   * @param asType the interface to implement or super class to override (cglib is required in this case). 
   * @param with the instance of the impl class.
   * @param defaultDelegate the default delegate.
   * @return the dynamic proxy that implements <i>asType</i>.
   */
  public Object implement(Class asType, Object with, Object defaultDelegate) {
    return newProxyInstance(asType.getClassLoader(), asType, 
        createInvocationHandler(with, defaultDelegate));
  }
  /**
   * create a dynamic proxy that implements <i>asType</i> by calling <i>with</i>
   * if a method is implemented by the impl class.
   * Otherwise call the invoke() method of defaultHandler, 
   * or throw UnsupportedOperationException if defaultHandler is null.
   * @param asType the interface to implement or super class to override (cglib is required in this case). 
   * @param with the instance of the impl class.
   * @param defaultHandler the default InvocationHandler.
   * @return the dynamic proxy that implements <i>asType</i>.
   */
  public Object implementWithDefaultHandler(Class asType, Object with, InvocationHandler defaultHandler) {
    return newProxyInstance(asType.getClassLoader(), asType, 
        createInvocationHandlerWithDefaultHandler(with, defaultHandler));
  }
  /**
   * create a dynamic proxy that implements <i>asType</i> by calling <i>with</i>
   * if a method is implemented by the impl class.
   * Otherwise call the invoke() method if <i>with</i> implements InvocationHandler,
   * or throw UnsupportedOperationException otherwise.
   * @param asType the interface to implement or super class to override (cglib is required in this case). 
   * @param with the instance of the impl class.
   * @return the dynamic proxy that implements <i>asType</i>.
   */
  public Object implement(Class asType, Object with){
    return newProxyInstance(asType.getClassLoader(), asType, 
        createInvocationHandler(with));
  }
  /**
   * create an InvocationHandler object by calling <i>instance</i>
   * if a method is implemented by the impl class.
   * Otherwise call the invoke() method if <i>instance</i> implements InvocationHandler,
   * or throw UnsupportedOperationException otherwise. 
   * @param instance the instance of the impl class.
   * @return the InvocationHandler object.
   */
  public InvocationHandler createInvocationHandler(final Object instance){
    return createInvocationHandlerWithDefaultHandler(instance, 
        (instance instanceof InvocationHandler)?(InvocationHandler)instance:null);
  }
  /**
   * create an InvocationHandler object by calling <i>instance</i>
   * if a method is implemented by the impl class.
   * Otherwise call the provided default InvocationHandler object. 
   * @param instance the instance of the impl class.
   * @param defaultHandler the InvocationHandler object to provide default behavior.
   * If null, UnsupportedOperationException is thrown.
   * @return the InvocationHandler object.
   */
  public InvocationHandler createInvocationHandlerWithDefaultHandler(final Object instance, final InvocationHandler defaultHandler){
    return createInvocationHandlerWithDefaults(instance, null, defaultHandler);
  }
  /**
   * create an InvocationHandler object by calling <i>instance</i>
   * if a method is implemented by the impl class.
   * Otherwise forward the call to defaultDelegate. 
   * @param instance the instance of the impl class.
   * @param defaultDelegate the default delegate. If null, UnsupportedOperationException is thrown.
   * @return the InvocationHandler object.
   */
  public InvocationHandler createInvocationHandler(final Object instance, Object defaultDelegate){
    return createInvocationHandlerWithDefaults(instance, defaultDelegate, null);
  }
  InvocationHandler createInvocationHandlerWithDefaults(Object instance, Object defaultDelegate, InvocationHandler defaultHandler){
    checkInstanceType(instance);
    return new ImplInvocationHandler(instance, defaultDelegate, defaultHandler);
  }
  private void checkInstanceType(final Object instance) {
    if(!implClass.isInstance(instance)){
      throw new IllegalArgumentException("instance of type "+implClass.getName() + " expected, "
          + ((instance==null)?null:instance.getClass().getName())+" encountered");
    }
  }
  /**
   * To create an Implementor class.
   * @param implClass the class used to implement.
   */
  public Implementor(Class implClass){
    this.implClass = implClass;
    addClass(implClass);
    sort();
  }
  public boolean equals(Object obj){
    if(obj instanceof Implementor){
      Implementor other = (Implementor)obj;
      return implClass.equals(other.implClass) && methods.equals(other.methods);
    }
    else return false;
  }
  public int hashCode(){
    return implClass.hashCode(); 
  }
  public String toString(){
    return implClass.toString();
  }
  /**
   * Get the impl class, which is the class whose public methods are used to implement target interface. 
   */
  public Class getImplClass(){
    return implClass;
  }
  void addClass(Class cls) {
    final boolean force = !Modifier.isPublic(cls.getModifiers());
    final Method[] mtds = cls.getMethods();
    for (int i = 0; i < mtds.length; i++) {
      final Method mtd = mtds[i];
      if(force) setAccessible(mtd);
      addMethod(mtd);
    }
  }
  private static void setAccessible(Method mtd){
    try{
      mtd.setAccessible(true);
    }
    catch(SecurityException e){}
  }
  private final Class implClass;
  private final Map methods = new HashMap();
  private static final Comparator SUB_PARAM_TYPES_FIRST = new Comparator(){
    public int compare(Object arg0, Object arg1) {
      return compareMyMethod((MyMethod)arg0, (MyMethod)arg1);
    }
  };
  private final class ImplInvocationHandler implements InvocationHandler, Serializable, Ref {
    private static final long serialVersionUID = 7723292911817172565L;
    private final Object instance;
    private final Object defaultDelegate;
    private final InvocationHandler defaultHandler;
    ImplInvocationHandler(Object instance, Object defaultDelegate, InvocationHandler fwd) {
      this.instance = instance;
      this.defaultDelegate = defaultDelegate;
      this.defaultHandler = fwd;
    }
    public Object get(){
      if(defaultDelegate!=null)
        return defaultDelegate;
      if(defaultHandler!=null){
        return unwrap(defaultHandler);
      }
      return instance;
    }
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      final Method impl = lookupImplementingMethod(method);
      if(impl == null) {
        return invokeDefault(proxy, method, args);
      }
      else if((defaultDelegate!=null || defaultHandler!=null)
          && Object.class.equals(impl.getDeclaringClass())){
        //forward call for default hashCode/equals/toString
        return invokeDefault(proxy, method, args);
      }
      else {
        return myCall(proxy, impl, args);
      }
    }
    private Object invokeDefault(Object proxy, Method method, Object[] args)
    throws Throwable {
      if(defaultDelegate != null) {
        return forwardCall(proxy, defaultDelegate, method, args);
      }
      if(defaultHandler != null) {
        return defaultHandler.invoke(proxy, method, args);
      }
      else{
        throw new UnsupportedOperationException();
      }
    }
    private Object myCall(Object proxy, final Method mtd, Object[] args) throws IllegalAccessException, Throwable {
      if(isEqualsMethodCall(mtd, args)){
        if(Object.class.equals(mtd.getDeclaringClass())){
          //the native equals() call.
          return valueOf(proxy==args[0]);
        }
        return valueOf(instance.equals(unwrap(args[0])));
      }
      return invokeMethod(instance, mtd, args);
    }
    
    private Object forwardCall(Object proxy, Object obj, final Method mtd, Object[] args) throws IllegalAccessException, Throwable {
      if(isEqualsMethodCall(mtd, args)){
        return valueOf(obj.equals(unwrap(args[0])));
      }
      return invokeMethod(obj, mtd, args);
    }
  }
  static final String EQUALS = "equals";
  static boolean isEqualsMethodCall(final Method mtd, Object[] args) {
    return args!=null && args.length==1 && EQUALS.equals(mtd.getName())
        && Object.class.equals(mtd.getParameterTypes()[0]);
  }
  static Object invokeMethod(Object obj, final Method mtd, Object[] args)
  throws IllegalAccessException, Throwable {
    try {
      return mtd.invoke(obj, args);
    }
    catch(InvocationTargetException e){
      throw e.getTargetException();
    }
  }
  static Object unwrap(Object obj){
    if(obj==null) return null;
    if(Proxy.isProxyClass(obj.getClass())){
      final InvocationHandler handler = Proxy.getInvocationHandler(obj);
      if(handler instanceof Ref){
        return ((Ref)handler).get();
      }
    }
    return obj;
  }

  static final class MyMethod implements Serializable {
    private static final long serialVersionUID = 8758558523031285785L;
    private transient Method method;
    private transient Class[] parameterTypes;
    private final long depth;
    public boolean equals(Object obj){
      if(obj instanceof MyMethod){
        final MyMethod other = (MyMethod)obj;
        return depth==other.depth
          && method.equals(other.method)
          && Arrays.equals(parameterTypes, other.parameterTypes);
      }
      else return false;
    }
    MyMethod(Method mtd) {
      this.method = mtd;
      this.parameterTypes = mtd.getParameterTypes();
      this.depth = TypingUtils.getHierarchyDepthSum(parameterTypes);
    }
    public Method getMethod() {
      return method;
    }
    public Class[] getParameterTypes() {
      return parameterTypes;
    }
    public String toString(){
      return method.toString();
    }
    public long getDepth(){
      return depth;
    }

    private void writeObject(java.io.ObjectOutputStream out)
    throws java.io.IOException{
      out.defaultWriteObject();
      out.writeObject(method.getDeclaringClass());
      out.writeObject(method.getName());
      out.writeObject(parameterTypes);
    }
    private void readObject(java.io.ObjectInputStream in)
      throws java.io.IOException, ClassNotFoundException{
      in.defaultReadObject();
      try{
        final Class c = (Class)in.readObject();
        final String name = (String)in.readObject();
        this.parameterTypes = (Class[])in.readObject();
        this.method = c.getDeclaredMethod(name, parameterTypes);
        if(!Modifier.isPublic(c.getModifiers()))
          setAccessible(this.method);
      }
      catch(NoSuchMethodException e){
        throw new IllegalStateException(e.getMessage());
      }
    }
  }
  void addMethod(Method mtd){
    final String name = mtd.getName();
    List suite = (List) methods.get(name);
    if(suite==null){
      suite = new ArrayList();
      methods.put(name, suite);
    }
    suite.add(new MyMethod(mtd));
  }
  void sort(){
    for(Iterator it = methods.values().iterator(); it.hasNext();){
      final List suite = (List)it.next();
      sortMethods(suite);
    }
  }
  static int compareMyMethod(MyMethod m1, MyMethod m2){
    return TypingUtils.compareParameterTypes(m1.getParameterTypes(), m1.getDepth(), 
        m2.getParameterTypes(), m2.getDepth());
  }
  /**
   * To find a method in the impl class that can be used in place of the <i>implemented</i> method.
   * @param implemented the method to be implemented.
   * @return the method that can be used to implement, or null if not found.
   */
  public Method lookupImplementingMethod(Method implemented) {
    final String name = implemented.getName();
    final List suite = (List)methods.get(name);
    if(suite==null) return null;
    final int size = suite.size();
    final Class[] implementedParamTypes = implemented.getParameterTypes();
    for(int i=0; i<size; i++){
      final MyMethod mm = (MyMethod)suite.get(i);
      final Class[] withParamTypes = mm.getParameterTypes();
      if(TypingUtils.isParamsCompatible(withParamTypes, implementedParamTypes)){
        return mm.getMethod();
      }
    }
    return null;
  }
  /**
   * To create a proxy instance for a given interface or superclass.
   * @param loader the class loader.
   * @param asType the interface or super class (cglib is required in this case).
   * @param handler the InvocationHandler to handle calls.
   * @return the proxy instance.
   */
  public static Object newProxyInstance(ClassLoader loader, Class asType, InvocationHandler handler){
    if(asType.isInterface()){
      return Proxy.newProxyInstance(loader, new Class[]{asType}, handler);
    }
    else {
      return CglibUtils.proxy(loader, asType, handler);
    }
  }
  /**
   * To assert that all methods in <i>implClass</i> will properly
   * implement some method in <i>asType</i>
   * @return the <i>asType</i>
   * @throws InvalidReturnTypeException
   * @throws UnusedMethodException
   */
  public static Class implementedBy(Class asType, Class implClass)
  throws InvalidReturnTypeException, UnusedMethodException {
    TypingUtils.checkImplementingMethods(implClass.getMethods(), TypingUtils.getAllMethodsToImplement(new Class[]{asType}));
    return asType;
  }
  /**
   * To assert that all methods in <i>implClass</i> will properly
   * implement some method in <i>asType</i>
   * @return the <i>implClass</i>
   * @throws InvalidReturnTypeException
   * @throws UnusedMethodException
   */
  public static Class willImplement(Class implClass, Class asType)
  throws InvalidReturnTypeException, UnusedMethodException {
    return willImplement(implClass, new Class[]{asType});
  }
  /**
   * To assert that all methods in <i>implClass</i> will properly
   * implement some method in any one of <i>asTypes</i>
   * @return the <i>implClass</i>
   * @throws InvalidReturnTypeException
   * @throws UnusedMethodException
   */
  public static Class willImplement(Class implClass, Class[] asTypes)
  throws InvalidReturnTypeException, UnusedMethodException {
    TypingUtils.checkImplementingMethods(implClass.getMethods(), TypingUtils.getAllMethodsToImplement(asTypes));
    return implClass;
  }
  /**
   * Overrides an object using methods defined in impl class and the overrider object
   * bound to "this".
   * All interfaces of <i>obj</i> are implemented by the proxy.
   * @param obj the object to be overriden.
   * @param overrider the overrider.
   * @return the proxy object.
   */
  public final Object override(Object obj, Object overrider){
    Class overriden = obj.getClass();
    return Proxy.newProxyInstance(overriden.getClassLoader(), TypingUtils.getAllInterfaces(overriden),
        createInvocationHandler(overrider, obj));
  }

  private static void sortMethods(List suite){
    Collections.sort(suite, SUB_PARAM_TYPES_FIRST);
  }
  /**
   * Overrides an object using the overrider object.
   * All interfaces of <i>obj</i> are implemented by the proxy.
   * @param obj the object to be overriden.
   * @param overrider the overrider.
   * @return the proxy object.
   */
  public static final Object overrideObject(Object obj, Object overrider){
    return new Implementor(overrider.getClass()).override(obj, overrider);
  }
  private static final long serialVersionUID = -5648266362433165290L;
  private static Boolean valueOf(boolean val){
    return val?Boolean.TRUE:Boolean.FALSE;
  }
}
