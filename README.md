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

5) Verify that authentication works with the following command (from the console):

```
curl -H "Content-Type: application/json" -X POST -d '{"username":"xyz","password":"password"}' http://localhost:8080/user/login
```

6) Implement the API for the TODO object. In order to do that you need to follow the next steps:

* Create a model class for the TODO object inside the models package.
* Create a *TodoService* and *TodoServiceImpl* that has the following methods and the corresponding implementations:

    ``` Java
     List<Todo> getTodoList();
     Todo addTodo( Todo todo );
    ```
* Make sure you use correctly the annotations [@Autowired](https://stackoverflow.com/questions/19414734/understanding-spring-autowired-usage) and [@Service](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/stereotype/Service.html) in order to do the proper dependencies injection configuration.

7) Create the *TodoController* to handle the API request that will handle the TODO logic and annotate the class with the following annotations:

    ````
    @RestController
    @RequestMapping( "api" )
    ````


### Part 2: Consume API from AngularJS project

1. Create a folder with the name "common" inside the app folder
2. Create a folder with the name "config" inside the common folder to hold the initial API configuration
3. Create the following configuration files:

* Create the config interface, `config.interafce.ts`
```typescript
export interface IConfig {
  apiURL: string;
}
```

* Create the initial config injection token, `initial-config.ts`
```typescript
import { InjectionToken } from '@angular/core';
import { IConfig } from './config.interface';

export let INITIAL_CONFIG = new InjectionToken<IConfig>('app.config');
```

* Create the app configuration service that will be injected in other app services, `app-configuration.service.ts`
```typescript
import { Injectable, Inject } from '@angular/core';
import { Http } from '@angular/http';

import { IConfig } from './config.interface';
import { INITIAL_CONFIG } from './initial-config';

@Injectable()
export class AppConfiguration {
  private config: IConfig;

  constructor( @Inject(INITIAL_CONFIG) initialConfig: IConfig) {
    this.config = initialConfig;
  }

  get apiURL(): String {
    return this.config && this.config.apiURL;
  }
}
```

* inject the app configuration service in the main module `app.module.ts` and initialize the configuration
```typescript
...
import { AppConfiguration } from './common/config/app-configuration.service';
import { INITIAL_CONFIG } from './common/config/initial-config';
...
providers: [
    {
      provide: INITIAL_CONFIG,
      useValue: {
        apiURL: 'http://localhost:8080'
      }
    },
    TodoService,
    AppConfiguration
  ],
...
```

4. Create an AppData service `app/common/app-data.service.ts` to store info locally and add the service as a provider in the main module
```typescript
import { Injectable } from '@angular/core';

@Injectable()
export class AppDataService {
  private _accessToken: string | null = null;

  public set accessToken(accessToken: string) {
    this._accessToken = accessToken;
    localStorage.setItem('AT', accessToken);
  }

  public get accessToken(): string {
    if (!this._accessToken) {
      this._accessToken = localStorage.getItem('AT');
    }
    return this._accessToken;
  }

  constructor() { }

  public removeAccessToken() {
    this._accessToken = null;
    localStorage.removeItem('AT');
  }
}
```

5. Create an AuthService service `app/common/auth.service.ts` that will manage the local session info, add the service as a provider in the main module
```typescript
import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Router, CanActivate } from '@angular/router';

import { AppConfiguration } from '../common/config/app-configuration.service';
import { AppDataService } from '../common/app-data.service';

@Injectable()
export class AuthService implements CanActivate {
  constructor(public router: Router, public appData: AppDataService) { }

  public get accessToken(): string {
    return this.appData.accessToken;
  }

  public set accessToken(accessToken: string) {
    this.appData.accessToken = accessToken;
  }

  public isLoggedIn(): boolean {
    return this.appData.accessToken != null && this.appData.accessToken !== undefined;
  }

  public signOut() {
    this.appData.removeAccessToken();
    this.router.navigate([''])
  }

  canActivate() {
    if (!this.isLoggedIn()) {
      this.router.navigate(['']);
      return false;
    }
    return true;
  }
}
```

6. Inject Http module in the main app module
```typescript
import { HttpModule } from '@angular/http';
...
...
  imports: [
    ...
    HttpModule
  ],
  ...
```

7. Create an APIService service `app/common/api.service.ts` to interact with the API, add the service as a provider in the main module
```typescript
import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';

import { AppConfiguration } from '../common/config/app-configuration.service';
import { AuthService } from '../common/auth.service';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';

@Injectable()
export class APIService {
  constructor(
    public config: AppConfiguration,
    public authService: AuthService,
    public http: Http
  ) { }

  post(url: string, body: any, options?: any): Observable<any> {
    return this.http
      .post(`${this.config.apiURL}/${url}`, body, this.getRequestOptions(options))
      .map(this.extractData)
      .catch(this.handleError);
  }

  private getRequestOptions(options?: any) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    const innerOptions = new RequestOptions({ headers });

    if (!options || options.credentials === undefined || options.credentials === true) {
      headers.append('Authorization', 'Bearer ' + this.authService.accessToken);
    }

    return innerOptions;
  }

  private extractData(res: Response) {
    return res.json();
  }

  private handleError(error: Response | any) {
    let errObj: any;

    if (error instanceof Response) {
      const body = error.json();
      errObj = body;
    } else {
      errObj = error.message ? { message: error.message } : { message: error };
    }

    return Observable.throw(errObj);
  }
}
```

