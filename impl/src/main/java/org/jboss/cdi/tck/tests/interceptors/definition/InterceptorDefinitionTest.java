/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.cdi.tck.tests.interceptors.definition;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.cdi.tck.AbstractTest;
import org.jboss.cdi.tck.shrinkwrap.WebArchiveBuilder;
import org.jboss.cdi.tck.util.HierarchyDiscovery;
import org.jboss.cdi.tck.util.ParameterizedTypeImpl;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecAssertions;
import org.jboss.test.audit.annotations.SpecVersion;
import org.testng.annotations.Test;

/**
 * Tests related to the definition of interceptors, but not necessarily their execution.
 * 
 * @author David Allen
 * @author Marius Bogoevici
 * @author Martin Kouba
 */
@SuppressWarnings("serial")
@SpecVersion(spec = "cdi", version = "20091101")
public class InterceptorDefinitionTest extends AbstractTest {

    private static final AnnotationLiteral<Transactional> TRANSACTIONAL_LITERAL = new AnnotationLiteral<Transactional>() {
    };

    private static final AnnotationLiteral<Secure> SECURE_LITERAL = new AnnotationLiteral<Secure>() {
    };

    private static final AnnotationLiteral<MissileBinding> MISSILE_LITERAL = new AnnotationLiteral<MissileBinding>() {
    };

    private static final AnnotationLiteral<Logged> LOGGED_LITERAL = new AnnotationLiteral<Logged>() {
    };

    private static final AnnotationLiteral<Atomic> ATOMIC_LITERAL = new AnnotationLiteral<Atomic>() {
    };

