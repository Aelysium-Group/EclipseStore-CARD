package group.aelysium;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface CardController {
    interface Create {
        /**
         * Returns a new {@link Creator} which will create a new Card and then insert it into the remote storage.
         * @return {@link Creator}
         */
        Creator<?> create();

        abstract class Creator<C extends Card> {
            protected final Storeable<C> owner;
            protected Creator(CardController.Storeable<C> owner) {
                this.owner = owner;
            }

            public abstract ReadyForInsert<C> prepare();

            public static class ReadyForInsert<C extends Card> {
                private final Storeable<C> owner;
                protected C newCard;
                public ReadyForInsert(CardController.Storeable<C> owner, C newCard) {
                    this.owner = owner;
                    this.newCard = newCard;
                }

                /**
                 * Create the new card and store it.
                 * @return The newly stored card.
                 * @throws NullPointerException If the new card couldn't be built.
                 */
                public C createAndStore() {
                    if(newCard == null) throw new NullPointerException("Your builder implementation must ensure that this.newCard is NOT null before calling .createAndStore()");

                    this.owner.store(this.newCard);

                    return this.newCard;
                }
            }
        }
    }
    interface Read {
        interface Entry<V extends Card> {
            /**
             * Takes in an indeterminate number of attributes and performs a {@link Stream#filter(Predicate)} looking for a Card that matches the parameter(s) that were passed.
             * Specifically, this will perform a {@link Card#attributeEquals(Card.Attribute)} on each Card, for each parameter that was passed.
             * @param attributes The parameter(s) to search with.
             * @return An {@link Optional <V>}. If multiple items match the parameter(s) that were passed, this method performs a {@link Stream#findAny()}.
             */
            Optional<V> searchFor(Card.Attribute... attributes);

            /**
             * Performs the passed predicate on the Cards.
             * @param predicate The predicate to use.
             * @return A {@link Stream} containing the Cards which match the predicate.
             */
            Stream<V> filter(Predicate<V> predicate);
        }
        interface KeyValue<K, V extends Card.WithKey<K>> extends Entry<V> {
            /**
             * Finds the value for a key.
             * This method is eqivalent to using {@link java.util.HashMap#get(Object)} except, instead of returning `null` if no item exists; it will return {@link Optional#empty()}.
             * @param key The key to fetch with.
             * @return {@link Optional<V>} if there was a value associated with the key, or {@link Optional#empty()} if there was no value.
             */
            Optional<V> fetch(K key);
        }
    }

    interface Alter {
        Altercator<?> alter();

        abstract class Altercator<C extends Card> {
            protected final C card;
            protected Altercator(C card) {
                this.card = card;
            }

            /**
             * Commits the changes to the database.
             */
            public final void commit() {
                this.card.store();
            }
        }
    }
    interface Delete {
        /**
         * Checks if the card has been deleted.
         * @return `true` if the card is deleted. `false` otherwise.
         */
        boolean deleted();

        /**
         * Deletes the card.
         * Once deleted, any calls to {@link Delete#catchIllegalCall()} will throw an {@link IllegalAccessException}.
         */
        void delete();

        /**
         * Checks for if the card has been deleted and throws an exception if it has.
         * @throws RuntimeException If the card has been deleted.
         */
        default void catchIllegalCall() {
            if(this.deleted()) throw new RuntimeException("Illegal access attempt for Card that was already deleted.");
        }
    }

    interface Storeable<C extends Card> {
        void store(C object);
    }

    interface Deletable<C extends Card> {
        void delete(C object);
    }
}
