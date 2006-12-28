package org.codehaus.dimple;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import junit.framework.TestCase;

public abstract class AbstractTestCase extends TestCase {
  @SuppressWarnings("unchecked")
  public static <T> T assertSerializable(T obj) throws IOException, ClassNotFoundException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(obj);
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bais);
    return (T)ois.readObject();
  }
  @SuppressWarnings("unchecked")
  public static void assertHashable(Object obj){
    HashMap hm = new HashMap();
    Object val = "value";
    hm.put(obj, val);
    assertSame(val, hm.get(obj));
    assertEquals(obj, obj);
  }
}
