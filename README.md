# spring_security_gamma
has and salted hash no longer enough, now use "adaptative one-way function" that use a work factor that has to be tuned to take 1s on your system
user are encouraged to exchange a longterm credentials with short term (oauth token) than can be validated quickly
utiliser : DelegatingPasswordEncoder

Method based authorization vs request based authorization
CSRF protection can only work if safe HTTP methods are read only (doesnt change application state)
