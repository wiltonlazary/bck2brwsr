/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */
package org.apidesign.vm4brwsr;

import javax.script.Invocable;
import javax.script.ScriptException;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NumberTest {
    @Test public void integerFromString() throws Exception {
        assertExec("Can convert string to integer", "java_lang_Integer_parseIntILjava_lang_String",
            Double.valueOf(333), "333"
        );
    }

    @Test public void doubleFromString() throws Exception {
        assertExec("Can convert string to double", "java_lang_Double_parseDoubleDLjava_lang_String",
            Double.valueOf(33.3), "33.3"
        );
    }

    @Test public void autoboxDouble() throws Exception {
        assertExec("Autoboxing of doubles is OK", "org_apidesign_vm4brwsr_Numbers_autoboxDblToStringLjava_lang_String",
            "3.3"
        );
    }
    
    @Test public void javalog1000() throws Exception {
        assertEquals(3.0, Math.log10(1000.0), 0.00003, "log_10(1000) == 3");
    }

    @Test public void jslog1000() throws Exception {
        assertExec("log_10(1000) == 3", "java_lang_Math_log10DD", 
            Double.valueOf(3.0), 1000.0
        );
    }
    
    @Test public void javaRem() {
        assertEquals(3, Numbers.rem(303, 10));
    }
    @Test public void jsRem() throws Exception {
        assertExec("Should be three", "org_apidesign_vm4brwsr_Numbers_remIII", 
            Double.valueOf(3.0), 303, 10
        );
    }

    
    private static CharSequence codeSeq;
    private static Invocable code;

    @BeforeClass
    public void compileTheCode() throws Exception {
        if (codeSeq == null) {
            StringBuilder sb = new StringBuilder();
            code = StaticMethodTest.compileClass(sb, "org/apidesign/vm4brwsr/Numbers");
            codeSeq = sb;
        }
    }

    private static void assertExec(
        String msg, String methodName, Object expRes, Object... args) throws Exception {

        Object ret = null;
        try {
            ret = code.invokeFunction(methodName, args);
        } catch (ScriptException ex) {
            fail("Execution failed in\n" + codeSeq, ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in\n" + codeSeq, ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes.equals(ret)) {
            return;
        }
        if (expRes instanceof Double && ret instanceof Double) {
            double expD = ((Double)expRes).doubleValue();
            double retD = ((Double)ret).doubleValue();
            assertEquals(retD, expD, 0.000004, msg + " was " + ret + "\n" + codeSeq);
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + codeSeq);
    }
    
}
