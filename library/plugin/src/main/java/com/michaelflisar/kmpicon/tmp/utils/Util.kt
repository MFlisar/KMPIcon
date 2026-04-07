package com.michaelflisar.kmpicongenerator.utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO

internal object Util {

    fun convertSvgToPng(svgFile: File): BufferedImage {
        return try {
            val pngTranscoderClass =
                Class.forName("org.apache.batik.transcoder.image.PNGTranscoder")
            val transcoder = pngTranscoderClass.getDeclaredConstructor().newInstance()

            val keyClass = Class.forName("org.apache.batik.transcoder.TranscodingHints\$Key")
            val addHint =
                pngTranscoderClass.getMethod("addTranscodingHint", keyClass, Any::class.java)
            val keyWidth = pngTranscoderClass.getField("KEY_WIDTH").get(null)
            val keyHeight = pngTranscoderClass.getField("KEY_HEIGHT").get(null)
            addHint.invoke(transcoder, keyWidth, 1024f)
            addHint.invoke(transcoder, keyHeight, 1024f)

            val transcoderInputClass = Class.forName("org.apache.batik.transcoder.TranscoderInput")
            val transcoderOutputClass =
                Class.forName("org.apache.batik.transcoder.TranscoderOutput")
            val transcodeMethod = pngTranscoderClass.getMethod(
                "transcode",
                transcoderInputClass,
                transcoderOutputClass
            )

            val outputStream = ByteArrayOutputStream()
            svgFile.inputStream().use { inputStream ->
                val input = transcoderInputClass.getConstructor(InputStream::class.java)
                    .newInstance(inputStream)
                val output = transcoderOutputClass.getConstructor(OutputStream::class.java)
                    .newInstance(outputStream)
                transcodeMethod.invoke(transcoder, input, output)
            }

            val bytes = outputStream.toByteArray()
            ImageIO.read(ByteArrayInputStream(bytes))
                ?: throw IllegalArgumentException("Could not decode transcoded SVG image: $svgFile")
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not transcode SVG file: $svgFile", e)
        }
    }

    fun readInputImage(input: File): BufferedImage {
        require(input.exists()) { "Input file not found: $input" }
        require(input.extension.lowercase() in listOf("png", "svg")) {
            "Input file must be PNG or SVG, got: ${input.extension}"
        }
        return if (input.extension.lowercase() == "svg") {
            convertSvgToPng(input)
        } else {
            ImageIO.read(input) ?: throw IllegalArgumentException("Could not read image: $input")
        }
    }

    fun scaleAndCenterImage(
        image: BufferedImage,
        width: Int,
        height: Int,
        scale: Float,
    ): BufferedImage {
        val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (height * scale).toInt().coerceAtLeast(1)
        val scaled = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = result.createGraphics()
        val x = (width - scaledWidth) / 2
        val y = (height - scaledHeight) / 2
        g2d.drawImage(scaled, x, y, null)
        g2d.dispose()
        return result
    }

    fun writeImage(image: BufferedImage, format: String, directory: File, fileName: String) {
        val foregroundFile = File(directory, fileName)
        writeImage(image, format, foregroundFile)
    }

    fun writeImage(image: BufferedImage, format: String, file: File) {
        ImageIO.scanForPlugins()
        val written = ImageIO.write(image, format, file)
        require(written) { "No ImageIO writer available for format '$format' (file: $file)" }
    }

    fun combineBackgroundAndForeground(
        background: BufferedImage,
        foreground: BufferedImage,
    ): BufferedImage {
        val result = BufferedImage(background.width, background.height, BufferedImage.TYPE_INT_ARGB)
        val g2d = result.createGraphics()
        g2d.drawImage(background, 0, 0, null)
        g2d.drawImage(foreground, 0, 0, null)
        g2d.dispose()
        return result
    }

    fun trimTransparentBounds(image: BufferedImage): BufferedImage {
        var minX = image.width
        var minY = image.height
        var maxX = -1
        var maxY = -1

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val alpha = (image.getRGB(x, y) ushr 24) and 0xFF
                if (alpha > 0) {
                    minX = minOf(minX, x)
                    minY = minOf(minY, y)
                    maxX = maxOf(maxX, x)
                    maxY = maxOf(maxY, y)
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return image
        }

        return image.getSubimage(minX, minY, (maxX - minX + 1), (maxY - minY + 1))
    }

    fun parseColor(hex: String): Int {
        val cleanHex = hex.removePrefix("#")
        return when (cleanHex.length) {
            6 -> cleanHex.toLong(16).toInt() or 0xFF000000.toInt() // Add full alpha if not provided
            8 -> cleanHex.toLong(16).toInt()
            else -> throw IllegalArgumentException("Invalid color hex: $hex. Must be in format #RRGGBB or #AARRGGBB")
        }
    }

    fun createColorBitmap(color: Int, width: Int, height: Int): BufferedImage {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                img.setRGB(x, y, color)
            }
        }
        return img
    }
}