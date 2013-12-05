package org.jboss.cdi.tck.interceptors.tests.order.aroundInvoke.bindings.inheritance;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.cdi.tck.util.ActionSequence;

@Priority(1010)
@Interceptor
public class Interceptor1 extends SuperInterceptor12 {

    @AroundInvoke
    public Object intercept1(InvocationContext ctx) throws Exception {
        ActionSequence.addAction(Interceptor1.class.getSimpleName());
        return ctx.proceed();
    }
}
