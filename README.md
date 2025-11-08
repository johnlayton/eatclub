### EatClub Offers API

#### Run:

```shell

./mvnw spring-boot:run

```

#### Invoke:

```shell

curl http://localhost:8080/offers?time=11:00

```

```shell

curl http://localhost:8080/peak

```

#### Download Challenge Data:

```shell

wget https://eccdn.com.au/misc/challengedata.json --directory-prefix=src/test/resource

```

```shell

wget https://eccdn.com.au/misc/challengedata.json --directory-prefix=src/main/resource

```