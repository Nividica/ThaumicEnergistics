package thaumicenergistics.fml.classtransformers.tc;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import thaumicenergistics.fml.AClassTransformer;

/**
 * Thaumcraft class <em>Aspect</em>
 * <ul>
 * <li>Adds hook to <em>constructor</em></li>
 * </ul>
 */
public class ClassTransformer_Aspect extends AClassTransformer {

    private static final String CLASS_Aspect = "thaumcraft/api/aspects/Aspect";
    private static final String CLASS_AspectHooks = "thaumicenergistics/common/integration/tc/AspectHooks";

    public ClassTransformer_Aspect() {
        super("thaumcraft.api.aspects.Aspect");
    }

    private boolean transformConstructor(final MethodNode method) {
        // Is this the full constructor?
        // Check description
        if (!"(Ljava/lang/String;I[Lthaumcraft/api/aspects/Aspect;Lnet/minecraft/util/ResourceLocation;I)V"
                .equals(method.desc)) {
            return false;
        }

        int opSequence[] = new int[] { Opcodes.GETSTATIC, Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL,
                Opcodes.POP };
        AbstractInsnNode insertionPoint = this.findSequence(method.instructions, opSequence, false);

        // Insert the hook
        // AspectHooks.hook_AspectInit( this )
        InsnList instructionList = new InsnList();

        // this
        instructionList.add(new VarInsnNode(Opcodes.ALOAD, 0));

        // AspectHooks.hook_AspectInit( Aspect )
        instructionList.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        ClassTransformer_Aspect.CLASS_AspectHooks,
                        "hook_AspectInit",
                        "(L" + ClassTransformer_Aspect.CLASS_Aspect + ";)V",
                        false));

        // Insert the static call
        method.instructions.insert(insertionPoint, instructionList);

        return true;
    }

    @Override
    protected void onTransformFailure() {
        // No action required.
    }

    @Override
    protected void transform(final ClassNode classNode) {
        // Transform methods
        for (MethodNode method : classNode.methods) {
            // Constructor
            if (method.name.equals(AClassTransformer.InstanceConstructorName)) {
                if (this.transformConstructor(method)) {
                    return; // Stop searching.
                }
            }
        }
    }
}
