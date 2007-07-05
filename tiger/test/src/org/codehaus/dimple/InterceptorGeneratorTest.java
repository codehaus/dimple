package org.codehaus.dimple;

import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.Assert.*;

public class InterceptorGeneratorTest {
  @Test public void testDeterminePackageName() {
    assertPackageName(Assert.class, Assert.class);
    assertPackageName(Interceptor.class, String.class);
    assertPackageName(Interceptor.class, javax.sql.DataSource.class);
  }

  private void assertPackageName(
          Class<?> classOfExpectedPackage, Class<?> overriderClass) {
    assertEquals(classOfExpectedPackage.getPackage().getName(),
        InterceptorGenerator.determinePackageName(overriderClass));
  }
}
