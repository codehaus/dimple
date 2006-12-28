package org.codehaus.dimple.misc;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.codehaus.dimple.Implementor;

/**
 * This class utilizes dimple to create dynamic proxy for summing up
 * return values of methods in an interface.
 * <p>
 * @author yub
 *
 */
public class All implements InvocationHandler, Serializable {
  private static final long serialVersionUID = 3992411460899603473L;
  private final List participants;
  private final Class targetType;
  private final Implementor implementor;
  public static <T> T sumOf(Class<T> targetType, Object[] arr) {
    return sumOf(targetType, Arrays.asList(arr), arr.getClass().getComponentType());
  }
  @SuppressWarnings("unchecked")
  public static <T> T sumOf(Class<T> targetType, List<?> participants, Class<?> componentType) {
    return (T)Proxy.newProxyInstance(targetType.getClassLoader(), 
        new Class<?>[]{targetType}, new All(participants, targetType, 
            determineImplementor(targetType, componentType))
    );
  }
  private static Implementor<?> determineImplementor(
      Class<?> targetType, Class<?> componentType) {
    return componentType==null||targetType.isAssignableFrom(componentType)?
        null:ImplementorPool.getInstance(componentType);
  }
  public static <T> T sumOf(Class<T> targetType, List<?> participants) {
    return sumOf(targetType, participants, null);
  }
  private Implementor getImplementorFor(Object obj) {
    if(implementor==null){
      return ImplementorPool.getInstance(obj.getClass());
    }
    else return implementor;
  }
  @SuppressWarnings("unchecked")
  private Object convert(Object from){
    return getImplementorFor(from).implement(targetType, from);
  }
  private All(List participants, Class targetType, Implementor implementor) {
    this.participants = participants;
    this.targetType = targetType;
    this.implementor = implementor;
  }
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
            participant = convert(participant);
          }
          Number val = (Number)method.invoke(participant, args);
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
