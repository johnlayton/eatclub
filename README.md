### EatClub Offers API

#### Run:

```shell

./mvnw spring-boot:run

```

#### Invoke:

```shell

curl http://localhost:8080/deals?time=11:00 | jq -r .

```
```shell

curl http://localhost:8080/deals?time=13:00 | jq -r .

```
```shell

curl http://localhost:8080/deals?time=17:00  | jq -r .

```

```shell

curl http://localhost:8080/deals?time=21:00  | jq -r .

```

```shell

curl http://localhost:8080/deals?time=23:30  | jq -r .

```

```shell

curl http://localhost:8080/peak  | jq -r .

```

#### Download Challenge Data:

```shell

wget https://eccdn.com.au/misc/challengedata.json --directory-prefix=src/test/resources

```

```shell

wget https://eccdn.com.au/misc/challengedata.json --directory-prefix=src/main/resources

```