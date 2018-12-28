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
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.description.annotation.AnnotationDescription.Loadable;
import net.bytebuddy.description.method.ParameterDescription.InDefinedShape;
import net.bytebuddy.matcher.ElementMatchers;

public class LoadFieldSandbox {

  @Test
  public void testFieldRefWithJavassist() throws CannotCompileException, IOException, NotFoundException {
    ClassPool pool = ClassPool.getDefault();
    CtClass clazz = pool.get(Some.class.getName());
    for (CtMethod method : clazz.getMethods()) {
      if (method.getDeclaringClass()
        .equals(clazz) && !Modifier.isNative(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers())) {
        method.insertBefore("System.out.println(\"field :\" + this.field);");
        method.insertAfter("this.field=\"end\";" + "System.out.println(\"field :\" + this.field);");
      }
    }
    byte[] bytes = clazz.toBytecode();
    Dump.dump(bytes);
  }

  @Test
  public void testFieldRefWithByteBuddy() throws IOException {
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
    public static void enter(@FieldValue(value = "field") String field) {
      System.out.println("field :" + field);
    }

    @OnMethodExit
    public static void exit(@FieldValue(value = "field", readOnly = false) String field) {
      field = "end";
      System.out.println("field :" + field);
    }
  }

  public static class Some {
    private String field;

    public void test() {
      System.out.println("inplemented code, field :" + field);
    }

    ;
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
