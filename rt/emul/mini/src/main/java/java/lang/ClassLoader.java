/*
 * Copyright (c) 1994, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 * A class loader is an object that is responsible for loading classes. The
 * class <tt>ClassLoader</tt> is an abstract class.  Given the <a
 * href="#name">binary name</a> of a class, a class loader should attempt to
 * locate or generate data that constitutes a definition for the class.  A
 * typical strategy is to transform the name into a file name and then read a
 * "class file" of that name from a file system.
 *
 * <p> Every {@link Class <tt>Class</tt>} object contains a {@link
 * Class#getClassLoader() reference} to the <tt>ClassLoader</tt> that defined
 * it.
 *
 * <p> <tt>Class</tt> objects for array classes are not created by class
 * loaders, but are created automatically as required by the Java runtime.
 * The class loader for an array class, as returned by {@link
 * Class#getClassLoader()} is the same as the class loader for its element
 * type; if the element type is a primitive type, then the array class has no
 * class loader.
 *
 * <p> Applications implement subclasses of <tt>ClassLoader</tt> in order to
 * extend the manner in which the Java virtual machine dynamically loads
 * classes.
 *
 * <p> Class loaders may typically be used by security managers to indicate
 * security domains.
 *
 * <p> The <tt>ClassLoader</tt> class uses a delegation model to search for
 * classes and resources.  Each instance of <tt>ClassLoader</tt> has an
 * associated parent class loader.  When requested to find a class or
 * resource, a <tt>ClassLoader</tt> instance will delegate the search for the
 * class or resource to its parent class loader before attempting to find the
 * class or resource itself.  The virtual machine's built-in class loader,
 * called the "bootstrap class loader", does not itself have a parent but may
 * serve as the parent of a <tt>ClassLoader</tt> instance.
 *
 * <p> Class loaders that support concurrent loading of classes are known as
 * <em>parallel capable</em> class loaders and are required to register
 * themselves at their class initialization time by invoking the
 * {@link
 * #registerAsParallelCapable <tt>ClassLoader.registerAsParallelCapable</tt>}
 * method. Note that the <tt>ClassLoader</tt> class is registered as parallel
 * capable by default. However, its subclasses still need to register themselves
 * if they are parallel capable. <br>
 * In environments in which the delegation model is not strictly
 * hierarchical, class loaders need to be parallel capable, otherwise class
 * loading can lead to deadlocks because the loader lock is held for the
 * duration of the class loading process (see {@link #loadClass
 * <tt>loadClass</tt>} methods).
 *
 * <p> Normally, the Java virtual machine loads classes from the local file
 * system in a platform-dependent manner.  For example, on UNIX systems, the
 * virtual machine loads classes from the directory defined by the
 * <tt>CLASSPATH</tt> environment variable.
 *
 * <p> However, some classes may not originate from a file; they may originate
 * from other sources, such as the network, or they could be constructed by an
 * application.  The method {@link #defineClass(String, byte[], int, int)
 * <tt>defineClass</tt>} converts an array of bytes into an instance of class
 * <tt>Class</tt>. Instances of this newly defined class can be created using
 * {@link Class#newInstance <tt>Class.newInstance</tt>}.
 *
 * <p> The methods and constructors of objects created by a class loader may
 * reference other classes.  To determine the class(es) referred to, the Java
 * virtual machine invokes the {@link #loadClass <tt>loadClass</tt>} method of
 * the class loader that originally created the class.
 *
 * <p> For example, an application could create a network class loader to
 * download class files from a server.  Sample code might look like:
 *
 * <blockquote><pre>
 *   ClassLoader loader&nbsp;= new NetworkClassLoader(host,&nbsp;port);
 *   Object main&nbsp;= loader.loadClass("Main", true).newInstance();
 *       &nbsp;.&nbsp;.&nbsp;.
 * </pre></blockquote>
 *
 * <p> The network class loader subclass must define the methods {@link
 * #findClass <tt>findClass</tt>} and <tt>loadClassData</tt> to load a class
 * from the network.  Once it has downloaded the bytes that make up the class,
 * it should use the method {@link #defineClass <tt>defineClass</tt>} to
 * create a class instance.  A sample implementation is:
 *
 * <blockquote><pre>
 *     class NetworkClassLoader extends ClassLoader {
 *         String host;
 *         int port;
 *
 *         public Class findClass(String name) {
 *             byte[] b = loadClassData(name);
 *             return defineClass(name, b, 0, b.length);
 *         }
 *
 *         private byte[] loadClassData(String name) {
 *             // load the class data from the connection
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote>
 *
 * <h4> <a name="name">Binary names</a> </h4>
 *
 * <p> Any class name provided as a {@link String} parameter to methods in
 * <tt>ClassLoader</tt> must be a binary name as defined by
 * <cite>The Java&trade; Language Specification</cite>.
 *
 * <p> Examples of valid class names include:
 * <blockquote><pre>
 *   "java.lang.String"
 *   "javax.swing.JSpinner$DefaultEditor"
 *   "java.security.KeyStore$Builder$FileBuilder$1"
 *   "java.net.URLClassLoader$3$1"
 * </pre></blockquote>
 *
 * @see      #resolveClass(Class)
 * @since 1.0
 */
