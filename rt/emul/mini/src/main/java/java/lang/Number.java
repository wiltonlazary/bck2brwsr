/*
 * Copyright (c) 1994, 2001, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.core.JavaScriptPrototype;

/**
 * The abstract class <code>Number</code> is the superclass of classes
 * <code>BigDecimal</code>, <code>BigInteger</code>,
 * <code>Byte</code>, <code>Double</code>, <code>Float</code>,
 * <code>Integer</code>, <code>Long</code>, and <code>Short</code>.
 * <p>
 * Subclasses of <code>Number</code> must provide methods to convert
 * the represented numeric value to <code>byte</code>, <code>double</code>,
 * <code>float</code>, <code>int</code>, <code>long</code>, and
 * <code>short</code>.
 *
 * @author      Lee Boynton
 * @author      Arthur van Hoff
 * @see     java.lang.Byte
 * @see     java.lang.Double
 * @see     java.lang.Float
 * @see     java.lang.Integer
 * @see     java.lang.Long
 * @see     java.lang.Short
 * @since   JDK1.0
 */
@ExtraJavaScript(
    resource="/org/apidesign/vm4brwsr/emul/lang/java_lang_Number.js",
    processByteCode=true
)
@JavaScriptPrototype(container = "Number.prototype", prototype = "new Number")
public abstract class Number implements java.io.Serializable {
    /**
     * Returns the value of the specified number as an <code>int</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>int</code>.
     */
    @JavaScriptBody(args = {}, body = "return this | 0;")
    public abstract int intValue();

    /**
     * Returns the value of the specified number as a <code>long</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>long</code>.
     */
    @JavaScriptBody(args = {}, body = "return this.toLong();")
    public abstract long longValue();

    /**
     * Returns the value of the specified number as a <code>float</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>float</code>.
     */
    @JavaScriptBody(args = {}, body = "return this;")
    public abstract float floatValue();

    /**
     * Returns the value of the specified number as a <code>double</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>double</code>.
     */
    @JavaScriptBody(args = {}, body = "return this;")
    public abstract double doubleValue();

    /**
     * Returns the value of the specified number as a <code>byte</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>byte</code>.
     * @since   JDK1.1
     */
    public byte byteValue() {
        return (byte)intValue();
    }

    /**
     * Returns the value of the specified number as a <code>short</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>short</code>.
     * @since   JDK1.1
     */
    public short shortValue() {
        return (short)intValue();
    }

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = -8742448824652078965L;
    
    static {
        // as last step of initialization, initialize valueOf method
        initValueOf();
    }
    @JavaScriptBody(args = {  }, body = 
        "var p = vm.java_lang_Number(false);\n" +
        "p.valueOf = function() { return this.doubleValue__D(); };\n" +
        "p.toString = function() { return this.toString__Ljava_lang_String_2(); };"
    )
    private native static void initValueOf();
}
