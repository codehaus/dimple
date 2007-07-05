package org.codehaus.dimple.misc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

abstract class AbstractInterceptor implements InvocationHandler {
  protected abstract Object getForward();
  protected abstract Object onInvocation(Object proxy, Method method, Object[] args)
  throws Throwable;
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      if(Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      }
      else {
        return onInvocation(proxy, method, args);
      }
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
  public String toString() {
    return ""+getForward();
  }
  public int hashCode() {
    Object fwd = getForward();
    return fwd==null?0:fwd.hashCode();
  }
  public boolean equals(Object other) {
    if(other==null) return false;
    if(Proxy.isProxyClass(other.getClass())) {
      other = Proxy.getInvocationHandler(other);
    }
    if(other instanceof AbstractInterceptor) {
      return equals(getForward(), ((AbstractInterceptor)other).getForward());
    }
    else return false;
  }
  static boolean equals(Object o1, Object o2) {
    return o1==null?o2==null:o1.equals(o2);
  }
  @SuppressWarnings("unchecked")
  public <T> T as(Class<T> itf) {
    return (T)Proxy.newProxyInstance(itf.getClassLoader(), new Class<?>[]{itf}, this);
  }
}