public abstract class ClassLoader {

    @JavaScriptBody(args = {}, body = "")
    private static native void registerNatives();
    static {
        registerNatives();
    }

    // The parent class loader for delegation
    // Note: VM hardcoded the offset of this field, thus all new fields
    // must be added *after* it.
    private final ClassLoader parent;


    /**
     * Creates a new class loader using the specified parent class loader for
     * delegation.
     *
     * <p> If there is a security manager, its {@link
     * SecurityManager#checkCreateClassLoader()
     * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
     * a security exception.  </p>
     *
     * @param  parent
     *         The parent class loader
     *
     * @throws  SecurityException
     *          If a security manager exists and its
     *          <tt>checkCreateClassLoader</tt> method doesn't allow creation
     *          of a new class loader.
     *
     * @since  1.2
     */
    protected ClassLoader(ClassLoader parent) {
        this.parent = parent;
    }

    /**
     * Creates a new class loader using the <tt>ClassLoader</tt> returned by
     * the method {@link #getSystemClassLoader()
     * <tt>getSystemClassLoader()</tt>} as the parent class loader.
     *
     * <p> If there is a security manager, its {@link
     * SecurityManager#checkCreateClassLoader()
     * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
     * a security exception.  </p>
     *
     * @throws  SecurityException
     *          If a security manager exists and its
     *          <tt>checkCreateClassLoader</tt> method doesn't allow creation
     *          of a new class loader.
     */
    protected ClassLoader() {
        this.parent = null;
    }

    // -- Class --

