package thaumicenergistics.fml;

import cpw.mods.fml.relauncher.FMLRelaunchLog;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public abstract class AClassTransformer {
    protected static final String InstanceConstructorName = "<init>";

    private static Printer asmPrinter = new Textifier();

    private static TraceMethodVisitor asmVisitor = new TraceMethodVisitor(AClassTransformer.asmPrinter);

    /**
     * Canonical name of the class to be transformed.
     */
    public final String classCanonicalName;

    protected AClassTransformer(final String classCanonicalName) {
        this.classCanonicalName = classCanonicalName;
    }

    /**
     * Locates the first occurrence of the specified opcode.
     *
     * @param instructions
     * @param opcode
     * @return
     */
    protected AbstractInsnNode findFirstOpCode(final InsnList instructions, final int opcode) {
        for (int index = 0; index < instructions.size(); ++index) {
            if (instructions.get(index).getOpcode() == opcode) {
                return instructions.get(index);
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
    protected AbstractInsnNode findLastOpCode(final InsnList instructions, final int opcode) {
        for (int index = instructions.size() - 1; index > 0; --index) {
            if (instructions.get(index).getOpcode() == opcode) {
                return instructions.get(index);
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
    protected AbstractInsnNode findLastType(final InsnList instructions, final int type, int skip) {
        for (int index = instructions.size() - 1; index > 0; --index) {
            if (instructions.get(index).getType() == type) {
                if (--skip < 0) {
                    return instructions.get(index);
                }
            }
        }
        return null;
    }

    /**
     * Finds the next occurrence of the opcode after the specified instruction.
     *
     * @param fromInstruction
     * @param opcode
     * @return
     */
    protected AbstractInsnNode findNextOpCode(final AbstractInsnNode fromInstruction, final int opcode) {
        AbstractInsnNode nextInsn = fromInstruction;
        do {
            if (nextInsn.getOpcode() == opcode) {
                return nextInsn;
            }
        } while ((nextInsn = nextInsn.getNext()) != null);

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
    protected AbstractInsnNode findSequence(
            final InsnList instructions, final int opSequence[], final boolean skipNons) {
        int seqIndex = 0;
        AbstractInsnNode insertionPoint = null;
        for (int index = 0; index < instructions.size(); ++index) {
            // Get the instruction
            AbstractInsnNode ins = instructions.get(index);

            if (skipNons && (ins.getOpcode() == -1)) {
                continue;
            }

            // Does it match the sequence?
            if (ins.getOpcode() == opSequence[seqIndex]) {
                // Has the full sequence been found?
                if (++seqIndex == opSequence.length) {
                    // Found the full sequence
                    insertionPoint = ins;
                    break;
                }
            } else if (ins.getOpcode() == opSequence[0]) {
                // Restart sequence
                seqIndex = 1;
            } else {
                // Reset sequence
                seqIndex = 0;
            }
        }

        return insertionPoint;
    }

    /**
     * Adds an entry to the log.
     *
     * @param text
     */
    protected void log(final String text) {
        FMLRelaunchLog.log("ThE-Core", Level.INFO, text);
    }

    protected void logInstructionDetails(final AbstractInsnNode insn) {
        // Visit the instruction
        insn.accept(AClassTransformer.asmVisitor);

        // Create the string writer
        StringWriter sw = new StringWriter();

        // Print the details into the writer
        AClassTransformer.asmPrinter.print(new PrintWriter(sw));

        // Clear the printer
        AClassTransformer.asmPrinter.getText().clear();

        // Log the details
        this.log(sw.toString());
    }

    /**
     * Action to take if the transform fails.
     */
    protected abstract void onTransformFailure();

    /**
     * Internal call to transform the class.
     *
     * @param classBytes
     * @return
     */
    protected abstract void transform(ClassNode classNode);

    /**
     * Transforms the class.
     *
     * @param classBytes
     * @return
     */
    public final byte[] transformClass(final byte[] classBytes) {
        try {
            this.log(String.format("Transforming Class (%s)", this.classCanonicalName));

            // Create the class node and read in the class
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classBytes);
            classReader.accept(classNode, 0);

            // Transform
            this.transform(classNode);

            // Create the writer
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);

            // Return the modified class
            return writer.toByteArray();
        } catch (Exception e) {
            this.log(String.format("Unable to transform (%s)", this.classCanonicalName));
            this.onTransformFailure();
        }
        return classBytes;
    }
}
