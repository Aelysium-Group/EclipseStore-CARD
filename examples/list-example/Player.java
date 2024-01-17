package group.aelysium;

import java.util.UUID;

public class Player extends Card implements CardController.Alter {
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

    @Override
    public Player.Altercator alter() {
        return new Player.Altercator(this);
    }

    public interface Attributes {
        String UUID = "uuid";
        String USERNAME = "username";
    }

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
}

