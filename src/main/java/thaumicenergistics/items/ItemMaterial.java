package thaumicenergistics.items;

import java.util.List;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.registries.ItemEnum;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

public class ItemMaterial
	extends Item
{

	/**
	 * List of icons based on damage/meta value
	 */
	private IIcon[] icons;

	// Material array
	private MaterialTypes[] materials;

	public ItemMaterial()
	{
		this.setMaxDamage( 0 );
		this.setHasSubtypes( true );
		this.setCreativeTab( ThaumicEnergistics.ModTab );

		// Get the material array
		this.materials = MaterialTypes.values();
	}
	
	/**
	 * Gets the icon for the specified material.
	 */
	@Override
	public IIcon getIconFromDamage( int damage )
	{
		int index = MathHelper.clamp_int( damage, 0, this.materials.length );
		
		return this.icons[index];
	}

	/**
	 * Registers each materials icon.
	 */
	@Override
	public void registerIcons( IIconRegister iconRegister )
	{

		// Create the icon array
		this.icons = new IIcon[this.materials.length];

		// Register each icon
		for( MaterialTypes material : this.materials )
		{
			this.icons[material.getID()] = iconRegister.registerIcon( material.getTextureLocation() );
		}
	}
	
	@Override
	public void getSubItems( Item item, CreativeTabs creativeTab, List itemList )
	{
		// Add each material item
		for( MaterialTypes material : this.materials )
		{
			itemList.add( new ItemStack( item, 1, material.getID() ) );
		}
	}

	@Override
	public String getUnlocalizedName( ItemStack itemStack )
	{
		int index = MathHelper.clamp_int( itemStack.getItemDamage(), 0, this.materials.length );
			
		return this.materials[index].getUnlocalizedName();
	}

	/**
	 * Enum of all materials
	 * 
	 * @author Nividica
	 * 
	 */
	public static enum MaterialTypes
	{
			DIFFUSION_CORE (0, "diffusion.core"),
			COALESCENCE_CORE (1, "coalescence.core");

		/**
		 * Numeric ID of the material.
		 */
		private int ID;

		/**
		 * Location of the material texture.
		 */
		private String textureLocation;

		/**
		 * Unlocalized name for the material.
		 */
		private String unlocalizedName;

		private MaterialTypes( int ID, String name )
		{
			this.ID = ID;

			this.textureLocation = ThaumicEnergistics.MOD_ID + ":material." + name;

			this.unlocalizedName = ThaumicEnergistics.MOD_ID + ".item.material." + name;
		}

		public int getID()
		{
			return this.ID;
		}

		public String getTextureLocation()
		{
			return this.textureLocation;
		}

		public String getUnlocalizedName()
		{
			return this.unlocalizedName;
		}
		
		public ItemStack getItemStack()
		{
			return ItemEnum.MATERIAL.getItemStackWithDamage( this.ordinal() );
		}
	}
}
