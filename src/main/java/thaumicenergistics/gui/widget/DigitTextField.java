package thaumicenergistics.gui.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class DigitTextField
	extends GuiTextField
{

	public DigitTextField( FontRenderer fontRenderer, int x, int y, int length, int height )
	{
		super( fontRenderer, x, y, length, height );
	}

	@Override
	public boolean textboxKeyTyped( char keyChar, int keyID )
	{
		if( this.isFocused() )
		{
			switch ( keyChar )
			{
				case '\001':
					this.setCursorPositionEnd();
					this.setSelectionPos( 0 );
					return true;

				case '\003':
					GuiScreen.setClipboardString( this.getSelectedText() );
					return true;

				case '\026':
					this.writeText( GuiScreen.getClipboardString() );
					return true;

				case '\030':
					GuiScreen.setClipboardString( this.getSelectedText() );
					this.writeText( "" );
					return true;
			}

			switch ( keyID )
			{
				case 1:
					this.setFocused( false );
					return true;

				case 14:
					if( GuiScreen.isCtrlKeyDown() )
					{
						this.deleteWords( -1 );
					}
					else
					{
						this.deleteFromCursor( -1 );
					}
					return true;

				case 199:
					if( GuiScreen.isShiftKeyDown() )
					{
						this.setSelectionPos( 0 );
					}
					else
					{
						this.setCursorPositionZero();
					}
					return true;

				case 203:
					if( GuiScreen.isShiftKeyDown() )
					{
						if( GuiScreen.isCtrlKeyDown() )
						{
							this.setSelectionPos( this.getNthWordFromPos( -1, this.getSelectionEnd() ) );
						}
						else
						{
							this.setSelectionPos( this.getSelectionEnd() - 1 );
						}
					}
					else if( GuiScreen.isCtrlKeyDown() )
					{
						this.setCursorPosition( this.getNthWordFromCursor( -1 ) );
					}
					else
					{
						this.moveCursorBy( -1 );
					}
					return true;

				case 205:
					if( GuiScreen.isShiftKeyDown() )
					{
						if( GuiScreen.isCtrlKeyDown() )
						{
							this.setSelectionPos( this.getNthWordFromPos( 1, this.getSelectionEnd() ) );
						}
						else
						{
							this.setSelectionPos( this.getSelectionEnd() + 1 );
						}
					}
					else if( GuiScreen.isCtrlKeyDown() )
					{
						this.setCursorPosition( this.getNthWordFromCursor( 1 ) );
					}
					else
					{
						this.moveCursorBy( 1 );
					}

					return true;

				case 207:
					if( GuiScreen.isShiftKeyDown() )
					{
						this.setSelectionPos( this.getText().length() );
					}
					else
					{
						this.setCursorPositionEnd();
					}
					return true;

				case 211:
					if( GuiScreen.isCtrlKeyDown() )
					{
						this.deleteWords( 1 );
					}
					else
					{
						this.deleteFromCursor( 1 );
					}

					return true;
			}

			if( Character.isDigit( keyChar ) )
			{
				this.writeText( Character.toString( keyChar ) );

				return true;
			}

			if( ( keyChar == '-' ) && ( this.getText().isEmpty() ) )
			{
				this.writeText( Character.toString( keyChar ) );

				return true;
			}

			return false;
		}

		return false;
	}
}
