package thaumicenergistics.fml.classtransformers.ae;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import thaumicenergistics.fml.AClassTransformer;

/**
 * Applied Energistics class <em>CraftingTreeProcess</em>
 * <ul>
 * <li>Adds <em>this.world = world</em> to constructor.</li>
 * </ul>
 */
public class ClassTransformer_CraftingTreeProcess extends AClassTransformer {

    public ClassTransformer_CraftingTreeProcess() {
        super("appeng.crafting.CraftingTreeProcess");
    }

    private void transformConstructor(final MethodNode method) {
        int opSequence[] =
                new int[] {Opcodes.ILOAD, Opcodes.PUTFIELD, Opcodes.ALOAD, Opcodes.INVOKEVIRTUAL, Opcodes.ASTORE};
        AbstractInsnNode insertionPoint = this.findSequence(method.instructions, opSequence, true);

        // Insert this.world = world
        InsnList instructionList = new InsnList();

        // this
        instructionList.add(new VarInsnNode(Opcodes.ALOAD, 0));

        // world
        instructionList.add(new VarInsnNode(Opcodes.ALOAD, 6));

        // this.world = world
        instructionList.add(new FieldInsnNode(
                Opcodes.PUTFIELD, "appeng/crafting/CraftingTreeProcess", "world", "Lnet/minecraft/world/World;"));

        // Insert the new code
        method.instructions.insert(insertionPoint, instructionList);
    }

    @Override
    protected void onTransformFailure() {
        this.log("Recipes containing Thaumcraft's primordial pearl will not function with AE2's crafting system.");
    }

    @Override
    protected void transform(final ClassNode classNode) {
        // Transform methods
        for (MethodNode method : classNode.methods) {
            // Constructor
            if (method.name.equals(AClassTransformer.InstanceConstructorName)) {
                this.transformConstructor(method);
                break; // Stop searching.
            }
        }
    }
}
