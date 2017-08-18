package com.github.wreulicke.bytecode.bytebuddy;

import static net.bytebuddy.asm.Advice.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;

import org.jboss.logging.Logger;
import org.junit.Test;

import com.github.wreulicke.bytecode.test.util.Dump;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice.AllArguments;
import net.bytebuddy.asm.Advice.Argument;
import net.bytebuddy.asm.Advice.Enter;
import net.bytebuddy.asm.Advice.FieldValue;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.asm.Advice.Return;
import net.bytebuddy.asm.Advice.StubValue;
import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.asm.Advice.Thrown;
import net.bytebuddy.asm.Advice.Unused;

public class ByteBuddyTest {

  @Test
  public void testForField() throws IOException, InstantiationException, IllegalAccessException {
    byte[] bytes = new ByteBuddy().rebase(Example.class)
      .visit(to(ForFieldAdvice.class).on(isMethod()))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }

  @Test
  public void testForReturn() throws IOException, InstantiationException, IllegalAccessException {
    byte[] bytes = new ByteBuddy().rebase(Example.class)
      .visit(to(ForReturnAdvice.class).on(isMethod().and(named("forReturn"))))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }

  @Test
  public void testForOrigin() throws IOException, InstantiationException, IllegalAccessException {
    byte[] bytes = new ByteBuddy().rebase(Example.class)
      .visit(to(ForOriginAdvice.class).on(isMethod()))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }



  @Test
  public void testForAllArguments() throws IOException, InstantiationException, IllegalAccessException {
    byte[] bytes = new ByteBuddy().rebase(Example.class)
      .visit(to(ForAllArgumentsAdvice.class).on(isMethod().and(named("forArgs"))))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }



  @Test
  public void testForArgument() throws IOException, InstantiationException, IllegalAccessException {
    byte[] bytes = new ByteBuddy().rebase(Example.class)
      .visit(to(ForArgumentAdvice.class).on(isMethod().and(named("forArgs"))))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }



  @Test
  public void testForThis() throws IOException, InstantiationException, IllegalAccessException {
    byte[] bytes = new ByteBuddy().rebase(Example.class)
      .visit(to(ForThisAdvice.class).on(isMethod()))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }



  @Test
  public void testForThrown() throws IOException, InstantiationException, IllegalAccessException {
    byte[] bytes = new ByteBuddy().rebase(Example.class)
      .visit(to(ForThrownAdvice.class).on(isMethod().and(named("forThrown"))))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }



  @Test
  public void testForEnter() throws IOException, InstantiationException, IllegalAccessException {
    byte[] bytes = new ByteBuddy().rebase(Example.class)
      .visit(to(ForEnterAdvice.class).on(isMethod()))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }



  @Test
  public void testForUnused() throws IOException, InstantiationException, IllegalAccessException {
    byte[] bytes = new ByteBuddy().rebase(Example.class)
      .visit(to(ForUnusedAdvice.class).on(isMethod()))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }



  @Test
  public void testForStub() throws IOException, InstantiationException, IllegalAccessException {
    byte[] bytes = new ByteBuddy().rebase(Example.class)
      .visit(to(ForStubAdvice.class).on(isMethod()))
      .make()
      .getBytes();
    Dump.dump(bytes);
  }



  public static class ForFieldAdvice {
    @OnMethodEnter
    public static void enter(@FieldValue("string") String field, @FieldValue("LOG") Logger logger) {
      logger.tracef("ForReturnAdvice:field 'string' is {0}", field);
    }
  }

  public static class ForReturnAdvice {
    @OnMethodExit
    public static void exit(@Return Object returns, @FieldValue("LOG") Logger logger) {
      logger.tracef("ForReturnAdvice:returninig value is {0}", returns);
    }
  }

  public static class ForOriginAdvice {
    @OnMethodEnter
    public static void enter(@Origin Method original, @FieldValue("LOG") Logger logger) {
      logger.tracef("ForOriginAdvice:original method is {0}", original);
    }
  }

  public static class ForAllArgumentsAdvice {
    @OnMethodEnter
    public static void enter(@AllArguments Object[] args, @FieldValue("LOG") Logger logger) {
      logger.tracef("ForAllArgumentsAdvice:arguments is {0}", args);
    }
  }

  public static class ForArgumentAdvice {
    @OnMethodEnter
    public static void enter(@Argument(0) Object arg, @FieldValue("LOG") Logger logger) {
      logger.tracef("ForArgumentAdvice:1st argument is {0}", arg);
    }
  }

  public static class ForThisAdvice {
    @OnMethodEnter
    public static void enter(@This Object arg, @FieldValue("LOG") Logger logger) {
      logger.tracef("ForThisAdvice:this is {0}", arg);
    }
  }

  public static class ForThrownAdvice {
    @OnMethodExit(onThrowable = IOException.class)
    public static void exit(@Thrown(readOnly = false) Throwable e, @FieldValue("LOG") Logger logger) {
      if (e != null)
        logger.tracef("ForThrownAdvice:1st this is {0}", e);
      if (e instanceof UncheckedIOException) {
        e = null;
      }
    }
  }

  public static class ForEnterAdvice {
    @OnMethodEnter
    public static String enter(@FieldValue("string") String string, @FieldValue("LOG") Logger logger, @FieldValue("any") String any) {
      if (string.equals("test")) {
        return string;
      }
      else if (string.equals(any)) {
        return any;
      }
      return "return value";
    }

    @OnMethodExit
    public static void exit(@Enter String enter, @FieldValue("LOG") Logger logger) {
      logger.tracef("OnMethodEnter returnd '{0}'", enter);
    }
  }
  public static class ForUnusedAdvice {
    @OnMethodEnter
    public static void enter(@Unused long ignore, @FieldValue("LOG") Logger logger) {
      logger.tracef("ForUnusedAdvice:1st this is {0}", ignore);
    }
  }
  public static class ForStubAdvice {
    @OnMethodEnter
    public static void enter(@StubValue Object stub, @FieldValue("LOG") Logger logger) {
      logger.tracef("ForStubAdvice:1st this is {0}", stub);
    }
  }



  public static class Example {
    private static Logger LOG = Logger.getLogger(Example.class);
    private String string = "test";
    private String any = "any";

    public Example() {
      some();
    }

    public void some() {
      int ignore = 1;
      System.out.println(ignore);
    }

    public void forArgs(String string, int test) {}

    public void forThrown() {
      throw new RuntimeException("test io exception");
    }

    public String forReturn() {
      return null;
    }

    public int forStubValue() {
      return 1;
    }
  }
}
