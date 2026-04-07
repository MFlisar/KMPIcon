package com.michaelflisar.kmpicongenerator.generators.android

import com.michaelflisar.kmpicon.tmp.KMPIconTask
import com.michaelflisar.kmpicon.tmp.KMPIconTaskAction
import com.michaelflisar.kmpicon.tmp.KMPIconTaskExtension
import com.michaelflisar.kmpicongenerator.common.Background
import com.michaelflisar.kmpicongenerator.common.Foreground
import com.michaelflisar.kmpicongenerator.generators.android.classes.IconFormat
import com.michaelflisar.kmpicongenerator.generators.android.classes.ImageNames
import com.michaelflisar.kmpicongenerator.generators.android.classes.LegacyIconShape
import com.michaelflisar.kmpicongenerator.utils.Util
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.setValue
import java.io.File
import javax.inject.Inject

abstract class AndroidAdaptiveIconTaskExtension @Inject constructor(objects: ObjectFactory) :
    KMPIconTaskExtension(objects) {

    @get:Internal
    internal abstract val outputResFolderProperty: Property<String>

    @get:Internal
    internal abstract val outputMainFolderProperty: Property<String>

    @get:Internal
    internal abstract val outputFormatProperty: Property<IconFormat>

    @get:Internal
    internal abstract val outputImageBaseNameProperty: Property<String>

    @get:Internal
    internal abstract val legacyIconShapeProperty: Property<LegacyIconShape>

    @get:Internal
    internal abstract val generateLegacyIconProperty: Property<Boolean>

    @get:Internal
    internal abstract val generateRoundIconProperty: Property<Boolean>

    @get:Internal
    internal abstract val generatePlayStoreIconProperty: Property<Boolean>

    var outputResFolder by outputResFolderProperty
    var outputMainFolder by outputMainFolderProperty
    var outputFormat by outputFormatProperty
    var outputImageBaseName by outputImageBaseNameProperty
    var legacyIconShape by legacyIconShapeProperty
    var generateLegacyIcon by generateLegacyIconProperty
    var generateRoundIcon by generateRoundIconProperty
    var generatePlayStoreIcon by generatePlayStoreIconProperty


    init {
        outputResFolderProperty.convention("src/main/res")
        outputMainFolderProperty.convention("src/main")
        outputFormatProperty.convention(IconFormat.WEBP)
        outputImageBaseNameProperty.convention("ic_launcher")
        legacyIconShapeProperty.convention(LegacyIconShape.Square)
        generateLegacyIconProperty.convention(true)
        generateRoundIconProperty.convention(true)
        generatePlayStoreIconProperty.convention(true)
    }
}

abstract class AndroidAdaptiveIconTask : KMPIconTask() {

    @get:Input
    protected abstract val outputResFolderProperty: Property<String>

    @get:Input
    protected abstract val outputMainFolderProperty: Property<String>

    @get:Input
    protected abstract val outputFormatProperty: Property<IconFormat>

    @get:Input
    protected abstract val outputImageBaseNameProperty: Property<String>

    @get:Input
    protected abstract val outputImageBaseName: Property<String>

    @get:Input
    protected abstract val legacyIconShapeProperty: Property<LegacyIconShape>

    @get:Input
    protected abstract val generateLegacyIconProperty: Property<Boolean>

    @get:Input
    protected abstract val generateRoundIconProperty: Property<Boolean>

    @get:Input
    protected abstract val generatePlayStoreIconProperty: Property<Boolean>

    companion object {
        internal const val Name = "kmpAndroidIconGeneratorPlugin"
        private const val ExtensionName = "kmpAndroidIconGenerator"

        fun register(project: Project) {
            val ext = project.extensions.create(
                ExtensionName,
                AndroidAdaptiveIconTaskExtension::class.java
            )

            project.tasks.register(Name, AndroidAdaptiveIconTask::class.java, configuration(ext))
        }

        private fun configuration(ext: AndroidAdaptiveIconTaskExtension) =
            object : KMPIconTaskAction<AndroidAdaptiveIconTask>(ext) {
                override fun execute(task: AndroidAdaptiveIconTask) {
                    super.execute(task)

                    val projectDir = task.project.layout.projectDirectory
                    val outputResFolderPath = ext.outputResFolderProperty.get()
                    val outputMainFolderPath = ext.outputMainFolderProperty.get()

                    task.outputResFolderProperty.set(projectDir.file(outputResFolderPath).asFile.absolutePath)
                    task.outputMainFolderProperty.set(projectDir.file(outputMainFolderPath).asFile.absolutePath)
                    task.outputFormatProperty.set(ext.outputFormatProperty)
                    task.outputImageBaseNameProperty.set(ext.outputImageBaseNameProperty)
                    task.legacyIconShapeProperty.set(ext.legacyIconShapeProperty)
                    task.generateLegacyIconProperty.set(ext.generateLegacyIconProperty)
                    task.generateRoundIconProperty.set(ext.generateRoundIconProperty)
                    task.generatePlayStoreIconProperty.set(ext.generatePlayStoreIconProperty)
                }
            }
    }

