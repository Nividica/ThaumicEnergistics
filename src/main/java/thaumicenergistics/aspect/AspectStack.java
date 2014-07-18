package thaumicenergistics.aspect;

import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.aspects.Aspect;

public class AspectStack
{
	public static AspectStack loadAspectStackFromNBT( NBTTagCompound nbt )
	{
		Aspect aspect = Aspect.aspects.get( nbt.getString( "AspectTag" ) );

		if ( aspect == null )
		{
			return null;
		}

		long amount = nbt.getLong( "Amount" );

		return new AspectStack( aspect, amount );
	}

	public Aspect aspect;

	public long amount;

	public AspectStack()
	{
		this.aspect = null;
		this.amount = 0;
	}

	public AspectStack( Aspect aspect, long amount )
	{
		this.aspect = aspect;

		this.amount = amount;
	}

	public AspectStack( AspectStack source )
	{
		this.aspect = source.aspect;

		this.amount = source.amount;
	}

	public AspectStack copy()
	{
		return new AspectStack( this );
	}
	
	public String getChatColor()
	{
		return this.aspect.getChatcolor();
	}
	
	public String getTag()
	{
		return this.aspect.getTag();
	}
	
	public String getName()
	{
		return this.aspect.getName();
	}

	public NBTTagCompound writeToNBT( NBTTagCompound nbt )
	{
		nbt.setString( "AspectTag", this.aspect.getTag() );

		nbt.setLong( "Amount", this.amount );

		return nbt;
	}
}
