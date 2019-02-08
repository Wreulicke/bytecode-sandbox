package com.github.wreulicke.bytecode.bcel;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Test;

public class BCELSandbox {
	
	@Test
	public void test1() throws ClassNotFoundException {
		JavaClass javaClass = Repository.lookupClass(BCELSandbox.class);
	}
}
