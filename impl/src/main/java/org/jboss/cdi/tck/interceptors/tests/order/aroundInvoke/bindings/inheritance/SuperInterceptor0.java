package org.jboss.cdi.tck.interceptors.tests.order.aroundInvoke.bindings.inheritance;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.cdi.tck.util.ActionSequence;

public class SuperInterceptor0 {

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        ActionSequence.addAction(SuperInterceptor0.class.getSimpleName());
        return ctx.proceed();
    }
}
