
/*
 *  Copyright 2006 Ben Yu
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *  Unless required by applicable law or agreed to in writing, 
 *  software distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions 
 *  and limitations under the License.
 *  
 */

package org.codehaus.dimple;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


class TypingUtils {

  private interface ObjectMethods {
    boolean equals(Object obj);
    int hashCode();
    String toString();
  }

  /**
   * To get all interfaces implemented by a class.
   * @param cls the class.
   * @return the interfaces.
   */
  static Class[] getAllInterfaces(Class cls) {
    final HashSet ret = new HashSet();
    for(;cls!=null && !Object.class.equals(cls); cls=cls.getSuperclass()){
      ret.addAll(Arrays.asList(cls.getInterfaces()));
    }
    return (Class[]) ret.toArray(new Class[ret.size()]);
  }

  static int getHierarchyDepth(Class c){
    int depth = 0;
    if(c==null || Object.class.equals(c)){
      return depth;
    }
    int superDepth = 1+getHierarchyDepth(c.getSuperclass());
    if(superDepth>depth){
      depth = superDepth;
    }
    final Class[] itfs = c.getInterfaces();
    for(int i=0; i<itfs.length; i++){
      int itfDepth = 1+getHierarchyDepth(itfs[i]);
      if(itfDepth>depth){
        depth = itfDepth;
      }
    }
    return depth;
  }

  static long getHierarchyDepthSum(Class[] classes){
    long sum = 0;
    for (int i = 0; i < classes.length; i++) {
      sum += getHierarchyDepth(classes[i]);
    }
    return sum;
  }

  static boolean isReturnTypeCompatible(Class with, Class implemented){
    if(void.class.equals(implemented)){
      return true;
    }
    return implemented.isAssignableFrom(with);
  }

  static boolean isParamsCompatible(Class[] with, Class[] implemented){
    if(with.length!=implemented.length) return false;
    for (int i = 0; i < implemented.length; i++) {
      if(!with[i].isAssignableFrom(implemented[i])) return false;
    }
    return true;
  }

  private static void checkImplementingMethods(Method implementingMethod, Class[] implementingParams, Method[] implemented, Class[][] parameterTypesArray) {
    if(Object.class.equals(implementingMethod.getDeclaringClass())) return;
    String name = implementingMethod.getName();
    Class returnType = implementingMethod.getReturnType();
    for(int i=0; i<implemented.length; i++) {
      Method mtd = implemented[i];
      Class[] implementedParams = parameterTypesArray[i];
      if(name.equals(mtd.getName()) && isParamsCompatible(implementingParams, implementedParams)){
        if(!isReturnTypeCompatible(returnType, mtd.getReturnType())) {
          throw new InvalidReturnTypeException(mtd, implementingMethod);
        }
        return;
      }
    }
    throw new UnusedMethodException(implementingMethod);
  }

  private static void checkImplementingMethods(Method implementing, Method[] implemented, Class[][] parameterTypesArray){
    checkImplementingMethods(implementing, implementing.getParameterTypes(), 
        implemented, parameterTypesArray);
  }

  static void checkImplementingMethods(Method[] implementing, Method[] implemented)
  throws InvalidReturnTypeException, UnusedMethodException {
    final Class[][] paramTypesArray = getParameterTypesArray(implemented);
    for(int i=0; i<implementing.length; i++) {
      checkImplementingMethods(implementing[i], implemented, paramTypesArray);
    }
  }

  static Class[][] getParameterTypesArray(Method[] implemented) {
    final Class[][] paramTypesArray = new Class[implemented.length][];
    for(int i=0; i<implemented.length; i++){
      paramTypesArray[i] = implemented[i].getParameterTypes();
    }
    return paramTypesArray;
  }

  static int compareParameterTypes(final Class[] params1, long depth1, 
      final Class[] params2, long depth2) {
    if(params1.length > params2.length) return -1;
    if(params1.length < params2.length) return 1;
    if(depth1 > depth2) return -1;
    if(depth1 < depth2) return 1;
    return 0;
  }

  private static final List objectMethodsSignatures = 
  Arrays.asList(ObjectMethods.class.getMethods());

  static Method[] getAllMethodsToImplement(Class[] asTypes) {
    ArrayList allmethods = new ArrayList();
    for(int i=0; i<asTypes.length; i++) {
      allmethods.addAll(Arrays.asList(asTypes[i].getMethods()));
    }
    allmethods.addAll(objectMethodsSignatures);
    return (Method[]) allmethods.toArray(new Method[allmethods.size()]);
  }

}
