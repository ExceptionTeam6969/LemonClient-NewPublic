package dev.lemonclient.asm.transformers;

import dev.lemonclient.asm.AsmTransformer;
import dev.lemonclient.asm.Descriptor;
import dev.lemonclient.asm.MethodInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class CanvasWorldRendererTransformer extends AsmTransformer {
    private final MethodInfo renderWorldMethod, drawMethod;

    public CanvasWorldRendererTransformer() {
        super("grondag.canvas.render.world.CanvasWorldRenderer");

        renderWorldMethod = new MethodInfo(null, "renderWorld", null, false);

        // OutlineVertexConsumerProvider.draw()
        drawMethod = new MethodInfo("net/minecraft/class_4618", "method_23285", new Descriptor("V"), true);
    }

    @Override
    public void transform(ClassNode klass) {
        // Inject PostProcessShaders.endRender() after OutlineVertexConsumerProvider.draw() in CanvasWorldRenderer
        MethodNode method = getMethod(klass, renderWorldMethod);
        if (method == null) return;

        for (AbstractInsnNode insn : method.instructions) {
            if (!(insn instanceof MethodInsnNode in)) continue;

            if (drawMethod.equals(in)) {
                method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/lemonclient/asm/AsmHelper", "endRenderPost", "()V"));
                break;
            }
        }
    }
}
