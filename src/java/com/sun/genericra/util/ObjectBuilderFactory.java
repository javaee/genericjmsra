/**
 * Copyright 2004-2005 Sun Microsystems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.sun.genericra.util;

import javax.naming.*;
import javax.resource.ResourceException;
import java.security.*;
import java.util.logging.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.lang.reflect.*;


/**
 * Build an object based on classname or look up
 * based on JNDI name.
 *
 * @author Binod P.G
 */
public class ObjectBuilderFactory {

    private static boolean debug = false;

    private static Logger logger;
    static {
        logger = LogUtils.getLogger();
    }

    public ObjectBuilder createUsingClassName(String name) {
        return  new ClassObjectBuilder(name);      
    }

    public ObjectBuilder createUsingJndiName(String jndiName,
                                             String jndiProps) {
        return new JndiObjectBuilder(jndiName, jndiProps);
    }

    class ClassObjectBuilder extends ObjectBuilder {
        private String className = null;

        public ClassObjectBuilder(String className) {
            this.className = className;
        }

        public Object createObject() throws ResourceException{
            try {
                 return Class.forName(className).newInstance();
            } catch (ClassNotFoundException e) {
                 throw ExceptionUtils.newInvalidPropertyException(e);
            } catch (InstantiationException ie) {
                 throw ExceptionUtils.newInvalidPropertyException(ie);
            } catch (IllegalAccessException iae) {
                 throw ExceptionUtils.newSecurityException(iae);
            }
        }
    }

    class JndiObjectBuilder extends ObjectBuilder {
        private String jndiName = null;
        private String jndiProps = null;

        JndiObjectBuilder(String jndiName, String jndiProps) {
            this.jndiName = jndiName;
            this.jndiProps = jndiProps;
        }

        public Object createObject() throws ResourceException {
            try {
                Hashtable props = parseToProperties(this.jndiProps);
                InitialContext ic = new InitialContext(props);
                debug("Looking the JNDI name :" + this.jndiName);
                return ic.lookup(this.jndiName);
            } catch (Exception e) {
                 throw ExceptionUtils.newInvalidPropertyException(e);
            }
        }
    }

    void debug(String str) {
        logger.log(Level.FINEST, str);
    }

}
