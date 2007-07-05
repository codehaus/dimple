package org.codehaus.dimple.misc;

public interface Function<T> {
  T apply(T[] args);
}
