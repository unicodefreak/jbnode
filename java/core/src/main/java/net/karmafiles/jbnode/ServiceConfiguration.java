package net.karmafiles.jbnode;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ilya Brodotsky
 * Date: 01.09.2010
 * Time: 22:07:56
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class ServiceConfiguration {
    private Map<String, Object> services = new HashMap();
    private Map<String, Map<String, Method>> methodNameMap = new HashMap();

    private static final Logger logger = Logger.getLogger(ServiceConfiguration.class.getName());
    

    protected Object getService(String serviceName) {
        return services.get(serviceName);
    }

    protected Method getServiceMethod(String serviceName, String methodName) {
        Map<String, Method> objectMethods = methodNameMap.get(serviceName);
        if(objectMethods != null) {
            return objectMethods.get(methodName);
        }
        return null;
    }

    public void configure(String name, Object service) throws JBNodeException {
        logger.log(Level.INFO, "Configuring service '" + name + "'");

        synchronized (services) {
            if (services.containsKey(name)) {
                throw new JBNodeException("Service '" + name + "' already configured.");
            }

            services.put(name, service);

            if (name.contains(".")
                    || name.contains("#")
                    || name.length() > Constants.MAX_HEADER_PART_LENGTH) {
                throw new JBNodeException("Service name (" + name + ") can't contain '" + "." +
                        "' or '" + "#" + "' and it's length must be less than 63 characters");
            }

            for (Method method : service.getClass().getDeclaredMethods()) {
                // skip CGLIB etc proxies  
                if(method.getName().contains("$")) {
                    continue;
                }                               
                
                Class<?>[] paramTypes = method.getParameterTypes();
                
                // parameter and return type must be byte[] 
                Class<?> matchType = (new byte[]{}).getClass().getComponentType();

                // only one parameter is accepted 
                if (paramTypes.length == 1) {
                    Class<?> paramType = paramTypes[0];

                    if (paramType.isArray() && paramType.getComponentType().isAssignableFrom(matchType)) {

                        Class<?> returnType = method.getReturnType();
                        if (returnType.isArray() && returnType.getComponentType().isAssignableFrom(matchType)) {
                            if (method.getName().length() > Constants.MAX_HEADER_PART_LENGTH) {
                                logger.log(Level.INFO, "Method name (" + method.getName() +
                                        ") length must be less than " + Constants.MAX_HEADER_PART_LENGTH + " charater(s). Skipping.");
                            }

                            Map<String, Method> objectMethods = methodNameMap.get(name);
                            if (objectMethods == null) {
                                objectMethods = new HashMap();
                                methodNameMap.put(name, objectMethods);
                            }
                            objectMethods.put(method.getName(), method);
                            logger.log(Level.INFO, "Configuring method '" + name + "." + method.getName() + "'");
                        }
                    }
                }
            }
        }
    }

    
}
