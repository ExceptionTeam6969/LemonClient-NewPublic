package dev.lemonclient.utils;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.tutorial.TutorialStep;
import top.fl0wowp4rty.phantomshield.annotations.license.UltraLock;

import static dev.lemonclient.LemonClient.mc;

public class Wrapper {
    @PreInit
    @UltraLock
    public static void init() {
        skipTutorial();
        addServers();
    }

/*    private static void setIcon() {
        RenderSystem.assertInInitPhase();
        List<InputStream> list = List.of(Objects.requireNonNull(Wrapper.class.getResourceAsStream("/assets/lemon-client/icons/icon_16x16.png")),
            Objects.requireNonNull(Wrapper.class.getResourceAsStream("/assets/lemon-client/icons/icon_32x32.png")),
            Objects.requireNonNull(Wrapper.class.getResourceAsStream("/assets/lemon-client/icons/icon_48x48.png")),
            Objects.requireNonNull(Wrapper.class.getResourceAsStream("/assets/lemon-client/icons/icon_128x128.png")),
            Objects.requireNonNull(Wrapper.class.getResourceAsStream("/assets/lemon-client/icons/icon_256x256.png")));
        List<ByteBuffer> list2 = new ArrayList<>(list.size());

        try {
            MemoryStack memoryStack = MemoryStack.stackPush();

            try {
                GLFWImage.Buffer buffer = GLFWImage.malloc(list.size(), memoryStack);

                for (int i = 0; i < list.size(); ++i) {
                    NativeImage nativeImage = NativeImage.read(list.get(i));

                    try {
                        ByteBuffer byteBuffer = MemoryUtil.memAlloc(nativeImage.getWidth() * nativeImage.getHeight() * 4);
                        list2.add(byteBuffer);
                        byteBuffer.asIntBuffer().put(nativeImage.copyPixelsRgba());
                        buffer.position(i);
                        buffer.width(nativeImage.getWidth());
                        buffer.height(nativeImage.getHeight());
                        buffer.pixels(byteBuffer);
                    } catch (Throwable var19) {
                        try {
                            nativeImage.close();
                        } catch (Throwable var18) {
                            var19.addSuppressed(var18);
                        }

                        throw var19;
                    }

                    nativeImage.close();
                }

                GLFW.glfwSetWindowIcon(mc.getWindow().getHandle(), buffer.position(0));
            } catch (Throwable var20) {
                try {
                    memoryStack.close();
                } catch (Throwable var17) {
                    var20.addSuppressed(var17);
                }

                try {
                    throw var20;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            memoryStack.close();
        } finally {
            list2.forEach(MemoryUtil::memFree);
        }
    }*/

    public static void skipTutorial() {
        mc.getTutorialManager().setStep(TutorialStep.NONE);
    }

    @UltraLock
    public static void addServers() {
        ServerList servers = new ServerList(mc);
        servers.loadFile();

        boolean b = false;
        for (int i = 0; i < servers.size(); i++) {
            ServerInfo server = servers.get(i);

            if (server.address.contains("pvp.obsserver.cn") || server.address.contains("2b2t.xin")) {
                b = true;
                break;
            }
        }

        if (!b) {
            servers.add(new ServerInfo("水晶PvP训练服", "pvp.obsserver.cn", true), false);
            servers.add(new ServerInfo("2B2T中国服", "2b2t.xin", true), false);
            servers.saveFile();
        }
    }
}
