package com.github.wreulicke.bytecode.bytebuddy;

import static net.bytebuddy.asm.Advice.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.validation.Validation;
import javax.validation.executable.ExecutableValidator;

import org.junit.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice.AllArguments;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.asm.Advice.Return;
import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.matcher.ElementMatcher.Junction;

public class ByteBuddyTest {

  @Test
  public void test1() throws IOException, InstantiationException, IllegalAccessException {
    Junction<TypeDefinition> noReturn = is(void.class);
    byte[] by = new ByteBuddy().rebase(ByteBuddyTest.class)
      .visit(to(ExampleExitAdvice.class).on(isMethod().and(returns(not(noReturn)))))
      .visit(to(ExampleEnterAdvice.class).on(isMethod()))
      .make()
      .getBytes();
    Path names = Paths.get(".", "test.class");
    Files.write(names, by);
  }

  public String test() {
    return null;
  }


  public static class ExampleEnterAdvice {
    @OnMethodEnter
    public static void enter(@This Object that, @AllArguments Object[] args, @Origin Method method) {
      int count = method.getParameterCount();
      if (count != 0) {
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory()
          .getValidator()
          .forExecutables();
        validator.validateParameters(that, method, args);
      }
    }


  }

  public static class ExampleExitAdvice {
    @OnMethodExit
    public static void exit(@This Object that, @AllArguments Object[] args, @Origin Method method, @Return Object returnValue) {
      ExecutableValidator validator = Validation.buildDefaultValidatorFactory()
        .getValidator()
        .forExecutables();
      validator.validateReturnValue(that, method, returnValue);
    }
  }
}
