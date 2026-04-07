package com.michaelflisar.kmpicon

import com.michaelflisar.kmpicon.tasks.CopyIcon
import com.michaelflisar.kmpicon.tasks.CopyIconSourceConfig
import com.michaelflisar.kmpicon.tasks.GenerateCommonIconConfig
import com.michaelflisar.kmpicon.tasks.GenerateIcoConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import javax.inject.Inject

abstract class KMPIconPluginExtension @Inject constructor(private val objects: ObjectFactory) {

    @get:Internal
    abstract val sourceConfig: Property<CopyIconSourceConfig>

    @get:Internal
    abstract val generateCommonIconConfig: Property<GenerateCommonIconConfig>

    @get:Internal
    abstract val generateIcoConfig: Property<GenerateIcoConfig>

    fun setup(configure: CopyIconSourceConfig.() -> Unit) {
        sourceConfig.set(objects.newInstance(CopyIconSourceConfig::class.java))
        sourceConfig.get().apply { configure() }
    }

    fun generateCommonIcon(configure: GenerateCommonIconConfig.() -> Unit) {
        generateCommonIconConfig.set(objects.newInstance(GenerateCommonIconConfig::class.java))
        generateCommonIconConfig.get().apply { configure() }
    }

    fun generateIco(configure: GenerateIcoConfig.() -> Unit) {
        generateIcoConfig.set(objects.newInstance(GenerateIcoConfig::class.java))
        generateIcoConfig.get().apply { configure() }
    }
}

class KMPIconPlugin : Plugin<Project> {

    private lateinit var project: Project

    override fun apply(project: Project) {

        this.project = project

        // 1) create extension
        val ext = project.extensions.create("kmpIcon", KMPIconPluginExtension::class.java)

        // 2) create tasks and set them up
        CopyIcon.apply(project, ext.sourceConfig, ext.generateCommonIconConfig, ext.generateIcoConfig)
    }
}