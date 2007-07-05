package org.codehaus.dimple.misc;

public class DoubleSum implements Function<Double> {
  public Double apply(Double[] args) {
    double result = 0;
    for(Object arg: args) {
      if(arg==null) continue;
      result += ((Double)arg).doubleValue();
    }
    return new Double(result);
  }
  private DoubleSum() {}
  private static Function singleton = new DoubleSum();
  public static Function instance() {
    return singleton;
  }
}
