package thaumicenergistics.fml.classtransformers.tc;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import thaumicenergistics.fml.AClassTransformer;
import thaumicenergistics.fml.ThECore;

/**
 * Thaumcraft class <em>RenderGolemBase</em>
 * <ul>
 * <li>Adds hook to method <em>render</em></li>
 * </ul>
 *
 * @author Nividica
 *
 */
public class ClassTransformer_RenderGolemBase
	extends AClassTransformer
{

	public ClassTransformer_RenderGolemBase()
	{
		super( "thaumcraft.client.renderers.entity.RenderGolemBase" );
	}

	private void transformMethod_Render( final MethodNode method )
	{
		// Find the return
		AbstractInsnNode insertionPoint = this.findLastOpCode( method.instructions, Opcodes.RETURN );

		// Insert the hook
		// GolemHooks.hook_RenderGolem( e, e.hookHandlers, par2, par4, par6, par9 )
		InsnList instructionList = new InsnList();

		// e
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );

		// e.hookHandlers
		GolemHookTransformHelper.addGetField_hookHandlers( instructionList, 1, false );

		// par2
		instructionList.add( new VarInsnNode( Opcodes.DLOAD, 2 ) );

		// par4
		instructionList.add( new VarInsnNode( Opcodes.DLOAD, 4 ) );

		// par6
		instructionList.add( new VarInsnNode( Opcodes.DLOAD, 6 ) );

		// par9
		instructionList.add( new VarInsnNode( Opcodes.FLOAD, 9 ) );

		// GolemHooks.hook_RenderGolem( EntityGolemBase, Hashmap, double, double, double, float )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						GolemHookTransformHelper.CLASS_GolemHooks, "hook_RenderGolem",
						"(L" + GolemHookTransformHelper.CLASS_EntityGolemBase + ";L" +
										GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";DDDF)V",
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
			if( method.name.equals( "render" ) )
			{
				// Render
				this.transformMethod_Render( method );
				break; // Stop searching.
			}
		}
	}

}
