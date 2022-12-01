package cryptography

import java.awt.image.BufferedImage
import java.awt.Color
import java.io.File
import javax.imageio.IIOException
import javax.imageio.ImageIO
import kotlin.experimental.xor
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
    private fun finalMessage(messageArr: ByteArray, passArr: ByteArray): String {
        var resPass = passArr
        if (passArr.size < messageArr.size) {
            repeat(messageArr.size / passArr.size + 1) {
                resPass += passArr
            }
        }
        return buildString {
            for (ind in messageArr.indices) {
                this.append(binaryTransformation(messageArr[ind] xor resPass[ind]))
            }
            byteArrayOf(0, 0, 3).forEach { this.append(binaryTransformation(it)) }
        }
    }
    private fun hide() {
        val inputImage: String = println("Input image file:").let { readlnOrNull() ?: return }
        val outputImage: String = println("Output image file:").let { readlnOrNull() ?: return }
        val text: String = println("Message to hide:").let { readlnOrNull() ?: return }
        val pass: String = println("Password:").let { readlnOrNull() ?: return }
        val encodedText = finalMessage(text.encodeToByteArray(), pass.encodeToByteArray())
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

    private fun decodeWithPass(encodedArr: List<Byte>, passArr: ByteArray): ByteArray {
        var resPass = passArr
        if (passArr.size < encodedArr.size) {
            repeat(encodedArr.size / passArr.size + 1) {
                resPass += passArr
            }
        }
        return buildList<Byte> {
            for (ind in encodedArr.indices) {
                this.add(encodedArr[ind] xor resPass[ind])
            }
        }.toByteArray()
    }
    private fun decimalTransformation(input: String): Byte {
        var res = 0.toByte()
        val arr = byteArrayOf(64, 32, 16, 8, 4, 2, 1)
        for (ind in input.indices) {
            if (input[ind] == '1') res = (res + arr[ind]).toByte()
        }
        return res
    }
    private fun showBit(bNum: Int) = Integer.toBinaryString(bNum).last()
    private fun decode(input: BufferedImage): List<Byte> {
        val list = mutableListOf<Byte>()
        fun chk() = list.takeLast(3) == listOf<Byte>(0, 0, 3)
        var str = ""
        for (x in 0 until input.height) {
            for (y in 0 until input.width) {
                str += showBit(Color(input.getRGB(y, x)).blue)
                if (str.length == 8) {
                    list.add(decimalTransformation(str.drop(1)))
                    str = ""
                }
                if (chk()) break
            }
            if (chk()) break
        }
        return list
    }
    private fun show() {
        val inputImage = println("Input image file:").let { readlnOrNull() ?: return }
        val inputPassword = println("Password:").let { readlnOrNull() ?: return }
        val chk = try {
            ImageIO.read(File(inputImage))
            println("Message:")
            true
        } catch(_: IIOException) {
            println("Can't read input file!")
            false
        }
        if (chk) {
            println(
                decodeWithPass(
                    decode(ImageIO.read(File(inputImage))),
                    inputPassword.encodeToByteArray()
                ).toString(Charsets.UTF_8)
            )
        }
    }
}
