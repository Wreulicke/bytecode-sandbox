package com.github.wreulicke.bytecode;

import static net.bytebuddy.matcher.ElementMatchers.isAbstract;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isNative;
import static net.bytebuddy.matcher.ElementMatchers.not;

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
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.description.annotation.AnnotationDescription.Loadable;
import net.bytebuddy.description.method.ParameterDescription.InDefinedShape;
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
        .bind(new MethodNameBinder())
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

  class MethodNameBinder implements Advice.OffsetMapping.Factory<MethodName> {

    @Override
    public Class<MethodName> getAnnotationType() {
      return MethodName.class;
    }

    @Override
    public Advice.OffsetMapping make(InDefinedShape target, Loadable<MethodName> annotation, AdviceType adviceType) {
    	
      return (instrumentedType, instrumentedMethod, assigner, argumentHandler, sort) -> {
        if (target.getType()
          .asErasure()
          .isAssignableFrom(String.class)) {
          String name = instrumentedMethod.getName();
          return Advice.OffsetMapping.Target.ForStackManipulation.of(name);
        }
        throw new IllegalStateException("not assignable type");
      };
    }
  }
}
