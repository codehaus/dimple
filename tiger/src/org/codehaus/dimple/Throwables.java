package org.codehaus.dimple;

/**
 * Utility functions handling exceptions
 */
class Throwables {
  /**
   * Rethrow an exception if it is an instance of the specified exception type.
   * @param <T> the exception type parameter.
   * @param e the exception instance.
   * @param exceptionType the exception type.
   */
  public static <T extends Throwable>
  void rethrow(Throwable e, Class<T> exceptionType) throws T {
    if(exceptionType.isInstance(e)) {
      throw exceptionType.cast(e);
    }
  }
  /**
   * Rethrow the exception if it is an instance of 
   * RuntimeException or Error.
   * @param e the exception instance.
   */
  public static void rethrowUnchecked(Throwable e) {
    rethrow(e, Error.class);
    rethrow(e, RuntimeException.class);
  }
  /**
   * This function will try to rethrow the exception instance
   * if it is RuntimeException or Error. Otherwise, a RuntimeException
   * will be used to wrap the exception instance and thrown.
   * @param e the exception instance.
   * @return this method will never return. Use
   * <pre>throw unchecked(e);</pre> to bypass type system.
   */
  public static RuntimeException unchecked(Throwable e) {
    rethrowUnchecked(e);
    throw new RuntimeException(e);
  }
}

