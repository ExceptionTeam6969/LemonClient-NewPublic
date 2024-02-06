package dev.lemonclient
/*
import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.ClassReader
import top.fl0wowp4rty.phantomshield.annotations.Native
import top.fl0wowp4rty.phantomshield.annotations.license.MemoryCheck
import top.fl0wowp4rty.phantomshield.annotations.license.UltraLock
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.net.MalformedURLException
import java.net.Socket
import java.net.URLClassLoader
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

//@Native // 如果加了@Native注解，就无法连接至我的云载服务器，不知道为什么
object ServiceImpl {
    private val IP = String(Base64.getDecoder().decode("NDMuMjQ4Ljc5Ljc4"))
    private val PORT = String(Base64.getDecoder().decode("MTAwMDQ=")).toInt()

    @UltraLock
    @MemoryCheck
    fun init() {
        // Modify classpath
        val tempFile = File(
            "${System.getProperty("java.io.tmpdir")}${File.separator}${
                String(
                    Base64.getDecoder().decode("X19wcm90ZWN0ZWRfXw==")
                )
            }"
        )
        if (!tempFile.exists()) tempFile.mkdir()
        tempFile.deleteOnExit()
        val classLoader = Thread.currentThread().contextClassLoader
        val urlClassLoader = classLoader.parent as URLClassLoader
        val clz = urlClassLoader.javaClass
        clz.module.addOpens("net.fabricmc.loader.impl.launch.knot", ServiceImpl::class.java.module)
        clz.declaredMethods.filter { it.name == "addURL" }.forEach { method ->
            method.isAccessible = true
            try {
                method.invoke(urlClassLoader, tempFile.toURI().toURL())
            } catch (e: IllegalAccessException) {
                // throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                // throw RuntimeException(e)
            } catch (e: MalformedURLException) {
                // throw RuntimeException(e)
            }
        }

        Socket(IP, PORT).use { client ->
            val input = DataInputStream(client.getInputStream())
            val output = DataOutputStream(client.getOutputStream())
            val hwid = getValue()
            output.writeUTF("${String(Base64.getDecoder().decode("W0hXSURd"))}$hwid")
            val tag = input.readUTF()
            // println(tag)
            if (tag == String(Base64.getDecoder().decode("W1BBU1Nd"))) {
                val byteArray = input.readAllBytes()
                val inputBytes = ByteArrayInputStream(byteArray)
                // println(inputBytes.available())
                // println(Arrays.toString(byteArray))
                ZipInputStream(inputBytes).use { zip ->
                    var entry: ZipEntry?
                    while (zip.nextEntry.also { entry = it } != null) {
                        // println("Zip avail " + zip.available())
                        // println("Found entry " + entry.getName())
                        if (entry!!.name.endsWith(String(Base64.getDecoder().decode("LmNsYXNz")))) {
                            // println("Found valid entry " + entry.getName())
                            val bytes = zip.readAllBytes()
                            val name = ClassReader(bytes).className.replace('/', '\\') + String(
                                Base64.getDecoder().decode("LmNsYXNz")
                            )
                            val temp = File("${tempFile.absolutePath}${File.separator}$name")
                            if (!temp.exists()) {
                                temp.parentFile.mkdirs()
                                temp.createNewFile()
                            }
                            temp.deleteOnExit()
                            temp.setWritable(true)
                            // println(temp.absolutePath)
                            try {
                                FileOutputStream(temp).use { fileOut ->
                                    fileOut.write(bytes)
                                }
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun readBytes(input: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream((8 * 1024).coerceAtLeast(input.available()))
        copyTo(input, buffer)
        return buffer.toByteArray()
    }

    @Throws(IOException::class)
    private fun copyTo(from: InputStream, to: OutputStream): Long {
        var bytesCopied: Long = 0
        val buffer = ByteArray(8 * 1024)
        var bytes = from.read(buffer)
        while (bytes >= 0) {
            to.write(buffer, 0, bytes)
            bytesCopied += bytes.toLong()
            bytes = from.read(buffer)
        }
        return bytesCopied
    }

    fun getValue(): String {
        return DigestUtils.sha256Hex(
            System.getenv("os")
                + System.getProperty("os.name")
                + System.getProperty("os.arch")
                + System.getProperty("user.name")
                + System.getenv("PROCESSOR_LEVEL")
                + System.getenv("PROCESSOR_REVISION")
                + System.getenv("PROCESSOR_IDENTIFIER")
                + System.getenv("PROCESSOR_ARCHITEW6432")
        )
    }
}
*/
