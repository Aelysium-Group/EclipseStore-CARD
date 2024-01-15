# CARD
CARD is a data abstraction layer for building intuitive data structures with EclipseStore.

## Installation
### Gradle

```gradle
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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

## Building a Card Data-Structure
### Creating a Card
Cards are the core of a Card data-structure so lets start with those.
```java
public class Player extends Card {
    protected UUID uuid;
    protected String username;

    protected Player(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public static class Builder extends CardController.Create.Creator<Player> {
        private UUID uuid;
        private String username;

        public Builder(PlayerHolder owner) {
            super(owner);
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        @Override
        public ReadyForInsert<Player> prepare() {
            return new ReadyForInsert<>(this.owner, new Player(this.uuid, this.username));
        }
    }
    public static class Altercator extends CardController.Alter.Altercator<Player> {
        protected Altercator(@NotNull Player card) {
            super(card);
        }

        public Altercator username(String username) {
            this.card.username = username;
            return this;
        }
    }
}
```