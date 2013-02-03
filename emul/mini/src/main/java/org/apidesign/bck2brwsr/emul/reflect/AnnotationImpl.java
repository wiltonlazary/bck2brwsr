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
package org.apidesign.bck2brwsr.emul.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class AnnotationImpl implements Annotation {
    public Class<? extends Annotation> annotationType() {
        return getClass();
    }

    @JavaScriptBody(args = { "a", "n", "arr", "values" }, body = ""
        + "function f(v, p) {\n"
        + "  var val = v;\n"
        + "  var prop = p;\n"
        + "  return function() {\n"
        + "    return val[prop];\n"
        + "  };\n"
        + "}\n"
        + "for (var i = 0; i < arr.length; i += 2) {\n"
        + "  var m = arr[i];\n"
        + "  var p = arr[i + 1];\n"
        + "  a[m] = new f(values, p);\n"
        + "}\n"
        + "a['$instOf_' + n] = true;\n"
        + "return a;"
    )
    private static native <T extends Annotation> T create(
        AnnotationImpl a, String n, String[] methodsAndProps, Object values
    );
    
    public static <T extends Annotation> T create(Class<T> annoClass, Object values) {
        return create(new AnnotationImpl(), 
            annoClass.getName().replace('.', '_'), 
            findProps(annoClass), values
        );
    }

    public static Annotation[] create(Object anno) {
        String[] names = findNames(anno);
        Annotation[] ret = new Annotation[names.length];
        for (int i = 0; i < names.length; i++) {
            String annoNameSlash = names[i].substring(1, names[i].length() - 1);
            Class<?> annoClass;
            try {
                annoClass = Class.forName(annoNameSlash.replace('/', '.'));
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException("Can't find annotation class " + annoNameSlash);
            }
            ret[i] = create(
                new AnnotationImpl(), 
                annoNameSlash.replace('/', '_'),
                findProps(annoClass),
                findData(anno, names[i])
            );
        }
        return ret;
    }
    @JavaScriptBody(args = "anno", body =
          "var arr = new Array();"
        + "var props = Object.getOwnPropertyNames(anno);\n"
        + "for (var i = 0; i < props.length; i++) {\n"
        + "  var p = props[i];\n"
        + "  arr.push(p);"
        + "}"
        + "return arr;"
    )
    private static native String[] findNames(Object anno);

    @JavaScriptBody(args={ "anno", "p"}, body="return anno[p];")
    private static native Object findData(Object anno, String p);

    private static String[] findProps(Class<?> annoClass) {
        final Method[] marr = MethodImpl.findMethods(annoClass, Modifier.PUBLIC);
        String[] arr = new String[marr.length * 2];
        int pos = 0;
        for (Method m : marr) {
            arr[pos++] = MethodImpl.toSignature(m);
            arr[pos++] = m.getName();
        }
        return arr;
    }
}
