package thaumicenergistics.fml.classtransformers.tc;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import thaumicenergistics.fml.AClassTransformer;
import thaumicenergistics.fml.ThECore;

/**
 * Thaumcraft class <em>ItemGolemPlacer</em>
 * <ul>
 * <li>Adds hook to method <em>spawnCreature</em></li>
 * </ul>
 * 
 * @author Nividica
 *
 */
public class ClassTransformer_ItemGolemPlacer
	extends AClassTransformer
{

	public ClassTransformer_ItemGolemPlacer()
	{
		super( "thaumcraft.common.entities.golems.ItemGolemPlacer" );
	}

	private void transformMethod_SpawnCreature( final MethodNode method )
	{
		// Locate "golem.setup(side);"
		int opSequence[] = new int[] { Opcodes.PUTFIELD, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.INVOKEVIRTUAL };
		AbstractInsnNode insertionPoint = this.findSequence( method.instructions, opSequence, true );

		// Move back to the aload
		insertionPoint = insertionPoint.getPrevious().getPrevious();

		// Insert the hook
		// GolemHooks.hook_Placer_SpawnGolem( golem, stack, golem.hookHandlers )
		InsnList instructionList = new InsnList();

		// golem
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 12 ) );

		// stack
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 9 ) );

		// golem.hookHandlers
		GolemHookTransformHelper.addGetField_hookHandlers( instructionList, 12, false );

		// GolemHooks.hook_Placer_SpawnGolem( EntityGolemBase, ItemStack, Hashmap )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						GolemHookTransformHelper.CLASS_GolemHooks, "hook_Placer_SpawnGolem",
						"(L" + GolemHookTransformHelper.CLASS_EntityGolemBase + ";Lnet/minecraft/item/ItemStack;L" +
										GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";)V",
						false ) );

		// Insert the static call
		method.instructions.insertBefore( insertionPoint, instructionList );

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
			// Spawn creature
			if( method.name.equals( "spawnCreature" ) )
			{
				this.transformMethod_SpawnCreature( method );
				break; // Stop searching.
			}
		}
	}

}
