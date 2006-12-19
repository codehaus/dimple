package org.codehaus.dimple;

import java.lang.reflect.Method;

/**
 * This exception indicates that a declared method
 * is not used to implement anything.
 * <p>
 * @author Ben Yu
 * Dec 17, 2006 7:23:29 PM
 */
public class UnusedMethodException extends RuntimeException {
  private static final long serialVersionUID = -9169564945324932438L;
  private final Method method;
  public Method getMethod(){
    return method;
  }
  public UnusedMethodException(Method method) {
    this(method, method.toString()+" is not used to implement anything.");
  }
  public UnusedMethodException(Method method, String msg){
    super(msg);
    this.method = method;
  }
}
