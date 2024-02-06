package dev.lemonclient.asm.transformers;

import dev.lemonclient.LemonClient;
import dev.lemonclient.asm.AsmHelper;
import dev.lemonclient.asm.AsmTransformer;
import dev.lemonclient.asm.Descriptor;
import dev.lemonclient.asm.MethodInfo;
import dev.lemonclient.events.render.GetFovEvent;
import meteordevelopment.orbit.IEventBus;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Arrays;

public class GameRendererTransformer extends AsmTransformer {
    private final MethodInfo getFovMethod;

    public GameRendererTransformer() {
        super(mapClassName("net/minecraft/class_757"));

        getFovMethod = new MethodInfo("net/minecraft/class_4184", null, new Descriptor("Lnet/minecraft/class_4184;", "F", "Z", "D"), true);
    }

    @Override
    public void transform(ClassNode klass) {
        // Modify GameRenderer.getFov()
        MethodNode method = getMethod(klass, getFovMethod);
        if (method == null) error("[Lemon Client] Could not find method GameRenderer.getFov()");

        int injectionCount = 0;

        //noinspection DataFlowIssue
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof LdcInsnNode in && in.cst instanceof Double && (double) in.cst == 90) {
                InsnList insns = new InsnList();
                generateEventCall(insns, new LdcInsnNode(in.cst));

                method.instructions.insert(insn, insns);
                method.instructions.remove(insn);
                injectionCount++;
            } else if (
                (insn instanceof MethodInsnNode in1 && in1.name.equals("intValue") && insn.getNext() instanceof InsnNode _in && _in.getOpcode() == Opcodes.I2D)
                    ||
                    (insn instanceof MethodInsnNode in2 && in2.owner.equals(klass.name) && in2.name.startsWith("redirect") && in2.name.endsWith("getFov")) // Wi Zoom compatibility
            ) {
                InsnList insns = new InsnList();

                insns.add(new VarInsnNode(Opcodes.DSTORE, method.maxLocals));
                generateEventCall(insns, new VarInsnNode(Opcodes.DLOAD, method.maxLocals));

                method.instructions.insert(insn.getNext(), insns);
                injectionCount++;
            }
        }

        if (injectionCount < 2) error("[Lemon Client] Failed to modify GameRenderer.getFov()");
    }

    private void generateEventCall(InsnList insns, AbstractInsnNode loadPreviousFov) {
        insns.add(new FieldInsnNode(Opcodes.GETSTATIC, AsmHelper.getClassPath(LemonClient.class), Arrays.stream(LemonClient.class.getDeclaredFields()).filter(f -> f.getType().equals(IEventBus.class)).findFirst().orElseGet(null).getName(), "Lmeteordevelopment/orbit/IEventBus;"));
        insns.add(loadPreviousFov);
        String eventName = AsmHelper.getClassPath(GetFovEvent.class);
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, eventName, AsmHelper.findMethod(GetFovEvent.class, double.class).getName(), "(D)L{};".replace("{}", eventName)));
        insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "meteordevelopment/orbit/IEventBus", "post", "(Ljava/lang/Object;)Ljava/lang/Object;"));
        insns.add(new TypeInsnNode(Opcodes.CHECKCAST, eventName));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, eventName, AsmHelper.findField(GetFovEvent.class, double.class).getName(), "D"));
    }
}
