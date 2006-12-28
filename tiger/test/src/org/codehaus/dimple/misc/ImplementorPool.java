package org.codehaus.dimple.misc;

import java.util.WeakHashMap;

import org.codehaus.dimple.Implementor;

/**
 * This class provides caching for Implementor objects of the same impl class.
 * <p>
 * @author Ben Yu
 * Dec 27, 2006 11:43:28 PM
 */
public class ImplementorPool {
  private static final WeakHashMap<Class, Implementor> cache = new WeakHashMap<Class, Implementor>();
  /**
   * Get an instance of Implementor for the impl class.
   * @param implClass the impl class.
   * @return the instance that's either from the cache or instantiated.
   */
  public static synchronized <T> Implementor<T> getInstance(Class<T> implClass) {
    Implementor<T> implementor = getCachedImplementor(implClass);
    if(implementor==null) {
      implementor = Implementor.instance(implClass);
      cache.put(implClass, implementor);
    }
    return implementor;
  }
  /**
   * Clear the cache.
   */
  public static synchronized void clear(){
    cache.clear();
  }
  @SuppressWarnings("unchecked")
  private static <T> Implementor<T> getCachedImplementor(Class<T> implClass) {
    return cache.get(implClass);
  }
}
