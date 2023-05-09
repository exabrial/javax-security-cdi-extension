# javax-security-cdi-extension
A CDI Portable Extension for Java EE 8 that allows you to use `@RolesAllowed({"role-name"})` on CDI Beans and their Methods.

## TL;DR

Makes this work:

```
@ApplicationScoped
@RolesAllowed({"required-role"})
public class MyBusinessLogic {
 public void wahoo() {
  // only invokable by users with required-role
 }
}
```

If the person doesn't have the the required role, the `DefaultJavaXSecurityFailureHandler` class throws a `new WebApplicationException(Response.Status.UNAUTHORIZED)`. This behavior is customizable, see below.
 
## Motivation
A common mistake is that someone sees [javax.annotation.security](https://docs.oracle.com/javaee/7/api/javax/annotation/security/RolesAllowed.html) and applies it to a CDI bean, thinking since it belongs to a general javax package, and not a EJB package. Well it doesn't sadly, it only works for EJB (Stateless, Stateful, Singleton, and MDB).

## License

All files in this project are licensed Apache Source License 2.0. Please consider submitting any changes you make back to this project!

## Caveats

You must be in a servlet lifecycle. Said differently: you have to be handling an HTTP Request. If the bean is being called by another initiator like an MDB or Timer, you'll run into some problems (and you probably have a bug in your program too).

## Usage

Maven Coordinates:

```
<dependency>
 <groupId>com.github.exabrial</groupId>
 <artifactId>javax-security-cdi-extension</artifactId>
 <version>2.0.0</version>
 <scope>runtime</scope>
</dependency>
```

If you are customizing the behavior, your `scope` will need to be `compile`.

## Setup

You must have authentication setup in your webapp. 

Example `WEB-INF/web.xml` for Basic auth:

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app
 version="3.1"
 xmlns="http://xmlns.jcp.org/xml/ns/javaee"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd">
 <login-config>
  <auth-method>BASIC</auth-method>
 </login-config>
</web-app>
```

## Configuration/Customization

### Logging
All logs are kicked to Slf4j. You may optionally turn on logging by using any one of the implementations.

### Disabling

If you want to disable security (maybe for localhost development), you can disable it by creating a Boolean CDI Producer with the qualifier `@com.github.exabrial.cdi.javaxsecurity.SkipSecurity`:

Example of skipping security:

```
 @Produces
 @SkipSecurity
 public Boolean skipSecurity() {
  return Boolean.TRUE;
 }
```

### Customizing Authentication/Authorization Failure modes

You can implement the `com.github.exabrial.cdi.javaxsecurity.JavaXSecurityFailureHandler` interface and create custom behavior when things go wrong. Implement the interface, then mark it as an `@Alternative` and with `@Priority`.

Example:

```
@ApplicationScoped
@Alternative
@Priority
public class MyJavaXSecurityFailureHandler implements JavaXSecurityFailureHandler {
 @Override
 public void authenticationFailure() {
  // ...
 }
 
 @Override
 public void authorizationFailure(Class<? extends Object> targetClass, Method targetMethod, String roleName) {
  // ...
 }
}
```
