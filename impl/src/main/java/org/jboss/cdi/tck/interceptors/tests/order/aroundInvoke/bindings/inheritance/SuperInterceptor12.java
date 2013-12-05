package org.jboss.cdi.tck.interceptors.tests.order.aroundInvoke.bindings.inheritance;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.cdi.tck.util.ActionSequence;

@Priority(1012)
@Binding
@Interceptor
public class SuperInterceptor12 {

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        ActionSequence.addAction(SuperInterceptor12.class.getSimpleName());
        return ctx.proceed();
    }
}
