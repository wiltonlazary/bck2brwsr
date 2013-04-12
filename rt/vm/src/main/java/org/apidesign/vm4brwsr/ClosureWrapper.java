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

import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.SourceFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.apidesign.vm4brwsr.ByteCodeParser.AnnotationParser;
import org.apidesign.vm4brwsr.ByteCodeParser.ClassData;
import org.apidesign.vm4brwsr.ByteCodeParser.FieldData;
import org.apidesign.vm4brwsr.ByteCodeParser.MethodData;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ExtraJavaScript(processByteCode = false, resource="")
final class ClosureWrapper extends CommandLineRunner {
    private static final String[] ARGS = { "--compilation_level", "SIMPLE_OPTIMIZATIONS", "--js", "bck2brwsr-raw.js" /*, "--debug", "--formatting", "PRETTY_PRINT" */ };

    private final ClosuresObfuscationDelegate obfuscationDelegate;
    private final Bck2Brwsr.Resources res;
    private final StringArray classes;

    private String compiledCode;
    private String externsCode;

    private ClosureWrapper(Appendable out, 
                           String compilationLevel,
                           ClosuresObfuscationDelegate obfuscationDelegate,
                           Bck2Brwsr.Resources res, StringArray classes) {
        super(
            generateArguments(compilationLevel),
            new PrintStream(new APS(out)), System.err
        );
        this.obfuscationDelegate = obfuscationDelegate;
        this.res = res;
        this.classes = classes;
    }

    @Override
    protected List<SourceFile> createInputs(List<String> files, boolean allowStdIn) throws FlagUsageException, IOException {
        if (files.size() != 1 || !"bck2brwsr-raw.js".equals(files.get(0))) {
            throw new IOException("Unexpected files: " + files);
        }
        return Collections.nCopies(
                   1,
                   SourceFile.fromGenerator(
                       "bck2brwsr-raw.js",
                       new SourceFile.Generator() {
                           @Override
                           public String getCode() {
                               return getCompiledCode();
                           }
                       }));
    }


    @Override
    protected List<SourceFile> createExterns()
            throws FlagUsageException, IOException {
        final List<SourceFile> externsFiles =
                new ArrayList<SourceFile>(super.createExterns());

        externsFiles.add(
                SourceFile.fromGenerator(
                        "bck2brwsr_externs.js",
                        new SourceFile.Generator() {
                            @Override
                            public String getCode() {
                                return getExternsCode();
                            }
                        }));
        return externsFiles;
    }

    private String getCompiledCode() {
        if (compiledCode == null) {
            StringBuilder sb = new StringBuilder();
            try {
                VM.compile(res, sb, classes, obfuscationDelegate);
                compiledCode = sb.toString();
            } catch (IOException ex) {
                compiledCode = ex.getMessage();
            }
        }
        return compiledCode;
    }

    private String getExternsCode() {
        if (externsCode == null) {
            // need compiled code at this point
            getCompiledCode();

            final StringBuilder sb = new StringBuilder("function RAW() {};\n");
            for (final String extern: obfuscationDelegate.getExterns()) {
                sb.append("RAW.prototype.").append(extern).append(";\n");
            }
            externsCode = sb.toString();
        }
        return externsCode;
    }

    private static final class APS extends OutputStream {
        private final Appendable out;

        public APS(Appendable out) {
            this.out = out;
        }
        @Override
        public void write(int b) throws IOException {
            out.append((char)b);
        }
    }

    private static String[] generateArguments(String compilationLevel) {
        String[] finalArgs = ARGS.clone();
        finalArgs[1] = compilationLevel;

        return finalArgs;
    }

    static int produceTo(Appendable w, ObfuscationLevel obfuscationLevel, Bck2Brwsr.Resources resources, StringArray arr) throws IOException {
        ClosureWrapper cw = create(w, obfuscationLevel, resources, arr);
        try {
            return cw.doRun();
        } catch (FlagUsageException ex) {
            throw new IOException(ex);
        }
    }

