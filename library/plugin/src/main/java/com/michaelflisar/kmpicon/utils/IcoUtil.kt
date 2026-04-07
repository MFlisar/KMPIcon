package com.michaelflisar.kmpicon.utils

import org.gradle.api.GradleException
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

object IcoUtil {

    val DEFAULT_SIZES = listOf(16, 32, 48, 64, 128, 256)

    fun writeFromSource(source: Path, target: Path, sizes: List<Int>) {
        val image = ImageIO.read(source.toFile())
            ?: throw GradleException("Unsupported source image format for ICO conversion: $source")

        val frames = sizes.distinct().sorted().map { size ->
            require(size in 1..256) { "ICO size must be between 1 and 256, was $size" }

            val scaled =
                java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
            val g = scaled.createGraphics()
            g.setRenderingHint(
                java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC
            )
            g.setRenderingHint(
                java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY
            )
            g.drawImage(image, 0, 0, size, size, null)
            g.dispose()

            val pngBytes = ByteArrayOutputStream().use { baos ->
                if (!ImageIO.write(scaled, "png", baos)) {
                    throw GradleException("Could not encode PNG frame for ICO (size ${size}x$size)")
                }
                baos.toByteArray()
            }

            size to pngBytes
        }

        val iconDirSize = 6
        val entrySize = 16
        var dataOffset = iconDirSize + (entrySize * frames.size)

        val output = ByteArrayOutputStream()

        writeLeShort(output, 0) // reserved
        writeLeShort(output, 1) // type = icon
        writeLeShort(output, frames.size)

        frames.forEach { (size, png) ->
            output.write(if (size == 256) 0 else size)
            output.write(if (size == 256) 0 else size)
            output.write(0) // color count
            output.write(0) // reserved
            writeLeShort(output, 1) // color planes
            writeLeShort(output, 32) // bits per pixel
            writeLeInt(output, png.size)
            writeLeInt(output, dataOffset)
            dataOffset += png.size
        }

        frames.forEach { (_, png) -> output.write(png) }

        Files.write(target, output.toByteArray())
    }

    private fun writeLeShort(out: ByteArrayOutputStream, value: Int) {
        out.write(value and 0xFF)
        out.write((value ushr 8) and 0xFF)
    }

    private fun writeLeInt(out: ByteArrayOutputStream, value: Int) {
        out.write(value and 0xFF)
        out.write((value ushr 8) and 0xFF)
        out.write((value ushr 16) and 0xFF)
        out.write((value ushr 24) and 0xFF)
    }
}