8. Create the UsersService `services/users.service.ts` that extends from the APIService to login and logout users, add the service as a provider in the main module( make sure you add the imports needed)
```typescript
...
@Injectable()
export class UsersService extends APIService {
  constructor(
    public config: AppConfiguration,
    public authService: AuthService,
    public http: Http
  ) {
    super(config, authService, http);
  }

  login(username: string, password: string) {
    return this.post('user/login', { username, password }, { credentials: false }).map(loginResponse => {
      if (loginResponse) {
        this.authService.accessToken = loginResponse.accessToken;
      }
    });
  }
}
```

9. Create the signIn view `pages/sign-in/sign-in-page.component.ts`, `pages/sign-in/sign-in-page.component.html` with the sign in fields using the angular generator in the `pages` folder and inject the UsersService to be able to login the user, add the page to the main module
```html
<div class="container">
  <h2>Sign In</h2>
  <form [formGroup]="signInForm" (ngSubmit)="doLogin()" novalidate>
    <div class="form-group">
      <label for="description">Username</label>
      <input type="text" class="form-control" id="description" formControlName="username" required>
    </div>

    <div class="form-group">
      <label for="priority">Password</label>
      <input type="password" class="form-control" id="alterEgo" formControlName="password">
    </div>

    <button type="submit" class="btn btn-success" [disabled]="!signInForm.valid">Sign In</button>

    <p class="text-danger mt-1" *ngIf="loginError">{{loginError}}</p>

  </form>
</div>
```
```typescript
...
  doLogin() {
    this.usersService.login(
      this.signInForm.get('username').value,
      this.signInForm.get('password').value).subscribe(loginResponse => {
        this.router.navigate(['tasks']);
      }, error => {
        this.loginError = 'Error Signing in: ' + (error && error.message ? error.message : '');
      })
  }
```

10. Restrict access to logged in user to some views on the app

* Import the Auth service into the main app module and add them to the routes that have to be protected from public access, also adjust the proper routing to load the login form by default
```typescript
...
import { AuthService } from './common/auth.service';
...
const ROUTES = [
  { path: '', component: SignInPageComponent },
  { path: 'home', component: HomePageComponent },
  {
    path: 'tasks', component: TaskListPageComponent,
    canActivate: [AuthService],
  },
  {
    path: 'edit', component: TaskEditPageComponent,
    canActivate: [AuthService],
  },
  {
    path: '**', component: PageNotFoundComponent
  }
]
...
```

12. Adjust the app component `app.component.ts` to load tasks page if the user is already logged in
```typescript
...
  constructor(
    public authService: AuthService,
    public router: Router
  ) {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/']);
    }
  }

  isLoggedIn() {
    return this.authService.isLoggedIn();
  }

  signOut() {
    this.authService.signOut();
  }
...
```

13. Add a logout option on the main menu `app.component.html`
```html
...
<li *ngIf="isLoggedIn()" class="nav-item">
  <a href="#" class="nav-link" (click)="signOut()">(Sign Out)</a>
</li>
...
```

14. Hide menu options if user is not logged in using the following directive with the `isLoggedIn` method
```typescript
*ngIf="!isLoggedIn()"
```

### Consume TODO API


1. Implement the get method in the APIService `api.service.ts`
```typescript
...
  get(url: string, options?: any): Observable<any> {
    return this.http
      .get(`${this.config.apiURL}/${url}`, this.getRequestOptions(options))
      .map(this.extractData)
      .catch(this.handleError);
  }
...
```

2. Integrate the task list service `app/services/todo.service.ts` with the back end, extend the task service from APIService and adjust the list to return an asyncrhonous observable
```typescript
...
import { APIService } from '../common/api.service';

@Injectable()
export class TodoService extends APIService {
  private resourceUrl = 'api/todo';
...
  list(): Observable<Todo[]> {
    return this.get(this.resourceUrl);
  }
...
```
3.Adjust the *task-list-page.component.ts* to suscribe to the server response:

 ```typescript
... 
  ngOnInit() {
     this.todoService.list().subscribe(todosResponse=>{
       this.todos = todosResponse;
     })
   }
...   
```
4. Investigate about asynchronous observable concept that Angular use (Reactive Programming). 
Once you understand implement the create method on the *todo.service.ts* to make a call to the *post* 
function created before on the *api.service.ts* to send the TODO object to the server API.

5. Adjust the *task-edit-page.component.ts* file to subscribe to the POST request observer when submitting the form
```typescript
...
 onSubmit() {
    this.todoService.create(
      this.todoForm.get('description').value,
      this.todoForm.get('priority').value,
      Boolean(this.todoForm.get('completed').value)
    ).subscribe(serverResponse=>{
        this.router.navigate(['/tasks']);
    }, error=>{
      console.log(error);
    });
  }
...  
```