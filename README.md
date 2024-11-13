# spring_security_gamma
has and salted hash no longer enough, now use "adaptative one-way function" that use a work factor that has to be tuned to take 1s on your system
user are encouraged to exchange a longterm credentials with short term (oauth token) than can be validated quickly
utiliser : DelegatingPasswordEncoder

### CSRF protection
Method based authorization vs request based authorization
CSRF protection can only work if safe HTTP methods are read only (doesnt change application state) (GET HEAD OPTION)

### spring security Header used for security
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
No cache to prevent user to press back button and see sensitive information.
Set content type > otherwise content sniffing necessary, and XSS attack possible with polyglot code 

### spring data extension
SecurityEvaluationContextExtension can be used to have principal id in spring data to check user info ~?

### Multithreading
Security context (spring security) is managed on a thread level, so if you create a new thread its lost.
To not loose it use DelegatingSecurityContextRunnable to create a new thread.

--> https://docs.spring.io/spring-security/reference/servlet/getting-started.html
### Servlet apps
Default :
- requires authenticated user for ANY endpoint
- default password encoder BCrypt
- Content negociation

Activation : @EnableWebSecurity, publishes default SpringSecurity filterchain

###
SecurityContextHolder : storage of principal + credentials + auth info, wherever they come from
can be even populated manually
AuthenticationManager : Defines how SecurityFilter will perform authentication
ProviderManager is the most commonly used implementation of AuthenticationManager. ProviderManager delegates to a List of AuthenticationProvider instances. Each AuthenticationProvider has an opportunity to indicate that authentication should be successful, fail, or indicate it cannot make a decision and allow a downstream AuthenticationProvider to decide

### SecurityContextRepository
Used to associate the security context to subsequent requests
default : DelegatingSecurityContextRepository, which delegates to
HttpSessionSecurityContextRepository
RequestAttributeSecurityContextRepository

HttpSessionSecurityContextRepository : Associate SecurityContext to the http session
NullSecurityContextRepository : when association with http session isnt desirable (ex : oauth)
RequestAttributeSecurityContextRepository : saves it as a request attribute

### Authorization
@Bean
public SecurityFilterChain web(HttpSecurity http) throws Exception {
    http
    .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/endpoint").hasAuthority("USER")
                        .anyRequest().authenticated()
                        )
// ...
    return http.build();
}
test
@WithMockUser
@Test
void endpointWhenNotUserAuthorityThenForbidden() {
    this.mvc.perform(get("/endpoint"))
        .andExpect(status().isForbidden());
}

### method security
activate it in your application by annotating any @Configuration class with @EnableMethodSecurity
Then, you are immediately able to annotate any Spring-managed class or method with @PreAuthorize, @PostAuthorize, @PreFilter, and @PostFilter
@PreAuthorize("hasAuthority('permission:read')")
@PostAuthorize("returnObject.owner == authentication.name")


### Domain object security ACL (action control list)
When Autorization also needs to know about the actual domain object
spring security acl services : spring-security-acl-xxx.jar
Every domain object has an ACL tag attached to it that reccords who can and cannot use it.
A way to ensure a given principal is permitted to work with your objects before and/or after methods are called

Se base sur 4 tables
ACL_SID list of principals
ACL_CLASS domain object id (equiv class)
ACL_OBJECT_IDENTITY indidual domain object info (equiv instance)
ACL_ENTRY 

### Oauth2 https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html
Spring Security provides : Resource Server & client
Bean JwtDecoder bean to validate signatures and decode tokens
The following example configures a JwtDecoder bean using Spring Boot configuration properties:
```
spring:
    security:
        oauth2:
            resourceserver:
                jwt:
                    issuer-uri: https://my-auth-server.com
```
this is equivalent to
```
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer((oauth2) -> oauth2
				.jwt(Customizer.withDefaults())
			);
		return http.build();
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		return JwtDecoders.fromIssuerLocation("https://my-auth-server.com");
	}

}
```
OpenID Connect 1.0 provides a special token called the id_token which is designed to provide an OAuth2 Client 
with the ability to perform user identity verification and log user in

In addition to the above configuration, the application requires at least one ClientRegistration to be configured through the use of a ClientRegistrationRepository bean. The following example configures an InMemoryClientRegistrationRepository bean using Spring Boot configuration properties:
```
spring:
  security:
    oauth2:
      client:
        registration:
          my-oidc-client:
            provider: my-oidc-provider
            client-id: my-client-id
            client-secret: my-client-secret
            authorization-grant-type: authorization_code
            scope: openid,profile
          provider:
            my-oidc-provider:
            issuer-uri: https://my-oidc-provider.com
```
With the above configuration, the application now supports two additional endpoints:

The login endpoint (e.g. /oauth2/authorization/my-oidc-client) 
is used to initiate login and perform a redirect to the third party authorization server.

The redirection endpoint (e.g. /login/oauth2/code/my-oidc-client)
is used by the authorization server to redirect back to the client application, and will contain a code parameter used to obtain an id_token and/or access_token via the access token request.

registration exists within the authorization server and was created when the developer registered its application
and corresponding redirect urls.
  scope allows application to request limited acces to user data - https://www.oauth.com/oauth2-servers/scope/defining-scopes/
  often on functionality base, read write base, billable not billable

this doesnt provide a way to log user in (for now)

OAuth2AuthorizedClientManager for obtaining access tokens that can be used to access protected resources.
Spring Security registers a default OAuth2AuthorizedClientManager bean for you when one does not exist.

recommended way to use OAuth2AuthorizedClientManager is with spring-webflux ExchangeFilterFunction 
```
@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
		ServletOAuth2AuthorizedClientExchangeFilterFunction filter =
				new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
		return WebClient.builder()
				.apply(filter.oauth2Configuration())
				.build();
	}

}
```

In OAuth 2.0, the term “grant type” refers to the way an application gets an access token
jwt bearer grant type isnt oauth2 standard so its called an extension grant type.
since its not part of core oauth2 spec its disabled by default, to enable it :

after 6.2
```
	@Bean
	public OAuth2AuthorizedClientProvider jwtBearer() {
		return new JwtBearerOAuth2AuthorizedClientProvider();
	}
```

before 6.2 : publish this bean ourselves and ensure we re-enabled default grant types as well.
```
	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(
			ClientRegistrationRepository clientRegistrationRepository,
			OAuth2AuthorizedClientRepository authorizedClientRepository) {

		OAuth2AuthorizedClientProvider authorizedClientProvider =
			OAuth2AuthorizedClientProviderBuilder.builder()
				.authorizationCode()
				.refreshToken()
				.clientCredentials()
				.password()
				.provider(new JwtBearerOAuth2AuthorizedClientProvider())
				.build();

		DefaultOAuth2AuthorizedClientManager authorizedClientManager =
			new DefaultOAuth2AuthorizedClientManager(
				clientRegistrationRepository, authorizedClientRepository);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

		return authorizedClientManager;
	}
```

see documentation for how to provide the authorization server with additional parameters if it requires them

