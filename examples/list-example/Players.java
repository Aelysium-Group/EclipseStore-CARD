package group.aelysium;

public class Players extends Card.Holder.List<Player> {
    @Override
    public Player.Creator create() {
        return new Player.Creator(this);
    }
}