    @TaskAction
    fun generateIcons() {

        // all properties are required
        val foreground =
            requireNotNull(foregroundConfig.toForeground()) { "foreground is required" }
        val background =
            requireNotNull(backgroundConfig.toBackground()) { "background is required" }
        val outputResDir = requireNotNull(
            outputResFolderProperty.getOrNull()?.let { File(it) }) { "outputResFolder is required" }
        val outputMainDir = requireNotNull(
            outputMainFolderProperty.getOrNull()
                ?.let { File(it) }) { "outputMainFolder is required" }
        val outputFormat =
            requireNotNull(outputFormatProperty.getOrNull()) { "outputFormat is required" }
        val outputImageBaseName =
            requireNotNull(outputImageBaseNameProperty.getOrNull()) { "outputImageBaseName is required" }
        val legacyIconShape =
            requireNotNull(legacyIconShapeProperty.getOrNull()) { "legacyIconShape is required" }
        val generateLegacyIcon =
            requireNotNull(generateLegacyIconProperty.getOrNull()) { "generateLegacyIcon is required" }
        val generateRoundIcon =
            requireNotNull(generateRoundIconProperty.getOrNull()) { "generateRoundIcon is required" }
        val generatePlayStoreIcon =
            requireNotNull(generatePlayStoreIconProperty.getOrNull()) { "generatePlayStoreIcon is required" }

        // Logging
        println("")
        println("Generating Android adaptive icons with the following configuration:")
        println("- Foreground: $foreground")
        println("- Background: $background")
        println("- Output Res Folder: $outputResDir")
        println("- Output Main Folder: $outputMainDir")
        println("- Output Format: $outputFormat")
        println("- Output Image Base Name: $outputImageBaseName")
        println("- Legacy Icon Shape: $legacyIconShape")
        println("- Generate Legacy Icon: $generateLegacyIcon")
        println("- Generate Round Icon: $generateRoundIcon")
        println("- Generate Play Store Icon: $generatePlayStoreIcon")

        // ----------
        // Task
        // ----------

        val imageNames = ImageNames.create(outputImageBaseName)

        // 1) clean up old generated icons
        AndroidUtil.cleanupOldGeneratedImages(
            outputResDir = outputResDir,
            outputMainDir = outputMainDir,
            imageNames = imageNames
        )

        // 2) generate icons
        when (foreground) {
            is Foreground.File -> {

                val fgImage = Util.readInputImage(foreground.file).let {
                    if (foreground.trim) Util.trimTransparentBounds(it) else it
                }
                val fgScale = foreground.scale / 100f
                val bgImage = when (background) {
                    is Background.File -> Util.readInputImage(background.file)
                    is Background.Color -> null
                }
                val bgColor = when (background) {
                    is Background.Color -> Util.parseColor(background.color)
                    is Background.File -> null
                }

                // 1) create density images
                AndroidUtil.createDensityImages(
                    fgImage = fgImage,
                    bgImage = bgImage,
                    bgColor = bgColor,
                    fgScale = fgScale,
                    outputFormat = outputFormat,
                    outputResDir = outputResDir,
                    imageNames = imageNames,
                    generateLegacyIcon = generateLegacyIcon,
                    generateRoundIcon = generateRoundIcon,
                    legacyIconShape = legacyIconShape
                )

                // 2) create xml icons
                AndroidUtil.createXMLImages(
                    outputResDir = outputResDir,
                    imageNames = imageNames,
                    usesBackgroundColor = false,
                    generateRoundIcon = generateRoundIcon,
                    generateLegacyIcon = generateLegacyIcon,
                    generatePlayStoreIcon = generatePlayStoreIcon
                )
            }

            is Foreground.Text -> {
                TODO("Text foreground not implemented yet")
            }
        }

    }
}