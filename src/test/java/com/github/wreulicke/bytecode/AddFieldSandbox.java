package com.github.wreulicke.bytecode;

import java.io.IOException;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import com.github.wreulicke.bytecode.test.util.Dump;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.core.GeneratorStrategy;
import net.sf.cglib.core.TypeUtils;
import net.sf.cglib.transform.ClassEmitterTransformer;
import net.sf.cglib.transform.ClassReaderGenerator;
import net.sf.cglib.transform.TransformingClassGenerator;

public class AddFieldSandbox {

  @Test
  public void testAddFieldWithJavassist() throws CannotCompileException, IOException, NotFoundException {
    ClassPool pool = ClassPool.getDefault();
    CtClass clazz = pool.get(Some.class.getName());
    CtClass string = pool.get("java.lang.String");
    CtField field = new CtField(string, "foo", clazz);
    field.setModifiers(Modifier.PRIVATE);
    clazz.addField(field);
    byte[] bytes = clazz.toBytecode();
    Dump.dump(bytes);

  }

  @Test
  public void testAddFieldWithCglib() throws IOException, Exception {
    GeneratorStrategy generator = new DefaultGeneratorStrategy() {
      @Override
      protected ClassGenerator transform(ClassGenerator cg) throws Exception {
        return new TransformingClassGenerator(cg, new ClassEmitterTransformer() {
          @Override
          public void end_class() {
            if (!TypeUtils.isAbstract(getAccess())) {
              this.declare_field(Constants.ACC_PRIVATE, "foo", Type.getType(String.class), null);
            }
            super.end_class();
          };
        });
      }
    };
    byte[] bytes = generator.generate(new ClassReaderGenerator(new ClassReader(Some.class.getName()), ClassReader.EXPAND_FRAMES));
    Dump.dump(bytes);
  }

  @Test
  public void testAddFieldWithByteBuddy() throws IOException {
    byte[] bytes = new ByteBuddy().rebase(Some.class)
      .defineField("foo", String.class, Visibility.PRIVATE)
      .make()
      .getBytes();
    Dump.dump(bytes);
  }

  public static class Some {
  }
}
