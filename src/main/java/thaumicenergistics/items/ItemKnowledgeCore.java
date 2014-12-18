package thaumicenergistics.items;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.lib.research.ResearchManager;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.util.EffectiveSide;

public class ItemKnowledgeCore
	extends Item
{
	/**
	 * NBT Keys
	 */
	private static final String NBT_KEY_PLAYER = "Player", NBT_KEY_RESEARCH = "Research";

	public ItemKnowledgeCore()
	{
		// Can not be damaged
		this.setMaxDamage( 0 );

		// Has no subtypes
		this.setHasSubtypes( false );

		// Can not stack
		this.setMaxStackSize( 1 );
	}

	/**
	 * Gets or creates the NBT tag for the specified core.
	 * 
	 * @param sCore
	 * @return
	 */
	private NBTTagCompound getOrCreateNBT( final ItemStack sCore )
	{
		if( !sCore.hasTagCompound() )
		{
			sCore.stackTagCompound = new NBTTagCompound();
		}

		return sCore.stackTagCompound;
	}

	/**
	 * Gets the number of stored researches.
	 * 
	 * @param sCore
	 * @return
	 */
	private int getStoredResearchCount( final ItemStack sCore )
	{
		// Get the core's data
		NBTTagCompound data = this.getOrCreateNBT( sCore );

		// Ensure there is stored research
		if( data.hasKey( ItemKnowledgeCore.NBT_KEY_RESEARCH ) )
		{
			// Get the list
			NBTTagList researchList = data.getTagList( ItemKnowledgeCore.NBT_KEY_RESEARCH, Constants.NBT.TAG_STRING );

			// Return the number of stored researches.
			return researchList.tagCount();
		}

		return 0;
	}

	/**
	 * Will update the stored research if needed.
	 * 
	 * @param sCore
	 * @param forceSync
	 * Will force the stored research to update, the player must be online.
	 */
	private void syncResearch( final ItemStack sCore, final boolean forceSync )
	{
		// Get the current research
		ArrayList<String> currentResearch = ResearchManager.getResearchForPlayerSafe( this.getBoundPlayer( sCore ) );

		// Is there any current research?
		if( ( currentResearch == null ) || ( currentResearch.size() == 0 ) )
		{
			// Nothing to sync (player is most likely offline)
			return;
		}

		// Do the counts differ?
		if( forceSync || ( this.getStoredResearchCount( sCore ) != currentResearch.size() ) )
		{
			// Update the stored research
			this.setStoredResearch( sCore, currentResearch );
		}
	}

	@Override
	public void addInformation( final ItemStack sCore, final EntityPlayer player, final List tooltip, final boolean advancedItemTooltips )
	{
		// Get the player name
		String boundPlayerName = this.getBoundPlayer( sCore );

		// Is the core un-bound?
		if( boundPlayerName.equals( "" ) )
		{
			// Add unbounded message
			tooltip.add( EnumChatFormatting.GRAY + StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".tooltip.core.unbound" ) );
		}
		else
		{
			// Add bound player
			tooltip.add( EnumChatFormatting.GRAY + StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".tooltip.core.bound" ) + " " +
							boundPlayerName );
		}

	}

	/**
	 * Returns true if the research has been added to the specified core.
	 * 
	 * @param sCore
	 * @param researchKey
	 * @return
	 */
	public boolean doesCoreHaveResearch( final ItemStack sCore, final String researchKey )
	{
		// Sync if needed
		this.syncResearch( sCore, false );

		// Return if the core has the research
		return this.getStoredResearch( sCore ).contains( researchKey );
	}

	/**
	 * Get's the name of the player bound to the specified core.
	 * 
	 * @param sCore
	 * @return Name of the bound player, or empty string if no player is bound.
	 */
	public String getBoundPlayer( final ItemStack sCore )
	{
		// Get the data tag
		NBTTagCompound data = this.getOrCreateNBT( sCore );

		// Is there a bound player?
		if( data.hasKey( ItemKnowledgeCore.NBT_KEY_PLAYER ) )
		{
			return data.getString( ItemKnowledgeCore.NBT_KEY_PLAYER );
		}

		// No bound player
		return "";
	}

	/**
	 * Gets the crafting recipe for the specified result.
	 * 
	 * @param sCore
	 * @param craftingResult
	 * @return Recipe if found and core has research, null otherwise.
	 */
	public IArcaneRecipe getCraftingRecipe( final ItemStack sCore, final ItemStack craftingResult )
	{
		// Search all TC recipes
		for( Object tcRecipe : ThaumcraftApi.getCraftingRecipes() )
		{
			// Is the recipe an arcane one?
			if( ( tcRecipe != null ) && ( tcRecipe instanceof IArcaneRecipe ) )
			{
				// Cast
				IArcaneRecipe recipe = ( (IArcaneRecipe)tcRecipe );

				// Get the result of the recipe
				ItemStack result = recipe.getRecipeOutput();

				// Does the recipe produce the desired result?
				if( ( result != null ) && ( result.isItemEqual( craftingResult ) ) )
				{
					// Get the research key for this recipe
					String researchKey = recipe.getResearch();

					/*
					 * NOTE: There is a potential situation where two arcane recipes produce the same result,
					 * in that instance this will not work properly if they are not both attached to the same research.
					 */
					// Does the core have the required research?
					if( this.doesCoreHaveResearch( sCore, researchKey ) )
					{
						return recipe;
					}
				}
			}
		}

		// The result can not be crafted.
		return null;
	}

	/**
	 * Gets the research stored on specified core.
	 * 
	 * @param sCore
	 * @return
	 */
	public ArrayList<String> getStoredResearch( final ItemStack sCore )
	{
		// Get the core's data
		NBTTagCompound data = this.getOrCreateNBT( sCore );

		// Create the research key list
		ArrayList<String> researchKeys = new ArrayList<String>();

		// Ensure there is stored research
		if( data.hasKey( ItemKnowledgeCore.NBT_KEY_RESEARCH ) )
		{
			// Get the list
			NBTTagList researchList = data.getTagList( ItemKnowledgeCore.NBT_KEY_RESEARCH, Constants.NBT.TAG_STRING );

			// Get the research and add to the keys
			for( int i = 0; i < researchList.tagCount(); i++ )
			{
				researchKeys.add( researchList.getStringTagAt( i ) );
			}
		}

		return researchKeys;
	}

	@Override
	public String getUnlocalizedName( final ItemStack itemStack )
	{
		return ThaumicEnergistics.MOD_ID + ".item." + ItemEnum.KNOWLEDGE_CORE.getInternalName();
	}

	/**
	 * Binds the core to the player that right-clicked.
	 */
	@Override
	public ItemStack onItemRightClick( final ItemStack sCore, final World world, final EntityPlayer player )
	{
		// Bind only on server side.
		if( !world.isRemote )
		{
			// Bind the player
			this.setBoundPlayer( sCore, player );

			// Inform the player
			player.addChatMessage( new ChatComponentTranslation( StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".tooltip.core.bound" ) +
							" " + player.getCommandSenderName() ) );
		}

		return sCore;
	}

	/**
	 * Registers and sets the core icon
	 */
	@Override
	public void registerIcons( final IIconRegister iconRegister )
	{
		this.itemIcon = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":knowledge.core" );
	}

	/**
	 * Set's the player the specified core is bound to.
	 * The player must be online when this is called.
	 * 
	 * @param sCore
	 */
	public void setBoundPlayer( final ItemStack sCore, final EntityPlayer player )
	{
		// Ignored on client side
		if( EffectiveSide.isClientSide() )
		{
			return;
		}

		// Get the data tag
		NBTTagCompound data = this.getOrCreateNBT( sCore );

		// Set the player name
		data.setString( ItemKnowledgeCore.NBT_KEY_PLAYER, player.getCommandSenderName() );

		// Sync
		this.syncResearch( sCore, true );
	}

	/**
	 * Sets the research stored on the specified core.
	 * 
	 * @param sCore
	 * @param researchKeys
	 */
	public void setStoredResearch( final ItemStack sCore, final ArrayList<String> researchKeys )
	{
		// Ignored on client side
		if( EffectiveSide.isClientSide() )
		{
			return;
		}

		// Create the list
		NBTTagList researchList = new NBTTagList();

		// Add the research to the list
		for( int i = 0; i < researchKeys.size(); i++ )
		{
			researchList.appendTag( new NBTTagString( researchKeys.get( i ) ) );
		}

		// Add the list to the core's data
		this.getOrCreateNBT( sCore ).setTag( ItemKnowledgeCore.NBT_KEY_RESEARCH, researchList );

	}
}