    /**
     * Loads the class with the specified <a href="#name">binary name</a>.
     * This method searches for classes in the same manner as the {@link
     * #loadClass(String, boolean)} method.  It is invoked by the Java virtual
     * machine to resolve class references.  Invoking this method is equivalent
     * to invoking {@link #loadClass(String, boolean) <tt>loadClass(name,
     * false)</tt>}.  </p>
     *
     * @param  name
     *         The <a href="#name">binary name</a> of the class
     *
     * @return  The resulting <tt>Class</tt> object
     *
     * @throws  ClassNotFoundException
     *          If the class was not found
     */
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    /**
     * Loads the class with the specified <a href="#name">binary name</a>.  The
     * default implementation of this method searches for classes in the
     * following order:
     *
     * <p><ol>
     *
     *   <li><p> Invoke {@link #findLoadedClass(String)} to check if the class
     *   has already been loaded.  </p></li>
     *
     *   <li><p> Invoke the {@link #loadClass(String) <tt>loadClass</tt>} method
     *   on the parent class loader.  If the parent is <tt>null</tt> the class
     *   loader built-in to the virtual machine is used, instead.  </p></li>
     *
     *   <li><p> Invoke the {@link #findClass(String)} method to find the
     *   class.  </p></li>
     *
     * </ol>
     *
     * <p> If the class was found using the above steps, and the
     * <tt>resolve</tt> flag is true, this method will then invoke the {@link
     * #resolveClass(Class)} method on the resulting <tt>Class</tt> object.
     *
     * <p> Subclasses of <tt>ClassLoader</tt> are encouraged to override {@link
     * #findClass(String)}, rather than this method.  </p>
     *
     * <p> Unless overridden, this method synchronizes on the result of
     * {@link #getClassLoadingLock <tt>getClassLoadingLock</tt>} method
     * during the entire class loading process.
     *
     * @param  name
     *         The <a href="#name">binary name</a> of the class
     *
     * @param  resolve
     *         If <tt>true</tt> then resolve the class
     *
     * @return  The resulting <tt>Class</tt> object
     *
     * @throws  ClassNotFoundException
     *          If the class could not be found
     */
    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class c = findLoadedClass(name);
            if (c == null) {
                try {
                    if (parent != null) {
                        c = parent.loadClass(name, false);
                    } else {
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }

                if (c == null) {
                    // If still not found, then invoke findClass in order
                    // to find the class.
                    c = findClass(name);

//                    // this is the defining class loader; record the stats
//                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
//                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
//                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    /**
     * Returns the lock object for class loading operations.
     * For backward compatibility, the default implementation of this method
     * behaves as follows. If this ClassLoader object is registered as
     * parallel capable, the method returns a dedicated object associated
     * with the specified class name. Otherwise, the method returns this
     * ClassLoader object. </p>
     *
     * @param  className
     *         The name of the to-be-loaded class
     *
     * @return the lock for class loading operations
     *
     * @throws NullPointerException
     *         If registered as parallel capable and <tt>className</tt> is null
     *
     * @see #loadClass(String, boolean)
     *
     * @since  1.7
     */
    protected Object getClassLoadingLock(String className) {
        Object lock = this;
        return lock;
    }

    /**
     * Finds the class with the specified <a href="#name">binary name</a>.
     * This method should be overridden by class loader implementations that
     * follow the delegation model for loading classes, and will be invoked by
     * the {@link #loadClass <tt>loadClass</tt>} method after checking the
     * parent class loader for the requested class.  The default implementation
     * throws a <tt>ClassNotFoundException</tt>.  </p>
     *
     * @param  name
     *         The <a href="#name">binary name</a> of the class
     *
     * @return  The resulting <tt>Class</tt> object
     *
     * @throws  ClassNotFoundException
     *          If the class could not be found
     *
     * @since  1.2
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    /**
     * Converts an array of bytes into an instance of class <tt>Class</tt>.
     * Before the <tt>Class</tt> can be used it must be resolved.  This method
     * is deprecated in favor of the version that takes a <a
     * href="#name">binary name</a> as its first argument, and is more secure.
     *
     * @param  b
     *         The bytes that make up the class data.  The bytes in positions
     *         <tt>off</tt> through <tt>off+len-1</tt> should have the format
     *         of a valid class file as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *
     * @param  off
     *         The start offset in <tt>b</tt> of the class data
     *
     * @param  len
     *         The length of the class data
     *
     * @return  The <tt>Class</tt> object that was created from the specified
     *          class data
     *
     * @throws  ClassFormatError
     *          If the data did not contain a valid class
     *
     * @throws  IndexOutOfBoundsException
     *          If either <tt>off</tt> or <tt>len</tt> is negative, or if
     *          <tt>off+len</tt> is greater than <tt>b.length</tt>.
     *
     * @throws  SecurityException
     *          If an attempt is made to add this class to a package that
     *          contains classes that were signed by a different set of
     *          certificates than this class, or if an attempt is made
     *          to define a class in a package with a fully-qualified name
     *          that starts with "{@code java.}".
     *
     * @see  #loadClass(String, boolean)
     * @see  #resolveClass(Class)
     *
     * @deprecated  Replaced by {@link #defineClass(String, byte[], int, int)
     * defineClass(String, byte[], int, int)}
     */
    @Deprecated
    protected final Class<?> defineClass(byte[] b, int off, int len)
        throws ClassFormatError
    {
        throw new SecurityException();
    }

    /**
     * Converts an array of bytes into an instance of class <tt>Class</tt>.
     * Before the <tt>Class</tt> can be used it must be resolved.
     *
     * <p> This method assigns a default {@link java.security.ProtectionDomain
     * <tt>ProtectionDomain</tt>} to the newly defined class.  The
     * <tt>ProtectionDomain</tt> is effectively granted the same set of
     * permissions returned when {@link
     * java.security.Policy#getPermissions(java.security.CodeSource)
     * <tt>Policy.getPolicy().getPermissions(new CodeSource(null, null))</tt>}
     * is invoked.  The default domain is created on the first invocation of
     * {@link #defineClass(String, byte[], int, int) <tt>defineClass</tt>},
     * and re-used on subsequent invocations.
     *
     * <p> To assign a specific <tt>ProtectionDomain</tt> to the class, use
     * the {@link #defineClass(String, byte[], int, int,
     * java.security.ProtectionDomain) <tt>defineClass</tt>} method that takes a
     * <tt>ProtectionDomain</tt> as one of its arguments.  </p>
     *
     * @param  name
     *         The expected <a href="#name">binary name</a> of the class, or
     *         <tt>null</tt> if not known
     *
     * @param  b
     *         The bytes that make up the class data.  The bytes in positions
     *         <tt>off</tt> through <tt>off+len-1</tt> should have the format
     *         of a valid class file as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *
     * @param  off
     *         The start offset in <tt>b</tt> of the class data
     *
     * @param  len
     *         The length of the class data
     *
     * @return  The <tt>Class</tt> object that was created from the specified
     *          class data.
     *
     * @throws  ClassFormatError
     *          If the data did not contain a valid class
     *
     * @throws  IndexOutOfBoundsException
     *          If either <tt>off</tt> or <tt>len</tt> is negative, or if
     *          <tt>off+len</tt> is greater than <tt>b.length</tt>.
     *
     * @throws  SecurityException
     *          If an attempt is made to add this class to a package that
     *          contains classes that were signed by a different set of
     *          certificates than this class (which is unsigned), or if
     *          <tt>name</tt> begins with "<tt>java.</tt>".
     *
     * @see  #loadClass(String, boolean)
     * @see  #resolveClass(Class)
     * @see  java.security.CodeSource
     * @see  java.security.SecureClassLoader
     *
     * @since  1.1
     */
    protected final Class<?> defineClass(String name, byte[] b, int off, int len)
        throws ClassFormatError
    {
        throw new SecurityException();
    }

    /**
     * Links the specified class.  This (misleadingly named) method may be
     * used by a class loader to link a class.  If the class <tt>c</tt> has
     * already been linked, then this method simply returns. Otherwise, the
     * class is linked as described in the "Execution" chapter of
     * <cite>The Java&trade; Language Specification</cite>.
     * </p>
     *
     * @param  c
     *         The class to link
     *
     * @throws  NullPointerException
     *          If <tt>c</tt> is <tt>null</tt>.
     *
     * @see  #defineClass(String, byte[], int, int)
     */
    protected final void resolveClass(Class<?> c) {
        resolveClass0(c);
    }

    private native void resolveClass0(Class c);


    /**
     * Returns the class with the given <a href="#name">binary name</a> if this
     * loader has been recorded by the Java virtual machine as an initiating
     * loader of a class with that <a href="#name">binary name</a>.  Otherwise
     * <tt>null</tt> is returned.  </p>
     *
     * @param  name
     *         The <a href="#name">binary name</a> of the class
     *
     * @return  The <tt>Class</tt> object, or <tt>null</tt> if the class has
     *          not been loaded
     *
     * @since  1.1
     */
    protected final Class<?> findLoadedClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    /**
     * Sets the signers of a class.  This should be invoked after defining a
     * class.  </p>
     *
     * @param  c
     *         The <tt>Class</tt> object
     *
     * @param  signers
     *         The signers for the class
     *
     * @since  1.1
     */
    protected final void setSigners(Class<?> c, Object[] signers) {
        //c.setSigners(signers);
        throw new UnsupportedOperationException();
    }


    // -- Resource --

    /**
     * Finds the resource with the given name.  A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     *
     * <p> The name of a resource is a '<tt>/</tt>'-separated path name that
     * identifies the resource.
     *
     * <p> This method will first search the parent class loader for the
     * resource; if the parent is <tt>null</tt> the path of the class loader
     * built-in to the virtual machine is searched.  That failing, this method
     * will invoke {@link #findResource(String)} to find the resource.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  A <tt>URL</tt> object for reading the resource, or
     *          <tt>null</tt> if the resource could not be found or the invoker
     *          doesn't have adequate  privileges to get the resource.
     *
     * @since  1.1
     */
    public URL getResource(String name) {
        URL url;
        if (parent != null) {
            url = parent.getResource(name);
        } else {
            url = getBootstrapResource(name);
        }
        if (url == null) {
            url = findResource(name);
        }
        return url;
    }

    /**
     * Finds all the resources with the given name. A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     *
     * <p>The name of a resource is a <tt>/</tt>-separated path name that
     * identifies the resource.
     *
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
     *          the resource.  If no resources could  be found, the enumeration
     *          will be empty.  Resources that the class loader doesn't have
     *          access to will not be in the enumeration.
     *
     * @throws  IOException
     *          If I/O errors occur
     *
     * @see  #findResources(String)
     *
     * @since  1.2
     */
    public Enumeration<URL> getResources(String name) throws IOException {
        if (this == SYSTEM) {
            return findResources(name);
        }
        Enumeration[] tmp = new Enumeration[2];
        if (parent != null) {
            tmp[0] = parent.getResources(name);
        } else {
            tmp[0] = getBootstrapResources(name);
        }
        tmp[1] = findResources(name);

        return new CompoundEnumeration(tmp);
    }

    /**
     * Finds the resource with the given name. Class loader implementations
     * should override this method to specify where to find resources.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  A <tt>URL</tt> object for reading the resource, or
     *          <tt>null</tt> if the resource could not be found
     *
     * @since  1.2
     */
    protected URL findResource(String name) {
        return null;
    }

    /**
     * Returns an enumeration of {@link java.net.URL <tt>URL</tt>} objects
     * representing all the resources with the given name. Class loader
     * implementations should override this method to specify where to load
     * resources from.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
     *          the resources
     *
     * @throws  IOException
     *          If I/O errors occur
     *
     * @since  1.2
     */
    protected Enumeration<URL> findResources(String name) throws IOException {
        return new CompoundEnumeration(new Enumeration[0]);
    }

    // index 0: java.lang.ClassLoader.class
    // index 1: the immediate caller of index 0.
    // index 2: the immediate caller of index 1.
    private static native Class<? extends ClassLoader> getCaller(int index);

    /**
     * Registers the caller as parallel capable.</p>
     * The registration succeeds if and only if all of the following
     * conditions are met: <br>
     * 1. no instance of the caller has been created</p>
     * 2. all of the super classes (except class Object) of the caller are
     * registered as parallel capable</p>
     * Note that once a class loader is registered as parallel capable, there
     * is no way to change it back. </p>
     *
     * @return  true if the caller is successfully registered as
     *          parallel capable and false if otherwise.
     *
     * @since   1.7
     */
//    protected static boolean registerAsParallelCapable() {
//        return false;
//    }

    /**
     * Find a resource of the specified name from the search path used to load
     * classes.  This method locates the resource through the system class
     * loader (see {@link #getSystemClassLoader()}).  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  A {@link java.net.URL <tt>URL</tt>} object for reading the
     *          resource, or <tt>null</tt> if the resource could not be found
     *
     * @since  1.1
     */
    public static URL getSystemResource(String name) {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return getBootstrapResource(name);
        }
        return system.getResource(name);
    }

    /**
     * Finds all resources of the specified name from the search path used to
     * load classes.  The resources thus found are returned as an
     * {@link java.util.Enumeration <tt>Enumeration</tt>} of {@link
     * java.net.URL <tt>URL</tt>} objects.
     *
     * <p> The search order is described in the documentation for {@link
     * #getSystemResource(String)}.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of resource {@link java.net.URL <tt>URL</tt>}
     *          objects
     *
     * @throws  IOException
     *          If I/O errors occur

     * @since  1.2
     */
    public static Enumeration<URL> getSystemResources(String name)
        throws IOException
    {
        ClassLoader system = null; // getSystemClassLoader();
        if (system == null) {
            return getBootstrapResources(name);
        }
        return system.getResources(name);
    }



    /**
     * Returns an input stream for reading the specified resource.
     *
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An input stream for reading the resource, or <tt>null</tt>
     *          if the resource could not be found
     *
     * @since  1.1
     */
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Open for reading, a resource of the specified name from the search path
     * used to load classes.  This method locates the resource through the
     * system class loader (see {@link #getSystemClassLoader()}).  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An input stream for reading the resource, or <tt>null</tt>
     *          if the resource could not be found
     *
     * @since  1.1
     */
    public static InputStream getSystemResourceAsStream(String name) {
        URL url = getSystemResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }


    // -- Hierarchy --

    /**
     * Returns the parent class loader for delegation. Some implementations may
     * use <tt>null</tt> to represent the bootstrap class loader. This method
     * will return <tt>null</tt> in such implementations if this class loader's
     * parent is the bootstrap class loader.
     *
     * <p> If a security manager is present, and the invoker's class loader is
     * not <tt>null</tt> and is not an ancestor of this class loader, then this
     * method invokes the security manager's {@link
     * SecurityManager#checkPermission(java.security.Permission)
     * <tt>checkPermission</tt>} method with a {@link
     * RuntimePermission#RuntimePermission(String)
     * <tt>RuntimePermission("getClassLoader")</tt>} permission to verify
     * access to the parent class loader is permitted.  If not, a
     * <tt>SecurityException</tt> will be thrown.  </p>
     *
     * @return  The parent <tt>ClassLoader</tt>
     *
     * @throws  SecurityException
     *          If a security manager exists and its <tt>checkPermission</tt>
     *          method doesn't allow access to this class loader's parent class
     *          loader.
     *
     * @since  1.2
     */
    public final ClassLoader getParent() {
        throw new SecurityException();
    }

    /**
     * Returns the system class loader for delegation.  This is the default
     * delegation parent for new <tt>ClassLoader</tt> instances, and is
     * typically the class loader used to start the application.
     *
     * <p> This method is first invoked early in the runtime's startup
     * sequence, at which point it creates the system class loader and sets it
     * as the context class loader of the invoking <tt>Thread</tt>.
     *
     * <p> The default system class loader is an implementation-dependent
     * instance of this class.
     *
     * <p> If the system property "<tt>java.system.class.loader</tt>" is defined
     * when this method is first invoked then the value of that property is
     * taken to be the name of a class that will be returned as the system
     * class loader.  The class is loaded using the default system class loader
     * and must define a public constructor that takes a single parameter of
     * type <tt>ClassLoader</tt> which is used as the delegation parent.  An
     * instance is then created using this constructor with the default system
     * class loader as the parameter.  The resulting class loader is defined
     * to be the system class loader.
     *
     * <p> If a security manager is present, and the invoker's class loader is
     * not <tt>null</tt> and the invoker's class loader is not the same as or
     * an ancestor of the system class loader, then this method invokes the
     * security manager's {@link
     * SecurityManager#checkPermission(java.security.Permission)
     * <tt>checkPermission</tt>} method with a {@link
     * RuntimePermission#RuntimePermission(String)
     * <tt>RuntimePermission("getClassLoader")</tt>} permission to verify
     * access to the system class loader.  If not, a
     * <tt>SecurityException</tt> will be thrown.  </p>
     *
     * @return  The system <tt>ClassLoader</tt> for delegation, or
     *          <tt>null</tt> if none
     *
     * @throws  SecurityException
     *          If a security manager exists and its <tt>checkPermission</tt>
     *          method doesn't allow access to the system class loader.
     *
     * @throws  IllegalStateException
     *          If invoked recursively during the construction of the class
     *          loader specified by the "<tt>java.system.class.loader</tt>"
     *          property.
     *
     * @throws  Error
     *          If the system property "<tt>java.system.class.loader</tt>"
     *          is defined but the named class could not be loaded, the
     *          provider class does not define the required constructor, or an
     *          exception is thrown by that constructor when it is invoked. The
     *          underlying cause of the error can be retrieved via the
     *          {@link Throwable#getCause()} method.
     *
     * @revised  1.4
     */
    public static ClassLoader getSystemClassLoader() {
        if (SYSTEM == null) {
            SYSTEM = new ClassLoader() {
                @Override
                protected Enumeration<URL> findResources(String name) throws IOException {
                    return getBootstrapResources(name);
                }

                @Override
                protected URL findResource(String name) {
                    return getBootstrapResource(name);
                }

                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    return Class.forName(name);
                }
            };
        }
        return SYSTEM;
    }
    private static ClassLoader SYSTEM;

    // Returns true if the specified class loader can be found in this class
    // loader's delegation chain.
    boolean isAncestor(ClassLoader cl) {
        ClassLoader acl = this;
        do {
            acl = acl.parent;
            if (cl == acl) {
                return true;
            }
        } while (acl != null);
        return false;
    }

    private Class findBootstrapClassOrNull(String name) {
        throw new UnsupportedOperationException();
    }

    private static URL getBootstrapResource(String name) {
        return Object.class.getResource("/" + name);
    }

    @JavaScriptBody(args = { "name", "skip" }, body
        = "var lb = vm.loadBytes ? vm.loadBytes : exports.loadBytes;"
        + "return lb ? lb(name, skip) : null;"
    )
    static native byte[] getResourceAsStream0(String name, int skip);

    private static Enumeration<URL> getBootstrapResources(String name) {
        return new ResEnum(name);
    }
    
    private static class ResEnum implements Enumeration<URL> {
        private final String name;
        private URL next;
        private int skip;

        public ResEnum(String name) {
            this.name = name;
        }


        public boolean hasMoreElements() {
            if (next == null && skip >= 0) {
                byte[] arr = getResourceAsStream0(name, skip++);
                if (arr != null) {
                    next = Class.newResourceURL(name, arr);
                } else {
                    skip = -1;
                }
            }
            return next != null;
        }

        public URL nextElement() {
            URL r = next;
            if (r == null) {
                throw new NoSuchElementException();
            }
            next = null;
            return r;
        }
    }
    
    private static class OneOrZeroEnum implements Enumeration<URL> {
        private URL u;

        public OneOrZeroEnum(URL u) {
            this.u = u;
        }

        public boolean hasMoreElements() {
            return u != null;
        }

        public URL nextElement() {
            URL r = u;
            if (r == null) {
                throw new NoSuchElementException();
            }
            u = null;
            return r;
        }
    }

    private static class CompoundEnumeration implements Enumeration<URL> {
        private URL next;
        private int index;
        private final Enumeration[] arr;

        public CompoundEnumeration(Enumeration[] arr) {
            this.arr = arr;
            this.index = 0;
        }

        public boolean hasMoreElements() {
            if (next == null && index < arr.length) {
                if (arr[index].hasMoreElements()) {
                    next = (URL) arr[index].nextElement();
                } else {
                    if (index < arr.length) {
                        index++;
                        return hasMoreElements();
                    }
                }
            }
            return next != null;
        }

        public URL nextElement() {
            if (!hasMoreElements()) {
                throw new NoSuchElementException();
            }
            URL r = next;
            next = null;
            return r;
        }
        
    }
}
