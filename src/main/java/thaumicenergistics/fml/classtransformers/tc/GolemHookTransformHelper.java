package thaumicenergistics.fml.classtransformers.tc;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

class GolemHookTransformHelper {

    public static final String CLASS_EntityGolemBase = "thaumcraft/common/entities/golems/EntityGolemBase";
    public static final String CLASS_GolemHooks = "thaumicenergistics/common/integration/tc/GolemHooks";
    public static final String INTERFACE_IGolemHookHandler = "thaumicenergistics/api/entities/IGolemHookHandler";
    public static final String FIELD_EntityGolemBase_hookHandlers = "hookHandlers";
    public static final String FIELDTYPE_EntityGolemBase_hookHandlers = "java/util/HashMap";

    /**
     * Adds a reference to a golems hookHandlers field to the stack.
     *
     * @param instructionList
     * @param golemVar
     * @param checkCast       If the var specified is not type EntityGolemBase(such as just Entity or EntityLiving) a
     *                        cast check must be performed.
     */
    public static void addGetField_hookHandlers(final InsnList instructionList, final int golemVar,
            final boolean checkCast) {
        instructionList.add(new VarInsnNode(Opcodes.ALOAD, golemVar));
        if (checkCast) {
            instructionList.add(new TypeInsnNode(Opcodes.CHECKCAST, GolemHookTransformHelper.CLASS_EntityGolemBase));
        }
        instructionList.add(
                new FieldInsnNode(
                        Opcodes.GETFIELD,
                        GolemHookTransformHelper.CLASS_EntityGolemBase,
                        GolemHookTransformHelper.FIELD_EntityGolemBase_hookHandlers,
                        "L" + GolemHookTransformHelper.FIELDTYPE_EntityGolemBase_hookHandlers + ";"));
    }
}
