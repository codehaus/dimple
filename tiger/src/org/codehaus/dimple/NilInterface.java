package org.codehaus.dimple;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class NilInterface implements InvocationHandler {
  public Object invoke(Object proxy, Method mtd, Object[] args)
      throws Throwable {
    //special handling for methods defined in Object.
    if(mtd.getDeclaringClass().equals(Object.class)) {
      return mtd.invoke(this, args);
    }
    throw new UnsupportedOperationException(mtd.toString());
  }
  private NilInterface() {}
  private static InvocationHandler singleton = new NilInterface();
  @SuppressWarnings("unchecked")
  public static <T> T as(Class<T> itf) {
    return (T)Proxy.newProxyInstance(itf.getClassLoader(), new Class[]{itf}, singleton);
  }
}
