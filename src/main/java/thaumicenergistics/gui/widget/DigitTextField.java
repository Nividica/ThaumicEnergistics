package thaumicenergistics.gui.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class DigitTextField
	extends GuiTextField
{

	/**
	 * Creates the text field
	 * 
	 * @param fontRenderer
	 * @param x
	 * @param y
	 * @param length
	 * @param height
	 */
	public DigitTextField( final FontRenderer fontRenderer, final int x, final int y, final int length, final int height )
	{
		super( fontRenderer, x, y, length, height );
	}

	@Override
	public boolean textboxKeyTyped( final char keyChar, final int keyID )
	{
		// Ensure we have focus
		if( this.isFocused() )
		{

			// Is the backspace key being pressed?
			if( keyID == Keyboard.KEY_BACK )
			{
				// Move the cursor back a character
				this.deleteFromCursor( -1 );
				return true;
			}

			// Is the typed character a numeric digit?
			if( Character.isDigit( keyChar ) )
			{
				// Append to the text
				this.writeText( Character.toString( keyChar ) );
				return true;
			}

			// Is the text empty or the value of the text 0?
			if( this.getText().isEmpty() || this.getText().equals( "0" ) )
			{
				// Did the player type a minus sign?
				if( ( keyChar == '-' ) )
				{
					// Player intends for number to be negative
					this.setText( "-" );
					return true;
				}
			}

			return false;
		}

		return false;
	}
}
