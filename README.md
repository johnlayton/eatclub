### EatClub Offers API

[![Build and Test](https://github.com/johnlayton/eatclub/actions/workflows/main.yml/badge.svg)](https://github.com/johnlayton/eatclub/actions/workflows/main.yml)

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

#### Persistence

```mermaid
  erDiagram
    Cuisine ||--o{ Restaurant_Cuisine : "is associated with"
    Restaurant ||--o{ Restaurant_Cuisine : "is categorised by"
    Restaurant ||--o{ Deal :  "has many"
      
    Restaurant {
        string objectId PK
        string name
        string address1
        string suburb
        time open
        time close
    }

    Cuisine {
        string objectId PK
        string name UK
    }
    
    Deal {
        string objectId PK
        string restaurantId FK
        int discount
        boolean dineIn
        boolean lightning
        time open
        time close
        int qtyLeft
    }
    
    Restaurant_Cuisine {
        string restaurantId FK
        string cuisineId FK
    }
```

Whilst NoSQl databases are often used for such applications, this implementation would use the postgresql relational database for simplicity.
The data is loaded from the provided JSON file into the database at application startup (or periodically from the remote repository).

- Restaurant cuisine is effectively tagging information,
  tags can be added or removed from restaurants and filtering applied by cusine type,
  tags should be managed in a separate table allowing for deduplication
  so a many-to-many relationship is used between Restaurant and Cuisine.

- Deals are associated with a single restaurant,
  and have time windows when they are valid,
  as well as quantity available.
  Deals will have a foreign key relationship to Restaurant.

- Indexes would be created on frequently queried fields such as deal open/close times,
  restaurant open/close times, and cuisine names to optimize query performance.