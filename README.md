# javax-security-cdi-extension
A CDI Portable Extension for Java EE 7 (and probably Java EE 8) that allows you to use `@RolesAllowed({"role-name"})` on CDI Beans and their Methods.

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

If the person doesn't have the the required role, the `` class throws a ``. This behavior is customizable, see below.
 
## Motivation
A common mistake is that someone sees [javax.annotation.security](https://docs.oracle.com/javaee/7/api/javax/annotation/security/RolesAllowed.html) and applies it to a CDI bean, thinking since it belongs to a general javax package, and not a EJB package. Well it doesn't sadly, it only works for EJB (Stateless, Stateful, Singleton, and MDB).

## Caveats

You must be in a servlet lifecycle. Said differently: you have to be handling an HTTP Request. If the bean is being called by another initiator like an MDB or Timer, you'll run into some problems (and you probably have a bug in your program too).

## Usage

Maven Coordinates:
```
```

## Setup

You must have authentication setup in your webapp. Example `WEB-INF/web.xml` for Basic auth:

```
```

## Configuration/Customization

### Logging
If you have a CDI `@Producer` for SLF4J loggers, the extension will produce useful warning logs on failures. _You probably want to know if a bunch of login failures are happening in your apps,_ so I suggest doing this.

Example CDI Logger Producer:
```
```

### Disabling

If you want to disable security (maybe for localhost development), you can disable it by creating a Boolean CDI Producer with the qualifer ``:

Example of skipping security:
```
```

### Customizing Authentication/Authorization Failure modes

You can implement the `` interface and create custom behavior when things go wrong. Implement the interface, then mark it as an `` and with ``

Example:

```
```
