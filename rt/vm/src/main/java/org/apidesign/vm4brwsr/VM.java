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

import java.io.IOException;
import java.io.InputStream;
import org.apidesign.vm4brwsr.ByteCodeParser.ClassData;
import org.apidesign.vm4brwsr.ByteCodeParser.FieldData;
import org.apidesign.vm4brwsr.ByteCodeParser.MethodData;

/** Generator of JavaScript from bytecode of classes on classpath of the VM.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
abstract class VM extends ByteCodeToJavaScript {
    protected final ClassDataCache classDataCache;

    private final Bck2Brwsr.Resources resources;
    private final ExportedSymbols exportedSymbols;
    private final StringArray invokerMethods;

    private static final Class<?> FIXED_DEPENDENCIES[] = {
            Class.class,
            ArithmeticException.class,
            VM.class
        };

    private VM(Appendable out, Bck2Brwsr.Resources resources) {
        super(out);
        this.resources = resources;
        this.classDataCache = new ClassDataCache(resources);
        this.exportedSymbols = new ExportedSymbols(resources);
        this.invokerMethods = new StringArray();
    }

    static {
        // uses VMLazy to load dynamic classes
        boolean assertsOn = false;
        assert assertsOn = true;
        if (assertsOn) {
            VMLazy.init();
            Zips.init();
        }
    }

    @Override
    boolean debug(String msg) throws IOException {
        return false;
    }
    
    static void compile(Appendable out, Bck2Brwsr.Resources l, StringArray names, boolean extension) throws IOException {
        VM vm = extension ? new Extension(out, l, names.toArray())
                          : new Standalone(out, l);

        final StringArray fixedNames = new StringArray();

        for (final Class<?> fixedClass: FIXED_DEPENDENCIES) {
            fixedNames.add(fixedClass.getName().replace('.', '/'));
        }

        vm.doCompile(fixedNames.addAndNew(names.toArray()));
    }

    private void doCompile(StringArray names) throws IOException {
        generatePrologue();
        out.append(
                "\n  var invoker = function Invoker() {"
                    + "\n    return Invoker.target[Invoker.method]"
                                      + ".apply(Invoker.target, arguments);"
                    + "\n  };");
        generateBody(names);
        for (String invokerMethod: invokerMethods.toArray()) {
            out.append("\n  invoker." + invokerMethod + " = function(target) {"
                           + "\n    invoker.target = target;"
                           + "\n    invoker.method = '" + invokerMethod + "';"
                           + "\n    return invoker;"
                           + "\n  };");
        }
        out.append("\n");
        generateEpilogue();
    }

    protected abstract void generatePrologue() throws IOException;

    protected abstract void generateEpilogue() throws IOException;

    protected abstract String getExportsObject();

    protected abstract boolean isExternalClass(String className);

    @Override
    protected final void declaredClass(ClassData classData, String mangledName)
            throws IOException {
        if (exportedSymbols.isExported(classData)) {
            out.append("\n").append(getExportsObject()).append("['")
                                               .append(mangledName)
                                               .append("'] = ")
                            .append(accessClass(mangledName))
               .append(";\n");
        }
    }

    protected String generateClass(String className) throws IOException {
        ClassData classData = classDataCache.getClassData(className);
        if (classData == null) {
            throw new IOException("Can't find class " + className);
        }
        return compile(classData);
    }

    @Override
    protected void declaredField(FieldData fieldData,
                                 String destObject,
                                 String mangledName) throws IOException {
        if (exportedSymbols.isExported(fieldData)) {
            exportMember(destObject, mangledName);
        }
    }

    @Override
    protected void declaredMethod(MethodData methodData,
                                  String destObject,
                                  String mangledName) throws IOException {
        if (isHierarchyExported(methodData)) {
            exportMember(destObject, mangledName);
        }
    }

    private void exportMember(String destObject, String memberName)
            throws IOException {
        out.append("\n").append(destObject).append("['")
                                           .append(memberName)
                                           .append("'] = ")
                        .append(destObject).append(".").append(memberName)
           .append(";\n");
    }

    private void generateBody(StringArray names) throws IOException {
        StringArray processed = new StringArray();
        StringArray initCode = new StringArray();
        for (String baseClass : names.toArray()) {
            references.add(baseClass);
            for (;;) {
                String name = null;
                for (String n : references.toArray()) {
                    if (processed.contains(n)) {
                        continue;
                    }
                    name = n;
                }
                if (name == null) {
                    break;
                }

                try {
                    String ic = generateClass(name);
                    processed.add(name);
                    initCode.add(ic == null ? "" : ic);
                } catch (RuntimeException ex) {
                    if (out instanceof CharSequence) {
                        CharSequence seq = (CharSequence)out;
                        int lastBlock = seq.length();
                        while (lastBlock-- > 0) {
                            if (seq.charAt(lastBlock) == '{') {
                                break;
                            }
                        }
                        throw new IOException("Error while compiling " + name + "\n"
                            + seq.subSequence(lastBlock + 1, seq.length()), ex
                        );
                    } else {
                        throw new IOException("Error while compiling " + name + "\n"
                            + out, ex
                        );
                    }
                }
            }

            for (String resource : scripts.toArray()) {
                while (resource.startsWith("/")) {
                    resource = resource.substring(1);
                }
                InputStream emul = resources.get(resource);
                if (emul == null) {
                    throw new IOException("Can't find " + resource);
                }
                readResource(emul, out);
            }
            scripts = new StringArray();

            StringArray toInit = StringArray.asList(references.toArray());
            toInit.reverse();

            for (String ic : toInit.toArray()) {
                int indx = processed.indexOf(ic);
                if (indx >= 0) {
                    final String theCode = initCode.toArray()[indx];
                    if (!theCode.isEmpty()) {
                        out.append(theCode).append("\n");
                    }
                    initCode.toArray()[indx] = "";
                }
            }
        }
    }

    private static void readResource(InputStream emul, Appendable out) throws IOException {
        try {
            int state = 0;
            for (;;) {
                int ch = emul.read();
                if (ch == -1) {
                    break;
                }
                if (ch < 0 || ch > 255) {
                    throw new IOException("Invalid char in emulation " + ch);
                }
                switch (state) {
                    case 0: 
                        if (ch == '/') {
                            state = 1;
                        } else {
                            out.append((char)ch);
                        }
                        break;
                    case 1:
                        if (ch == '*') {
                            state = 2;
                        } else {
                            out.append('/').append((char)ch);
                            state = 0;
                        }
                        break;
                    case 2:
                        if (ch == '*') {
                            state = 3;
                        }
                        break;
                    case 3:
                        if (ch == '/') {
                            state = 0;
                        } else {
                            state = 2;
                        }
                        break;
                }
            }
        } finally {
            emul.close();
        }
    }

    static String toString(String name) throws IOException {
        StringBuilder sb = new StringBuilder();
//        compile(sb, name);
        return sb.toString().toString();
    }

    private StringArray scripts = new StringArray();
    private StringArray references = new StringArray();
    
    @Override
    protected boolean requireReference(String cn) {
        if (references.contains(cn)) {
            return false;
        }
        references.add(cn);
        return true;
    }

    @Override
    protected void requireScript(String resourcePath) {
        scripts.add(resourcePath);
    }

    @Override
    String assignClass(String className) {
        return "vm." + className + " = ";
    }
    
    @Override
    String accessClass(String className) {
        return "vm." + className;
    }

    @Override
    protected String accessField(String object, String mangledName,
                                 String[] fieldInfoName) throws IOException {
        final FieldData field =
                classDataCache.findField(fieldInfoName[0],
                                         fieldInfoName[1],
                                         fieldInfoName[2]);
        return accessNonVirtualMember(object, mangledName,
                                      (field != null) ? field.cls : null);
    }

    @Override
    protected String accessStaticMethod(
                             String object,
                             String mangledName,
                             String[] fieldInfoName) throws IOException {
        final MethodData method =
                classDataCache.findMethod(fieldInfoName[0],
                                          fieldInfoName[1],
                                          fieldInfoName[2]);
        return accessNonVirtualMember(object, mangledName,
                                      (method != null) ? method.cls : null);
    }

    @Override
    protected String accessVirtualMethod(
                             String object,
                             String mangledName,
                             String[] fieldInfoName) throws IOException {
        final ClassData referencedClass =
                classDataCache.getClassData(fieldInfoName[0]);
        final MethodData method =
                classDataCache.findMethod(referencedClass,
                                          fieldInfoName[1],
                                          fieldInfoName[2]);

        if ((method != null)
                && !isExternalClass(method.cls.getClassName())
                && (((method.access & ByteCodeParser.ACC_FINAL) != 0)
                        || ((referencedClass.getAccessFlags()
                                 & ByteCodeParser.ACC_FINAL) != 0)
                        || !isHierarchyExported(method))) {
            return object + "." + mangledName;
        }

        return accessThroughInvoker(object, mangledName);
    }

    private String accessThroughInvoker(String object, String mangledName) {
        if (!invokerMethods.contains(mangledName)) {
            invokerMethods.add(mangledName);
        }
        return "invoker." + mangledName + '(' + object + ')';
    }

    private boolean isHierarchyExported(final MethodData methodData)
            throws IOException {
        if (exportedSymbols.isExported(methodData)) {
            return true;
        }
        if ((methodData.access & (ByteCodeParser.ACC_PRIVATE
                                      | ByteCodeParser.ACC_STATIC)) != 0) {
            return false;
        }

        final ExportedMethodFinder exportedMethodFinder =
                new ExportedMethodFinder(exportedSymbols);

        classDataCache.findMethods(
                methodData.cls,
                methodData.getName(),
                methodData.getInternalSig(),
                exportedMethodFinder);

        return (exportedMethodFinder.getFound() != null);
    }

    private String accessNonVirtualMember(String object,
                                          String mangledName,
                                          ClassData declaringClass) {
        return ((declaringClass != null)
                    && !isExternalClass(declaringClass.getClassName()))
                            ? object + "." + mangledName
                            : object + "['" + mangledName + "']";
    }

    private static final class ExportedMethodFinder
            implements ClassDataCache.TraversalCallback<MethodData> {
        private final ExportedSymbols exportedSymbols;
        private MethodData found;

        public ExportedMethodFinder(final ExportedSymbols exportedSymbols) {
            this.exportedSymbols = exportedSymbols;
        }

        @Override
        public boolean traverse(final MethodData methodData) {
            try {
                if (exportedSymbols.isExported(methodData)) {
                    found = methodData;
                    return false;
                }
            } catch (final IOException e) {
            }

            return true;
        }

        public MethodData getFound() {
            return found;
        }
    }

    private static final class Standalone extends VM {
        private Standalone(Appendable out, Bck2Brwsr.Resources resources) {
            super(out, resources);
        }

        @Override
        protected void generatePrologue() throws IOException {
            out.append("(function VM(global) {var fillInVMSkeleton = function(vm) {");
        }

        @Override
        protected void generateEpilogue() throws IOException {
            out.append(
                  "  return vm;\n"
                + "  };\n"
                + "  var extensions = [];\n"
                + "  global.bck2brwsr = function() {\n"
                + "    var args = Array.prototype.slice.apply(arguments);\n"
                + "    var vm = fillInVMSkeleton({});\n"
                + "    for (var i = 0; i < extensions.length; ++i) {\n"
                + "      extensions[i](vm);\n"
                + "    }\n"
                + "    var knownExtensions = extensions.length;\n"
                + "    var loader = {};\n"
                + "    loader.vm = vm;\n"
                + "    loader.loadClass = function(name) {\n"
                + "      var attr = name.replace__Ljava_lang_String_2CC('.','_');\n"
                + "      var fn = vm[attr];\n"
                + "      if (fn) return fn(false);\n"
                + "      try {\n"
                + "        return vm.org_apidesign_vm4brwsr_VMLazy(false).\n"
                + "          load__Ljava_lang_Object_2Ljava_lang_Object_2Ljava_lang_String_2_3Ljava_lang_Object_2(loader, name, args);\n"
                + "      } catch (err) {\n"
                + "        while (knownExtensions < extensions.length) {\n"
                + "          extensions[knownExtensions++](vm);\n"
                + "        }\n"
                + "        fn = vm[attr];\n"
                + "        if (fn) return fn(false);\n"
                + "        throw err;\n"
                + "      }\n"
                + "    }\n"
                + "    if (vm.loadClass) {\n"
                + "      throw 'Cannot initialize the bck2brwsr VM twice!';\n"
                + "    }\n"
                + "    vm.loadClass = loader.loadClass;\n"
                + "    vm.loadBytes = function(name) {\n"
                + "      return vm.org_apidesign_vm4brwsr_VMLazy(false).\n"
                + "        loadBytes___3BLjava_lang_Object_2Ljava_lang_String_2_3Ljava_lang_Object_2(loader, name, args);\n"
                + "    }\n"
                + "    vm.java_lang_reflect_Array(false);\n"
                + "    vm.org_apidesign_vm4brwsr_VMLazy(false).\n"
                + "      loadBytes___3BLjava_lang_Object_2Ljava_lang_String_2_3Ljava_lang_Object_2(loader, null, args);\n"
                + "    return loader;\n"
                + "  };\n");
            out.append(
                  "  global.bck2brwsr.registerExtension = function(extension) {\n"
                + "    extensions.push(extension);\n"
                + "    return null;\n"
                + "  };\n");
            out.append("}(this));");
        }

        @Override
        protected String getExportsObject() {
            return "vm";
        }

        @Override
        protected boolean isExternalClass(String className) {
            return false;
        }
    }

    private static final class Extension extends VM {
        private final StringArray extensionClasses;

        private Extension(Appendable out, Bck2Brwsr.Resources resources,
                          String[] extClassesArray) {
            super(out, resources);
            this.extensionClasses = StringArray.asList(extClassesArray);
        }

        @Override
        protected void generatePrologue() throws IOException {
            out.append("bck2brwsr.registerExtension(function(exports) {\n"
                           + "  var vm = {};\n");
            out.append("  function link(n, inst) {\n"
                           + "    var cls = n['replace__Ljava_lang_String_2CC']"
                                                  + "('/', '_').toString();\n"
                           + "    var dot = n['replace__Ljava_lang_String_2CC']"
                                                  + "('/', '.').toString();\n"
                           + "    exports.loadClass(dot);\n"
                           + "    vm[cls] = exports[cls];\n"
                           + "    return vm[cls](inst);\n"
                           + "  };\n");
        }

        @Override
        protected void generateEpilogue() throws IOException {
            out.append("});");
        }

        @Override
        protected String generateClass(String className) throws IOException {
            if (isExternalClass(className)) {
                out.append("\n").append(assignClass(
                                            className.replace('/', '_')))
                   .append("function() {\n  return link('")
                   .append(className)
                   .append("', arguments.length == 0 || arguments[0] === true);"
                               + "\n};");

                return null;
            }

            return super.generateClass(className);
        }

        @Override
        protected String getExportsObject() {
            return "exports";
        }

        @Override
        protected boolean isExternalClass(String className) {
            return !extensionClasses.contains(className);
        }
    }
}
