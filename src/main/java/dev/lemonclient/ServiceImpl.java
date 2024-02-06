package dev.lemonclient;

import org.apache.commons.codec.digest.DigestUtils;

public class ServiceImpl {
    /*private static String getIP() {
        return "43.248.79.78";
    }

    private static int getPort() {
        return Integer.parseInt("10005");
    }

    private static String getPrefix() {
        return "[HWID]";
    }

    private static String getReturn() {
        return "[PASS]";
    }

    @UltraLock
    @MemoryCheck
    public static void init() {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "__protected__");
        if (!tempFile.exists()) tempFile.mkdir();
        tempFile.deleteOnExit();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader urlClassLoader = (URLClassLoader) classLoader.getParent();
        var clz = urlClassLoader.getClass();
        clz.getModule().addOpens("net.fabricmc.loader.impl.launch.knot", ServiceImpl.class.getModule());
        Arrays.stream(clz.getDeclaredMethods()).toList().stream().filter((m) -> m.getName().equals("addURL")).forEach(method -> {
            method.setAccessible(true);
            try {
                method.invoke(urlClassLoader, tempFile.toURI().toURL());
            } catch (IllegalAccessException | InvocationTargetException | MalformedURLException ignored) {
            }
        });

        try (var client = new Socket(getIP(), getPort())) {
            var input = new DataInputStream(client.getInputStream());
            var output = new DataOutputStream(client.getOutputStream());
            output.writeUTF(getPrefix() + getHwid());
            var tag = input.readUTF();
            if (tag.equals(getReturn())) {
                var byArray = input.readAllBytes();
                var inputBytes = new ByteArrayInputStream(byArray);
                try (var zip = new ZipInputStream(inputBytes)) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) {
                        if (entry.getName().endsWith(".class")) {
                            var bytes = zip.readAllBytes();
                            var name = new ClassReader(bytes).getClassName();
                            var temp = new File(tempFile.getAbsolutePath() + File.separator + name.replace('/', '\\') + ".class");
                            if (!temp.exists()) {
                                temp.getParentFile().mkdirs();
                                temp.createNewFile();
                            }
                            temp.deleteOnExit();
                            temp.setWritable(true);
                            try (var fileOut = new FileOutputStream(temp)) {
                                fileOut.write(bytes);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static byte[] readBytes(InputStream input) throws IOException {
        var buffer = new ByteArrayOutputStream(Math.max(8 * 1024, input.available()));
        copyTo(input, buffer);
        return buffer.toByteArray();
    }

    private static long copyTo(InputStream from, OutputStream to) throws IOException {
        var bytesCopied = 0L;
        var buffer = new byte[8 * 1024];
        var bytes = from.read(buffer);
        while (bytes >= 0) {
            to.write(buffer, 0, bytes);
            bytesCopied += bytes;
            bytes = from.read(buffer);
        }
        return bytesCopied;
    }*/

    public static String getHwid() {
        return DigestUtils.sha256Hex(
            System.getenv("os")
                + System.getProperty("os.name")
                + System.getProperty("os.arch")
                + System.getProperty("user.name")
                + System.getenv("PROCESSOR_LEVEL")
                + System.getenv("PROCESSOR_REVISION")
                + System.getenv("PROCESSOR_IDENTIFIER")
                + System.getenv("PROCESSOR_ARCHITEW6432")
        );
    }
}
