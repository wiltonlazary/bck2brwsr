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
package org.apidesign.bck2brwsr.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Console {
    static {
        turnAssetionStatusOn();
    }
    
    @JavaScriptBody(args = {"id", "attr"}, body = 
        "return window.document.getElementById(id)[attr].toString();")
    private static native Object getAttr(String id, String attr);

    @JavaScriptBody(args = {"id", "attr", "value"}, body = 
        "window.document.getElementById(id)[attr] = value;")
    private static native void setAttr(String id, String attr, Object value);
    
    @JavaScriptBody(args = {}, body = "return; window.close();")
    private static native void closeWindow();

    private static void log(String newText) {
        String id = "bck2brwsr.result";
        String attr = "value";
        setAttr(id, attr, getAttr(id, attr) + "\n" + newText);
        setAttr(id, "scrollTop", getAttr(id, "scrollHeight"));
    }
    
    public static void execute() throws Exception {
        String clazz = (String) getAttr("clazz", "value");
        String method = (String) getAttr("method", "value");
        Object res = invokeMethod(clazz, method);
        setAttr("bck2brwsr.result", "value", res);
    }

    @JavaScriptBody(args = { "url", "callback", "arr" }, body = ""
        + "var request = new XMLHttpRequest();\n"
        + "request.open('GET', url, true);\n"
        + "request.onreadystatechange = function() {\n"
        + "  if (this.readyState!==4) return;\n"
        + "  arr[0] = this.responseText;\n"
        + "  callback.run__V();\n"
        + "};"
        + "request.send();"
    )
    private static native void loadText(String url, Runnable callback, String[] arr) throws IOException;
    
    public static void harness(String url) throws IOException {
        log("Connecting to " + url);
        Request r = new Request(url);
    }
    
    private static class Request implements Runnable {
        private final String[] arr = { null };
        private final String url;

        private Request(String url) throws IOException {
            this.url = url;
            loadText(url, this, arr);
        }
        
        @Override
        public void run() {
            try {
                String data = arr[0];
                log("\nGot \"" + data + "\"");
                
                if (data == null) {
                    log("Some error exiting");
                    closeWindow();
                    return;
                }
                
                if (data.isEmpty()) {
                    log("No data, exiting");
                    closeWindow();
                    return;
                }
                
                Case c = Case.parseData(data);
                if (c.getHtmlFragment() != null) {
                    setAttr("bck2brwsr.fragment", "innerHTML", c.getHtmlFragment());
                }
                log("Invoking " + c.getClassName() + '.' + c.getMethodName() + " as request: " + c.getRequestId());

                Object result = invokeMethod(c.getClassName(), c.getMethodName());
                
                setAttr("bck2brwsr.fragment", "innerHTML", "");
                log("Result: " + result);
                
                result = encodeURL("" + result);
                
                log("Sending back: " + url + "?request=" + c.getRequestId() + "&result=" + result);
                String u = url + "?request=" + c.getRequestId() + "&result=" + result;
                
                loadText(u, this, arr);
                
            } catch (Exception ex) {
                log(ex.getMessage());
            }
        }
    }
    
    private static String encodeURL(String r) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < r.length(); i++) {
            int ch = r.charAt(i);
            if (ch < 32 || ch == '%' || ch == '+') {
                sb.append("%").append(("0" + Integer.toHexString(ch)).substring(0, 2));
            } else {
                if (ch == 32) {
                    sb.append("+");
                } else {
                    sb.append((char)ch);
                }
            }
        }
        return sb.toString();
    }
    
    static String invoke(String clazz, String method) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
        final Object r = invokeMethod(clazz, method);
        return r == null ? "null" : r.toString().toString();
    }

    /** Helper method that inspects the classpath and loads given resource
     * (usually a class file). Used while running tests in Rhino.
     * 
     * @param name resource name to find
     * @return the array of bytes in the given resource
     * @throws IOException I/O in case something goes wrong
     */
    public static byte[] read(String name) throws IOException {
        URL u = null;
        Enumeration<URL> en = Console.class.getClassLoader().getResources(name);
        while (en.hasMoreElements()) {
            u = en.nextElement();
        }
        if (u == null) {
            throw new IOException("Can't find " + name);
        }
        try (InputStream is = u.openStream()) {
            byte[] arr;
            arr = new byte[is.available()];
            int offset = 0;
            while (offset < arr.length) {
                int len = is.read(arr, offset, arr.length - offset);
                if (len == -1) {
                    throw new IOException("Can't read " + name);
                }
                offset += len;
            }
            return arr;
        }
    }
   
    private static Object invokeMethod(String clazz, String method) 
    throws ClassNotFoundException, InvocationTargetException, 
    SecurityException, IllegalAccessException, IllegalArgumentException,
    InstantiationException {
        Method found = null;
        Class<?> c = Class.forName(clazz);
        for (Method m : c.getMethods()) {
            if (m.getName().equals(method)) {
                found = m;
            }
        }
        Object res;
        if (found != null) {
            try {
                if ((found.getModifiers() & Modifier.STATIC) != 0) {
                    res = found.invoke(null);
                } else {
                    res = found.invoke(c.newInstance());
                }
            } catch (Throwable ex) {
                res = ex.getClass().getName() + ":" + ex.getMessage();
            }
        } else {
            res = "Can't find method " + method + " in " + clazz;
        }
        return res;
    }

    @JavaScriptBody(args = {}, body = "vm.desiredAssertionStatus = true;")
    private static void turnAssetionStatusOn() {
    }
    
    private static final class Case {
        private final Object data;

        private Case(Object data) {
            this.data = data;
        }
        
        public static Case parseData(String s) {
            return new Case(toJSON(s));
        }
        
        public String getMethodName() {
            return value("methodName", data);
        }

        public String getClassName() {
            return value("className", data);
        }
        
        public String getRequestId() {
            return value("request", data);
        }

        public String getHtmlFragment() {
            return value("html", data);
        }
        
        @JavaScriptBody(args = "s", body = "return eval('(' + s + ')');")
        private static native Object toJSON(String s);
        
        @JavaScriptBody(args = {"p", "d"}, body = 
              "var v = d[p];\n"
            + "if (typeof v === 'undefined') return null;\n"
            + "return v.toString();"
        )
        private static native String value(String p, Object d);
    }
}
