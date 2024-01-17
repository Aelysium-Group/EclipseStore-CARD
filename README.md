# CARD
CARD is a data abstraction layer for building intuitive data structures with EclipseStore.

## Installation
### Gradle

```gradle
dependencyResolutionManagement {
	repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' }
	}
}
```
```gradle
implementation 'com.github.Aelysium-Group:EclipseStore-CARD:0.0.1-SNAPSHOT'
```

### Maven

```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```
```xml
<dependency>
    <groupId>com.github.Aelysium-Group</groupId>
    <artifactId>EclipseStore-CARD</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Introduction
Data structures in CARD are broken into two major units:
1. **Card** - A single entity.
2. **CardHolder** - A collection for managing Cards.

## Creating a Card
Cards are the core of a Card data-structure so lets start with those. Let's start with a simple card.
```java
public class Player extends Card {
    private UUID uuid;
    private String username;

    public Player(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public boolean attributeEquals(Card.Attribute<?> attribute) {
        return switch(attribute.key()) {
            case Attributes.UUID -> attribute.value().equals(this.uuid);
            case Attributes.USERNAME -> attribute.value().equals(this.username);
            default -> false;
        };
    }
}
```
In the above snippet we see the card `Player`.

`attributeEquals` is a required method which allows for a passed attribute to be compared against
the Card's own attributes.

If you want to add better support for people using your software, you can create a nifty interface with the valid attribute keys your Card contains:
```java
public class Player extends Card {
    private UUID uuid;
    private String username;

