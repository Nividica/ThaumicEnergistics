package thaumicenergistics.fml.classtransformers.tc;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import thaumicenergistics.fml.AClassTransformer;
import thaumicenergistics.fml.ThECore;

/**
 * Thaumcraft class <em>ItemGolemBell</em>
 * <ul>
 * <li>Adds hook to method <em>onLeftClickEntity</em></li>
 * </ul>
 * 
 * @author Nividica
 *
 */
public class ClassTransformer_ItemGolemBell
	extends AClassTransformer
{

	public ClassTransformer_ItemGolemBell()
	{
		super( "thaumcraft.common.entities.golems.ItemGolemBell" );
	}

	private void transformMethod_OnLeftClickEntity( final MethodNode method )
	{
		int opSequence[] = new int[] { Opcodes.GETSTATIC, Opcodes.ICONST_1, Opcodes.ILOAD, Opcodes.INVOKESPECIAL, Opcodes.ASTORE };

		// Locate the sequence
		AbstractInsnNode insertionPoint = this.findSequence( method.instructions, opSequence, true );

		// Insert the hook
		// GolemHooks.hook_Bell_OnLeftClickGolem( entity, dropped, player, ((EntityGolemBase)entity).hookHandlers )
		InsnList instructionList = new InsnList();

		// ((EntityGolemBase)entity)
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 3 ) );
		instructionList.add( new TypeInsnNode( Opcodes.CHECKCAST, GolemHookTransformHelper.CLASS_EntityGolemBase ) );

		// dropped
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 9 ) );

		// player
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 2 ) );

		// ((EntityGolemBase)entity).hookHandlers
		GolemHookTransformHelper.addGetField_hookHandlers( instructionList, 3, true );

		// GolemHooks.hook_Bell_OnLeftClickGolem( EntityGolemBase, ItemStack, EntityPlayer, Hashmap )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						GolemHookTransformHelper.CLASS_GolemHooks, "hook_Bell_OnLeftClickGolem",
						"(L" + GolemHookTransformHelper.CLASS_EntityGolemBase + ";Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;L" +
										GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";)V",
						false ) );

		// Insert the static call
		method.instructions.insert( insertionPoint, instructionList );
	}

	@Override
	protected void onTransformFailure()
	{
		ThECore.golemHooksTransformFailed = true;
	}

	@Override
	protected void transform( final ClassNode classNode )
	{
		// Transform methods
		for( MethodNode method : classNode.methods )
		{
			// Left click entity
			if( method.name.equals( "onLeftClickEntity" ) )
			{
				this.transformMethod_OnLeftClickEntity( method );
				return; // Stop searching.
			}
		}
	}

}
