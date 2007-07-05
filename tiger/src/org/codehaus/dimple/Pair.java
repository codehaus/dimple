package org.codehaus.dimple;

final class Pair<A,B> {
  private final A a;
  private final B b;
  public Pair(final A a, final B b) {
    this.a = a;
    this.b = b;
  }
  public A getFirst() {
    return a;
  }
  public B getSecond() {
    return b;
  }
  public String toString() {
    return "("+a+","+b+")";
  }
  public int hashCode() {
    return a.hashCode()*31+b.hashCode();
  }
  public boolean equals(Object obj) {
    if(obj instanceof Pair) {
      Pair other = (Pair)obj;
      return a.equals(other.a) && b.equals(other.b);
    }
    else return false;
  }
}
