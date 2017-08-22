# spring-boot-secure-api

**Goals**

* Implement a secure API using JSON Web Token on a Spring Boot Project. 
* Implementation to consume the API from an Angular JS project.


### Part 1: Implement API


1) Include the following dependency on your project:


```
compile('io.jsonwebtoken:jjwt:0.6.0')
```

2) Go to class *JwtFilter* and uncomment the *doFilter* method implementation.

3) Go to the class *UserController* and uncomment the lines 60 and 61.

4) Run the project using the Gradle command *bootRun*.

5) Try that authentication works with the following command from the console:

```
curl -H "Content-Type: application/json" -X POST -d '{"username":"xyz","password":"password"}' http://localhost:8080/user/login
```

6) Implement the API for the TODO objects. In order to do that you need to follow the next steps:

* Create a model class for the TODO object inside the models package.
* Create a *TodoService* and *TodoServiceImpl* that has the following methods and the corresponding implementations:

    ``` Java
     List<Todo> getTodoList();
     Todo addTodo( Todo todo );
    ```
* Make sure you use correctly the annotations [@Autowired](https://stackoverflow.com/questions/19414734/understanding-spring-autowired-usage) and [@Service](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/stereotype/Service.html) in order to do the proper dependencies injection configuration.


### Part 2: Consume API from AngularJS project