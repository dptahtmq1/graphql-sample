## GraphQL이란 무엇인가?

REST와 SOAP 사이에 있는 것. 꽤 가볍고, JSON protocol을 사용한다.(따라서 browser / mobile app에서 효율적임)

REST처럼 schema, attributes, operation and payload가 있고, SOAP과 다르게 schema는 진화하도록 디자인되었고, 수신하려는 데이터의 범위를 강력하게 제한한다.

```graphql
query {
    recentPosts(count: 10, offset: 0) {
        id
        title
        category
        author {
            id
            name
            thumbnail
        }
    }
}
```

- 10개의 최신 posts 요청
- 각 post에는 ID, title, category 요청
- 각 post의 author는 ID, name, thumbnail 정보를 리턴

### Schema

```graphql
type Post {
    id: ID!
    title: String!
    text: String!
    category: String
    author: Author!
}
 
type Author {
    id: ID!
    name: String!
    thumbnail: String
    posts: [Post]!
}
 
# The Root Query for the application
type Query {
    recentPosts(count: Int, offset: Int): [Post]!
}
 
# The Root Mutation for the application
type Mutation {
    writePost(title: String!, text: String!, category: String) : Post!
}
```

### 왜 등장했는지?

REST처럼 구현 한다면 모든 데이터를 다 가져와서 필요한 것(요청된 것)만 리턴 → 비효율적임

1. RESTful API는 항상 모든 것을 리턴함. 
2. 더 작은 resource로 쪼개면, N+1 problem과 multiple network round trip 을 겪게 됨.

GraphQL은 이 이슈들을 해결하기 위해 나옴

- 오직 요청된 data만 리턴해서 불필요한 network traffic을 줄인다
- client에서 필요한 데이터를 한 번의 request로 가져갈 수 있어서 전체 latency를 줄일 수 있음

### new property 추가

GraphQL에는 API versioning이라는 개념 자체가 없음.

```graphql
type Player {
id: String!
name: String!
points: Int!
inventory: [Item!]!
billing: Billing!
trustworthiness: Float!
}
```

1. schema에는 추가 되었으나 서버 구현 X : client fails fast. 서버가 해당 필드를 처리할 수 없기 때문에 바로 알 수 있음. 반면 REST에서는 서버가 null로 줬다고 간주될 수도 있고, 왜 에러가 났는지 알기 힘들어짐.
2. 서버 구현 O / schema에 추가 X : client가 해당 property에 대해 모르기 때문에 아무 영향 X

## Spring Boot 연동

```groovy
implementation 'com.graphql-java-kickstart:graphql-spring-boot-starter:7.0.0'
implementation 'com.graphql-java-kickstart:graphql-java-tools:6.0.0'
```

Spring Boot는 적절한 핸들러를 자동으로 설정해 준다. 기본적으로는 **POST** */graphql* endpoint를 사용한다.

### Schema 작성

GraphQL Tools library를 통해 schema file을 작성할 수 있고, 이것들을 적절한 bean으로 등록해 사용한다. Spring Boot GraphQL Starter는 `.graphqls` 확장자를 가진 schema 파일들을 찾아 등록해준다.

단 한가지 요구사항은 반드시 하나의 root query와 root mutation이 존재해야 함이다. 이것은 나머지 scheme과 다르게 여러 파일에 나뉠 수 없다. 

### Root Query Resolver

root query는 Spring context에서 특별한 bean이 필요한데, 이를 통해 다양한 field를 다루게 된다. schema 정의와 다르게, 반드시 하나의 spring bean일 필요는 없다.

유일한 요구사항은 `GraphQLQueryResolver`를 구현하고, root query의 모든 field가 동일한 이름(명명 규칙에 따라)의 method를 가져야 한다.

1. `<field>`
2. `is<field>` : boolean type일 때
3. `get<field>`

```java
public class Query implements GraphQLQueryResolver {
    private PostDao postDao;
    public List<Post> getRecentPosts(int count, int offset) {
        return postsDao.getRecentPosts(count, offset);
    }
}
```

위의 sample에서 `Query.recentPosts` 에 대한 method이다. 이 메소드는 schema에 정의된 parameter들을 가져아 하고, 정확한 return type을 준수해야 한다. String, Int, List 등 간단한 타입도 시스템이 알아서 맵핑 시켜 준다.

### Represent Types

