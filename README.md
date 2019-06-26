# Hazelcast Audit Log Framework POC

Small annotation based audit logging framework. It supports unique message codes with project related prefix.

Underlying logging technology is Apache Log4J 2 in this POC.

## Usage

Clone and build the framework:

```bash
git clone https://github.com/kwart/hazelcast-auditlog.git
cd hazelcast-auditlog
mvn clean install
```

Add Maven dependencies to your project: 

```xml
<dependencies>
    <dependency>
        <groupId>com.hazelcast</groupId>
        <artifactId>hazelcast-auditlog</artifactId>
        <version>0.1-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.hazelcast</groupId>
        <artifactId>hazelcast-auditlog-annotations</artifactId>
        <version>0.1-SNAPSHOT</version>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-api</artifactId>
        <version>2.11.2</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.11.2</version>
    </dependency>
</dependencies>
```

Add an interface with auditable events to your project:

```java
import com.hazelcast.auditlog.AuditLogUtils;
import com.hazelcast.auditlog.annotations.AuditMessages;
import com.hazelcast.auditlog.annotations.Message;

@AuditMessages(prefix="MC")
public interface ManCenterAuditLogger {

    ManCenterAuditLogger LOGGER = AuditLogUtils.getLogger(ManCenterAuditLogger.class);
    
    @Message(value="User %s has logged in from address %s", code=1)
    public void userLoggedIn(String name, String address);
    
    @Message(value="User %s has logged out", code=2)
    public void userLoggedOut(String name);
}
```

Use the `LOGGER` constant from the interface within your business logic:

```java
ManCenterAuditLogger.LOGGER.userLoggedIn("admin", "192.168.1.5");
// ...
ManCenterAuditLogger.LOGGER.userLoggedOut("admin");
```

Rebuild your project, run it and check if proper log message appears in the console:

```
09:59:29.346 [main] INFO  hazelcast.auditlog - MC-000001: User admin has logged in from address 192.168.1.5
09:59:29.350 [main] INFO  hazelcast.auditlog - MC-000002: User admin has logged out
```
