package cryptography

import java.awt.image.BufferedImage
import java.awt.Color
import java.io.File
import javax.imageio.IIOException
import javax.imageio.ImageIO
import kotlin.system.exitProcess

fun main() {
    Crypto()
}

class Crypto {

    init {
        while(true) {
            println("Task (hide, show, exit):")
            menu(readln())
        }
    }

    private fun menu(input: String) {
        when (input) {
            "hide" -> hide()
            "show" -> show()
            "exit" -> println("Bye!").also { exitProcess(0) }
            else -> println("Wrong task")
        }
    }

    private fun hideBit(bNum: Int, bit: Char) = (Integer.toBinaryString(bNum).dropLast(1) + bit).toInt(2)
    private fun writing(input: BufferedImage, output: File, message: String) {
        return BufferedImage(input.width, input.height, BufferedImage.TYPE_INT_RGB).let { image ->
            var ind = 0
            for (x in 0 until input.height) {
                for (y in 0 until input.width) {
                    if (ind in message.indices) {
                        val color: Color = Color(input.getRGB(y, x)).let {
                            Color(
                                it.red,
                                it.green,
                                hideBit(it.blue, message[ind])
                            )
                        }
                        image.setRGB(y, x, color.rgb)
                        ind++
                    } else image.setRGB(y, x, input.getRGB(y, x))
                }
            }
            ImageIO.write(image, "png", output)
        }
    }
    class Small : Exception("The input image is not large enough to hold this message.")
    private fun binaryTransformation(byte: Byte): String {
        var num: Byte = byte
        val arr = byteArrayOf(64, 32, 16, 8, 4, 2, 1)
        return buildString {
            this.append("0")
            for (ind in arr.indices) {
                if (num >= arr[ind]) {
                    num = (num - arr[ind]).toByte()
                    this.append("1")
                } else this.append("0")
            }
        }
    }
    private fun transformMessage(message: String): String {
        return (message.encodeToByteArray() + byteArrayOf(0, 0, 3)).let {
            it.joinToString("") { byte -> binaryTransformation(byte) }
        }
    }
    private fun hide() {
        println("Input image file:")
        val inputImage: String = readlnOrNull() ?: return
        println("Output image file:")
        val outputImage: String = readlnOrNull() ?: return
        println("Message to hide:")
        val text: String = readlnOrNull() ?: return
        val encodedText = transformMessage(text)
        val chk = try {
            ImageIO.read(File(inputImage)).let {
                if (it.width * it.height < encodedText.length) throw Small()
            }
            println("Message saved in $outputImage image.")
            true
        } catch (small: Small) {
            println(small.message)
            false
        } catch (_: Exception) {
            println("Can't read input file!")
            false
        }
        if (chk) {
            writing(ImageIO.read(File(inputImage)), File(outputImage), encodedText)
        }
    }

    private fun binaryDecode(input: String): Byte {
        var res = 0.toByte()
        val arr = byteArrayOf(64, 32, 16, 8, 4, 2, 1)
        for (ind in input.indices) {
            if (input[ind] == '1') res = (res + arr[ind]).toByte()
        }
        return res
    }
    private fun showBit(bNum: Int) = Integer.toBinaryString(bNum).last()
    private fun decode(input: BufferedImage): String {
        val list = mutableListOf<Byte>()
        fun chk() = list.takeLast(3) == listOf<Byte>(0, 0, 3)
        var str = ""
        for (x in 0 until input.height) {
            for (y in 0 until input.width) {
                str += showBit(Color(input.getRGB(y, x)).blue)
                if (str.length == 8) {
                    list.add(binaryDecode(str.drop(1)))
                    str = ""
                }
                if (chk()) break
            }
            if (chk()) break
        }
        return list.dropLast(3).toByteArray().toString(Charsets.UTF_8)
    }
    private fun show() {
        val inputImage = println("Input image file:").let { readln() }
        val chk = try {
            ImageIO.read(File(inputImage))
            println("Message:")
            true
        } catch(_: IIOException) {
            println("Can't read input file!")
            false
        }
        if (chk) {
            println(decode(ImageIO.read(File(inputImage))))
        }
    }
}
