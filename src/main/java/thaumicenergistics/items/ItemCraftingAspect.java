package thaumicenergistics.items;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumicenergistics.ThaumicEnergistics;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCraftingAspect
	extends Item
{
	private static final String NBTKEY_ASPECT = "Aspect";

	public ItemCraftingAspect()
	{
		this.setMaxStackSize( 64 );
		this.setHasSubtypes( true );
		this.setMaxDamage( 0 );
		this.setCreativeTab( ThaumicEnergistics.ThETab );
		this.setUnlocalizedName( "itemCraftingAspect" );
	}

	/**
	 * Returns true if the player has discovered the aspect.
	 * 
	 * @param player
	 * @param aspect
	 * @return
	 */
	public static boolean canPlayerSeeAspect( @Nonnull final EntityPlayer player, @Nonnull final Aspect aspect )
	{
		return Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect( player.getCommandSenderName(), aspect );
	}

	/**
	 * Gets the aspect associated with the item
	 * 
	 * @param itemstack
	 * @return
	 */
	public static Aspect getAspect( final ItemStack itemstack )
	{
		if( itemstack.hasTagCompound() )
		{
			// Get the tag
			NBTTagCompound tag = itemstack.getTagCompound();

			if( tag.hasKey( NBTKEY_ASPECT ) )
			{
				// Get the aspect
				return Aspect.getAspect( tag.getString( NBTKEY_ASPECT ) );
			}
		}
		return null;
	}

	public static void setAspect( final ItemStack stack, final Aspect aspect )
	{
		// Null check
		if( aspect == null )
		{
			return;
		}

		NBTTagCompound tag = null;

		// Is there a tag?
		if( !stack.hasTagCompound() )
		{
			// Create the tag
			tag = new NBTTagCompound();

			// Set the tag
			stack.setTagCompound( tag );
		}
		else
		{
			tag = stack.getTagCompound();
		}

		// Set the aspect
		tag.setString( NBTKEY_ASPECT, aspect.getTag() );
	}

	@Override
	public void addInformation( final ItemStack stack, final EntityPlayer player, final List list, final boolean advancedInfo )
	{
		// Call super
		super.addInformation( stack, player, list, advancedInfo );

		// Is there an aspect associated?
		Aspect aspect = ItemCraftingAspect.getAspect( stack );
		if( aspect != null )
		{
			// Has the player discovered this aspect?
			if( ItemCraftingAspect.canPlayerSeeAspect( player, aspect ) )
			{
				// Add the aspect info
				list.add( aspect.getLocalizedDescription() );
			}
			else
			{
				// Show unknown
				list.add( StatCollector.translateToLocal( "tc.aspect.unknown" ) );
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack( final ItemStack stack, final int renderPass )
	{
		// Is there an aspect associated?
		Aspect aspect = ItemCraftingAspect.getAspect( stack );
		if( aspect != null )
		{
			// Return it's color
			return aspect.getColor();
		}

		// Pass to super
		return super.getColorFromItemStack( stack, renderPass );
	}

	@Override
	public String getItemStackDisplayName( final ItemStack stack )
	{
		return this.getUnlocalizedNameInefficiently( stack );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems( final Item item, final CreativeTabs par2CreativeTabs, final List itemList )
	{
		// Note: Don't show these
		/*
		for( Aspect aspect : Aspect.aspects.values() )
		{
			ItemStack stack = new ItemStack( this, 1, 0 );
			ItemCraftingAspect.setAspect( stack, aspect );
			itemList.add( stack );
		}
		*/
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getUnlocalizedName( final ItemStack stack )
	{
		Aspect aspect = ItemCraftingAspect.getAspect( stack );

		// Null check
		if( ( stack == null ) || ( aspect == null ) )
		{
			return "";
		}

		// Get the player
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		// Has the player discovered the aspect?
		if( ( player != null ) && ItemCraftingAspect.canPlayerSeeAspect( player, aspect ) )
		{
			// Show name
			return aspect.getName();
		}

		// Show unknown
		return StatCollector.translateToLocal( "tc.aspect.unknown" );

	}
}
