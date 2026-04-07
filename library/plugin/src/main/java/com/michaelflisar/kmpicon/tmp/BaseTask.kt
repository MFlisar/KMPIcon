package com.michaelflisar.kmpicon.tmp

import com.michaelflisar.kmpicongenerator.common.BackgroundConfig
import com.michaelflisar.kmpicongenerator.common.ForegroundConfig
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.provideDelegate
import javax.inject.Inject


abstract class KMPIconTaskExtension @Inject constructor(objects: ObjectFactory) {

    @get:Internal
    @get:Nested
    internal abstract val backgroundConfig: BackgroundConfig

    @get:Internal
    @get:Nested
    internal abstract val foregroundConfig: ForegroundConfig

    var background by backgroundConfig
    var foreground by foregroundConfig

}

abstract class KMPIconTask : DefaultTask() {

    @get:Nested
    internal abstract val backgroundConfig: BackgroundConfig

    @get:Nested
    internal abstract val foregroundConfig: ForegroundConfig
}

abstract class KMPIconTaskAction<Task : KMPIconTask>(val ext: KMPIconTaskExtension) : Action<Task> {
    override fun execute(task: Task) {
        task.backgroundConfig.setFrom(ext.backgroundConfig)
        task.foregroundConfig.setFrom(ext.foregroundConfig)
    }
}