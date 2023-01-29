package thaumicenergistics.fml.classtransformers.ae;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import thaumicenergistics.fml.AClassTransformer;

public class ClassTransformer_GuiMEMonitorable extends AClassTransformer {

    public ClassTransformer_GuiMEMonitorable() {
        super("appeng.client.gui.implementations.GuiMEMonitorable");
    }

    private void transformPostUpdate(final MethodNode method) {
        // Search for:
        // for( final IAEItemStack is : list )
        int opSequence[] = new int[] { Opcodes.IFEQ, Opcodes.ALOAD, Opcodes.INVOKEINTERFACE, Opcodes.CHECKCAST,
                Opcodes.ASTORE };
        AbstractInsnNode insertionPoint = this.findSequence(method.instructions, opSequence, true);

        // Get the end of loop jump
        JumpInsnNode EOL_Jump = (JumpInsnNode) this.findNextOpCode(insertionPoint, Opcodes.GOTO);

        // Insert hook:
        // if( AEHooks.isItemGUIBlacklisted( is ) ) continue;
        InsnList instructionList = new InsnList();

        // is
        instructionList.add(new VarInsnNode(Opcodes.ALOAD, 3));

        // Static call: isItemGUIBlacklisted( IAEItemStack )
        instructionList.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "thaumicenergistics/common/integration/AEHooks",
                        "isItemGUIBlacklisted",
                        "(Lappeng/api/storage/data/IAEItemStack;)Z",
                        false));

        // continue
        instructionList.add(new JumpInsnNode(Opcodes.IFNE, EOL_Jump.label));

        // Insert into method
        method.instructions.insert(insertionPoint, instructionList);
    }

    @Override
    protected void onTransformFailure() {
        // No action required
    }

    @Override
    protected void transform(final ClassNode classNode) {
        // Transform methods
        for (MethodNode method : classNode.methods) {
            // Constructor
            if (method.name.equals("postUpdate")) {
                this.transformPostUpdate(method);
                return; // Stop searching.
            }
        }
    }
}