    public Player(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public boolean attributeEquals(Card.Attribute<?> attribute) {
        return switch(attribute.key()) {
            case Attributes.UUID -> attribute.value().equals(this.uuid);
            case Attributes.USERNAME -> attribute.value().equals(this.username);
            default -> false;
        };
    }

    public interface Attributes {
        String UUID = "uuid";
        String USERNAME = "username";
    }
}
```

You now have a valid Card!
The card can be persisted properly, and even has a built-in `.delete()` method for
removing it from your storage.

## Card Holders
Creating a new card holder is just as easy as creating a card.
First we have to pick what Collection type we'd like to use, then we just implement the methods.
```java
public class Players extends Card.Holder.List<Player> {
    @Override
    public Creator<?> create() {
        return null;
    }
}
```
The above will create a simple CardHolder which uses a `LazyArrayList` as its collection.
Notice the `.create()` method? We'll need to create a `Creator` for our Player Card. There's more on creating Creators below.
This way new Player Cards can be created and persisted.

## Card Holders (/w Map)
Implementing Cards and CardHolders using a Map requires a slightly different implementation from when you use a Set or List.

First, your Cards must be updated to extend `Card.WithKey`. `Card.WithKey` will also take a generic type which represents the Key of the Key/Value pair.
```java
public class Player extends Card.WithKey<UUID> {
```
Next, you'll have to implement the `.key()` method in your card:
```java
    @Override
    public UUID key() {
        return this.uuid;
    }
```
This will tell the Card Holder which parameter of your Card is used as the key in the Map.

Lastly, you just need to update your Card Holder to extend `Card.Holder.Map`.
```java
public class Players extends Card.Holder.Map<UUID, Player> {
    @Override
    public Creator<?> create() {
        return null;
    }
}
```
Notice how we also added the key, `UUID`, to the generic of the Map too.

## Reading
Reading data is effectively the same as in EclipseStream.
Each CardHolder has some convenience methods for common computations, if you need more specific access you can use the `.expose()` method to get the underlying data set.

For List based Card Holders you can use:

> ### #filter(Predicate<C> predicate)
> ```java
> public Stream<C> filter(Predicate<C> predicate);
> ```
> Standard `.filter()` implementation.

> ### #searchFor(Attribute... attributes)
> ```java
> public Optional<C> searchFor(Attribute... attributes);
> ```
> A slightly more abstracted version of `.filter()` which takes in a list of Attributes and will search for an exact match for all of them.

For Map based Card Holders, you have access to an additional method:

> ### #fetch(K key)
> ```java
> public Optional<CWK> fetch(K key);
> ```
> Fetches the value which is associated with the passed key.
> If no value could be found, returns an empty Optional.

As mentioned earlier, if you need more specific functionality, you can call `#expose()` to get the underlying data set.

## Creating
Creators implement the builder data structure and are used by Card Holders to create and persist new instances of Cards.
Let's create a simple Creator implementation for our Player Card.
```java
// Static class inside the Player.java class
public static class Creator extends CardController.Create.Creator<Player> {
    private UUID uuid;
    private String username;

    protected Creator(Players owner) {
        super(owner);
    }

    public Creator uuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Creator username(String username) {
        this.username = username;
        return this;
    }

    @Override
    public ReadyForInsert<Player> prepare() {
        return new ReadyForInsert<>(this.owner, new Player(this.uuid, this.username));
    }
}
```
This creator can then be called from within our Card Holder:
```java
public class Players extends Card.Holder.List<Player> {
    @Override
    public Player.Creator create() {
        return new Player.Creator(this);
    }
}
```
Now, anytime you want to create a new instance of Player, you can call the `Players.create()` method!
```java
    Players players = /* Fetch Players Card Holder */;
    Player player = players.create()
        .uuid(UUID.randomUUID())
        .username("John Doe")
        .prepare()
        .createAndStore();
```
Calling the `.createAndStore()` method will then persist your new instance and return it upon successful storing.

## Deleting
Deleting a card is a simple call to `Player.delete()`.
Once you've deleted a Card, an internal variable will be switched for that Card which will cause `.catchIllegalCall()` to throw a `RuntimeException`.
Make sure that, after you call `.delete()` you don't try to access that Card anymore!
In order to properly guard our Card, Be sure to add `catchIllegalCall();` to any methods you implement!
```java
public class Player extends Card {
    private UUID uuid;
    private String username;

    public Player(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public UUID uuid() {
        catchIllegalCall();
        return this.uuid;
    }

    public String username() {
        catchIllegalCall();
        return this.username;
    }

    @Override
    public boolean attributeEquals(Card.Attribute<?> attribute) {
        catchIllegalCall();
        
        return switch(attribute.key()) {
            case Attributes.UUID -> attribute.value().equals(this.uuid);
            case Attributes.USERNAME -> attribute.value().equals(this.username);
            default -> false;
        };
    }

    public interface Attributes {
        String UUID = "uuid";
        String USERNAME = "username";
    }
}
```
Now, if we attempt to call `.uuid()` after we've deleted the object, we'll get an exception.
```java
    Players players = /* Fetch Players Card Holder */;
    Player player = players.create()
        .uuid(UUID.randomUUID())
        .username("John Doe")
        .prepare()
        .createAndStore();
    
    System.out.println(player.uuid());
    
    player.delete();

    System.out.println(player.uuid()); // This line will throw an exception.
```

## Altering
Implementing the ability to alter a Card is very similar to adding a Creator.
First, we want to extend the `Alter` interface in our Card:
```java
public class Player extends Card implements CardController.Alter {
```

Next we want to make a new Altercator.
```java
// Added inside the Player.java class
public static class Altercator extends CardController.Alter.Altercator<Player> {
    protected Altercator(Player card) {
        super(card);
    }

    public Altercator uuid(UUID uuid) {
        this.card.uuid = uuid;
        return this;
    }

    public Altercator username(String username) {
        this.card.username = username;
        return this;
    }
}
```
Notice how the Altercator directly accesses the private Card members?

This is only possible as long as the Altercator class is inside of it's associated Card class.

From here you can implement the `.alter()` method in your Card class.
```java
    @Override
    public Player.Altercator alter() {
        return new Player.Altercator(this);
    }
```

Using this system, you can modify as few or as many attributes of the associated Card as you want!

```java
    Players players = /* Fetch Players Card Holder */;
    Player player = players.create()
        .uuid(UUID.randomUUID())
        .username("John Doe")
        .prepare()
        .createAndStore();
    System.out.println(player.uuid());
    System.out.println(player.username());
    
    // Update just the uuid
    player.alter()
        .uuid(UUID.randomUUID())
        .commit();
    System.out.println(player.uuid());

    // Update just the username
    player.alter()
        .username("new username!")
        .commit();
    System.out.println(player.username());
    
    // Update both
    player.alter()
        .uuid(UUID.randomUUID())
        .username("new new username!")
        .commit();
    System.out.println(player.uuid());
    System.out.println(player.username());
```

## Persisting With EclipseStore
Once you've created your data structure.
All you have to do is add it to your EclipseStore root!

We like to name our root `Database` so that's what you see below:
```java
public class Database {
    private Players players = new Players();
    
    public Players players() {
        return this.players;
    }
}
```

You can then interact with your data just like you would in EclipseStore:
```java
Database database = /* Get database object */;
Players players = database.players();

UUID someUUID = UUID.randomUUID();

players.create()
    .uuid(someUUID)
    .username("John Doe")
    .prepare()
    .createAndStore();

// .fetch() is exclusive to Map-based Card Holders.
Player player = players.fetch(someUUID).orElseThrow();

player.alter()
        .username("an updated username!")
        .commit();

System.out.println(player.username());

player.delete();
```