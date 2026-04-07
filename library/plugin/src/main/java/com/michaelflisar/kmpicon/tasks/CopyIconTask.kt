package com.michaelflisar.kmpicon.tasks

import com.michaelflisar.kmpicon.utils.IcoUtil
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

object CopyIcon {

    fun apply(
        project: Project,
        copyIconSourceConfig: Property<CopyIconSourceConfig>,
        generateCommonIconConfig: Property<GenerateCommonIconConfig>,
        generateIcoConfig: Property<GenerateIcoConfig>,
    ) {
        project.afterEvaluate {

            if (generateCommonIconConfig.isPresent || generateIcoConfig.isPresent) {

                // 1) ensure source config is available
                val sourceConfig = copyIconSourceConfig.orNull
                    ?: throw GradleException("KMPIcon: setup() must be called in kmpIcon block: kmpIcon { setup { ... } }")

                // 2) create task
                val copyIconTask = project.tasks.register("copyIconTask", CopyIconTask::class.java)
                copyIconTask.configure {
                    rootDirectory.set(project.rootProject.layout.projectDirectory)
                    config.set(objects.newInstance(CopyIconConfig::class.java))
                    config.get().enabled.set(true)
                    config.get().sourceModule.set(sourceConfig.sourceModule)
                    config.get().sourceFile.set(sourceConfig.sourceFile)
                }

                val commonIconConfig = generateCommonIconConfig.orNull
                val icoConfig = generateIcoConfig.orNull

                // 3) set up generateCommonIconTask and dependencies
                if (commonIconConfig != null) {
                    copyIconTask.configure {
                        config.get().createComposeResource.set(true)
                        config.get().targetComposeFile.set(commonIconConfig.file)
                    }
                }

                // 4) set up generateIcoTask and dependencies
                if (icoConfig != null) {
                    copyIconTask.configure {
                        config.get().createIco.set(true)
                        config.get().targetIcoFile.set(icoConfig.file)
                    }
                }

                project.tasks.configureEach {
                    if (name.startsWith("compile") || name == "copyNonXmlValueResourcesForCommonMain") {
                        dependsOn(copyIconTask)
                    }
                }
            }
        }
    }
}

abstract class CopyIconSourceConfig @Inject constructor(objects: ObjectFactory) {
    abstract val sourceModule: Property<String>
    abstract val sourceFile: Property<String>

    init {
        sourceModule.convention("app/app/android")
        sourceFile.convention("src/main/ic_launcher-playstore.png")
    }
}

abstract class GenerateCommonIconConfig @Inject constructor(objects: ObjectFactory) {
    abstract val file: Property<String>

    init {
        file.convention("src/commonMain/composeResources/drawable/icon.png")
    }
}

abstract class GenerateIcoConfig @Inject constructor(objects: ObjectFactory) {
    abstract val file: Property<String>

    init {
        file.convention("icon.ico")
    }
}

abstract class CopyIconConfig @Inject constructor(objects: ObjectFactory) {
    abstract val enabled: Property<Boolean>
    abstract val sourceModule: Property<String>
    abstract val sourceFile: Property<String>
    abstract val targetComposeFile: Property<String>
    abstract val targetIcoFile: Property<String>
    abstract val createComposeResource: Property<Boolean>
    abstract val createIco: Property<Boolean>

    init {
        enabled.convention(false)
        sourceModule.convention("app/app/android")
        sourceFile.convention("src/main/ic_launcher-playstore.png")
        targetComposeFile.convention("src/commonMain/composeResources/drawable/icon.png")
        targetIcoFile.convention("icon.ico")
        createComposeResource.convention(false)
        createIco.convention(false)
    }

    fun enableAll() {
        enabled.set(true)
        createComposeResource.set(true)
        createIco.set(true)
    }
}

@CacheableTask
abstract class CopyIconTask : DefaultTask() {

    @get:Input
    abstract val config: Property<CopyIconConfig>

    @get:Internal
    abstract val rootDirectory: DirectoryProperty

    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sourceInputFile: File
        get() = rootDirectory.get()
            .file("${config.get().sourceModule.get()}/${config.get().sourceFile.get()}").asFile

    @get:OutputFile
    @get:Optional
    val composeOutputFile: File?
        get() = if (config.get().createComposeResource.get()) {
            layout.projectDirectory.file(config.get().targetComposeFile.get()).asFile
        } else {
            null
        }

    @get:OutputFile
    @get:Optional
    val icoOutputFile: File?
        get() = if (config.get().createIco.get()) {
            layout.projectDirectory.file(config.get().targetIcoFile.get()).asFile
        } else {
            null
        }

    @TaskAction
    fun copy() {
        val src = sourceInputFile.toPath()

        if (!Files.exists(src)) {
            throw GradleException("Source icon does not exist: $src")
        }

        if (config.get().createComposeResource.get()) {
            val tgt = composeOutputFile!!.toPath()
            Files.createDirectories(tgt.parent)
            Files.copy(src, tgt, StandardCopyOption.REPLACE_EXISTING)
        }

        if (config.get().createIco.get()) {
            val tgt = icoOutputFile!!.toPath()
            Files.createDirectories(tgt.parent)
            IcoUtil.writeFromSource(src, tgt, IcoUtil.DEFAULT_SIZES)
        }
    }

}