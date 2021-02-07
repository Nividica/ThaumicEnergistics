package thaumicenergistics.util;

import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author Alex811
 */
public interface IThESubscribable {
    ArrayList<EntityPlayer> subscribers = new ArrayList<>();

    default void subscribe(EntityPlayer player){
        subscribers.add(player);
    }

    default void unsubscribe(EntityPlayer player){
        subscribers.remove(player);
    }

    default void notifySubs(Consumer<EntityPlayer> consumer){
        subscribers.forEach(consumer);
    }
}
