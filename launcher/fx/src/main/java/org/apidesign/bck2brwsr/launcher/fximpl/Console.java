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
package org.apidesign.bck2brwsr.launcher.fximpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Console {
    public Console() {
    }
    
    private static Object getAttr(Object elem, String attr) {
        return InvokeJS.CObject.call("getAttr", elem, attr);
    }

    private static void setAttr(String id, String attr, Object value) {
        InvokeJS.CObject.call("setAttrId", id, attr, value);
    }
    private static void setAttr(Object id, String attr, Object value) {
        InvokeJS.CObject.call("setAttr", id, attr, value);
    }
    
    private static void closeWindow() {}

    private static Object textArea;
    private static Object statusArea;
    
    private static void log(String newText) {
        if (textArea == null) {
            return;
        }
        String attr = "value";
        setAttr(textArea, attr, getAttr(textArea, attr) + "\n" + newText);
        setAttr(textArea, "scrollTop", getAttr(textArea, "scrollHeight"));
    }
    
    private static void beginTest(Case c) {
        Object[] arr = new Object[2];
        beginTest(c.getClassName() + "." + c.getMethodName(), c, arr);
        textArea = arr[0];
        statusArea = arr[1];
    }
    
    private static void finishTest(Case c, Object res) {
        if ("null".equals(res)) {
            setAttr(statusArea, "innerHTML", "Success");
        } else {
            setAttr(statusArea, "innerHTML", "Result " + res);
        }
        statusArea = null;
        textArea = null;
    }

    private static final String BEGIN_TEST =  
        "var ul = window.document.getElementById('bck2brwsr.result');\n"
        + "var li = window.document.createElement('li');\n"
        + "var span = window.document.createElement('span');"
        + "span.innerHTML = test + ' - ';\n"
        + "var details = window.document.createElement('a');\n"
        + "details.innerHTML = 'Details';\n"
        + "details.href = '#';\n"
        + "var p = window.document.createElement('p');\n"
        + "var status = window.document.createElement('a');\n"
        + "status.innerHTML = 'running';"
        + "details.onclick = function() { li.appendChild(p); li.removeChild(details); status.innerHTML = 'Run Again'; status.href = '#'; };\n"
        + "status.onclick = function() { c.again(arr); }\n"
        + "var pre = window.document.createElement('textarea');\n"
        + "pre.cols = 100;"
        + "pre.rows = 10;"
        + "li.appendChild(span);\n"
        + "li.appendChild(status);\n"
        + "var span = window.document.createElement('span');"
        + "span.innerHTML = ' ';\n"
        + "li.appendChild(span);\n"
        + "li.appendChild(details);\n"
        + "p.appendChild(pre);\n"
        + "ul.appendChild(li);\n"
        + "arr[0] = pre;\n"
        + "arr[1] = status;\n";
        
    private static void beginTest(String test, Case c, Object[] arr) {
        InvokeJS.CObject.call("beginTest", test, c, arr);
    }
    
    private static final String LOAD_TEXT = 
          "var request = new XMLHttpRequest();\n"
        + "request.open('GET', url, true);\n"
        + "request.setRequestHeader('Content-Type', 'text/plain; charset=utf-8');\n"
        + "request.onreadystatechange = function() {\n"
        + "  if (this.readyState!==4) return;\n"
        + " try {"
        + "  arr[0] = this.responseText;\n"
        + "  callback.run__V();\n"
        + " } catch (e) { alert(e); }"
        + "};"
        + "request.send();";
    private static void loadText(String url, Runnable callback, String[] arr) throws IOException {
        InvokeJS.CObject.call("loadText", url, new Run(callback), arr);
    }
    
    public static void runHarness(String url) throws IOException {
        new Console().harness(url);
    }
    
    public void harness(String url) throws IOException {
        log("Connecting to " + url);
        Request r = new Request(url);
    }
    
    private static class Request implements Runnable {
        private final String[] arr = { null };
        private final String url;
        private Case c;
        private int retries;

        private Request(String url) throws IOException {
            this.url = url;
            loadText(url, this, arr);
        }
        private Request(String url, String u) throws IOException {
            this.url = url;
            loadText(u, this, arr);
        }
        
        @Override
        public void run() {
            try {
                if (c == null) {
                    String data = arr[0];

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

                    c = Case.parseData(data);
                    beginTest(c);
                    log("Got \"" + data + "\"");
                } else {
                    log("Processing \"" + arr[0] + "\" for " + retries + " time");
                }
                Object result = retries++ >= 10 ? "java.lang.InterruptedException:timeout" : c.runTest();
                finishTest(c, result);
                
                String u = url + "?request=" + c.getRequestId() + "&result=" + result;
                new Request(url, u);
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    log("Re-scheduling in 100ms");
                    schedule(this, 100);
                    return;
                }
                log(ex.getClass().getName() + ":" + ex.getMessage());
            }
        }
    }
    
    private static String encodeURL(String r) throws UnsupportedEncodingException {
        final String SPECIAL = "%$&+,/:;=?@";
        StringBuilder sb = new StringBuilder();
        byte[] utf8 = r.getBytes("UTF-8");
        for (int i = 0; i < utf8.length; i++) {
            int ch = utf8[i] & 0xff;
            if (ch < 32 || ch > 127 || SPECIAL.indexOf(ch) >= 0) {
                final String numbers = "0" + Integer.toHexString(ch);
                sb.append("%").append(numbers.substring(numbers.length() - 2));
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
    
    static String invoke(String clazz, String method) throws 
    ClassNotFoundException, InvocationTargetException, IllegalAccessException, 
    InstantiationException, InterruptedException {
        final Object r = new Case(null).invokeMethod(clazz, method);
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
   
    private static void turnAssetionStatusOn() {
    }

    private static Object schedule(Runnable r, int time) {
        return InvokeJS.CObject.call("schedule", new Run(r), time);
    }
    
    private static final class Case {
        private final Object data;
        private Object inst;

        private Case(Object data) {
            this.data = data;
        }
        
        public static Case parseData(String s) {
            return new Case(toJSON(s));
        }
        
        public String getMethodName() {
            return (String) value("methodName", data);
        }

        public String getClassName() {
            return (String) value("className", data);
        }
        
        public int getRequestId() {
            Object v = value("request", data);
            if (v instanceof Number) {
                return ((Number)v).intValue();
            }
            return Integer.parseInt(v.toString());
        }

        public String getHtmlFragment() {
            return (String) value("html", data);
        }
        
        void again(Object[] arr) {
            try {
                textArea = arr[0];
                statusArea = arr[1];
                setAttr(textArea, "value", "");
                runTest();
            } catch (Exception ex) {
                log(ex.getClass().getName() + ":" + ex.getMessage());
            }
        }

        private Object runTest() throws IllegalAccessException, 
        IllegalArgumentException, ClassNotFoundException, UnsupportedEncodingException, 
        InvocationTargetException, InstantiationException, InterruptedException {
            if (this.getHtmlFragment() != null) {
                setAttr("bck2brwsr.fragment", "innerHTML", this.getHtmlFragment());
            }
            log("Invoking " + this.getClassName() + '.' + this.getMethodName() + " as request: " + this.getRequestId());
            Object result = invokeMethod(this.getClassName(), this.getMethodName());
            setAttr("bck2brwsr.fragment", "innerHTML", "");
            log("Result: " + result);
            result = encodeURL("" + result);
            log("Sending back: ...?request=" + this.getRequestId() + "&result=" + result);
            return result;
        }

        private Object invokeMethod(String clazz, String method)
        throws ClassNotFoundException, InvocationTargetException,
        InterruptedException, IllegalAccessException, IllegalArgumentException,
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
                        if (inst == null) {
                            inst = c.newInstance();
                        }
                        res = found.invoke(inst);
                    }
                } catch (Throwable ex) {
                    if (ex instanceof InvocationTargetException) {
                        ex = ((InvocationTargetException) ex).getTargetException();
                    }
                    if (ex instanceof InterruptedException) {
                        throw (InterruptedException)ex;
                    }
                    res = ex.getClass().getName() + ":" + ex.getMessage();
                }
            } else {
                res = "Can't find method " + method + " in " + clazz;
            }
            return res;
        }
        
        private static Object toJSON(String s) {
            return InvokeJS.CObject.call("toJSON", s);
        }
        
        private static Object value(String p, Object d) {
            return ((JSObject)d).getMember(p);
        }
    }
    
    private static String safe(String txt) {
        return "try {" + txt + "} catch (err) { alert(err); }";
    }
    
    static {
        turnAssetionStatusOn();
    }
    
    private static final class InvokeJS {
        static final JSObject CObject = initJS();

        private static JSObject initJS() {
            WebEngine web = (WebEngine) System.getProperties().get("webEngine");
            return (JSObject) web.executeScript("(function() {"
                + "var CObject = {};"

                + "CObject.getAttr = function(elem, attr) { return elem[attr].toString(); };"

                + "CObject.setAttrId = function(id, attr, value) { window.document.getElementById(id)[attr] = value; };"
                + "CObject.setAttr = function(elem, attr, value) { elem[attr] = value; };"

                + "CObject.beginTest = function(test, c, arr) {" + safe(BEGIN_TEST) + "};"

                + "CObject.loadText = function(url, callback, arr) {" + safe(LOAD_TEXT.replace("run__V", "run")) + "};"

                + "CObject.schedule = function(r, time) { return window.setTimeout(function() { r.run(); }, time); };"

                + "CObject.toJSON = function(s) { return eval('(' + s + ')'); };"

                + "return CObject;"
            + "})(this)");
        }
    }
    
}
