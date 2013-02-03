/*
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.apidesign.bck2brwsr.emul.reflect;

import java.lang.reflect.Method;
import java.util.Enumeration;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MethodImplTest {
    
    public MethodImplTest() {
    }
    
    public static String[] arr(String... arr) {
        return arr;
    }

    @Test
    public void testSignatureForMethodWithAnArray() throws NoSuchMethodException {
        Method m = MethodImplTest.class.getMethod("arr", String[].class);
        String sig = MethodImpl.toSignature(m);
        int sep = sig.indexOf("__");
        assert sep > 0 : "Separator found " + sig;
        
        Enumeration<Class> en = MethodImpl.signatureParser(sig.substring(sep + 2));
        
        assert en.nextElement() == m.getReturnType() : "Return type is the same";
        assert en.nextElement() == m.getParameterTypes()[0] : "1st param type is the same";
    }
}