package thaumicenergistics.fml.classtransformers.tc;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import thaumicenergistics.fml.AClassTransformer;
import thaumicenergistics.fml.ThECore;

/**
 * Thaumcraft class <em>EntityGolemBase</em>
 * <ul>
 * <li>Adds field <em>hookHandlers</em> of type HashMap</li>
 * <li>Adds hook to method <em>setupGolem</em></li>
 * <li>Adds hook to method <em>customInteraction</em></li>
 * <li>Adds hook to method <em>writeEntityNBT</em></li>
 * <li>Adds hook to method <em>readEntityFromNBT</em></li>
 * <li>Adds hook to method <em>func_70088_a | entityInit</em></li>
 * <li>Adds hook to method <em>func_70030_z | onEntityUpdate</em></li>
 * </ul>
 * 
 * @author Nividica
 *
 */
public class ClassTransformer_EntityGolemBase
	extends AClassTransformer
{

	public ClassTransformer_EntityGolemBase()
	{
		super( "thaumcraft.common.entities.golems.EntityGolemBase" );
	}

	private void insertMethod_GolemBase_onEntityUpdate( final ClassNode classNode, final boolean isObf )
	{
		// Create the onEntityUpdate() method
		String oEU_Name = ( isObf ? "func_70030_z" : "onEntityUpdate" );
		MethodNode onEntityUpdate = new MethodNode( Opcodes.ACC_PUBLIC,
						oEU_Name, "()V", null, null );

		// super.onEntityUpdate()
		onEntityUpdate.instructions.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );
		onEntityUpdate.instructions.add( new MethodInsnNode( Opcodes.INVOKESPECIAL,
						"net/minecraft/entity/monster/EntityGolem", oEU_Name, "()V", false ) );

		// GolemHooks.hook_onEntityUpdate( this, this.hookHandlers )
		// this
		onEntityUpdate.instructions.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );
		// this.hookHandlers
		GolemHookTransformHelper.addGetField_hookHandlers( onEntityUpdate.instructions, 0, false );
		// GolemHooks.hook_onEntityUpdate( EntityGolemBase, Hashmap )
		onEntityUpdate.instructions.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						GolemHookTransformHelper.CLASS_GolemHooks, "hook_onEntityUpdate",
						"(L" + GolemHookTransformHelper.CLASS_EntityGolemBase + ";L" + GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";)V", false ) );

		// return
		onEntityUpdate.instructions.add( new InsnNode( Opcodes.RETURN ) );

		// Add the method to the class
		classNode.methods.add( onEntityUpdate );
	}

	private void transformMethod_CustomInteraction( final MethodNode method )
	{
		int opSequence[] = new int[] { Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.ICONST_M1 };
		AbstractInsnNode insertionPoint = this.findSequence( method.instructions, opSequence, false );

		// Go two back
		insertionPoint = insertionPoint.getPrevious().getPrevious();

		// Find the last label
		LabelNode lastLabel = (LabelNode)this.findLastType( method.instructions, AbstractInsnNode.LABEL, 1 );

		// Insert the hook
		// GolemHooks.hook_CustomInteraction( this, player, this.hookHandlers );
		InsnList instructionList = new InsnList();

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// player
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );

		// this.hookHandlers
		GolemHookTransformHelper.addGetField_hookHandlers( instructionList, 0, false );

		// GolemHooks.setupGolemHook( EntityGolemBase, Hashmap )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						GolemHookTransformHelper.CLASS_GolemHooks, "hook_CustomInteraction",
						"(L" + GolemHookTransformHelper.CLASS_EntityGolemBase + ";Lnet/minecraft/entity/player/EntityPlayer;L" +
										GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers +
										";)Z",
						false ) );

		// Insert the return check
		instructionList.add( new JumpInsnNode( Opcodes.IFNE, lastLabel ) );

		// Insert the static call
		method.instructions.insertBefore( insertionPoint, instructionList );
	}

	private void transformMethod_EntityInit( final MethodNode method )
	{
		// Locate the return statement
		AbstractInsnNode insertionPoint = this.findLastOpCode( method.instructions, Opcodes.RETURN );

		// Insert the hookHandlers initializer
		// hookHandlers = new HashMap<IGolemHookHandler, Object>();
		InsnList instructionList = new InsnList();

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// new HashMap
		instructionList.add( new TypeInsnNode( Opcodes.NEW, GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers ) );
		instructionList.add( new InsnNode( Opcodes.DUP ) );

		// Hashmap.<init>
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESPECIAL,
						GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers, "<init>", "()V", false ) );

		// .hookHandlers =
		instructionList.add( new FieldInsnNode( Opcodes.PUTFIELD, GolemHookTransformHelper.CLASS_EntityGolemBase,
						GolemHookTransformHelper.FIELD_EntityGolemBase_hookHandlers, "L" + GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";" ) );

		// Insert the hook
		// GolemHooks.hook_EntityInit( this, this.hookHandlers );

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// this.hookHandlers
		GolemHookTransformHelper.addGetField_hookHandlers( instructionList, 0, false );

		// GolemHooks.hook_EntityInit( EntityGolemBase, Hashmap )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						GolemHookTransformHelper.CLASS_GolemHooks, "hook_EntityInit",
						"(L" + GolemHookTransformHelper.CLASS_EntityGolemBase + ";L" + GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";)V", false ) );

		// Insert the static call
		method.instructions.insertBefore( insertionPoint, instructionList );

	}

	private void transformMethod_ReadEntityFromNBT( final MethodNode method )
	{
		// Locate the super call
		AbstractInsnNode insertionPoint = this.findFirstOpCode( method.instructions, Opcodes.INVOKESPECIAL );

		// Insert the hook
		// GolemHooks.hook_ReadEntityFromNBT( this, this.hookHandlers, nbt);
		InsnList instructionList = new InsnList();

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// this.hookHandlers
		GolemHookTransformHelper.addGetField_hookHandlers( instructionList, 0, false );

		// nbt
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );

		// GolemHooks.hook_ReadEntityFromNBT( EntityGolemBase, Hashmap, NBTTagCompound )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						GolemHookTransformHelper.CLASS_GolemHooks, "hook_ReadEntityFromNBT",
						"(L" + GolemHookTransformHelper.CLASS_EntityGolemBase + ";L" + GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";" +
										"Lnet/minecraft/nbt/NBTTagCompound;" +
										")V",
						false ) );

		// Insert the static call
		method.instructions.insert( insertionPoint, instructionList );
	}

	private void transformMethod_SetupGolem( final MethodNode method )
	{
		// Set the insertion point to the instruction before return
		// Previous node is important, since the method returns a value, the previous
		// instruction is what sets that return value.
		AbstractInsnNode insertionPoint = this.findLastOpCode( method.instructions, Opcodes.IRETURN );
		insertionPoint = insertionPoint.getPrevious();

		// Insert the hook
		// GolemHooks.setupGolemHook( this, this.hookHandlers );
		InsnList instructionList = new InsnList();

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// this.hookHandlers
		GolemHookTransformHelper.addGetField_hookHandlers( instructionList, 0, false );

		// GolemHooks.setupGolemHook( EntityGolemBase, Hashmap )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						GolemHookTransformHelper.CLASS_GolemHooks, "hook_SetupGolem",
						"(L" + GolemHookTransformHelper.CLASS_EntityGolemBase + ";L" + GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";)V", false ) );

		// Insert the static call
		method.instructions.insertBefore( insertionPoint, instructionList );

	}

	private void transformMethod_WriteEntityToNBT( final MethodNode method )
	{
		// Locate the return statement
		AbstractInsnNode insertionPoint = this.findLastOpCode( method.instructions, Opcodes.RETURN );

		// Insert the hook
		// GolemHooks.hook_WriteEntityToNBT( this, this.hookHandlers, nbt);
		InsnList instructionList = new InsnList();

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// this.hookHandlers
		GolemHookTransformHelper.addGetField_hookHandlers( instructionList, 0, false );

		// nbt
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );

		// GolemHooks.hook_WriteEntityToNBT( EntityGolemBase, Hashmap, NBTTagCompound )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						GolemHookTransformHelper.CLASS_GolemHooks, "hook_WriteEntityToNBT",
						"(L" + GolemHookTransformHelper.CLASS_EntityGolemBase + ";L" + GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";" +
										"Lnet/minecraft/nbt/NBTTagCompound;" +
										")V",
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
		// Add the handlers field
		// public HashMap<IGolemHookHandler, Object> hookHandlers;
		classNode.fields.add( new FieldNode( Opcodes.ACC_PUBLIC,
						GolemHookTransformHelper.FIELD_EntityGolemBase_hookHandlers,
						"L" + GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";",
						"L" + GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + "<L" + GolemHookTransformHelper.INTERFACE_IGolemHookHandler +
										";Ljava/lang/Object;>;",
						null ) );

		boolean isObf = false;

		// Transform methods
		for( MethodNode method : classNode.methods )
		{
			// Look for an obfuscated method
			if( method.name.equals( "func_70014_b" ) )
			{
				isObf = true;
			}
			// Setup golem
			if( method.name.equals( "setupGolem" ) )
			{
				this.transformMethod_SetupGolem( method );
			}
			// Custom interaction
			else if( method.name.equals( "customInteraction" ) )
			{
				this.transformMethod_CustomInteraction( method );
			}
			// Write entity NBT
			else if( method.name.equals( "func_70014_b" ) || method.name.equals( "writeEntityToNBT" ) )
			{
				this.transformMethod_WriteEntityToNBT( method );
			}
			//  Read entity from NBT
			else if( method.name.equals( "func_70037_a" ) || method.name.equals( "readEntityFromNBT" ) )
			{
				this.transformMethod_ReadEntityFromNBT( method );
			}
			// Entity Init
			else if( method.name.equals( "func_70088_a" ) || method.name.equals( "entityInit" ) )
			{
				this.transformMethod_EntityInit( method );
			}
		}

		// Add the onEntityUpdate method.
		this.insertMethod_GolemBase_onEntityUpdate( classNode, isObf );
	}

}