GraphQL에서 복잡한 type은 Java bean으로 나타낸다. Java bean의 field는 GraphQL response의 field로 맵핑된다.

```java
public class Post {
    private String id;
    private String title;
    private String category;
    private String authorId;
}
```

schema에 맵핑되지 않는 필드는 무시되며, 문제가 되지 않는다. 예를 들어 `authorId` 는 schema에 존재하지 않기 때문에 무시된다.

### Complex Values

때때로 특정 값은 로드하기에 사소하지 않다. database lookups, 복잡한 연산 등. GraphQL Tools에는 이를 위한 field resolver가 있다. field resolver는 data bean과 동일한 이름(*suffix*Resolver)으로 spring context에 존재하며, field resolver의 메소드 역시 동일한 룰을 따른다. field resolver와 data bean이 동일한 메소드를 갖는 경우 field resolver가 우선이다.

```java
public class PostResolver implements GraphQLResolver<Post> {
    private AuthorDao authorDao;
 
    public Author getAuthor(Post post) {
        return authorDao.getAuthorById(post.getAuthorId());
    }
}
```

여기서 중요한 점은 이 resolver는 다른 spring bean을 사용한다는 점이다. 만약 클라이언트가 해당 필드에 대해 요청하지 않으면, GraphQL 서버는 절대 해당 값을 가져오기 위한 행위를 하지 않는다. 

즉 클라이언트가 Post에 대해 요청하되, 그 Author에 대해 요청하지 않는다면, 위의 `getAuthor()` 는 호출되지 않는다.

### Nullable Values

null / Optional type 둘 다 사용 가능하다.

### Mutations

GraphQL은 또한 서버에 저장된 데이터를 업데이트 하는 기능도 제공한다. 코드 관점에서 보면, 쿼리가 데이터를 업데이트 못 할 이유가 없다. 쉽게 write query resolver를 만들어서 데이터를 저장할 수 있다. 다만 이 경우 side effect이 발생할 수 있기 때문에 bad practice로 간주된다.

대신 mutation은 클라이언트에게 데이터가 변경될 것임을 알려야 한다. `GraphQLMutationResolver` 가 존재한다. mutation 도 쿼리와 동일한 규칙(필드, 리턴 값)을 따라야 한다.

```java
public class Mutation implements GraphQLMutationResolver {
    private PostDao postDao;
 
    public Post writePost(String title, String text, String category) {
        return postDao.savePost(title, text, category);
    }
}
```

### 어떻게 호출하는가?

GET / POST 메소드를 처리해야 한다. GET은 query param을 통해 호출하며, POST는 `application/json` 및 body payload를 통해 호출한다.

**GET** 

```graphql
http://myapi/graphql?query={me{name}}
```

**POST**

```graphql
{
  "query": "...",
  "operationName": "...",
  "variables": { "myVariable": "someValue", ... }
}
```

### Introspection

GraphQL schema 를 요청하는 것. 보통은 tool들이 지원해준다. 예를들어 GraphiQL 웹 페이지에 접속하면 오른쪽에 전체 문서를 볼 수 있다.

또한 POST 메소드를 이용할 수 있다.

```graphql
{
  __schema {
    queryType {
      name
    }
  }
}
```

## GraphiQL

GraphQL 서버와 UI를 통해  통신할 수 있는 툴

`'com.graphql-java-kickstart:graphiql-spring-boot-starter:7.0.1'`

spring boot에 위 dependency 추가하면 기본적으로 `{host}/graphiql` endpoint로 UI에 접속 가능하다.

## Test는?

`testImplementation 'com.graphql-java-kickstart:graphql-spring-boot-starter-test:7.0.1'` 얘를 사용하면 쉽게 할 수 있음.

`@GraphQLTest` annotation을 사용할 수 있고 얘가 `@SpringBootTest` + GraphQL 관련 auto configure 역할을 해줌.

근데 지금 버전에서도 `@GraphQLTest` annotation이 잘 동작하지 않아서, `@SpringBootTest` 를 사용해야 함. 대신 아래와 같이 `GraphQLTestTemplate` 를 사용해서 쉽게 호출할 수 있음.

```java
@SpringBootTest

@Autowired
private GraphQLTestTemplate graphQLTestTemplate;

GraphQLResponse response = graphQLTestTemplate.postForResource("graphql/getPost.graphql");
```
