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

package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.Enumeration;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.emul.AnnotationImpl;
import org.apidesign.bck2brwsr.emul.MethodImpl;

/**
 * A {@code Method} provides information about, and access to, a single method
 * on a class or interface.  The reflected method may be a class method
 * or an instance method (including an abstract method).
 *
 * <p>A {@code Method} permits widening conversions to occur when matching the
 * actual parameters to invoke with the underlying method's formal
 * parameters, but it throws an {@code IllegalArgumentException} if a
 * narrowing conversion would occur.
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getMethods()
 * @see java.lang.Class#getMethod(String, Class[])
 * @see java.lang.Class#getDeclaredMethods()
 * @see java.lang.Class#getDeclaredMethod(String, Class[])
 *
 * @author Kenneth Russell
 * @author Nakul Saraiya
 */
public final
    class Method extends AccessibleObject implements GenericDeclaration,
                                                     Member {
    private final Class<?> clazz;
    private final String name;
    private final Object data;
    private final String sig;

   // Generics infrastructure

    private String getGenericSignature() {return null;}

    /**
     * Package-private constructor used by ReflectAccess to enable
     * instantiation of these objects in Java code from the java.lang
     * package via sun.reflect.LangReflectAccess.
     */
    Method(Class<?> declaringClass, String name, Object data, String sig)
    {
        this.clazz = declaringClass;
        this.name = name;
        this.data = data;
        this.sig = sig;
    }

    /**
     * Package-private routine (exposed to java.lang.Class via
     * ReflectAccess) which returns a copy of this Method. The copy's
     * "root" field points to this Method.
     */
    Method copy() {
        return this;
    }

    /**
     * Returns the {@code Class} object representing the class or interface
     * that declares the method represented by this {@code Method} object.
     */
    public Class<?> getDeclaringClass() {
        return clazz;
    }

    /**
     * Returns the name of the method represented by this {@code Method}
     * object, as a {@code String}.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the Java language modifiers for the method represented
     * by this {@code Method} object, as an integer. The {@code Modifier} class should
     * be used to decode the modifiers.
     *
     * @see Modifier
     */
    public int getModifiers() {
        return getAccess(data);
    }
    
    @JavaScriptBody(args = "self", body = "return self.access;")
    private static native int getAccess(Object self);
    
    /**
     * Returns an array of {@code TypeVariable} objects that represent the
     * type variables declared by the generic declaration represented by this
     * {@code GenericDeclaration} object, in declaration order.  Returns an
     * array of length 0 if the underlying generic declaration declares no type
     * variables.
     *
     * @return an array of {@code TypeVariable} objects that represent
     *     the type variables declared by this generic declaration
     * @throws GenericSignatureFormatError if the generic
     *     signature of this generic declaration does not conform to
     *     the format specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @since 1.5
     */
    public TypeVariable<Method>[] getTypeParameters() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a {@code Class} object that represents the formal return type
     * of the method represented by this {@code Method} object.
     *
     * @return the return type for the method this object represents
     */
    public Class<?> getReturnType() {
        return MethodImpl.signatureParser(sig).nextElement();
    }

    /**
     * Returns a {@code Type} object that represents the formal return
     * type of the method represented by this {@code Method} object.
     *
     * <p>If the return type is a parameterized type,
     * the {@code Type} object returned must accurately reflect
     * the actual type parameters used in the source code.
     *
     * <p>If the return type is a type variable or a parameterized type, it
     * is created. Otherwise, it is resolved.
     *
     * @return  a {@code Type} object that represents the formal return
     *     type of the underlying  method
     * @throws GenericSignatureFormatError
     *     if the generic method signature does not conform to the format
     *     specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @throws TypeNotPresentException if the underlying method's
     *     return type refers to a non-existent type declaration
     * @throws MalformedParameterizedTypeException if the
     *     underlying method's return typed refers to a parameterized
     *     type that cannot be instantiated for any reason
     * @since 1.5
     */
    public Type getGenericReturnType() {
        throw new UnsupportedOperationException();
    }


    /**
     * Returns an array of {@code Class} objects that represent the formal
     * parameter types, in declaration order, of the method
     * represented by this {@code Method} object.  Returns an array of length
     * 0 if the underlying method takes no parameters.
     *
     * @return the parameter types for the method this object
     * represents
     */
    public Class<?>[] getParameterTypes() {
        Class[] arr = new Class[MethodImpl.signatureElements(sig) - 1];
        Enumeration<Class> en = MethodImpl.signatureParser(sig);
        en.nextElement(); // return type
        for (int i = 0; i < arr.length; i++) {
            arr[i] = en.nextElement();
        }
        return arr;
    }

    /**
     * Returns an array of {@code Type} objects that represent the formal
     * parameter types, in declaration order, of the method represented by
     * this {@code Method} object. Returns an array of length 0 if the
     * underlying method takes no parameters.
     *
     * <p>If a formal parameter type is a parameterized type,
     * the {@code Type} object returned for it must accurately reflect
     * the actual type parameters used in the source code.
     *
     * <p>If a formal parameter type is a type variable or a parameterized
     * type, it is created. Otherwise, it is resolved.
     *
     * @return an array of Types that represent the formal
     *     parameter types of the underlying method, in declaration order
     * @throws GenericSignatureFormatError
     *     if the generic method signature does not conform to the format
     *     specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @throws TypeNotPresentException if any of the parameter
     *     types of the underlying method refers to a non-existent type
     *     declaration
     * @throws MalformedParameterizedTypeException if any of
     *     the underlying method's parameter types refer to a parameterized
     *     type that cannot be instantiated for any reason
     * @since 1.5
     */
    public Type[] getGenericParameterTypes() {
        throw new UnsupportedOperationException();
    }


    /**
     * Returns an array of {@code Class} objects that represent
     * the types of the exceptions declared to be thrown
     * by the underlying method
     * represented by this {@code Method} object.  Returns an array of length
     * 0 if the method declares no exceptions in its {@code throws} clause.
     *
     * @return the exception types declared as being thrown by the
     * method this object represents
     */
    public Class<?>[] getExceptionTypes() {
        throw new UnsupportedOperationException();
        //return (Class<?>[]) exceptionTypes.clone();
    }

    /**
     * Returns an array of {@code Type} objects that represent the
     * exceptions declared to be thrown by this {@code Method} object.
     * Returns an array of length 0 if the underlying method declares
     * no exceptions in its {@code throws} clause.
     *
     * <p>If an exception type is a type variable or a parameterized
     * type, it is created. Otherwise, it is resolved.
     *
     * @return an array of Types that represent the exception types
     *     thrown by the underlying method
     * @throws GenericSignatureFormatError
     *     if the generic method signature does not conform to the format
     *     specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @throws TypeNotPresentException if the underlying method's
     *     {@code throws} clause refers to a non-existent type declaration
     * @throws MalformedParameterizedTypeException if
     *     the underlying method's {@code throws} clause refers to a
     *     parameterized type that cannot be instantiated for any reason
     * @since 1.5
     */
      public Type[] getGenericExceptionTypes() {
        throw new UnsupportedOperationException();
      }

    /**
     * Compares this {@code Method} against the specified object.  Returns
     * true if the objects are the same.  Two {@code Methods} are the same if
     * they were declared by the same class and have the same name
     * and formal parameter types and return type.
     */
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Method) {
            Method other = (Method)obj;
            return data == other.data;
        }
        return false;
    }

    /**
     * Returns a hashcode for this {@code Method}.  The hashcode is computed
     * as the exclusive-or of the hashcodes for the underlying
     * method's declaring class name and the method's name.
     */
    public int hashCode() {
        return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
    }

    /**
     * Returns a string describing this {@code Method}.  The string is
     * formatted as the method access modifiers, if any, followed by
     * the method return type, followed by a space, followed by the
     * class declaring the method, followed by a period, followed by
     * the method name, followed by a parenthesized, comma-separated
     * list of the method's formal parameter types. If the method
     * throws checked exceptions, the parameter list is followed by a
     * space, followed by the word throws followed by a
     * comma-separated list of the thrown exception types.
     * For example:
     * <pre>
     *    public boolean java.lang.Object.equals(java.lang.Object)
     * </pre>
     *
     * <p>The access modifiers are placed in canonical order as
     * specified by "The Java Language Specification".  This is
     * {@code public}, {@code protected} or {@code private} first,
     * and then other modifiers in the following order:
     * {@code abstract}, {@code static}, {@code final},
     * {@code synchronized}, {@code native}, {@code strictfp}.
     */
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.methodModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(' ');
            }
            sb.append(Field.getTypeName(getReturnType())).append(' ');
            sb.append(Field.getTypeName(getDeclaringClass())).append('.');
            sb.append(getName()).append('(');
            Class<?>[] params = getParameterTypes(); // avoid clone
            for (int j = 0; j < params.length; j++) {
                sb.append(Field.getTypeName(params[j]));
                if (j < (params.length - 1))
                    sb.append(',');
            }
            sb.append(')');
            /*
            Class<?>[] exceptions = exceptionTypes; // avoid clone
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append(exceptions[k].getName());
                    if (k < (exceptions.length - 1))
                        sb.append(',');
                }
            }
            */
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    /**
     * Returns a string describing this {@code Method}, including
     * type parameters.  The string is formatted as the method access
     * modifiers, if any, followed by an angle-bracketed
     * comma-separated list of the method's type parameters, if any,
     * followed by the method's generic return type, followed by a
     * space, followed by the class declaring the method, followed by
     * a period, followed by the method name, followed by a
     * parenthesized, comma-separated list of the method's generic
     * formal parameter types.
     *
     * If this method was declared to take a variable number of
     * arguments, instead of denoting the last parameter as
     * "<tt><i>Type</i>[]</tt>", it is denoted as
     * "<tt><i>Type</i>...</tt>".
     *
     * A space is used to separate access modifiers from one another
     * and from the type parameters or return type.  If there are no
     * type parameters, the type parameter list is elided; if the type
     * parameter list is present, a space separates the list from the
     * class name.  If the method is declared to throw exceptions, the
     * parameter list is followed by a space, followed by the word
     * throws followed by a comma-separated list of the generic thrown
     * exception types.  If there are no type parameters, the type
     * parameter list is elided.
     *
     * <p>The access modifiers are placed in canonical order as
     * specified by "The Java Language Specification".  This is
     * {@code public}, {@code protected} or {@code private} first,
     * and then other modifiers in the following order:
     * {@code abstract}, {@code static}, {@code final},
     * {@code synchronized}, {@code native}, {@code strictfp}.
     *
     * @return a string describing this {@code Method},
     * include type parameters
     *
     * @since 1.5
     */
    public String toGenericString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.methodModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod)).append(' ');
            }
            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append('<');
                for(TypeVariable<?> typeparm: typeparms) {
                    if (!first)
                        sb.append(',');
                    // Class objects can't occur here; no need to test
                    // and call Class.getName().
                    sb.append(typeparm.toString());
                    first = false;
                }
                sb.append("> ");
            }

            Type genRetType = getGenericReturnType();
            sb.append( ((genRetType instanceof Class<?>)?
                        Field.getTypeName((Class<?>)genRetType):genRetType.toString()))
                    .append(' ');

            sb.append(Field.getTypeName(getDeclaringClass())).append('.');
            sb.append(getName()).append('(');
            Type[] params = getGenericParameterTypes();
            for (int j = 0; j < params.length; j++) {
                String param = (params[j] instanceof Class)?
                    Field.getTypeName((Class)params[j]):
                    (params[j].toString());
                if (isVarArgs() && (j == params.length - 1)) // replace T[] with T...
                    param = param.replaceFirst("\\[\\]$", "...");
                sb.append(param);
                if (j < (params.length - 1))
                    sb.append(',');
            }
            sb.append(')');
            Type[] exceptions = getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append((exceptions[k] instanceof Class)?
                              ((Class)exceptions[k]).getName():
                              exceptions[k].toString());
                    if (k < (exceptions.length - 1))
                        sb.append(',');
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    /**
     * Invokes the underlying method represented by this {@code Method}
     * object, on the specified object with the specified parameters.
     * Individual parameters are automatically unwrapped to match
     * primitive formal parameters, and both primitive and reference
     * parameters are subject to method invocation conversions as
     * necessary.
     *
     * <p>If the underlying method is static, then the specified {@code obj}
     * argument is ignored. It may be null.
     *
     * <p>If the number of formal parameters required by the underlying method is
     * 0, the supplied {@code args} array may be of length 0 or null.
     *
     * <p>If the underlying method is an instance method, it is invoked
     * using dynamic method lookup as documented in The Java Language
     * Specification, Second Edition, section 15.12.4.4; in particular,
     * overriding based on the runtime type of the target object will occur.
     *
     * <p>If the underlying method is static, the class that declared
     * the method is initialized if it has not already been initialized.
     *
     * <p>If the method completes normally, the value it returns is
     * returned to the caller of invoke; if the value has a primitive
     * type, it is first appropriately wrapped in an object. However,
     * if the value has the type of an array of a primitive type, the
     * elements of the array are <i>not</i> wrapped in objects; in
     * other words, an array of primitive type is returned.  If the
     * underlying method return type is void, the invocation returns
     * null.
     *
     * @param obj  the object the underlying method is invoked from
     * @param args the arguments used for the method call
     * @return the result of dispatching the method represented by
     * this object on {@code obj} with parameters
     * {@code args}
     *
     * @exception IllegalAccessException    if this {@code Method} object
     *              is enforcing Java language access control and the underlying
     *              method is inaccessible.
     * @exception IllegalArgumentException  if the method is an
     *              instance method and the specified object argument
     *              is not an instance of the class or interface
     *              declaring the underlying method (or of a subclass
     *              or implementor thereof); if the number of actual
     *              and formal parameters differ; if an unwrapping
     *              conversion for primitive arguments fails; or if,
     *              after possible unwrapping, a parameter value
     *              cannot be converted to the corresponding formal
     *              parameter type by a method invocation conversion.
     * @exception InvocationTargetException if the underlying method
     *              throws an exception.
     * @exception NullPointerException      if the specified object is null
     *              and the method is an instance method.
     * @exception ExceptionInInitializerError if the initialization
     * provoked by this method fails.
     */
    public Object invoke(Object obj, Object... args)
        throws IllegalAccessException, IllegalArgumentException,
           InvocationTargetException
    {
        final boolean isStatic = (getModifiers() & Modifier.STATIC) == 0;
        if (isStatic && obj == null) {
            throw new NullPointerException();
        }
        Class[] types = getParameterTypes();
        if (types.length != args.length) {
            throw new IllegalArgumentException("Types len " + types.length + " args: " + args.length);
        } else {
            args = args.clone();
            for (int i = 0; i < types.length; i++) {
                Class c = types[i];
                if (c.isPrimitive()) {
                    args[i] = toPrimitive(c, args[i]);
                }
            }
        }
        Object res = invoke0(isStatic, this, obj, args);
        if (getReturnType().isPrimitive()) {
            res = fromPrimitive(getReturnType(), res);
        }
        return res;
    }
    
    @JavaScriptBody(args = { "st", "method", "self", "args" }, body =
          "var p;\n"
        + "if (st) {\n"
        + "  p = new Array(1);\n"
        + "  p[0] = self;\n"
        + "  p = p.concat(args);\n"
        + "} else {\n"
        + "  p = args;\n"
        + "}\n"
        + "return method.fld_data.apply(self, p);\n"
    )
    private static native Object invoke0(boolean isStatic, Method m, Object self, Object[] args);

    private static Object fromPrimitive(Class<?> type, Object o) {
        if (type == Integer.TYPE) {
            return fromRaw(Integer.class, "valueOf__Ljava_lang_Integer_2I", o);
        }
        if (type == Long.TYPE) {
            return fromRaw(Long.class, "valueOf__Ljava_lang_Long_2J", o);
        }
        if (type == Double.TYPE) {
            return fromRaw(Double.class, "valueOf__Ljava_lang_Double_2D", o);
        }
        if (type == Float.TYPE) {
            return fromRaw(Float.class, "valueOf__Ljava_lang_Float_2F", o);
        }
        if (type == Byte.TYPE) {
            return fromRaw(Byte.class, "valueOf__Ljava_lang_Byte_2B", o);
        }
        if (type == Boolean.TYPE) {
            return fromRaw(Boolean.class, "valueOf__Ljava_lang_Boolean_2Z", o);
        }
        if (type == Short.TYPE) {
            return fromRaw(Short.class, "valueOf__Ljava_lang_Short_2S", o);
        }
        if (type == Character.TYPE) {
            return fromRaw(Character.class, "valueOf__Ljava_lang_Character_2C", o);
        }
        if (type.getName().equals("void")) {
            return null;
        }
        throw new IllegalStateException("Can't convert " + o);
    }
    
    @JavaScriptBody(args = { "cls", "m", "o" }, 
        body = "return cls.cnstr(false)[m](o);"
    )
    private static native Integer fromRaw(Class<?> cls, String m, Object o);

    private static Object toPrimitive(Class<?> type, Object o) {
        if (type == Integer.TYPE) {
            return toRaw("intValue__I", o);
        }
        if (type == Long.TYPE) {
            return toRaw("longValue__J", o);
        }
        if (type == Double.TYPE) {
            return toRaw("doubleValue__D", o);
        }
        if (type == Float.TYPE) {
            return toRaw("floatValue__F", o);
        }
        if (type == Byte.TYPE) {
            return toRaw("byteValue__B", o);
        }
        if (type == Boolean.TYPE) {
            return toRaw("booleanValue__Z", o);
        }
        if (type == Short.TYPE) {
            return toRaw("shortValue__S", o);
        }
        if (type == Character.TYPE) {
            return toRaw("charValue__C", o);
        }
        if (type.getName().equals("void")) {
            return o;
        }
        throw new IllegalStateException("Can't convert " + o);
    }
    
    @JavaScriptBody(args = { "m", "o" }, 
        body = "return o[m](o);"
    )
    private static native Object toRaw(String m, Object o);
    
    /**
     * Returns {@code true} if this method is a bridge
     * method; returns {@code false} otherwise.
     *
     * @return true if and only if this method is a bridge
     * method as defined by the Java Language Specification.
     * @since 1.5
     */
    public boolean isBridge() {
        return (getModifiers() & Modifier.BRIDGE) != 0;
    }

    /**
     * Returns {@code true} if this method was declared to take
     * a variable number of arguments; returns {@code false}
     * otherwise.
     *
     * @return {@code true} if an only if this method was declared to
     * take a variable number of arguments.
     * @since 1.5
     */
    public boolean isVarArgs() {
        return (getModifiers() & Modifier.VARARGS) != 0;
    }

    /**
     * Returns {@code true} if this method is a synthetic
     * method; returns {@code false} otherwise.
     *
     * @return true if and only if this method is a synthetic
     * method as defined by the Java Language Specification.
     * @since 1.5
     */
    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    @JavaScriptBody(args = { "self", "ac" }, 
        body = 
          "if (self.fld_data.anno) {"
        + "  return self.fld_data.anno['L' + ac.jvmName + ';'];"
        + "} else return null;"
    )
    private Object getAnnotationData(Class<?> annotationClass) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Object data = getAnnotationData(annotationClass);
        return data == null ? null : AnnotationImpl.create(annotationClass, data);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the default value for the annotation member represented by
     * this {@code Method} instance.  If the member is of a primitive type,
     * an instance of the corresponding wrapper type is returned. Returns
     * null if no default is associated with the member, or if the method
     * instance does not represent a declared member of an annotation type.
     *
     * @return the default value for the annotation member represented
     *     by this {@code Method} instance.
     * @throws TypeNotPresentException if the annotation is of type
     *     {@link Class} and no definition can be found for the
     *     default class value.
     * @since  1.5
     */
    public Object getDefaultValue() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an array of arrays that represent the annotations on the formal
     * parameters, in declaration order, of the method represented by
     * this {@code Method} object. (Returns an array of length zero if the
     * underlying method is parameterless.  If the method has one or more
     * parameters, a nested array of length zero is returned for each parameter
     * with no annotations.) The annotation objects contained in the returned
     * arrays are serializable.  The caller of this method is free to modify
     * the returned arrays; it will have no effect on the arrays returned to
     * other callers.
     *
     * @return an array of arrays that represent the annotations on the formal
     *    parameters, in declaration order, of the method represented by this
     *    Method object
     * @since 1.5
     */
    public Annotation[][] getParameterAnnotations() {
        throw new UnsupportedOperationException();
    }

    static {
        MethodImpl.INSTANCE = new MethodImpl() {
            protected Method create(Class<?> declaringClass, String name, Object data, String sig) {
                return new Method(declaringClass, name, data, sig);
            }
        };
    }
}
