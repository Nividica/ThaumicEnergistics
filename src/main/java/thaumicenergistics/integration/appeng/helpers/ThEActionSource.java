package thaumicenergistics.integration.appeng.helpers;

import javax.annotation.Nonnull;
import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;

import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;

/**
 * @author BrockWS
 */
public class ThEActionSource implements IActionSource {

    private EntityPlayer player;
    private IActionHost host;

    public ThEActionSource(EntityPlayer player) {
        this.player = player;
    }

    public ThEActionSource(IActionHost host) {
        this.host = host;
    }

    @Nonnull
    @Override
    public Optional<EntityPlayer> player() {
        if (this.player != null)
            return Optional.of(this.player);
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<IActionHost> machine() {
        if (this.host != null)
            return Optional.of(this.host);
        return Optional.empty();
    }

    @Nonnull
    @Override
    public <T> Optional<T> context(@Nonnull Class<T> key) {
        return Optional.empty();
    }
}
