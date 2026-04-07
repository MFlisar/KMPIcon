package com.michaelflisar.kmpicongenerator.generators.android

import com.michaelflisar.kmpicongenerator.generators.android.classes.Density
import com.michaelflisar.kmpicongenerator.generators.android.classes.IconFormat
import com.michaelflisar.kmpicongenerator.generators.android.classes.ImageNames
import com.michaelflisar.kmpicongenerator.generators.android.classes.LegacyIconShape
import com.michaelflisar.kmpicongenerator.utils.Util
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File

internal object AndroidUtil {

    fun createDensityImages(
        fgImage: BufferedImage,
        bgImage: BufferedImage?,
        bgColor: Int?,
        fgScale: Float,
        outputFormat: IconFormat,
        outputResDir: File,
        imageNames: ImageNames,
        generateLegacyIcon: Boolean,
        generateRoundIcon: Boolean,
        legacyIconShape: LegacyIconShape,
    ) {
        Density.entries.forEach {

            val density = it.name.lowercase()
            val size = it.size

            val extension = outputFormat.format
            val fgImageName = "${imageNames.foregroundName}.$extension"
            val bgImageName = "${imageNames.backgroundName}.$extension"
            val legacyImageName = "${imageNames.legacyIconName}.$extension"
            val roundImageName = "${imageNames.roundIconName}.$extension"

            // 3) combined image for legacy and round icons
            if (generateLegacyIcon || generateRoundIcon) {

                val resDir = File(outputResDir, "mipmap-$density")
                resDir.mkdirs()

                // 1) foreground image
                val scaledImage = Util.scaleAndCenterImage(fgImage, size, size, fgScale)
                Util.writeImage(scaledImage, outputFormat.format, resDir, fgImageName)

                // 2) background image
                val backgroundImage = if (bgImage != null) {
                    Util.scaleAndCenterImage(bgImage, size, size, 1f)
                } else {
                    Util.createColorBitmap(bgColor!!, size, size)
                }
                Util.writeImage(backgroundImage, outputFormat.format, resDir, bgImageName)

                val combinedImage =
                    Util.combineBackgroundAndForeground(backgroundImage, scaledImage)

                if (generateLegacyIcon) {
                    val legacyImage = applyLegacyShape(combinedImage, legacyIconShape)
                    Util.writeImage(legacyImage, outputFormat.format, resDir, legacyImageName)
                }

                if (generateRoundIcon) {
                    val roundImage = applyLegacyShape(
                        combinedImage,
                        LegacyIconShape.Circle
                    )
                    Util.writeImage(roundImage, outputFormat.format, resDir, roundImageName)
                }

                // delete temp files
                File(resDir, fgImageName).delete()
                File(resDir, bgImageName).delete()
            }
        }
    }

    fun createXMLImages(
        outputResDir: File,
        imageNames: ImageNames,
        usesBackgroundColor: Boolean,
        generateRoundIcon: Boolean,
        generateLegacyIcon: Boolean,
        generatePlayStoreIcon: Boolean
    ) {
        // drawable folder (only if background image is used)
        if (!usesBackgroundColor) {

            val dirDrawable = File(outputResDir, "drawable")
            dirDrawable.mkdirs()

            // file anlegen,
            val file = File(dirDrawable, "${imageNames.foregroundName}.xml")
        }

        // drawable-v24 folder
        val dirV24 = File(outputResDir, "drawable-v24")
        dirV24.mkdirs()

        // // mipmap-anydpi-v26 folder
         val dirAnyDpiV26 = File(outputResDir, "mipmap-anydpi-v26")
         dirAnyDpiV26.mkdirs()

        // values folder (only if background color is used)
        if (usesBackgroundColor) {
            val valuesDir = File(outputResDir, "values")
            valuesDir.mkdirs()
        }




/*
        //  2) create adaptive XML icon
        AndroidUtil.createAdaptiveXMLImage(
            outputResDir = outputResDir,
            imageNames = imageNames,
            generateRoundIcon = generateRoundIcon
        )

        // 3) create round XML icon
        if (generateRoundIcon) {
            AndroidUtil.createRoundXMLImage(
                outputResDir = outputResDir,
                imageNames = imageNames
            )
        }

        // 4) create legacy XML icon
        if (generateLegacyIcon) {
            AndroidUtil.createLegacyXMLImage(
                outputResDir = outputResDir,
                imageNames = imageNames
            )
        }

        // 5) create Play Store icon
        if (generatePlayStoreIcon) {
            AndroidUtil.createPlayStoreImage(
                outputMainDir = outputMainDir,
                fgImage = fgImage,
                fallbackBackgroundColor = bgColor ?: Util.parseColor("#ffffff"),
                scale = fgScale,
                outputFormat = outputFormat,
                imageNames = imageNames
            )
        }

        */
    }

