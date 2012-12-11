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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class VMLazy {
    private final Object loader;
    private final Object[] args;
    
    private VMLazy(Object loader, Object[] args) {
        this.loader = loader;
        this.args = args;
    }
    
    static void init() {
    }
    
    @JavaScriptBody(args={"l", "res", "args" }, body = ""
        + "\ntry {"
        + "\n  return args[0](res.toString());"
        + "\n} catch (x) {"
        + "\n  throw Object.getOwnPropertyNames(l.vm).toString() + x.toString();"
        + "\n}")
    private static native byte[] read(Object l, String res, Object[] args);
    
    static Object load(Object loader, String name, Object[] arguments) 
    throws IOException, ClassNotFoundException {
        return new VMLazy(loader, arguments).load(name);
    }
    
    private Object load(String name)
    throws IOException, ClassNotFoundException {
        String res = name.replace('.', '/') + ".class";
        byte[] arr = read(loader, res, args);
        if (arr == null) {
            throw new ClassNotFoundException(name);
        }
//        beingDefined(loader, name);
        StringBuilder out = new StringBuilder();
        out.append("var loader = arguments[0];\n");
        out.append("var vm = loader.vm;\n");
        new Gen(this, out).compile(new ByteArrayInputStream(arr));
        String code = out.toString().toString();
        String under = name.replace('.', '_');
        return applyCode(loader, under, code);
    }

/* possibly not needed:
    @JavaScriptBody(args = {"loader", "n" }, body =
        "var cls = n.replace__Ljava_lang_String_2CC(n, '.','_').toString();" +
        "loader.vm[cls] = true;\n"
    )
    private static native void beingDefined(Object loader, String name);
*/
    

    @JavaScriptBody(args = {"loader", "name", "script" }, body =
        "try {\n" +
        "  new Function(script)(loader, name);\n" +
        "} catch (ex) {\n" +
        "  throw 'Cannot compile ' + ex + ' script:\\\\n' + script;\n" +
        "}\n" +
        "return vm[name](false);\n"
    )
    private static native Object applyCode(Object loader, String name, String script);
    
    
    private static final class Gen extends ByteCodeToJavaScript {
        private final VMLazy lazy;

        public Gen(VMLazy vm, Appendable out) {
            super(out);
            this.lazy = vm;
        }
        
        @JavaScriptBody(args = {"self", "n"},
        body =
        "var cls = n.replace__Ljava_lang_String_2CC(n, '/','_').toString();"
        + "\nvar dot = n.replace__Ljava_lang_String_2CC(n,'/','.').toString();"
        + "\nvar lazy = self.fld_lazy;"
        + "\nvar loader = lazy.fld_loader;"
        + "\nvar vm = loader.vm;"
        + "\nif (vm[cls]) return false;"
        + "\nvm[cls] = function() {"
        + "\n  return lazy.load__Ljava_lang_Object_2Ljava_lang_String_2(lazy, dot);"
        + "\n};"
        + "\nreturn true;")
        @Override
        protected boolean requireReference(String internalClassName) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void requireScript(String resourcePath) {
        }

        @Override
        String assignClass(String className) {
            return "vm[arguments[1]]=";
        }

        @Override
        String accessClass(String classOperation) {
            return "vm." + classOperation;
        }
    }
}
