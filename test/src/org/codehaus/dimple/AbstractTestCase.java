package org.codehaus.dimple;

import java.io.IOException;
import java.util.HashMap;

import org.codehaus.dimple.testutils.CloneUtils;

import junit.framework.TestCase;

public abstract class AbstractTestCase extends TestCase {
  public static Object assertSerializable(Object obj) throws IOException, ClassNotFoundException {
    return CloneUtils.cloneSerializable(obj);
  }
  public static void assertHashable(Object obj){
    HashMap hm = new HashMap();
    Object val = "value";
    hm.put(obj, val);
    assertSame(val, hm.get(obj));
    assertEquals(obj, obj);
  }
}