    private fun createAdaptiveXMLImage(
        outputResDir: File,
        imageNames: ImageNames,
        generateRoundIcon: Boolean,

        ) {
        val dir = File(outputResDir, "drawable-anydpi-v26")
        dir.mkdirs()

        val launcherXml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
""".trimIndent()
        File(dir, "${imageNames.legacyIconName}.xml").writeText(launcherXml)

        if (generateRoundIcon) {
            val launcherXmlRound = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
""".trimIndent()
            File(dir, "${imageNames.roundIconName}.xml").writeText(launcherXmlRound)
        }
    }

    private fun createRoundXMLImage(
        outputResDir: File,
        imageNames: ImageNames,
    ) {
        val dir = File(outputResDir, "drawable-v26")
        dir.mkdirs()

        val launcherRoundXml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/${imageNames.backgroundName}" />
    <foreground android:drawable="@mipmap/${imageNames.foregroundName}" />
</adaptive-icon>
""".trimIndent()
        File(dir, "${imageNames.roundIconName}.xml").writeText(launcherRoundXml)
    }

    private fun createLegacyXMLImage(
        outputResDir: File,
        imageNames: ImageNames,
    ) {
        val drawableDir = File(outputResDir, "drawable")
        drawableDir.mkdirs()
        val launcherLegacyXml = """<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@mipmap/${imageNames.backgroundName}" />
    <item android:drawable="@mipmap/${imageNames.foregroundName}" />
</layer-list>
""".trimIndent()
        File(
            drawableDir,
            "${imageNames.legacyIconName}_background.xml"
        ).writeText(launcherLegacyXml)
    }

    private fun createPlayStoreImage(
        outputMainDir: File,
        fgImage: BufferedImage,
        fallbackBackgroundColor: Int,
        scale: Float,
        imageNames: ImageNames,
        outputFormat: IconFormat,
    ) {
        val playStoreDir = outputMainDir
        playStoreDir.mkdirs()

        val size = 512
        val result = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = result.createGraphics()
        g2d.color = Color(fallbackBackgroundColor)
        g2d.fillRect(0, 0, size, size)

        val scaledWidth = (size * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (size * scale).toInt().coerceAtLeast(1)
        val scaled = fgImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
        val x = (size - scaledWidth) / 2
        val y = (size - scaledHeight) / 2
        g2d.drawImage(scaled, x, y, null)

        g2d.dispose()

        Util.writeImage(
            result,
            outputFormat.format,
            File(playStoreDir, "${imageNames.playStoreIconName}.${outputFormat.format}")
        )
    }

    fun cleanupOldGeneratedImages(
        outputResDir: File,
        outputMainDir: File,
        imageNames: ImageNames,
    ) {
        // 1) delete old generated images in mipmap directories
        val targetBaseNames = setOf(
            imageNames.legacyIconName,
            imageNames.foregroundName,
            imageNames.backgroundName,
            imageNames.roundIconName
        )
        val targetExtensions = IconFormat.entries.map { it.format }
        outputResDir.listFiles { file -> file.isDirectory && file.name.startsWith("mipmap-") }
            ?.forEach { mipmapDir ->
                mipmapDir.listFiles { file ->
                    file.isFile &&
                            file.extension.lowercase() in targetExtensions &&
                            file.nameWithoutExtension in targetBaseNames
                }?.forEach { oldFile ->
                    oldFile.delete()
                }
            }

        // 2) delete old generates files in values directory
        val valuesDir = File(outputResDir, "values")
        valuesDir.listFiles { file ->
            file.isFile && file.name.startsWith(imageNames.backgroundName) && file.extension == "xml"
        }?.forEach { it.delete() }

        // 3) delete old generated files in main directory
        targetExtensions.forEach { ext ->
            File(outputMainDir, "${imageNames.playStoreIconName}.$ext").delete()
        }
    }

    private fun applyLegacyShape(
        image: BufferedImage,
        shape: LegacyIconShape,
    ): BufferedImage {
        if (shape == LegacyIconShape.Square || shape == LegacyIconShape.None) {
            return image
        }

        val result = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        val g2d = result.createGraphics()
        g2d.drawImage(image, 0, 0, null)

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val keep = when (shape) {
                    LegacyIconShape.Circle -> {
                        val cx = image.width / 2.0
                        val cy = image.height / 2.0
                        val radius = minOf(image.width, image.height) / 2.0
                        val dx = x - cx
                        val dy = y - cy
                        (dx * dx + dy * dy) <= (radius * radius)
                    }

                    LegacyIconShape.Vertical -> {
                        val margin = (image.width * 0.15).toInt()
                        x in margin until (image.width - margin)
                    }

                    LegacyIconShape.Horizontal -> {
                        val margin = (image.height * 0.15).toInt()
                        y in margin until (image.height - margin)
                    }

                    else -> true
                }
                if (!keep) {
                    result.setRGB(x, y, 0x00000000)
                }
            }
        }

        g2d.dispose()
        return result
    }
}