    private static ClosureWrapper create(Appendable w,
                                         ObfuscationLevel obfuscationLevel,
                                         Bck2Brwsr.Resources resources,
                                         StringArray arr) {
        switch (obfuscationLevel) {
            case MINIMAL:
                return new ClosureWrapper(w, "SIMPLE_OPTIMIZATIONS",
                                          new SimpleObfuscationDelegate(),
                                          resources, arr);
/*                
            case MEDIUM:
                return new ClosureWrapper(w, "ADVANCED_OPTIMIZATIONS",
                                          new MediumObfuscationDelegate(
                                                  resources),
                                          resources, arr);
*/
            case FULL:
                return new ClosureWrapper(w, "ADVANCED_OPTIMIZATIONS",
                                          new FullObfuscationDelegate(
                                                  resources),
                                          resources, arr);
            default:
                throw new IllegalArgumentException(
                        "Unsupported level: " + obfuscationLevel);
        }
    }

    private static abstract class ClosuresObfuscationDelegate
            extends ObfuscationDelegate {
        public abstract Collection<String> getExterns();
    }

    private static final class SimpleObfuscationDelegate
            extends ClosuresObfuscationDelegate {
        @Override
        public void exportJSProperty(Appendable out,
                                     String destObject,
                                     String propertyName) throws IOException {
        }

        @Override
        public void exportClass(Appendable out,
                                String destObject,
                                String mangledName,
                                ClassData classData) throws IOException {
        }

        @Override
        public void exportMethod(Appendable out,
                                 String destObject,
                                 String mangledName,
                                 MethodData methodData) throws IOException {
        }

        @Override
        public void exportField(Appendable out,
                                String destObject,
                                String mangledName,
                                FieldData fieldData) throws IOException {
        }

        @Override
        public Collection<String> getExterns() {
            return Collections.EMPTY_LIST;
        }
    }

