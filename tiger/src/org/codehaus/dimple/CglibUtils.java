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

import java.lang.reflect.InvocationHandler;

import net.sf.cglib.proxy.Enhancer;


/**
 * This class provides helper methods for cglib.
 * <p>
 * @author Ben Yu
 * Dec 10, 2006 9:09:47 PM
 */
class CglibUtils {
  /**
   * To create a proxy using cglib enhancement.
   * @param loader the class loader used to load the class.
   * @param superclass the super class to extend. null if none.
   * @param interfaces the interfaces to implement. null if none.
   * @param handler the InvocationHandler to handle calls.
   * @return the proxy object.
   */
  @SuppressWarnings("unchecked")
  public static <Super> Super proxy(
      ClassLoader loader, Class<Super> superclass, Class<?>[] interfaces, InvocationHandler handler){
    Enhancer enhancer = new Enhancer();
    enhancer.setCallback(new CglibInvocationHandlerAdapter(handler));
    enhancer.setClassLoader(loader);
    if(interfaces != null)
      enhancer.setInterfaces(interfaces);
    if(superclass != null)
      enhancer.setSuperclass(superclass);
    return (Super)enhancer.create();
  }
  /**
   * To create a proxy using cglib enhancement.
   * @param loader the class loader used to load the class.
   * @param superclass the super class to extend.
   * @param handler the InvocationHandler to handle calls.
   * @return the proxy object.
   */
  public static <Super> Super proxy(ClassLoader loader, Class<Super> superclass, InvocationHandler handler){
    return proxy(loader, superclass, null, handler);
  }
}
