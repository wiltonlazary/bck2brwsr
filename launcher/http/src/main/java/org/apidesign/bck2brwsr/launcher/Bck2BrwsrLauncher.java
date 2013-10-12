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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.logging.Level;
import org.apidesign.vm4brwsr.Bck2Brwsr;

/**
 * Lightweight server to launch Bck2Brwsr applications and tests.
 * Supports execution in native browser as well as Java's internal 
 * execution engine.
 */
final class Bck2BrwsrLauncher extends BaseHTTPLauncher {
    
    public Bck2BrwsrLauncher(String cmd) {
        super(cmd);
    }
    
    @Override
    String harnessResource() {
        return "org/apidesign/bck2brwsr/launcher/harness.xhtml";
    }
    
    @Override
    void generateBck2BrwsrJS(StringBuilder sb, final Res loader) throws IOException {
        class R implements Bck2Brwsr.Resources {
            @Override
            public InputStream get(String resource) throws IOException {
                return loader.get(resource);
            }
        }
        String b2b = System.getProperty("bck2brwsr.js");
        if (b2b != null) {
            LOG.log(Level.INFO, "Serving bck2brwsr.js from {0}", b2b);
            try (Reader r = new InputStreamReader(new URL(b2b).openStream())) {
                char[] arr = new char[4096];
                for (;;) {
                   int len = r.read(arr);
                   if (len == -1) {
                       break;
                   }
                   sb.append(arr, 0, len);
                }
            }
        } else {
            LOG.log(Level.INFO, "Generating bck2brwsr.js from scratch", b2b);
            Bck2Brwsr.generate(sb, new R());
        }
        sb.append(
              "(function WrapperVM(global) {"
            + "  function ldCls(res) {\n"
            + "    var request = new XMLHttpRequest();\n"
            + "    request.open('GET', '/classes/' + res, false);\n"
            + "    request.send();\n"
            + "    if (request.status !== 200) return null;\n"
            + "    var arr = eval('(' + request.responseText + ')');\n"
            + "    return arr;\n"
            + "  }\n"
            + "  var prevvm = global.bck2brwsr;\n"
            + "  global.bck2brwsr = function() {\n"
            + "    var args = Array.prototype.slice.apply(arguments);\n"
            + "    args.unshift(ldCls);\n"
            + "    return prevvm.apply(null, args);\n"
            + "  };\n"
            + "})(this);\n"
        );
        LOG.log(Level.INFO, "Serving bck2brwsr.js", b2b);
    }

}
