package group.aelysium;

public class Players extends Card.Holder.Set<Player> {
    @Override
    public Player.Creator create() {
        return new Player.Creator(this);
    }
}
