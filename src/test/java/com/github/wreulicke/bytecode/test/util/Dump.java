package com.github.wreulicke.bytecode.test.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Dump {
  public static void dump(byte[] bytes) {
    StackTraceElement trace = Thread.currentThread()
      .getStackTrace()[2];
    String path = trace.getClassName()
      .replace('.', '/');
    try {
      Files.createDirectories(Paths.get("dump", path));
      Files.write(Paths.get("dump", path, trace.getMethodName() + ".class"), bytes);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