    @Deployment
    public static WebArchive createTestArchive() {
        return new WebArchiveBuilder()
                .withTestClassPackage(InterceptorDefinitionTest.class)
                .withBeansXml(
                        Descriptors
                                .create(BeansDescriptor.class)
                                .createInterceptors()
                                .clazz(AtomicInterceptor.class.getName(), MissileInterceptor.class.getName(),
                                        SecureInterceptor.class.getName(), TransactionalInterceptor.class.getName(),
                                        FileLogger.class.getName(), NetworkLogger.class.getName()).up()).build();
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = "11.1.2", id = "a") })
    public void testInterceptorsImplementInterceptorInterface() {
        boolean interfaceFound = false;
        for (Type type : getInterfacesImplemented(getTransactionalInterceptor().getClass())) {
            if (type instanceof ParameterizedTypeImpl && ((ParameterizedTypeImpl) type).getRawType().equals(Interceptor.class)) {
                interfaceFound = true;
                break;
            }
        }
        assertTrue(interfaceFound);
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = "11.1.2", id = "b") })
    public void testInterceptorBindingTypes() {
        Interceptor<?> interceptorBean = getTransactionalInterceptor();
        assertEquals(interceptorBean.getInterceptorBindings().size(), 1);
        assertTrue(interceptorBean.getInterceptorBindings().contains(TRANSACTIONAL_LITERAL));
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = "11.1.2", id = "c"), @SpecAssertion(section = "11.1.2", id = "e") })
    public void testInterceptionType() {
        Interceptor<?> interceptorBean = getTransactionalInterceptor();
        assertTrue(interceptorBean.intercepts(InterceptionType.AROUND_INVOKE));
        assertFalse(interceptorBean.intercepts(InterceptionType.POST_ACTIVATE));
        assertFalse(interceptorBean.intercepts(InterceptionType.POST_CONSTRUCT));
        assertFalse(interceptorBean.intercepts(InterceptionType.PRE_DESTROY));
        assertFalse(interceptorBean.intercepts(InterceptionType.PRE_PASSIVATE));
        assertFalse(interceptorBean.intercepts(InterceptionType.AROUND_TIMEOUT));
    }

    @Test
    @SpecAssertion(section = "11.1.2", id = "f")
    public void testInstanceOfInterceptorForEveryEnabledInterceptor() {
        List<AnnotationLiteral<?>> annotationLiterals = Arrays.<AnnotationLiteral<?>> asList(TRANSACTIONAL_LITERAL,
                SECURE_LITERAL, MISSILE_LITERAL, LOGGED_LITERAL);

        List<Class<?>> interceptorClasses = new ArrayList<Class<?>>(Arrays.<Class<?>> asList(AtomicInterceptor.class,
                MissileInterceptor.class, SecureInterceptor.class, TransactionalInterceptor.class, NetworkLogger.class,
                FileLogger.class, NotEnabledAtomicInterceptor.class));

        for (AnnotationLiteral<?> annotationLiteral : annotationLiterals) {
            List<Interceptor<?>> interceptors = getCurrentManager().resolveInterceptors(InterceptionType.AROUND_INVOKE,
                    annotationLiteral);
            for (Interceptor<?> interceptor : interceptors) {
                interceptorClasses.remove(interceptor.getBeanClass());
            }
        }

        List<Interceptor<?>> interceptors = getCurrentManager().resolveInterceptors(InterceptionType.AROUND_INVOKE,
                ATOMIC_LITERAL, MISSILE_LITERAL);
        for (Interceptor<?> interceptor : interceptors) {
            interceptorClasses.remove(interceptor.getBeanClass());
        }

        assertEquals(interceptorClasses.size(), 1);
        assertEquals(interceptorClasses.get(0), NotEnabledAtomicInterceptor.class);
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = "11.3.13", id = "a") })
    public void testResolveInterceptorsReturnsOrderedList() {

        List<Interceptor<?>> interceptors = getCurrentManager().resolveInterceptors(InterceptionType.AROUND_INVOKE,
                TRANSACTIONAL_LITERAL, SECURE_LITERAL);

        assertEquals(interceptors.size(), 2);
        assertEquals(interceptors.get(0).getInterceptorBindings().size(), 1);
        assertTrue(interceptors.get(0).getInterceptorBindings().contains(SECURE_LITERAL));
        assertEquals(interceptors.get(1).getInterceptorBindings().size(), 1);
        assertTrue(interceptors.get(1).getInterceptorBindings().contains(TRANSACTIONAL_LITERAL));
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    @SpecAssertions({ @SpecAssertion(section = "11.3.13", id = "b") })
    public void testSameBindingTypesToResolveInterceptorsFails() {
        Annotation transactionalBinding = new AnnotationLiteral<Transactional>() {
        };
        getCurrentManager().resolveInterceptors(InterceptionType.AROUND_INVOKE, transactionalBinding, transactionalBinding);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    @SpecAssertions({ @SpecAssertion(section = "11.3.13", id = "c") })
    public void testNoBindingTypesToResolveInterceptorsFails() {
        getCurrentManager().resolveInterceptors(InterceptionType.AROUND_INVOKE);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    @SpecAssertions({ @SpecAssertion(section = "11.3.13", id = "d") })
    public void testNonBindingTypeToResolveInterceptorsFails() {
        Annotation nonBinding = new AnnotationLiteral<NonBindingType>() {
        };
        getCurrentManager().resolveInterceptors(InterceptionType.AROUND_INVOKE, nonBinding);
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = "9.1", id = "a"), @SpecAssertion(section = "9.1", id = "b"),
            @SpecAssertion(section = "9.1", id = "c"), @SpecAssertion(section = "9.3", id = "a") })
    public void testInterceptorBindingAnnotation() {
        List<Interceptor<?>> interceptors = getLoggedInterceptors();
        assertTrue(interceptors.size() > 1);

        Interceptor<?> interceptorBean = interceptors.iterator().next();
        assertEquals(interceptorBean.getInterceptorBindings().size(), 1);
        assertTrue(interceptorBean.getInterceptorBindings().contains(LOGGED_LITERAL));

        Target target = (interceptorBean.getInterceptorBindings().iterator().next()).annotationType().getAnnotation(
                Target.class);
        List<ElementType> elements = Arrays.asList(target.value());
        assertTrue(elements.contains(ElementType.TYPE));
        assertTrue(elements.contains(ElementType.METHOD));
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = "9.1.2", id = "a"), @SpecAssertion(section = "9.1.2", id = "b"),
            @SpecAssertion(section = "2.7.1.2", id = "b") })
    public void testStereotypeInterceptorBindings() {
        FileLogger.intercepted = false;
        NetworkLogger.intercepted = false;

        SecureTransaction secureTransaction = getInstanceByType(SecureTransaction.class);
        secureTransaction.transact();

        assertTrue(FileLogger.intercepted);
        assertTrue(NetworkLogger.intercepted);
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = "9.1.1", id = "a"), @SpecAssertion(section = "9.1.1", id = "b") })
    public void testInterceptorBindingsCanDeclareOtherInterceptorBindings() {
        AtomicInterceptor.intercepted = false;
        MissileInterceptor.intercepted = false;

        AtomicFoo foo = getInstanceByType(AtomicFoo.class);
        foo.doAction();

        assertTrue(AtomicInterceptor.intercepted);
        assertTrue(MissileInterceptor.intercepted);
    }

    private Interceptor<?> getTransactionalInterceptor() {
        return getCurrentManager().resolveInterceptors(InterceptionType.AROUND_INVOKE, new AnnotationLiteral<Transactional>() {
        }).iterator().next();
    }

    private List<Interceptor<?>> getLoggedInterceptors() {
        return getCurrentManager().resolveInterceptors(InterceptionType.AROUND_INVOKE, new AnnotationLiteral<Logged>() {
        });
    }

    private Set<Type> getInterfacesImplemented(Class<?> clazz) {
        Set<Type> interfaces = new HashSet<Type>();
        interfaces.addAll(new HierarchyDiscovery(clazz).getFlattenedTypes());
        return interfaces;
    }

}