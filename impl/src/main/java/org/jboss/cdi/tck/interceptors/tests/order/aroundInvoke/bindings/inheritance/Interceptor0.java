package org.jboss.cdi.tck.interceptors.tests.order.aroundInvoke.bindings.inheritance;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.cdi.tck.util.ActionSequence;

@Priority(1000)
@Binding
@Interceptor
public class Interceptor0 extends SuperInterceptor0 {

    @AroundInvoke
    public Object intercept0(InvocationContext ctx) throws Exception {
        ActionSequence.addAction(Interceptor0.class.getSimpleName());
        return ctx.proceed();
    }
}
