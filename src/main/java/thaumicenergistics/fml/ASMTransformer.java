package thaumicenergistics.fml;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import net.minecraft.launchwrapper.IClassTransformer;

/**
 * <strong>Modifications made by the transformer:</strong></br>
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
 * Thaumcraft class <em>ItemGolemBell</em>
 * <ul>
 * <li>Adds hook to method <em>onLeftClickEntity</em></li>
 * </ul>
 * 
 * Thaumcraft class <em>ItemGolemPlacer</em>
 * <ul>
 * <li>Adds hook to method <em>spawnCreature</em></li>
 * </ul>
 * 
 * Thaumcraft class <em>RenderGolemBase</em>
 * <ul>
 * <li>Adds hook to method <em>render</em></li>
 * </ul>
 * 
 * Thaumcraft class <em>Aspect</em>
 * <ul>
 * <li>Adds hook to <em>constructor</em></li>
 * </ul>
 * 
 * @author Nividica
 * 
 */
public class ASMTransformer
	implements IClassTransformer
{
	private static final String CLASS_EntityGolemBase = "thaumcraft/common/entities/golems/EntityGolemBase";
	private static final String CLASS_Aspect = "thaumcraft/api/aspects/Aspect";
	private static final String CLASS_GolemHooks = "thaumicenergistics/common/integration/tc/GolemHooks";
	private static final String CLASS_AspectHooks = "thaumicenergistics/common/integration/tc/AspectHooks";
	private static final String INTERFACE_IGolemHookHandler = "thaumicenergistics/api/entities/IGolemHookHandler";

	private static final String FIELD_EntityGolemBase_hookHandlers = "hookHandlers";
	private static final String FIELDTYPE_EntityGolemBase_hookHandlers = "java/util/HashMap";

	/**
	 * Adds a reference to a golems hookHandlers field to the stack.
	 * 
	 * @param instructionList
	 * @param golemVar
	 * @param checkCast
	 * If the var specified is not type EntityGolemBase(such as just Entity or EntityLiving) a cast check must be performed.
	 */
	private void addGetField_hookHandlers( final InsnList instructionList, final int golemVar, final boolean checkCast )
	{
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, golemVar ) );
		if( checkCast )
		{
			instructionList.add( new TypeInsnNode( Opcodes.CHECKCAST, CLASS_EntityGolemBase ) );
		}
		instructionList.add( new FieldInsnNode( Opcodes.GETFIELD,
						CLASS_EntityGolemBase,
						FIELD_EntityGolemBase_hookHandlers, "L" + FIELDTYPE_EntityGolemBase_hookHandlers + ";" ) );
	}

	/**
	 * Locates the first occurrence of the specified opcode.
	 * 
	 * @param instructions
	 * @param opcode
	 * @return
	 */
	private AbstractInsnNode findFirstOpCode( final InsnList instructions, final int opcode )
	{
		for( int index = 0; index < instructions.size(); ++index )
		{
			if( instructions.get( index ).getOpcode() == opcode )
			{
				return instructions.get( index );
			}
		}
		return null;
	}

	/**
	 * Locates the last occurrence of the specified opcode.
	 * 
	 * @param instructions
	 * @param opcode
	 * @return
	 */
	private AbstractInsnNode findLastOpCode( final InsnList instructions, final int opcode )
	{
		for( int index = instructions.size() - 1; index > 0; --index )
		{
			if( instructions.get( index ).getOpcode() == opcode )
			{
				return instructions.get( index );
			}
		}
		return null;
	}

	/**
	 * Locates the last occurrence of the specified type.
	 * 
	 * @param instructions
	 * @param type
	 * @param number
	 * of occurrences to skip
	 * @return
	 */
	private AbstractInsnNode findLastType( final InsnList instructions, final int type, int skip )
	{
		for( int index = instructions.size() - 1; index > 0; --index )
		{
			if( instructions.get( index ).getType() == type )
			{
				if( --skip < 0 )
				{
					return instructions.get( index );
				}
			}
		}
		return null;
	}

	/**
	 * Locates a sequence of instructions.
	 * 
	 * @param instructions
	 * @param opSequence
	 * @param skipNons
	 * If true all -1 instructions will be skipped
	 * @return The last instruction in the sequence.
	 */
	private AbstractInsnNode findSequence( final InsnList instructions, final int opSequence[], final boolean skipNons )
	{
		int seqIndex = 0;
		AbstractInsnNode insertionPoint = null;
		for( int index = 0; index < instructions.size(); ++index )
		{
			// Get the instruction
			AbstractInsnNode ins = instructions.get( index );

			if( skipNons && ( ins.getOpcode() == -1 ) )
			{
				continue;
			}

			// Does it match the sequence?
			if( ins.getOpcode() == opSequence[seqIndex] )
			{
				// Has the full sequence been found?
				if( ++seqIndex == opSequence.length )
				{
					// Found the full sequence
					insertionPoint = ins;
					break;
				}
			}
			else if( ins.getOpcode() == opSequence[0] )
			{
				// Restart sequence
				seqIndex = 1;
			}
			else
			{
				// Reset sequence
				seqIndex = 0;
			}
		}

		return insertionPoint;
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
		this.addGetField_hookHandlers( onEntityUpdate.instructions, 0, false );
		// GolemHooks.hook_onEntityUpdate( EntityGolemBase, Hashmap )
		onEntityUpdate.instructions.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						CLASS_GolemHooks, "hook_onEntityUpdate",
						"(L" + CLASS_EntityGolemBase + ";L" + FIELDTYPE_EntityGolemBase_hookHandlers + ";)V", false ) );

		// return
		onEntityUpdate.instructions.add( new InsnNode( Opcodes.RETURN ) );

		// Add the method to the class
		classNode.methods.add( onEntityUpdate );
	}

	/**
	 * Adds an entry to the log.
	 * 
	 * @param text
	 */
	private void log( final String text )
	{
		FMLRelaunchLog.log( "ThE-Core", Level.INFO, text );
	}

	private byte[] transformClass_Aspect( final byte[] classBytes )
	{
		// Create the class node and read in the class
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader( classBytes );
		classReader.accept( classNode, 0 );

		// Transform methods
		for( MethodNode method : classNode.methods )
		{
			// Constructor
			if( method.name.equals( "<init>" ) )
			{
				if( this.transformMethod_Aspect_init( method ) )
				{
					break; // Stop searching.
				}
			}
		}

		// Create the writer
		ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		classNode.accept( writer );

		// Return the modified class
		return writer.toByteArray();
	}

	private byte[] transformClass_CraftingTreeProcess( final byte[] classBytes )
	{
		// Create the class node and read in the class
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader( classBytes );
		classReader.accept( classNode, 0 );

		// Transform methods
		for( MethodNode method : classNode.methods )
		{
			// Constructor
			if( method.name.equals( "<init>" ) )
			{
				this.transformMethod_CraftingTreeProcess_init( method );
				break; // Stop searching.
			}
		}

		// Create the writer
		ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		classNode.accept( writer );

		// Return the modified class
		return writer.toByteArray();
	}

	private byte[] transformClass_GolemBase( final byte[] classBytes )
	{

		// Create the class node and read in the class
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader( classBytes );
		classReader.accept( classNode, 0 );

		// Add the handlers field
		// public HashMap<IGolemHookHandler, Object> hookHandlers;
		classNode.fields.add( new FieldNode( Opcodes.ACC_PUBLIC,
						FIELD_EntityGolemBase_hookHandlers,
						"L" + FIELDTYPE_EntityGolemBase_hookHandlers + ";",
						"L" + FIELDTYPE_EntityGolemBase_hookHandlers + "<L" + INTERFACE_IGolemHookHandler + ";Ljava/lang/Object;>;",
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
				this.transformMethod_GolemBase_SetupGolem( method );
			}
			// Custom interaction
			else if( method.name.equals( "customInteraction" ) )
			{
				this.transformMethod_GolemBase_CustomInteraction( method );
			}
			// Write entity NBT
			else if( method.name.equals( "func_70014_b" ) || method.name.equals( "writeEntityToNBT" ) )
			{
				this.transformMethod_GolemBase_WriteEntityToNBT( method );
			}
			//  Read entity from NBT
			else if( method.name.equals( "func_70037_a" ) || method.name.equals( "readEntityFromNBT" ) )
			{
				this.transformMethod_GolemBase_ReadEntityFromNBT( method );
			}
			// Entity Init
			else if( method.name.equals( "func_70088_a" ) || method.name.equals( "entityInit" ) )
			{
				this.transformMethod_GolemBase_EntityInit( method );
			}
		}

		// Add the onEntityUpdate method.
		this.insertMethod_GolemBase_onEntityUpdate( classNode, isObf );

		// Create the writer
		ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		classNode.accept( writer );

		// Return the modified class
		return writer.toByteArray();
	}

	private byte[] transformClass_ItemGolemBell( final byte[] classBytes )
	{
		// Create the class node and read in the class
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader( classBytes );
		classReader.accept( classNode, 0 );

		// Transform methods
		for( MethodNode method : classNode.methods )
		{
			// Left click entity
			if( method.name.equals( "onLeftClickEntity" ) )
			{
				this.transformMethod_ItemGolemBell_OnLeftClickEntity( method );
				break; // Stop searching.
			}
		}

		// Create the writer
		ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		classNode.accept( writer );

		// Return the modified class
		return writer.toByteArray();
	}

	private byte[] transformClass_ItemGolemPlacer( final byte[] classBytes )
	{
		// Create the class node and read in the class
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader( classBytes );
		classReader.accept( classNode, 0 );

		// Transform methods
		for( MethodNode method : classNode.methods )
		{
			// Spawn creature
			if( method.name.equals( "spawnCreature" ) )
			{
				this.transformMethod_ItemGolemPlacer_SpawnCreature( method );
				break; // Stop searching.
			}
		}

		// Create the writer
		ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		classNode.accept( writer );

		// Return the modified class
		return writer.toByteArray();
	}

	private byte[] transformClass_RenderGolemBase( final byte[] classBytes )
	{
		// Create the class node and read in the class
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader( classBytes );
		classReader.accept( classNode, 0 );

		// Transform methods
		for( MethodNode method : classNode.methods )
		{
			if( method.name.equals( "render" ) )
			{
				// Render
				this.transformMethod_RenderGolemBase_Render( method );
				break; // Stop searching.
			}
		}

		// Create the writer
		ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
		classNode.accept( writer );

		// Return the modified class
		return writer.toByteArray();
	}

	private boolean transformMethod_Aspect_init( final MethodNode method )
	{
		// Is this the full constructor?
		// Check description
		if( !"(Ljava/lang/String;I[Lthaumcraft/api/aspects/Aspect;Lnet/minecraft/util/ResourceLocation;I)V".equals( method.desc ) )
		{
			return false;
		}

		int opSequence[] = new int[] { Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.POP };
		AbstractInsnNode insertionPoint = this.findSequence( method.instructions, opSequence, false );

		// Insert the hook
		// AspectHooks.hook_AspectInit( this )
		InsnList instructionList = new InsnList();

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// AspectHooks.hook_AspectInit( Aspect )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						CLASS_AspectHooks, "hook_AspectInit",
						"(L" + CLASS_Aspect + ";)V", false ) );

		// Insert the static call
		method.instructions.insert( insertionPoint, instructionList );

		return true;

	}

	private void transformMethod_CraftingTreeProcess_init( final MethodNode method )
	{
		int opSequence[] = new int[] { Opcodes.ILOAD, Opcodes.PUTFIELD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.ASTORE };
		AbstractInsnNode insertionPoint = this.findSequence( method.instructions, opSequence, true );

		// Insert this.world = world
		InsnList instructionList = new InsnList();

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// world
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 6 ) );

		// this.world = world
		instructionList.add( new FieldInsnNode( Opcodes.PUTFIELD,
						"appeng/crafting/CraftingTreeProcess", "world", "Lnet/minecraft/world/World;" ) );

		// Insert the new code
		method.instructions.insert( insertionPoint, instructionList );

	}

	private void transformMethod_GolemBase_CustomInteraction( final MethodNode method )
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
		this.addGetField_hookHandlers( instructionList, 0, false );

		// GolemHooks.setupGolemHook( EntityGolemBase, Hashmap )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						CLASS_GolemHooks, "hook_CustomInteraction",
						"(L" + CLASS_EntityGolemBase + ";Lnet/minecraft/entity/player/EntityPlayer;L" + FIELDTYPE_EntityGolemBase_hookHandlers +
										";)Z",
						false ) );

		// Insert the return check
		instructionList.add( new JumpInsnNode( Opcodes.IFNE, lastLabel ) );

		// Insert the static call
		method.instructions.insertBefore( insertionPoint, instructionList );
	}

	private void transformMethod_GolemBase_EntityInit( final MethodNode method )
	{
		// Locate the return statement
		AbstractInsnNode insertionPoint = this.findLastOpCode( method.instructions, Opcodes.RETURN );

		// Insert the hookHandlers initializer
		// hookHandlers = new HashMap<IGolemHookHandler, Object>();
		InsnList instructionList = new InsnList();

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// new HashMap
		instructionList.add( new TypeInsnNode( Opcodes.NEW, FIELDTYPE_EntityGolemBase_hookHandlers ) );
		instructionList.add( new InsnNode( Opcodes.DUP ) );

		// Hashmap.<init>
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESPECIAL,
						FIELDTYPE_EntityGolemBase_hookHandlers, "<init>", "()V", false ) );

		// .hookHandlers =
		instructionList.add( new FieldInsnNode( Opcodes.PUTFIELD, CLASS_EntityGolemBase,
						FIELD_EntityGolemBase_hookHandlers, "L" + FIELDTYPE_EntityGolemBase_hookHandlers + ";" ) );

		// Insert the hook
		// GolemHooks.hook_EntityInit( this, this.hookHandlers );

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// this.hookHandlers
		this.addGetField_hookHandlers( instructionList, 0, false );

		// GolemHooks.hook_EntityInit( EntityGolemBase, Hashmap )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						CLASS_GolemHooks, "hook_EntityInit",
						"(L" + CLASS_EntityGolemBase + ";L" + FIELDTYPE_EntityGolemBase_hookHandlers + ";)V", false ) );

		// Insert the static call
		method.instructions.insertBefore( insertionPoint, instructionList );

	}

	private void transformMethod_GolemBase_ReadEntityFromNBT( final MethodNode method )
	{
		// Locate the super call
		AbstractInsnNode insertionPoint = this.findFirstOpCode( method.instructions, Opcodes.INVOKESPECIAL );

		// Insert the hook
		// GolemHooks.hook_ReadEntityFromNBT( this, this.hookHandlers, nbt);
		InsnList instructionList = new InsnList();

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// this.hookHandlers
		this.addGetField_hookHandlers( instructionList, 0, false );

		// nbt
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );

		// GolemHooks.hook_ReadEntityFromNBT( EntityGolemBase, Hashmap, NBTTagCompound )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						CLASS_GolemHooks, "hook_ReadEntityFromNBT",
						"(L" + CLASS_EntityGolemBase + ";L" + FIELDTYPE_EntityGolemBase_hookHandlers + ";" + "Lnet/minecraft/nbt/NBTTagCompound;" +
										")V",
						false ) );

		// Insert the static call
		method.instructions.insert( insertionPoint, instructionList );
	}

	private void transformMethod_GolemBase_SetupGolem( final MethodNode method )
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
		this.addGetField_hookHandlers( instructionList, 0, false );

		// GolemHooks.setupGolemHook( EntityGolemBase, Hashmap )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						CLASS_GolemHooks, "hook_SetupGolem",
						"(L" + CLASS_EntityGolemBase + ";L" + FIELDTYPE_EntityGolemBase_hookHandlers + ";)V", false ) );

		// Insert the static call
		method.instructions.insertBefore( insertionPoint, instructionList );

	}

	private void transformMethod_GolemBase_WriteEntityToNBT( final MethodNode method )
	{
		// Locate the return statement
		AbstractInsnNode insertionPoint = this.findLastOpCode( method.instructions, Opcodes.RETURN );

		// Insert the hook
		// GolemHooks.hook_WriteEntityToNBT( this, this.hookHandlers, nbt);
		InsnList instructionList = new InsnList();

		// this
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );

		// this.hookHandlers
		this.addGetField_hookHandlers( instructionList, 0, false );

		// nbt
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );

		// GolemHooks.hook_WriteEntityToNBT( EntityGolemBase, Hashmap, NBTTagCompound )
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						CLASS_GolemHooks, "hook_WriteEntityToNBT",
						"(L" + CLASS_EntityGolemBase + ";L" + FIELDTYPE_EntityGolemBase_hookHandlers + ";" + "Lnet/minecraft/nbt/NBTTagCompound;" +
										")V",
						false ) );

		// Insert the static call
		method.instructions.insertBefore( insertionPoint, instructionList );
	}

	private void transformMethod_ItemGolemBell_OnLeftClickEntity( final MethodNode method )
	{
		int opSequence[] = new int[] { Opcodes.GETSTATIC, Opcodes.ICONST_1, Opcodes.ILOAD, Opcodes.INVOKESPECIAL, Opcodes.ASTORE };

		// Locate the sequence
		AbstractInsnNode insertionPoint = this.findSequence( method.instructions, opSequence, true );

		// Insert the hook
		// GolemHooks.hook_Bell_OnLeftClickGolem( entity, dropped, player, ((EntityGolemBase)entity).hookHandlers )
		InsnList instructionList = new InsnList();

		// ((EntityGolemBase)entity)
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 3 ) );
		instructionList.add( new TypeInsnNode( Opcodes.CHECKCAST, CLASS_EntityGolemBase ) );

		// dropped
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 9 ) );

		// player
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 2 ) );

		// ((EntityGolemBase)entity).hookHandlers
		this.addGetField_hookHandlers( instructionList, 3, true );

		// GolemHooks.hook_Bell_OnLeftClickGolem( EntityGolemBase, ItemStack, EntityPlayer, Hashmap ) 
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						CLASS_GolemHooks, "hook_Bell_OnLeftClickGolem",
						"(L" + CLASS_EntityGolemBase + ";Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;L" +
										FIELDTYPE_EntityGolemBase_hookHandlers + ";)V",
						false ) );

		// Insert the static call
		method.instructions.insert( insertionPoint, instructionList );
	}

	private void transformMethod_ItemGolemPlacer_SpawnCreature( final MethodNode method )
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
		this.addGetField_hookHandlers( instructionList, 12, false );

		// GolemHooks.hook_Placer_SpawnGolem( EntityGolemBase, ItemStack, Hashmap ) 
		instructionList.add( new MethodInsnNode( Opcodes.INVOKESTATIC,
						CLASS_GolemHooks, "hook_Placer_SpawnGolem",
						"(L" + CLASS_EntityGolemBase + ";Lnet/minecraft/item/ItemStack;L" + FIELDTYPE_EntityGolemBase_hookHandlers + ";)V", false ) );

		// Insert the static call
		method.instructions.insertBefore( insertionPoint, instructionList );

	}

	private void transformMethod_RenderGolemBase_Render( final MethodNode method )
	{
		// Find the return
		AbstractInsnNode insertionPoint = this.findLastOpCode( method.instructions, Opcodes.RETURN );

		// Insert the hook
		// GolemHooks.hook_RenderGolem( e, e.hookHandlers, par2, par4, par6, par9 )
		InsnList instructionList = new InsnList();

		// e
		instructionList.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );

		// e.hookHandlers
		this.addGetField_hookHandlers( instructionList, 1, false );

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
						CLASS_GolemHooks, "hook_RenderGolem",
						"(L" + CLASS_EntityGolemBase + ";L" + FIELDTYPE_EntityGolemBase_hookHandlers + ";DDDF)V", false ) );

		// Insert the static call
		method.instructions.insertBefore( insertionPoint, instructionList );
	}

	@Override
	public byte[] transform( final String name, final String transformedName, final byte[] basicClass )
	{
		// AE2 Crafting Process?
		if( transformedName.equals( "appeng.crafting.CraftingTreeProcess" ) )
		{
			try
			{
				this.log( "Transforming Class (appeng.crafting.CraftingTreeProcess)" );
				return this.transformClass_CraftingTreeProcess( basicClass );
			}
			catch( Exception e )
			{
				this.log( "Unable to transform (appeng.crafting.CraftingTreeProcess)" );
				e.printStackTrace();
			}
		}
		// Thaumcraft class?
		else if( transformedName.startsWith( "thaumcraft" ) )
		{
			if( transformedName.equals( "thaumcraft.common.entities.golems.EntityGolemBase" ) )
			{
				try
				{
					this.log( "Transforming Class (thaumcraft.common.entities.golems.EntityGolemBase)" );
					return this.transformClass_GolemBase( basicClass );
				}
				catch( Exception e )
				{
					this.log( "Unable to transform (thaumcraft.common.entities.golems.EntityGolemBase)" );
					ThECore.golemHooksTransformFailed = true;
				}
			}

			else if( transformedName.equals( "thaumcraft.common.entities.golems.ItemGolemBell" ) )
			{
				try
				{
					this.log( "Transforming Class (thaumcraft.common.entities.golems.ItemGolemBell)" );
					return this.transformClass_ItemGolemBell( basicClass );
				}
				catch( Exception e )
				{
					this.log( "Unable to transform (thaumcraft.common.entities.golems.ItemGolemBell)" );
					ThECore.golemHooksTransformFailed = true;
				}
			}

			else if( transformedName.equals( "thaumcraft.common.entities.golems.ItemGolemPlacer" ) )
			{
				try
				{
					this.log( "Transforming Class (thaumcraft.common.entities.golems.ItemGolemPlacer)" );
					return this.transformClass_ItemGolemPlacer( basicClass );
				}
				catch( Exception e )
				{
					this.log( "Unable to transform (thaumcraft.common.entities.golems.ItemGolemPlacer)" );
					ThECore.golemHooksTransformFailed = true;
				}
			}

			else if( transformedName.equals( "thaumcraft.client.renderers.entity.RenderGolemBase" ) )
			{
				try
				{
					this.log( "Transforming Class (thaumcraft/client/renderers/entity/RenderGolemBase)" );
					return this.transformClass_RenderGolemBase( basicClass );
				}
				catch( Exception e )
				{
					this.log( "Unable to transform (thaumcraft/client/renderers/entity/RenderGolemBase)" );
					ThECore.golemHooksTransformFailed = true;
				}

			}

			else if( transformedName.equals( "thaumcraft.api.aspects.Aspect" ) )
			{
				try
				{
					this.log( "Transforming Class (thaumcraft.api.aspects.Aspect)" );
					return this.transformClass_Aspect( basicClass );
				}
				catch( Exception e )
				{
					this.log( "Unable to transform (thaumcraft.api.aspects.Aspect)" );
				}

			}
		}

		return basicClass;

	}

}
