package org.jboss.cdi.tck.interceptors.tests.order.aroundInvoke.bindings.inheritance;

import org.jboss.cdi.tck.util.ActionSequence;

public class MyBean {

    @Binding
    public void myMethod() {
        ActionSequence.addAction(MyBean.class.getSimpleName());
    }
}
