
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
