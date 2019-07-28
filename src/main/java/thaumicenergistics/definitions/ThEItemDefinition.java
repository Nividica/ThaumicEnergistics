package thaumicenergistics.definitions;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.definitions.IItemDefinition;

/**
 * @author BrockWS
 */
public class ThEItemDefinition implements IItemDefinition {

    private Item item;

    public ThEItemDefinition(Item item) {
        this.item = item;
    }

    @Nonnull
    @Override
    public String identifier() {
        return Objects.requireNonNull(this.item.getRegistryName()).getPath();
    }

    @Override
    public Optional<Item> maybeItem() {
        return Optional.of(this.item);
    }

    @Override
    public Optional<ItemStack> maybeStack(int i) {
        return Optional.of(new ItemStack(this.item, i));
    }

    @Override
    public boolean isEnabled() {
        return this.maybeItem().isPresent();
    }

    @Override
    public boolean isSameAs(ItemStack itemStack) {
        return itemStack != null && this.maybeStack(1).isPresent() && itemStack.isItemEqual(this.maybeStack(1).get());
    }
}
