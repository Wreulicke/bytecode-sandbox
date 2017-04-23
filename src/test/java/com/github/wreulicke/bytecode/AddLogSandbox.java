package com.github.wreulicke.bytecode;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import org.junit.Test;

import com.github.wreulicke.bytecode.test.util.Dump;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.DynamicValue;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.description.annotation.AnnotationDescription.Loadable;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.matcher.ElementMatchers;

public class AddLogSandbox {

  @Test
  public void testAddLogMethodWithJavassist() throws CannotCompileException, IOException, NotFoundException {
    ClassPool pool = ClassPool.getDefault();
    CtClass clazz = pool.get(Some.class.getName());
    for (CtMethod method : clazz.getMethods()) {
      if (method.getDeclaringClass()
        .equals(clazz) && !Modifier.isNative(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers())) {
        method.insertBefore("System.out.println(\"" + method.getName() + ":start\");");
        method.insertAfter("System.out.println(\"" + method.getName() + ":end\");");
      }
    }
    byte[] bytes = clazz.toBytecode();
    Dump.dump(bytes);
  }

  @Test
  public void testAddLogMethodWithByteBuddy() throws IOException {
    byte[] bytes = new ByteBuddy().rebase(Some.class)
      .visit(Advice.withCustomMapping()
        .bind(MethodName.class, new MethodNameBinder())
        .to(ExampleAdvice.class)
        .on(ElementMatchers.isMethod()
          .and(isDeclaredBy(Some.class))
          .and(not(isAbstract()))
          .and(not(isNative()))))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }

  public static class ExampleAdvice {
    @OnMethodEnter
    public static void enter(@MethodName String methodName) {
      System.out.println(methodName + ":start");
    }

    @OnMethodExit
    public static void exit(@MethodName String methodName) {
      System.out.println(methodName + ":end");
    }
  }

  public static class Some {
    public void someMethod() {
      System.out.println("some implementation");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public static @interface MethodName {
  }

  class MethodNameBinder implements DynamicValue<MethodName> {
    @Override
    public StackManipulation resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, InDefinedShape target,
      Loadable<MethodName> annotation, Assigner assigner, boolean initialized) {
      if (target.getType()
        .asErasure()
        .isAssignableFrom(String.class)) {
        String name = instrumentedMethod.getName();
        return new TextConstant(name);
      }
      throw new IllegalStateException("not assignable type");
    }
  }
}
