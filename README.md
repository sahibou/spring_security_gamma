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
