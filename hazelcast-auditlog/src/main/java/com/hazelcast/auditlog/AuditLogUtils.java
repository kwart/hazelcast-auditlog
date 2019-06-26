package com.hazelcast.auditlog;

import java.lang.reflect.Proxy;
import java.util.logging.Logger;

public class AuditLogUtils {

    public static final String GENERATED_CLASS_NAME_SUFFIX = "$Impl";
    public static final String AUDITLOG_CATEGORY = "hazelcast.auditlog";

    public static <T> T getLogger(Class<T> msgInterface) {
        try {
            return (T) Class.forName(msgInterface.getName() + GENERATED_CLASS_NAME_SUFFIX).newInstance();
        } catch (Exception | NoClassDefFoundError e) {
            Logger.getLogger(AUDITLOG_CATEGORY).info("Auditlog is disabled: " + e);
            return (T) Proxy.newProxyInstance(
                    AuditLogUtils.class.getClassLoader(), 
                    new Class[] { msgInterface }, 
                    (proxy, method, methodArgs) -> null);
        }
    }

}
