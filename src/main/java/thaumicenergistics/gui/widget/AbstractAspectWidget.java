package thaumicenergistics.gui.widget;

import thaumcraft.api.aspects.Aspect;

public abstract class AbstractAspectWidget
	extends AbstractWidget
{
	
	protected Aspect aspect;

	public AbstractAspectWidget( IWidgetHost hostGui, Aspect aspect, int xPos, int yPos )
	{
		super( hostGui, xPos, yPos );
		
		this.aspect = aspect;
	}
	
	public Aspect getAspect()
	{
		return this.aspect;
	}
	

	public void setAspect( Aspect aspect )
	{
		this.aspect = aspect;
	}
}
