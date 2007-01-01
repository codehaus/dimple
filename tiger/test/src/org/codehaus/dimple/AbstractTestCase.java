package org.codehaus.dimple;

import java.io.IOException;
import java.util.HashMap;

import org.codehaus.dimple.testutils.CloneUtils;

import junit.framework.TestCase;

public abstract class AbstractTestCase extends TestCase {
  @SuppressWarnings("unchecked")
  public static <T> T assertSerializable(T obj) throws IOException, ClassNotFoundException {
    return (T)CloneUtils.cloneSerializable(obj);
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
