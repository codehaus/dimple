package org.codehaus.dimple.misc;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;


/**
 * This class utilizes dimple to create dynamic proxy for summing up
 * return values of methods in an interface.
 * <p>
 * @author yub
 *
 */
public class All extends AbstractInterceptor
implements InvocationHandler, Serializable {
  private static final long serialVersionUID = 3992411460899603473L;
  private final List participants;
  //private final Class targetType;
  //private final Implementor implementor;
  public static <T> T sumOf(Class<T> targetType, Object[] arr) {
    return sumOf(targetType, Arrays.asList(arr), arr.getClass().getComponentType());
  }
  public static <T> T sumOf(Class<T> targetType, List<?> participants, Class<?> componentType) {
    return new All(participants).as(targetType);
  }
  public static <T> T sumOf(Class<T> targetType, List<?> participants) {
    return sumOf(targetType, participants, null);
  }

  @Override
  protected Object getForward() {
    return participants;
  }
  private All(List participants) {
    this.participants = participants;
  }
  private static Method forceAccess(Method mtd) {
    if(Modifier.isPublic(mtd.getModifiers()) && Modifier.isPublic(mtd.getDeclaringClass().getModifiers()))
      return mtd;
    try {
      mtd.setAccessible(true);
    }
    catch(SecurityException e){}
    return mtd;
  }
  @Override
  protected Object onInvocation(Object proxy, final Method method, final Object[] args) throws Throwable {
    if(method.getReturnType().isInterface()) {
      return null;
//      return new Thunk(){
//        @Override
//        protected List<?> evalParticipants() throws Throwable {
//          final ArrayList<?> result = new ArrayList<?>(participants.size());
//          for(Object participant: participants){
//            if(participant==null) {
//              result.add(participant);
//            }
//            else {
//              result.add(method.invoke(participant, args));
//            }
//          }
//          return result;
//        }
//      }.as(method.getReturnType());
    }
    else {
      return calculateSum(method, args);
    }
  }
  private Object calculateSum(Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
    double result = 0;
    for(Object participant: participants){
      if(participant==null) continue;
      Number val = (Number)forceAccess(method).invoke(participant, args);
      if(val != null)
        result += val.doubleValue();
    }
    return new Double(result);
  }
  /**
   * if the object compared to is a dynamic proxy, unwrap it and get the InvocationHandler to compare with.
   */
  @Override
  public boolean equals(Object obj) {
    if(obj!=null && Proxy.isProxyClass(obj.getClass())){
      return super.equals(Proxy.getInvocationHandler(obj));
    }
    else return super.equals(obj);
  }
}
