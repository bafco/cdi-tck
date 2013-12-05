package org.jboss.cdi.tck.interceptors.tests.order.aroundInvoke.bindings.inheritance;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.cdi.tck.util.ActionSequence;

@Priority(1020)
@Interceptor
public class Interceptor2 extends SuperInterceptor12 {

    @AroundInvoke
    public Object intercept2(InvocationContext ctx) throws Exception {
        ActionSequence.addAction(Interceptor2.class.getSimpleName());
        return ctx.proceed();
    }
}
