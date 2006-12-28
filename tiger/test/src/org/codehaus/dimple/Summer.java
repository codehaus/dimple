package org.codehaus.dimple;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This class utilizes dimple to create dynamic proxy for summing up
 * return values of methods in an interface.
 * <p>
 * @author yub
 *
 */
public class Summer implements InvocationHandler {
  private final List participants;
  private final Class targetType;
  private final Implementor implementor;
  public static <T> T sumOf(Class<T> targetType, Object[] arr) {
    return sumOf(targetType, Arrays.asList(arr), arr.getClass().getComponentType());
  }
  @SuppressWarnings("unchecked")
  public static <T> T sumOf(Class<T> targetType, List<?> participants, Class<?> componentType) {
    return (T)Proxy.newProxyInstance(targetType.getClassLoader(), 
        new Class<?>[]{targetType}, new Summer(participants, targetType, 
            determineImplementor(targetType, componentType))
    );
  }
  private static Implementor<?> determineImplementor(
      Class<?> targetType, Class<?> componentType) {
    return componentType==null||targetType.isAssignableFrom(componentType)?
        null:Implementor.instance(componentType);
  }
  public static <T> T sumOf(Class<T> targetType, List<?> participants) {
    return sumOf(targetType, participants, null);
  }
  private Implementor getImplementorFor(Object obj) {
    if(implementor==null){
      return Implementor.instance(obj.getClass());
    }
    else return implementor;
  }
  private Summer(List participants, Class targetType, Implementor implementor) {
    this.participants = participants;
    this.targetType = targetType;
    this.implementor = implementor;
  }
  @SuppressWarnings("unchecked")
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if(Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      }
      else {
        double result = 0;
        for(Iterator<?> it = participants.iterator(); it.hasNext();){
          Object participant = it.next();
          if(participant==null) continue;
          if(!targetType.isInstance(participant)) {
            participant = getImplementorFor(participant).implement(targetType, participant);
          }
          Double val = (Double)method.invoke(participant, args);
          if(val != null)
            result += val.doubleValue();
        }
        return new Double(result);
      }
    }
    catch(InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
}
