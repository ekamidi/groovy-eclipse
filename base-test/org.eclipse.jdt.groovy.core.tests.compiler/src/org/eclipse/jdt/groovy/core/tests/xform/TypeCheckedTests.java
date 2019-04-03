/*
 * Copyright 2009-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.groovy.core.tests.xform;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test cases for {@link groovy.transform.TypeChecked}.
 */
public final class TypeCheckedTests extends GroovyCompilerTestSuite {

    @Test
    public void testTypeChecked1() {
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.TypeChecked\n"+
            "@TypeChecked\n"+
            "void method(String message) {\n"+
            "   if (rareCondition) {\n"+
            "        println \"Did you spot the error in this ${message.toUppercase()}?\"\n"+
            "   }\n"+
            "}\n",
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 4)\n" +
            "\tif (rareCondition) {\n" +
            "\t    ^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - The variable [rareCondition] is undeclared.\n" +
            "----------\n" +
            "2. ERROR in Foo.groovy (at line 5)\n" +
            "\tprintln \"Did you spot the error in this ${message.toUppercase()}?\"\n" +
            "\t                                          ^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:[Static type checking] - Cannot find matching method java.lang.String#toUppercase(). Please check if the declared type is correct and if the method exists.\n" +
            "----------\n");
    }

    @Test
    public void testTypeChecked2() {
        String[] sources = {
            "Foo.groovy",
            "import groovy.transform.TypeChecked\n"+
            "@TypeChecked\n"+
            "void method(String message) {\n"+
            "   List<Integer> ls = new ArrayList<Integer>();\n"+
            "   ls.add(123);\n"+
            "   ls.add('abc');\n"+
            "}\n",
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Foo.groovy (at line 6)\n" +
            "\tls.add(\'abc\');\n" +
            "\t^^^^^^^^^^^^^\n" +
            (!isAtLeastGroovy(25)
                ? "Groovy:[Static type checking] - Cannot call java.util.ArrayList <Integer>#add(java.lang.Integer) with arguments [java.lang.String] \n"
                : "Groovy:[Static type checking] - Cannot find matching method java.util.List#add(java.lang.String). Please check if the declared type is correct and if the method exists.\n"
            ) +
            "----------\n");
    }

    @Test
    public void testTypeChecked3() {
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Foo {" +
            "  def method() {\n" +
            "    Set<java.beans.BeanInfo> defs = []\n" +
            "    defs*.additionalBeanInfo\n" +
            "  }\n" +
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test
    public void testTypeChecked4() {
        String[] sources = {
            "Foo.groovy",
            "@groovy.transform.TypeChecked\n" +
            "class Foo {" +
            "  static def method() {\n" + // static method alters type checking
            "    Set<java.beans.BeanInfo> defs = []\n" +
            "    defs*.additionalBeanInfo\n" +
            "  }\n" +
            "}\n",
        };

        runNegativeTest(sources, "");
    }

    @Test @Ignore("VM argument not accepted on CI server")
    public void testTypeChecked1506() {
        String[] sources = {
            "LoggerTest.groovy",
            "import groovy.transform.*\n"+
            "import groovy.util.logging.*\n"+
            "@TypeChecked @Log\n"+
            "class LoggerTest {\n"+
            "  static void main(String... args) {\n"+
            "    LoggerTest.log.info('one')\n"+
            "    log.info('two')\n"+
            "  }\n"+
            "}\n",
        };
        vmArguments = new String[] {"-Djava.util.logging.SimpleFormatter.format=%4$s %5$s%6$s%n"};

        runConformTest(sources, "", "INFO one\nINFO two");
    }
}
