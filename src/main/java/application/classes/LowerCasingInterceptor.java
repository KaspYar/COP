package application.classes;

import framework.core.Interceptor;

public class LowerCasingInterceptor implements Interceptor {

    @Override
    public String interceptOutputString(String interceptedString) {        
        return interceptedString.toLowerCase();
    }

}
