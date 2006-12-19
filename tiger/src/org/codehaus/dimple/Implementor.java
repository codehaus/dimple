
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
import java.util.HashSet;
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
 * Connection nonCloseableConnection = Implementor.proxy(Connection.class, new Object(){
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
public class Implementor<ImplClass> implements Serializable {
  /**
   * Equivalent as new Implementor(with.getClass()).implementWithDefaultHandler(itf, with, defaultHandler)
   * <p>
   * This method is a convenience shortcut.
   * As the constructor of Implementor may be expensive, it is recommended to create an Implementor object once
   * and then use it repeatedly.
   */
  public static <T, ImplClass> T proxyWithDefaultHandler(Class<T> asType, ImplClass with, InvocationHandler defaultHandler) {
    return getInstanceForImplObject(with).implementWithDefaultHandler(asType, with, defaultHandler);
  }
  /**
   * Equivalent as new Implementor(with.getClass()).implement(asType, with, defaultDelegate)
   * <p>
   * This method is a convenience shortcut.
   * As the constructor of Implementor may be expensive, it is recommended to create an Implementor object once
   * and then use it repeatedly.
   */
  public static <T, ImplClass> T proxy(Class<T> asType, ImplClass with, T defaultDelegate){
    if(!asType.isInstance(defaultDelegate)){
      throw new IllegalArgumentException("default delegate of type "+asType.getName() + " expected, "
          + ((defaultDelegate==null)?null:defaultDelegate.getClass().getName())+" encountered");
    }
    return getInstanceForImplObject(with).implement(asType, with, defaultDelegate);
  }
  
  /**
   * Equivalent as new Implementor(with.getClass()).implement(asType)
   * <p>
   * This method is a convenience shortcut.
   * As the constructor of Implementor may be expensive, it is recommended to create an Implementor object once
   * and then use it repeatedly. 
   */
  public static <T, ImplClass> T proxy(Class<T> asType, ImplClass with) {
    return getInstanceForImplObject(with).implement(asType, with);
  }

  @SuppressWarnings("unchecked")
  private static <ImplClass> Implementor<ImplClass> getInstanceForImplObject(ImplClass with){
    return new Implementor<ImplClass>((Class<ImplClass>)with.getClass());
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
  public <T> T implement(Class<T> asType, ImplClass with, T defaultDelegate) {
    checkImplementingMethods(asType);
    return newProxyInstance(with.getClass().getClassLoader(), asType, 
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
  public <T> T implementWithDefaultHandler(Class<T> asType, ImplClass with, InvocationHandler defaultHandler) {
    checkImplementingMethods(asType);
    return newProxyInstance(with.getClass().getClassLoader(), asType, 
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
  public <T> T implement(Class<T> asType, ImplClass with){
    checkImplementingMethods(asType);
    return newProxyInstance(with.getClass().getClassLoader(), asType, 
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
  public InvocationHandler createInvocationHandler(final ImplClass instance){
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
  public InvocationHandler createInvocationHandlerWithDefaultHandler(final ImplClass instance, final InvocationHandler defaultHandler){
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
  public InvocationHandler createInvocationHandler(final ImplClass instance, Object defaultDelegate){
    return createInvocationHandlerWithDefaults(instance, defaultDelegate, null);
  }
  InvocationHandler createInvocationHandlerWithDefaults(ImplClass instance, Object defaultDelegate, InvocationHandler defaultHandler){
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
  public Implementor(Class<ImplClass> implClass){
    this.implClass = implClass;
    addClass(implClass);
    sort();
  }
  /**
   * Convenience method to create an Implementor object.
   * @param <ImplClass> the impl class.
   * @param implClass the iml class object.
   * @return the Implementor object.
   */
  public static <ImplClass> Implementor<ImplClass> instance(Class<ImplClass> implClass){
    return new Implementor<ImplClass>(implClass);
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
  public Class<ImplClass> getImplClass(){
    return implClass;
  }
  void addClass(Class<?> cls) {
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
  private final Class<ImplClass> implClass;
  private final Map<String, List<MyMethod>> methods = new HashMap<String, List<MyMethod>>();
  private final ArrayList<MyMethod> mustUses = new ArrayList<MyMethod>();
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
          return Boolean.valueOf(proxy==args[0]);
        }
        return Boolean.valueOf(instance.equals(unwrap(args[0])));
      }
      return invokeMethod(instance, mtd, args);
    }
    
    private Object forwardCall(Object proxy, Object obj, final Method mtd, Object[] args) throws IllegalAccessException, Throwable {
      if(isEqualsMethodCall(mtd, args)){
        return Boolean.valueOf(obj.equals(unwrap(args[0])));
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

  private static final class MyMethod implements Serializable {
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
      this.depth = getHierarchyDepthSum(parameterTypes);
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
    public Class getReturnType() {
      return method.getReturnType();
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
    List<MyMethod> suite = methods.get(name);
    if(suite==null){
      suite = new ArrayList<MyMethod>();
      methods.put(name, suite);
    }
    MyMethod mm = new MyMethod(mtd); 
    suite.add(mm);
    checkAnnotation(mm);
  }
  private void checkAnnotation(MyMethod mm) {
    Method mtd = mm.getMethod();
    //add mustUses
    Implement annotation = mtd.getAnnotation(Implement.class);
    if(annotation == null) {
      annotation = mtd.getDeclaringClass().getAnnotation(Implement.class);
    }
    if(annotation != null) {
      mustUses.add(mm);
    }
  }
  void sort(){
    for(List<MyMethod> suite : methods.values()){
      sortMethods(suite);
    }
  }
  private static final Comparator<MyMethod> SUB_PARAM_TYPES_FIRST = new Comparator<MyMethod>(){
    public int compare(MyMethod m1, MyMethod m2) {
      return compareMyMethod(m1, m2);
    }
  };
  private static int compareMyMethod(MyMethod m1, MyMethod m2){
    return compareParameterTypes(m1.getParameterTypes(), m1.getDepth(), 
        m2.getParameterTypes(), m2.getDepth());
  }
  static int compareParameterTypes(final Class<?>[] params1, long depth1, 
      final Class<?>[] params2, long depth2) {
    if(params1.length > params2.length) return -1;
    if(params1.length < params2.length) return 1;
    if(depth1 > depth2) return -1;
    if(depth1 < depth2) return 1;
    return 0;
  }
  private static void sortMethods(List<MyMethod> suite){
    Collections.sort(suite, SUB_PARAM_TYPES_FIRST);
  }
  /**
   * To find a method in the impl class that can be used in place of the <i>implemented</i> method.
   * @param implemented the method to be implemented.
   * @return the method that can be used to implement, or null if not found.
   */
  public Method lookupImplementingMethod(Method implemented) {
    final String name = implemented.getName();
    final List<MyMethod> suite = methods.get(name);
    if(suite==null) return null;
    final int size = suite.size();
    final Class[] implementedParamTypes = implemented.getParameterTypes();
    for(int i=0; i<size; i++){
      final MyMethod mm = suite.get(i);
      final Class[] withParamTypes = mm.getParameterTypes();
      if(isParamsCompatible(withParamTypes, implementedParamTypes)){
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
  @SuppressWarnings("unchecked")
  public static <T> T newProxyInstance(ClassLoader loader, Class<T> asType, InvocationHandler handler){
    if(asType.isInterface()){
      return (T)Proxy.newProxyInstance(loader, new Class[]{asType}, handler);
    }
    else {
      return (T)CglibUtils.proxy(loader, asType, handler);
    }
  }
  private interface ObjectMethods {
    boolean equals(Object obj);
    int hashCode();
    String toString();
  }
  private static final List<Method> objectMethodsSignatures = 
    Arrays.asList(ObjectMethods.class.getMethods());
  /**
   * Makes sure that methods defined by ImplClass implement some method
   * in <i>asType</i>.
   * This checking is only performed on methods annotated by Implement.
   * @param asType the interface to implement.
   */
  public void checkImplementingMethods(Class<?> asType)
  throws InvalidReturnTypeException, UnusedMethodException {
    if(mustUses.isEmpty()) return;
    checkImplementingMethods(new Class[]{asType});
  }
  /**
   * Makes sure that methods defined by ImplClass implement some method
   * in any Class object in <i>asTypes</i>.
   * This checking is only performed on methods annotated by Implement.
   * @param asTypes the interfaces to implement.
   */
  public void checkImplementingMethods(Class<?>[] asTypes)
  throws InvalidReturnTypeException, UnusedMethodException {
    if(mustUses.isEmpty()) return;
    ArrayList<Method> allmethods = new ArrayList<Method>();
    for(Class<?> asType : asTypes) {
      allmethods.addAll(Arrays.asList(asType.getMethods()));
    }
    allmethods.addAll(objectMethodsSignatures);
    checkImplementingMethods((Method[]) allmethods.toArray(new Method[allmethods.size()]));
  }
  void checkImplementingMethods(Method[] implemented)
  throws InvalidReturnTypeException, UnusedMethodException {
    final Class<?>[][] paramTypesArray = new Class<?>[implemented.length][];
    for(int i=0; i<implemented.length; i++){
      paramTypesArray[i] = implemented[i].getParameterTypes();
    }
    for(MyMethod mm : mustUses) {
      checkImplementingMethods(mm, implemented, paramTypesArray);
    }
  }
  private static void checkImplementingMethods(MyMethod implementing, Method[] implemented, Class<?>[][] parameterTypesArray)
  throws InvalidReturnTypeException, UnusedMethodException {
    Class[] implementingParams = implementing.getParameterTypes();
    Class returnType = implementing.getReturnType();
    String name = implementing.getMethod().getName();
    for(int i=0; i<implemented.length; i++) {
      Method mtd = implemented[i];
      Class<?>[] implementedParams = parameterTypesArray[i];
      if(name.equals(mtd.getName()) && isParamsCompatible(implementingParams, implementedParams)){
        if(!isReturnTypeCompatible(returnType, mtd.getReturnType())) {
          throw new InvalidReturnTypeException(mtd, implementing.getMethod());
        }
        return;
      }
    }
    throw new UnusedMethodException(implementing.getMethod());
  }
  static boolean isParamsCompatible(Class<?>[] with, Class<?>[] implemented){
    if(with.length!=implemented.length) return false;
    for (int i = 0; i < implemented.length; i++) {
      if(!with[i].isAssignableFrom(implemented[i])) return false;
    }
    return true;
  }
  static boolean isReturnTypeCompatible(Class<?> with, Class<?> implemented){
    if(void.class.equals(implemented)){
      return true;
    }
    return implemented.isAssignableFrom(with);
  }
  static long getHierarchyDepthSum(Class[] classes){
    long sum = 0;
    for (int i = 0; i < classes.length; i++) {
      sum += getHierarchyDepth(classes[i]);
    }
    return sum;
  }
  static int getHierarchyDepth(Class c){
    int depth = 0;
    if(c==null || Object.class.equals(c)){
      return depth;
    }
    int superDepth = 1+getHierarchyDepth(c.getSuperclass());
    if(superDepth>depth){
      depth = superDepth;
    }
    final Class[] itfs = c.getInterfaces();
    for(Class itf : itfs){
      int itfDepth = 1+getHierarchyDepth(itf);
      if(itfDepth>depth){
        depth = itfDepth;
      }
    }
    return depth;
  }

  /**
   * Overrides an object using methods defined in impl class and the overrider object
   * bound to "this".
   * All interfaces of <i>obj</i> are implemented by the proxy.
   * @param obj the object to be overriden.
   * @param overrider the overrider.
   * @return the proxy object.
   */
  public final Object override(Object obj, ImplClass overrider){
    Class overriden = obj.getClass();
    final Class[] itfs = getAllInterfaces(overriden);
    checkImplementingMethods(itfs);
    return Proxy.newProxyInstance(overriden.getClassLoader(), itfs,
        createInvocationHandler(overrider, obj));
  }

  /**
   * Overrides an object using the overrider object.
   * All interfaces of <i>obj</i> are implemented by the proxy.
   * @param obj the object to be overriden.
   * @param overrider the overrider.
   * @return the proxy object.
   */
  public static <ImplClass> Object overrideObject(Object obj, ImplClass overrider){
    return getInstanceForImplObject(overrider).override(obj, overrider);
  }
  /**
   * To get all interfaces implemented by a class.
   * @param cls the class.
   * @return the interfaces.
   */
  static Class<?>[] getAllInterfaces(Class<?> cls) {
    final HashSet<Class> ret = new HashSet<Class>();
    for(;cls!=null && !Object.class.equals(cls); cls=cls.getSuperclass()){
      ret.addAll(Arrays.asList(cls.getInterfaces()));
    }
    return (Class<?>[]) ret.toArray(new Class<?>[ret.size()]);
  }
  private static final long serialVersionUID = -5648266362433165290L;
}
