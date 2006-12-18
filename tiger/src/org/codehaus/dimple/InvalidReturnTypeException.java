package org.codehaus.dimple;

import java.lang.reflect.Method;

/**
 * This exception indicates that a method is used to implement a target method
 * but the return type is not compatible.
 * <p>
 * @author Ben Yu
 * Dec 17, 2006 9:20:06 PM
 */
public class InvalidReturnTypeException extends RuntimeException {
  private static final long serialVersionUID = -4155698297716997696L;
  private final Method implementedMethod;
  private final Method implementingMethod;
  /**
   * Get the method that we are trying to implement.
   */
  public Method getImplementedMethod() {
    return implementedMethod;
  }
  /**
   * Get the method used to implement the target method.
   */
  public Method getImplementingMethod() {
    return implementingMethod;
  }

  /**
   * Create an InvalidReturnTypeException instance.
   * @param implementedMethod the method that is being implemented.
   * @param implementingMethod the method used to implement the target method.
   */
  public InvalidReturnTypeException(Method implementedMethod, Method implementingMethod) {
    this(implementedMethod, implementingMethod, 
        implementingMethod.toString()+" is used to implement "
        +implementedMethod.toString()+" with incompatible return type");
  }
  /**
   * Create an InvalidReturnTypeException instance.
   * @param implementedMethod the method that is being implemented.
   * @param implementingMethod the method used to implement the target method.
   * @param message the error message.
   */
  public InvalidReturnTypeException(Method implementedMethod, Method implementingMethod,
      String message) {
    super(message);
    this.implementedMethod = implementedMethod;
    this.implementingMethod = implementingMethod;
  }
  
}
