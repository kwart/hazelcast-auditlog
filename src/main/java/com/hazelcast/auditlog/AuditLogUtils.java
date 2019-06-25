package com.hazelcast.auditlog;

public class AuditLogUtils {

    public static final String GENERATED_CLASS_NAME_SUFFIX = "$Impl";

    public static <T> T getLogger(Class<T> msgInterface) {
        try {
            return (T) Class.forName(msgInterface.getName()+GENERATED_CLASS_NAME_SUFFIX).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("Auditlog initialization failed", e);
        }
    }

}
