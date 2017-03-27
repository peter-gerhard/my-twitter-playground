

# MyTwitter - Towards Domain-Driven Design with Akka

In this article I want to describe my journey of getting hands on with Domain-Driven Design, Event Sourcing and CQRS, the Akka-toolkit and finally GraphQL as a new way of exposing an API to clients. I will describe my experience of implementing a small example application and hope this can help others that want to learn. The source code for the example app can be found on [github.com/peter-gerhard/my-twitter-playground](https://github.com/peter-gerhard/my-twitter-playground/tree/v1).

## Domain-Driven Design
Traditional applications are often backed by relational databases that offer strong consistency through global transactions and normalized data. That is easy for application developers since the responsibility of data integrity is moved to the database.

However this approach didn't scale well enough to meet the demand for modern applications to be responsive. In order to meet these demands applications need denormalized data that is prepared to be read fast, and relaxed consistency to achieve fast writes. Consistency can be relaxed because global transactions are rarely a requirement for business use cases but the price for that is that application developers become responsible of data integrity.

Domain-driven design (DDD) is an approach of tackling the increased complexity in application development by a set of strategical and tactical patterns for software development. The concepts were described by Eric Evans in his book ["Domain-driven Design: Tackling Complexity in the Heart of Software"](https://books.google.de/books/about/Domain_driven_Design.html?id=7dlaMs0SECsC&redir_esc=y). Another popular book is
["Implementing Domain-Driven Design"](https://books.google.de/books/about/Implementing_Domain_Driven_Design.html?id=X7DpD5g3VP8C) by Vaughn Vernon.

DDD is all about acquiring a deep understanding of the underlying domain of a business and using this knowledge to develop a model that will be the foundation for an ubiquitous language shared by everyone involved in the development of the software, and also as a foundation for the resulting software itself. 

While mastering the strategical patterns of DDD probably needs more engagement with real world business software and development in a team,
I'm still looking to familiarize myself more with the tactical design patterns that are often applied DDD applications.

### Tactical Design

Every business domain contains objects that have certain roles and certain associations to each other. An aggregate is *a cluster of associated objects* that belong together. An aggregate forms a transactional boundary meaning within an aggregate data must be consistent at all times. Between aggregates there is eventual consistency. At the root of an aggregate there is a single entity. It is the only object of an aggregate that can be referenced from the outside. The aggregate pattern will be a guide for the relaxed consistency in our application. While I think that the Aggregate pattern is worth mentioning here there are other building blocks of a model-driven design that can be read in Part II of Evans DDD book.

### CQRS and Event Sourcing

There are two additional patterns that often find usage in modern applications that I consider worth mentioning here - CQRS and Event Sourcing.

CQRS - Command Query Responsibility Segregation - means having a separate model on the command side and on the query side of an application with possibly multiple different models on the query side.

Event Sourcing means that instead of persisting the state of an application the events that lead to that state are persisted. An event journal containing all events that occurred thereby becomes a replayable source of truth that can recreate application state of any point in time.

CQRS and Event Sourcing are often deployed together since they compliment each other. A clients intend to change data is communicated through a command. If business rules allow the change, an event is persisted and the command-model updates its state. The query-model will eventually be informed that the event happened and will change its state accordingly.

![CQRS and Event Sourcing](https://cdn.rawgit.com/peter-gerhard/my-twitter-playground/a103a92c/blog/CQRS_Event_Sourcing.svg)

## Model

As an exercise we will develop a simple twitter-like application that employs these techniques. Since it is quite a small core domain it seems to be a good point to start with and then iterate on it.
Our application should be able to fulfill the following tasks.

  * Register/Unregister different users
  * Users can follow/unfollow other users
  * Users can manage their information
  * Users can tweet/retweet tweets
  * Users can like/unlike tweets 
  * Users can watch their own and other users user-timeline
  * Users can watch their home-timeline

Where the user-timeline is a history of all tweets of a user and the home-timeline is a history of tweets off all users a user is following.

Users seem to be an obvious candidate for aggregates since they have identity and registering and unregistering seem to frame the lifecycle of users. When we take a look at tweets, users can post them and delete them but also they can be referenced through retweets. So tweets have global identity too and are a certain candidate for aggregates. Timelines seem to be simply a collection of tweets. They will not be referenced by anything and are candidates for views.

Users have a twitter handle and some profile information. For the start we will be content with the profile information being just the username. Lets ignore profile pictures for the start. Also users have a set of other users they follow, and a set of users following them. We will keep only one of these in the command-model as this information is redundant. Since we don't want that users can follow the same user multiple times we will keep track of the users that a user follows. The information which users follow a user will be accessible in a view.

Tweets have an author which is a user. They also have a body, and a notation of time when they were posted. Tweets also have users who liked and users who retweeted them. Since users should not be able to like or retweet a tweet multiple times we will keep track of that as well.

That gives us the following command-models.
```scala
  case class User(
      id: String,
      handle: String,
      name: String,
      subscriptions: Set[String])
```

```scala
  case class Tweet(
      id: String,
      authorId: String,
      timestamp: Long,
      body: String,
      likedBy: Set[String],
      repostedBy: Set[String])
```

On the query side we will add a field to the user query-model that shows which users are following that user. For tweets we will discard the information who liked or retweeted them but just keep a count.

```scala
  case class _User(
      id: String,
      handle: String,
      name: String,
      subscriptions: Set[String],
      subscribers: Set[String])
```

```scala
  case class _Tweet(
      id: String,
      authorId: String,
      timestamp: Long,
      body: String,
      likeCount: Int,
      repostCount: Int)
```

User-timelines will be provided via a view that listens to tweet events. It will keep track of tweets and retweets a user posts and which tweets a user likes. When we want to show a tweet on a timeline we need information about the author of the tweet, the tweet itself, and in case of a retweet also about the user who retweeted.

```scala
  case class UserTimeline(
      tweets: Seq[TweetLike],
      likes: Seq[String])

  case class TweetLike(
      tweetId: String, 
      authorId: String, 
      reposterId: Option[String])
```

Home-timelines will be similar to user-timelines but their view will additionally keep track of the subscribers of a user. When a user tweets the information will be copied to each subscribers home-timeline. This way most of the work is done when writing a tweet so that querying can be faster which is desirable for our use case.

## Implementation

In this section I will describe the tools and reusable building blocks that I used to build this application and explain how to wire them together.

### Akka 
[Akka](http://akka.io/docs/) is an implementation of the [actor model](https://en.wikipedia.org/wiki/Actor_model) - an abstraction for concurrent programming - for Java and Scala.
The core abstraction of the model are actors. An actor has internal state that can only be interacted with by exchanging messages with the actor.
Actors are a natural fit to model Aggregates since they ensure the afore mentioned transaction boundary and also prevent the outside world to reference any object inside the actor that is owned by the Aggregate.

There are several tools built on top of Akka, two of which are of special interest for our example application, namely [akka-persistence](http://doc.akka.io/docs/akka/current/scala/persistence.html) and [akka-streams](http://doc.akka.io/docs/akka/2.4.17/scala/stream/stream-introduction.html).

Akka-persistence gives developers the ability to define a journal via a plugin and then persist objects from within actors. We will use this to persist events produced by our Aggregates. There is also an option to define a snapshot store to persist snapshots of an actors state but for the beginning we will ignore this opportunity.

Akka-streams is a higher level abstraction that is build on top of actors and is an implementation of the [reactive streams](http://www.reactive-streams.org/) interface. We will use akka-streams to stream events from our event-journal to our views.

### Building Blocks

#### AggregateRoot

In the paragraph about [CQRS and Event Sourcing](#cqrs-and-event-sourcing) we said that a command model updates its state after an event is persisted successfully, so every event sourced aggregate root needs a method that takes an event as input parameter and returns its updated state after applying this event.

```scala
  trait Event

  trait AggregateRoot[A, Ev <: Event] {
    def updated(ev: Ev): A
  }
```

Here `A` will be the concrete type of the aggregate and `Ev` will be the specific event type of that aggregate-type.

#### AggregateRootFactory
To keep structure and construction of an `AggregateRoot` encapsulated we want to provide a factory to create `AggregateRoot` entities. The factory takes an event as input parameter and returns a created `AggregateRoot`.

```scala
  trait AggregateRootFactory[A <: AggregateRoot[A, Ev], Ev <: Event] {
    def fromCreatedEvent(ev: Ev): A
  }
```

You might argue now that this approach is not very type safe since every event-type is likely to have only one event that describes that an entity was created and several events that describe updates of state. While it is possible to achieve this type safety, to start with I am satisfied with this solution since `AggregateRoot` and `AggregateRootFactory` will only be used from within an encapsulating `Processor` actor.

#### Processor
A `Processor` is a [`PersistentActor`](http://doc.akka.io/docs/akka/current/scala/persistence.html#Event_sourcing) that will handle all communication with an aggregate. It is important to distinguish between the actors lifecycle and the aggregates lifecycle. The actor is an object in memory that is bound to the application lifetime, while the aggregate or the events that lead to an aggregates state are persisted and might live through application restarts.
The internal state of a `Processor` will be an optional aggregate root entity. To understand why, we need to consider the lifecycle of an aggregate in an application. Most aggregates will be created at some point and might be deleted or archived at a later point in time. Inbetween, aggregates will have one or more lifecycle states where they can be modified. For more sophisticated applications it will make sense to provide classes that represent each lifecycle state. For our use case we just want to distinguish between an aggregate that are alive or not. Akka itself provides the [FSM](http://doc.akka.io/docs/akka/2.4.17/scala/fsm.html) trait to model finite state machines with actors which looks like a promising solution for that problem and I will definitely check it out in the future.

```scala
  trait Processor[A <: AggregateRoot[A, Ev], Ev <: Event] 
    extends PersistentActor with ActorLogging {

    def persistenceId: String

    def factory: AggregateRootFactory[A, Ev]

    def receiveRecover: Receive

    def uninitialized: Receive

    def initialized: Receive

    protected var state: Option[A] = None

    override def receiveCommand: Receive = uninitialized

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.debug(reason.getMessage)
      super.preRestart(reason, message)
    }

    protected def handleCreation(ev: Ev): Unit = {
      state = Some(factory.fromCreatedEvent(ev))
      context.become(initialized)
    }

    protected def handleUpdate(ev: Ev): Unit = {
      state = state.map(_.updated(ev))
    }

    protected def handleDeletion(): Unit = {
      state = None
      context.become(uninitialized)
    }
  }
```

Implementers of `Processor` will have to provide a `persistenceId` that defines the internal aggregate towards the event journal, an instance of `AggregateRootFactory`, [`receiveRecover`](http://doc.akka.io/docs/akka/2.4.17/scala/persistence.html#Recovery) to replay events on upstart of the processor, as well as behaviors for when aggregates are `uninitialized` and `initialized`.

#### Repository
The role of a repository is to abstract away persistence and infrastructure logic from application services. An application service is supposed to retrieve entities from a repository and coordinate changes to domain objects while the actual logic resides in the domain-model. Since services can only communicate with `Processors` via messages and through the `Repository` it becomes harder to fall into the pitfall of having an [anemic domain model](https://martinfowler.com/bliki/AnemicDomainModel.html). At the same time the `Repository` becomes more of a post office supervising `Processors` as their children.
For the start I did not care about shutting down processors when they are not needed anymore but this would also be the responsibility of the `Repository`.

```scala
  case class Envelope(id: String, msg: Any)

  trait Repository extends Actor {

    protected def randomId: String = 
      UUID.randomUUID.toString

    protected def childProps(id: String): Props

    protected def getChild(id: String): ActorRef =
      context.child(id).getOrElse(createChild(id))

    private def createChild(id: String): ActorRef =
      context watch context.actorOf(childProps(id), id)
  }
```

Since actors are a totally different concurrency abstractions than `scala.concurrent.Future`s and we don't want to work with actors throughout our whole application, we will add a translation layer that will send a message to the `Repository` and expects to receive a message from a processor within a given timeout and returns the result as a `Future[Any]`.

```scala
  trait RepositoryConnector {

    protected implicit def ec: ExecutionContext

    protected implicit def timeout: Timeout

    protected def repository: ActorRef

    protected def askRepo(msg: Any): Future[Any] =
      ask(repository, msg)

    protected def askRepo(id: String, msg: Any): Future[Any] =
      ask(repository, Envelope(id, msg))
  }
```

#### View
As we said before a view is updated eventually as it receives events that were persisted to the event-journal. It will receive this messages via an event stream. A view implementation has to define how it reacts on an incoming event. It is likely that a view is backed by a database and will translate events to updates in that database. In our case we will use an in memory view for demo purposes. If this was not the case it would just make sense to split the updating and querying logic since with the below implementation we limit the access to our view.

Although the trait `View` doesn't enforce it we expect each view to process `GetById`, `GetOptById`, and `GetSeqById` messages.

```scala
  case class GetById(id: String)
  case class GetOptById(id: String)
  case class GetSeqByIds(ids: Seq[String])

  trait View extends ActorSubscriber with ActorLogging {

    type EventHandler = PartialFunction[Event, Unit]

    protected def handleEvent: EventHandler

    protected def receiveClientMessage: Receive

    override protected def requestStrategy: RequestStrategy =
      OneByOneRequestStrategy

    override def receive: Receive = 
      receiveEventMessage orElse receiveClientMessage

    private def receiveEventMessage: Receive = {
      case msg: OnNext ⇒
        val ev: Event = msg.element.asInstanceOf[Event]
        if (handleEvent.isDefinedAt(ev)) handleEvent(ev)

      case msg: OnError ⇒
        log.debug(msg.cause.getMessage)

      case OnComplete ⇒
        log.debug("Stream to view completed unexpectedly.")
    }
  }
```

We want to add a translation from actors to futures for the same reason we did for the `Repository`.

```scala
  trait ViewConnector[A] {

    protected implicit def ec: ExecutionContext

    protected implicit def timeout: Timeout

    protected implicit def classTag: ClassTag[A]

    protected def view: ActorRef

    def getById(id: String): Future[A] =
      askView(GetById(id)).mapTo[A]

    def getOptById(id: String): Future[Option[A]] =
      askView(GetOptById(id)).mapTo[Option[A]]

    def getSeqByIds(ids: Seq[String]): Future[Seq[A]] =
      askView(GetSeqByIds(ids)).mapTo[Seq[A]]

    protected def askView(msg: Any): Future[Any] =
      ask(view, msg)
  }
```

To wire the event journal and the views together we will use [akka-persistence-query](http://doc.akka.io/docs/akka/2.4.17/scala/persistence-query.html). It lets us read events from our event-journal and feed them to the views via reactive streams.

First we need a read journal. The exact way of getting a read journal depends on the journal plugin you are using. For the demo app I chose to use the leveldb plugin.

```scala
  implicit val system = ActorSystem("my-twitter-playground")

  val readJournal =
    PersistenceQuery(system)
      .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
```

By default the read journal lets us only read events by a certain persistenceId. That's not really useful since we rather want to query for events by aggregate type or by a specific event type. Luckily the read journal also provides querying events by a certain tag but we have to setup the tagging first. Therefore we define an event-adapter and tell akka persistence where to find it via configuration.

```scala
  class TaggingEventAdapter extends WriteEventAdapter {

  override def toJournal(event: Any): Any = event match {

    case ev: UserRegisteredEvent ⇒ 
      Tagged(event, Set("user-event", "user-registered"))
    case ev: UserNameSetEvent ⇒ 
      Tagged(event, Set("user-event", "user-name-set"))
    /* ... */

    case ev: TweetPostedEvent ⇒ 
      Tagged(event, Set("tweet-event", "tweet-posted"))
    case ev: TweetRepostedEvent ⇒ 
      Tagged(event, Set("tweet-event", "tweet-reposted"))
    /* ... */
    
    case _ ⇒ event
  }

  override def manifest(event: Any): String = ""
}
```

```
  akka.persistence.journal.leveldb {
    event-adapters {
      tagging = "de.htw.pgerhard.TaggingEventAdapter"
    }
    event-adapter-bindings {
      "de.htw.pgerhard.domain.users.events.UserRegisteredEvent" = tagging
      "de.htw.pgerhard.domain.users.events.UserNameSetEvent" = tagging
      /* ... */
    }
  }
```

Each event is tagged with its specific event type as well as the aggregate type it belongs to. Now we can query e.g. for all tweet-related events.

To connect the source of events to the view we have to create a sink for our concrete implementation of the `View` trait and pipe the event source to it like shown below.

```
  val eventSource: Source[EventEnvelope2, NotUsed] =
    readJournal.eventsByTag("tag")

  val view: ActorRef = system.actorOf(MyViewClass.props)

  val viewSink: Sink[Event, NotUsed] = 
   Sink.fromSubscriber(ActorSubscriber[Event](view))

  eventSource
    .map(_.event.asInstanceOf[Event])
    .runWith(viewSink)
```

That was all there is to setup event topics. Now views should be updated to be eventually consistent.

### Exposing the API with GraphQL

Now that we have our application we need to define an API for clients to interact with this. Usually a REST-API would be the method of choice but chose to adapt a relatively new technology called [GraphQL](http://graphql.org/learn/). GraphQL is a data query language developed by Facebook who began open-sourcing a specification in 2015.
There are many articles that describe the advantages of GraphQL so I wont go into much detail here.

GraphQL allows you to define a type system for the data your API provides and lets clients specify detailed information about what data they need. Types have fields and you provide functions for each field that resolve the value for this field, so GraphQL is backed by your application and is not dependent on a certain database technology. The fact that clients can communicate their data needs to the server allows to solve a few issues that we usually face with REST style APIs.

#### Round-trips - Under Fetching

Consider we would write a client for our twitter application and we want to render a view of the followers of a user. With a REST-API we would probably have the following request/response.

`GET http://mytwitter.com/api/users/c306c412-a633-4081-bc92-e65fed810c80` 

```json
{
  "id": "c306c412-a633-4081-bc92-e65fed810c80",
  "handle": "@donald",
  "name": "Donald",
  "subscriptions": [],
  "subscribers": [
    "04ea5f0b-b7a9-48cd-8715-e0886a7c8de3"
    "aa5e512d-d2a3-4af2-89a5-0769c5221b2d"
    "c3157350-3940-4fd9-aa16-39758a28ada4"
  ]
}
```

Now we would need to make subsequent requests for the other users. In our case this would be a lot of requests but only one additional round trip. Depending on the object graph of the domain it might take more round-trips to get all wanted resources.

GraphQL lets the client specify that it wants to resolve the follower by their id and what information is needed from a follower.

```
  query user(userId: "c306c412-a633-4081-bc92-e65fed810c80") {
    subscribers {
      name
    }
  }
```

The result will have exactly the fields defined that we requested.

```json
{
  "data": {
    "user": {
      "subscribers": [
        { "name": "Tick" },
        { "name": "Trick" },
        { "name": "Track" },
      ]
    }
  }
}
```

#### Over Fetching

Another problem arises when we want to render a view for a home-timeline. When we show a tweet we want to show the handle and user-name (and the profile picture) of the author together with the tweet. We are not interested in the followers of an author. We could provide different query models for users and provide one that does not include this information, however GraphQL lets us simply specify that we don't want to fetch this information.

```
  query home(userId: "04ea5f0b-b7a9-48cd-8715-e0886a7c8de3") {
    tweets {
      author {
        handle
        name
      }
      tweet {
        id
        body
        timestamp
        likeCount
        repostCount
      }
    }
  }
```

#### Deprecation

Consider we want to deprecate a field on a resource. Since REST does not enable the client to specify which fields of a resource it is interested
in the server wont have information about which clients still use this field.
While there are solutions for solving this problem using REST they take more efford and are less elegant then GraphQLs approach. By letting the client specify its data requirements the server will know when all clients migrated and we can remove the field from the resource.

#### Discoverability

To provide proper discoverability in a REST API requires additional work and that means more complexity for developers. Hence most REST APIs lack discoverability. With GraphQL we get discoverability for free when we define the type system for the data we provide. Clients can query a schema to find out exactly what types are provided by the API, what queries, and what operations are supported.

```
{
  __schema {
    types {
      name
    }
  }
}
```

#### Sangria

[Sangria](http://sangria-graphql.org/) is a GraphQL implementation in Scala that gives us easy access to the power of GraphQL. To define a type system we simply define a type per resource and provide descriptions and resolve functions for each field. Here is an example for a Tweet.

```scala
  lazy val TweetType: ObjectType[Environment, Tweet] =
    ObjectType("Tweet", "A short message that can be posted on MyTwitter.",
      () ⇒ fields[Environment, Tweet](
        Field("id", StringType,
          Some("The id of the tweet."),
          resolve = _.value.id),
        Field("author", UserType,
          Some("The user who posted the tweet."),
          resolve = ctx ⇒ users.defer(ctx.value.authorId)),
        Field("timestamp", LongType,
          Some("The timestamp when the tweet was posted."),
          resolve = _.value.timestamp),
        Field("body", StringType,
          Some("The content of the tweet."),
          resolve = _.value.body),
        Field("likeCount", IntType,
          Some("The number of users who liked this tweet."),
          resolve = _.value.likeCount),
        Field("retweetCount", IntType,
          Some("The number of users who retweeted this tweet."),
          resolve = _.value.repostCount)))
```

You can see that we replaced the `authorId` field of our model with an `author` field that has the type `UserType`. In order to defer a `User` value in case it is asked for by the client we need to tell sangria how to fetch it. We do this by providing a more general fetcher that can fetch a list of users given a list of user ids.

```scala
  val users: Fetcher[Environment, User, String] =
    Fetcher.caching((ctx: Environment, ids: Seq[String]) ⇒
      ctx.users.getSeqByIds(ids))(HasId(_.id))
```

Now we define a `QueryType` that will contain all possible queries that are supported by our API. 

```scala
  val TweetIdArg = 
    Argument("tweetId", StringType, description = "Id of a tweet")

  val QueryType = ObjectType(
    "Query", fields[Environment, Unit](
      Field("tweet", ListType(TweetType),
        arguments = TweetIdArg :: Nil,
        resolve = (ctx) ⇒ ctx.ctx.tweets.getById(ctx.arg(TweetIdArg))),
      /* More queries */
    ))
```

We will also provide a `MutationType` for all possible state mutating operations on our API. Then we wrap our queries and mutations up to a finished schema and define an executor that can execute grapql-queries. 

```scala
  val schema = Schema(QueryType, Some(MutationType))
```

```scala
  val executor = Executor(
    GraphQlSchema.schema,
    deferredResolver = 
      DeferredResolver.fetchers(GraphQlSchema.tweets, GraphQlSchema.users))
```

I used [akka-http](http://doc.akka.io/docs/akka-http/current/scala/http/introduction.html) to serve http requests. There is an excellent example on how to use sangria with akka-http on [github](https://github.com/sangria-graphql/sangria-akka-http-example).

## Conclusion

We saw that using the right tools it does not have to be complicated to write applications that maintain their own eventual consistent data integrity. While our example domain was a quite easy one it becomes increasingly important with more complex domains to take the time to get deep domain-knowledge and develop a solid model. To learn more about the strategies of developing a good model I can only advise you to read the books by [Evans](https://books.google.de/books/about/Domain_driven_Design.html?id=7dlaMs0SECsC) and [Vernon](https://books.google.de/books/about/Implementing_Domain_Driven_Design.html?id=X7DpD5g3VP8C) that I mentioned earlier.

Using the akka toolkit introduced a few additional challenges for me but ultimately was a great help as it makes it so easy to provide a solid event-journal / event-streaming infrastructure.

There are still a a lot of things missing in this application and I hope I will add them in the future. It was fun getting hands on with this cool techniques and I hope I could help anybody who tries to learn.







