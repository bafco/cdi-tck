/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.cdi.tck.tests.extensions.interceptors.custom;

import java.io.Serializable;

import javax.interceptor.InvocationContext;

/**
 * This is not an interceptor (it misses required annotations). The CustomInterceptor delegates to instances of this class.
 * 
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 * 
 */
@SuppressWarnings("serial")
public class FooInterceptor implements Serializable {

    private static boolean invoked = false;

    Object intercept(InvocationContext ctx) throws Exception {
        invoked = true;
        return ctx.proceed();
    }

    public static boolean isInvoked() {
        return invoked;
    }

    public static void reset() {
        invoked = false;
    }
}