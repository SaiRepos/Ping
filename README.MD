Ping
===

Ping is a library that can help Spring applications to decide whether to execute certain methods or not based on Resource availability.
For eg. Ping can help service A to avoid call to service B in case service B is down and A can then optionally call a fallback method instead.

## Quick Start
 

Below example provides a quick introduction to get started with Ping Library

```java
@Configuration
@EnablePing
public class Application {

        @Bean("check")
        public PingInterceptor interceptor(){
             return new PingInterceptor() {
                 @Override
                 public boolean ping() {
                     // logic to determine whether the call to resource should be made or not
                     return true;
                 }
             };
        }
}

@Service
class Service {
    
    @Ping(interceptor = "check",fallback = "action")
    public void serve(){
        // do something
    }
    @Fallback("action")
    public void act(){
         // fallback action
     }
}

```

Above example uses bean named `check` of type `PingInterceptor` to decide whether to execute method `Service.serve()` or not in subsequent calls. As the `PingInterceptor.ping()` returns true in the example, the `Service.serve()` is called.
if `PingInterceptor.ping()` returns false,`Service.serve()` will not be executed and instead,`Service.serve()` is executed as fallback.

Note that the fallback methods should have same arguments as that of method for which it has been marked as fallback for.
## Building

Ping requires Maven and Java 8 (or greater) to build. To build, run the following maven command
```
$ mvn install
```

### Dependencies

Ping requires `aspectjrt` and `aspectjweaver` libraries to run in Spring boot applications. Include below dependencies to use Ping 

```xml
                <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</dependency>
		<dependency>
			<groupId>com.gsaikiran</groupId>
			<artifactId>ping</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>org.aspectj</groupId>
			<artifactId>aspectjrt</artifactId>
		</dependency>
		<dependency>
			<groupId>org.aspectj</groupId>
			<artifactId>aspectjweaver</artifactId>
		</dependency>
```

## Executor

By default , Ping uses `SimpleAsyncTaskExecutor` to run `PingInterceptor.ping()` asynchronously. To use a custom Executor, refer below example

```java
@Configuration
@EnablePing("threadPoolTaskExecutor")
public class Application {
    
    @Bean(name = "threadPoolTaskExecutor")
     public Executor threadPoolTaskExecutor() {
            return new ThreadPoolTaskExecutor();
      }
      
}
```

### Async calls

Ping executes `PingInterceptor.ping()` in async manner so that it does not block or add any latency to original call.
For eg. method `getStats` is annotated with `@Ping` having interceptor set to `StatsPingInterceptor` .Whenever the `getStats()` is invoked, Ping will execute `StatsPingInterceptor.ping()` asynchronously to decide whether to allow subsequent invocations of `getStats()` or not.

Note that as this check is done in async way, the `getStats()` is invoked atleast one time after `StatsPingInterceptor.ping()` return `false` for first time after it has been returning true or after app init, but not subsequently until `StatsPingInterceptor.ping()` returns `true`

### Use cases

One of the possible use cases is listed below

 * ##### Inter-service communications 
    * Consider a system having multiple microservices which are interconnected where service `A` calls `B` to get some data. If `B` is down due to planned maintenance, this can be marked in DB or some Key value store so that other services are aware of state of `B`. To avoid `A` calling `B` until `B` is up , `A` can use Ping where it implements `PingInterceptor` to get status of `B` from DB or Key value store.
       
    