package org.codehaus.dimple.testutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

public class CloneUtils {
  /**
   * to clone an object that's serializable.
   * @param from the object to be cloned.
   * @param loader the class loader used to load class when cloning.
   * @return the cloned instance.
   */
  public static Object cloneSerializable(Object from, final ClassLoader loader){
    if(from==null) return null;
    try{
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      new ObjectOutputStream(baos).writeObject(from);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      return new ObjectInputStream(bais){
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
          try{
            return Class.forName(desc.getName(), true, loader);
          }
          catch(ClassNotFoundException e) {
            return super.resolveClass(desc);
          }
        }
        
      }.readObject();
    }
    catch(ClassNotFoundException e){
      throw new RuntimeException(e);
    }
    catch(IOException e){
      throw new RuntimeException(e);
    }
  }
  /**
   * to clone an object that's serializable.
   * @param from the object to be cloned.
   * @return the cloned instance.
   */
  public static Object cloneSerializable(Object from){
    if(from==null) return null;
    return cloneSerializable(from, from.getClass().getClassLoader());
  }
}

