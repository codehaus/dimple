package org.codehaus.dimple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.core.ReflectUtils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/*
 * This class is used to generate interceptor byte code.
 */
class InterceptorGenerator {
  private static int seed = 0;
  private static synchronized int nextSeed() {
    return ++seed;
  }
  interface MethodMapping {
    Method getOverrrider(Method method);
  }
  static void generateMethods(String className, Class<?> type, 
      ClassVisitor classVisitor, MethodMapping overriding) {
    for(Method mtd: type.getMethods()) {
      Method overrider = overriding.getOverrrider(mtd);
      if(overrider==null) {
        //no overriding, just delegate.
        generateMethod(className, classVisitor, mtd, "interceptee", mtd);
      }
      else {
        generateMethod(className, classVisitor, mtd, "overrider", overrider);
      }
    }
  }
  static <T, Impl> Interceptor<T, Impl> generateInterceptor(Class<T> type, 
      Class<Impl> overriderType, MethodMapping overriding) {
    String className = determinePackageName(overriderType)+"."
      + type.getName().replace('.', '_')+"$$"
      + overriderType.getName().replace('.', '_')
      +"$$GeneratedInterceptor"+nextSeed();
    ClassLoader loader = type.getClassLoader();
    ClassWriter classWriter = beginInterceptorClass(type, className);
    //need to define all methods of the target interface.
    generateMethods(className, type, classWriter, overriding);
    classWriter.visitEnd();
    return generateInterceptor(
            classWriter, className, type, overriderType, loader);
  }
  static String determinePackageName(Class<?> implType) {
    String implPackage = implType.getPackage().getName();
    if(implPackage.startsWith("java.") || implPackage.startsWith("javax.")) {
      return Interceptor.class.getPackage().getName();
    }
    return implPackage;
  }
  static <T> ClassWriter beginInterceptorClass(Class<T> type, String className) {
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    classWriter.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC, 
        toTypeInternalName(className), null, 
        Type.getInternalName(InternalDimpleInterceptorBase.class), 
        new String[]{Type.getInternalName(type)});
    classWriter.visitSource(className+".class", "generated");
    generateDefaultConstructor(toTypeDescriptor(className), classWriter);
    return classWriter;
  }
  static <T,Impl> Interceptor<T,Impl> generateInterceptor(
      ClassWriter classWriter, String className, final Class<T> interceptedType, 
      Class<Impl> overriderType, ClassLoader loader) {
    final Class generatedClass = defineClass(className, classWriter, loader);
    return new Interceptor<T,Impl>() {
      public T stub(Impl obj) {
        return instantiate(NilInterface.as(interceptedType), obj);
      }
      public T intercept(T intercepted, Impl overrider) {
        return instantiate(intercepted, overrider);
      }
      @SuppressWarnings("unchecked")
      private  T instantiate(T intercepted, Impl overrider) {
        return (T)instantiateInterceptor(generatedClass, intercepted, overrider);
      }
    };
  }
  private static Class defineClass(
      String className, ClassWriter classWriter, ClassLoader loader) {
    try {
      return ReflectUtils.defineClass(
          className, classWriter.toByteArray(), loader);
    } catch(InvocationTargetException e) {
      throw Throwables.unchecked(e.getTargetException());
    }
    catch (Exception e) {
      throw Throwables.unchecked(e);
    }
  }
  static Object instantiateInterceptor(
      Class<? extends InternalDimpleInterceptorBase> generatedClass,
      Object intercepted, Object overrider) {
    try {
      InternalDimpleInterceptorBase instance = (InternalDimpleInterceptorBase)generatedClass.newInstance();
      instance.interceptee = intercepted;
      instance.overrider = overrider;
      return instance;
    } catch (Exception e) {
      throw Throwables.unchecked(e);
    }
  }
  static int getInvokeOp(Method targetMethod) {
    if(targetMethod.getDeclaringClass().isInterface()) {
      return Opcodes.INVOKEINTERFACE;
    }
    else if(Modifier.isStatic(targetMethod.getModifiers())) {
      return Opcodes.INVOKESTATIC;
    }
    else {
      return Opcodes.INVOKEVIRTUAL;
    }
  }
  //TODO: how do we deal with signatures?
  static void generateMethod(String className, ClassVisitor classVisitor, 
      Method overriddenMethod, String targetName, Method targetMethod) {
    Class targetType = targetMethod.getDeclaringClass();
    int invokeOp = getInvokeOp(targetMethod);
    MethodVisitor code = classVisitor.visitMethod(
        Opcodes.ACC_PUBLIC, overriddenMethod.getName(), 
        Type.getMethodDescriptor(overriddenMethod), 
        null, toInternalNames(overriddenMethod.getExceptionTypes()));
    code.visitCode();
    Label begin = new Label();
    code.visitLabel(begin);
    if(Opcodes.INVOKESTATIC != invokeOp) {
      code.visitVarInsn(Opcodes.ALOAD, 0);
      code.visitFieldInsn(Opcodes.GETFIELD, toTypeInternalName(className), 
          targetName, "Ljava/lang/Object;");
      code.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(targetType));
    }
    int stackIndex = 1;
    for(Type argType: Type.getArgumentTypes(overriddenMethod)) {
      code.visitVarInsn(argType.getOpcode(Opcodes.ILOAD), stackIndex);
      stackIndex += argType.getSize();
    }
    code.visitMethodInsn(invokeOp, Type.getInternalName(targetType), 
        targetMethod.getName(), Type.getMethodDescriptor(targetMethod));
    code.visitInsn(
        Type.getReturnType(overriddenMethod).getOpcode(Opcodes.IRETURN));
    Label end = new Label();
    code.visitLabel(end);
    declareParameters(className, overriddenMethod, code, begin, end);
    code.visitEnd();
  }
  private static String[] toInternalNames(Class<?>[] classes) {
    String[] exceptionNames = new String[classes.length];
    for(int i=0; i<classes.length; i++) {
      exceptionNames[i] = Type.getInternalName(classes[i]);
    }
    return exceptionNames;
  }
  static void declareParameters(String className, Method overriddenMethod, 
      MethodVisitor code, Label begin, Label end) {
    //need to get signatures of parameters.
    code.visitLocalVariable(
        "this", toTypeDescriptor(className), null, begin, end, 0);
    int stackSize = 1;
    Type[] argTypes = Type.getArgumentTypes(overriddenMethod);
    for(int i=0; i<argTypes.length; i++) {
      Type argType = argTypes[i];
      String descriptor = argType.getDescriptor();
      code.visitLocalVariable("arg"+i, descriptor, null, begin, end, stackSize);
      stackSize += argType.getSize();
    }
    code.visitMaxs(stackSize, argTypes.length+1);
  }
  static String toTypeDescriptor(String className) {
    return "L"+toTypeInternalName(className)+";";
  }
  static String toTypeInternalName(String className) {
    return className.replace('.', '/');
  }
  static void generateDefaultConstructor(
      String typeDescriptor, ClassVisitor visitor) {
    /*
     the current way only works for interface. 
     But it is concise because we can extend from a base class
     that defines the delegate target field already.
    */
    MethodVisitor ctor = visitor.visitMethod(
        Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
    ctor.visitCode();
    Label begin = new Label();
    ctor.visitLabel(begin);
    ctor.visitVarInsn(Opcodes.ALOAD, 0);
    ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, 
        Type.getInternalName(InternalDimpleInterceptorBase.class), "<init>", "()V");
    ctor.visitInsn(Opcodes.RETURN);
    Label end = new Label();
    ctor.visitLabel(end);
    ctor.visitLocalVariable("this", typeDescriptor, null, begin, end, 0);
    ctor.visitMaxs(1, 1);
    ctor.visitEnd();
  }
}
