package application.classes;

import framework.core.Interceptor;

public class CapitalizingInterceptor implements Interceptor {

    @Override
    public String interceptOutputString(String interceptedString) {
        return interceptedString.toUpperCase();
    }
}
