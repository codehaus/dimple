package org.codehaus.dimple;


/**
 * An interceptor represents byte code generated at runtime
 * that knows how to intercept instances of {@code T}
 * with an instance of {@code Impl} type.
 * <br>
 * Interceptor also knows how to use instances of {@code Impl}
 * to create stub of {@code T}.
 * <p>
 * Interceptor is faster than the regular reflection based
 * proxies.
 * @author benyu
 *
 * @param <T> the type to be stubbed/intercepted.
 * @param <Impl> the type used to stub or intercept.
 */
public interface Interceptor<T, Impl> {
  /**
   * To create a stub of type {@code T}.
   * @param obj the instance used to create stub.
   * @return the stub instance.
   */
  T stub(Impl obj);
  /**
   * To create an instance of {@code T}
   * that is backed by {@code intercepted}
   * and is intercepted by {@code overrider}
   * @param intercepted the intercepted instance.
   * @param overrider the instance used to intercept.
   * @return the new instance.
   */
  T intercept(T intercepted, Impl overrider);
}