    private static abstract class AdvancedObfuscationDelegate
            extends ClosuresObfuscationDelegate {
        private static final String[] INITIAL_EXTERNS = {
            "bck2brwsr",
            "$class",
            "anno",
            "array",
            "access",
            "cls",
            "vm",
            "loadClass",
            "loadBytes",
            "jvmName",
            "primitive",
            "superclass",
            "cnstr",
            "add32",
            "sub32",
            "mul32",
            "neg32",
            "toInt8",
            "toInt16",
            "next32",
            "high32",
            "toInt32",
            "toFP",
            "toLong",
            "toExactString",
            "add64",
            "sub64",
            "mul64",
            "and64",
            "or64",
            "xor64",
            "shl64",
            "shr64",
            "ushr64",
            "compare64",
            "neg64",
            "div32",
            "mod32",
            "div64",
            "mod64",
            "at",
            "getClass__Ljava_lang_Class_2",
            "clone__Ljava_lang_Object_2"
        };

        private final Bck2Brwsr.Resources resources;

        private final Collection<String> externs;
        private final Map<Object, Boolean> isMarkedAsExportedCache;

        protected AdvancedObfuscationDelegate(Bck2Brwsr.Resources resources) {
            this.resources = resources;

            externs = new ArrayList<String>(Arrays.asList(INITIAL_EXTERNS));
            isMarkedAsExportedCache = new HashMap<Object, Boolean>();
        }

        @Override
        public void exportClass(Appendable out,
                                String destObject,
                                String mangledName,
                                ClassData classData) throws IOException {
            if (isExportedClass(classData)) {
                exportJSProperty(out, destObject, mangledName);
            }
        }

        @Override
        public void exportMethod(Appendable out,
                                 String destObject,
                                 String mangledName,
                                 MethodData methodData) throws IOException {
            if (isAccessible(methodData.access)
                        && isExportedClass(methodData.cls)
                    || isMarkedAsExported(methodData)) {
                exportJSProperty(out, destObject, mangledName);
            }
        }

        @Override
        public void exportField(Appendable out,
                                String destObject,
                                String mangledName,
                                FieldData fieldData) throws IOException {
            if (isAccessible(fieldData.access)
                        && isExportedClass(fieldData.cls)
                    || isMarkedAsExported(fieldData)) {
                exportJSProperty(out, destObject, mangledName);
            }
        }

        @Override
        public Collection<String> getExterns() {
            return externs;
        }

        protected void addExtern(String extern) {
            externs.add(extern);
        }

        private boolean isExportedClass(ClassData classData)
                throws IOException {
            return classData.isPublic() && isMarkedAsExportedPackage(
                                               classData.getPkgName())
                       || isMarkedAsExported(classData);
        }

        private boolean isMarkedAsExportedPackage(String pkgName) {
            if (pkgName == null) {
                return false;
            }

            final Boolean cachedValue = isMarkedAsExportedCache.get(pkgName);
            if (cachedValue != null) {
                return cachedValue;
            }

            final boolean newValue = resolveIsMarkedAsExportedPackage(pkgName);
            isMarkedAsExportedCache.put(pkgName, newValue);

            return newValue;
        }

        private boolean isMarkedAsExported(ClassData classData)
                throws IOException {
            final Boolean cachedValue = isMarkedAsExportedCache.get(classData);
            if (cachedValue != null) {
                return cachedValue;
            }

            final boolean newValue =
                    isMarkedAsExported(classData.findAnnotationData(true),
                                       classData);
            isMarkedAsExportedCache.put(classData, newValue);

            return newValue;
        }

        private boolean isMarkedAsExported(MethodData methodData)
                throws IOException {
            return isMarkedAsExported(methodData.findAnnotationData(true),
                                      methodData.cls);
        }

        private boolean isMarkedAsExported(FieldData fieldData)
                throws IOException {
            return isMarkedAsExported(fieldData.findAnnotationData(true),
                                      fieldData.cls);
        }

        private boolean resolveIsMarkedAsExportedPackage(String pkgName) {
            try {
                final InputStream is =
                        resources.get(pkgName + "/package-info.class");

                try {
                    final ClassData pkgInfoClass = new ClassData(is);
                    return isMarkedAsExported(
                                   pkgInfoClass.findAnnotationData(true),
                                   pkgInfoClass);
                } finally {
                    is.close();
                }
            } catch (final IOException e) {
                return false;
            }
        }

        private boolean isMarkedAsExported(byte[] arrData, ClassData cd)
                throws IOException {
            if (arrData == null) {
                return false;
            }

            final boolean[] found = { false };
            final AnnotationParser annotationParser =
                    new AnnotationParser(false, false) {
                        @Override
                        protected void visitAnnotationStart(
                                String type,
                                boolean top) {
                            if (top && type.equals("Lorg/apidesign/bck2brwsr"
                                                       + "/core/Exported;")) {
                                found[0] = true;
                            }
                        }
                    };
            annotationParser.parse(arrData, cd);
            return found[0];
        }

        private static boolean isAccessible(int access) {
            return (access & (ByteCodeParser.ACC_PUBLIC
                                  | ByteCodeParser.ACC_PROTECTED)) != 0;
        }
    }

    private static final class MediumObfuscationDelegate
            extends AdvancedObfuscationDelegate {
        public MediumObfuscationDelegate(Bck2Brwsr.Resources resources) {
            super(resources);
        }

        @Override
        public void exportJSProperty(Appendable out,
                                     String destObject,
                                     String propertyName) {
            addExtern(propertyName);
        }
    }

    private static final class FullObfuscationDelegate
            extends AdvancedObfuscationDelegate {
        public FullObfuscationDelegate(Bck2Brwsr.Resources resources) {
            super(resources);
        }

        @Override
        public void exportJSProperty(Appendable out,
                                     String destObject,
                                     String propertyName) throws IOException {
            out.append("\n").append(destObject).append("['")
                                               .append(propertyName)
                                               .append("'] = ")
                            .append(destObject).append(".").append(propertyName)
               .append(";\n");
        }
    }
}
