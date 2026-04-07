package com.michaelflisar.kmpicongenerator.common

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import kotlin.reflect.KProperty

sealed class Background {

    abstract fun applyTo(config: BackgroundConfig)

    data class File(
        val file: java.io.File,
    ) : Background() {
        override fun applyTo(config: BackgroundConfig) {
            config.type.set(this.javaClass)
            config.imageFileProperty.set(file)
        }
    }

    data class Color(
        val color: String,
    ) : Background() {
        override fun applyTo(config: BackgroundConfig) {
            config.type.set(this.javaClass)
            config.colorProperty.set(color)
        }
    }
}

abstract class BackgroundConfig {

    @get:Internal
    internal abstract val imageFileProperty: RegularFileProperty

    @get:Internal
    internal abstract val colorProperty: Property<String>

    @get:Internal
    internal abstract val type: Property<Class<*>>

    @get:Internal
    internal var value: Background
        get() = toBackground()
        set(value) {
            value.applyTo(this)
        }

    internal operator fun getValue(thisRef: Any?, property: KProperty<*>): Background = value

    internal operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: Background) {
        value = newValue
    }

    fun setFrom(other: BackgroundConfig) {
        other.toBackground().applyTo(this)
    }

    fun toBackground(): Background {
        return when (type.get()) {
            Background.File::class.java -> {
                val file = imageFileProperty.orNull?.asFile
                    ?: throw IllegalStateException("Background.Image requires an image file")
                Background.File(file)
            }

            Background.Color::class.java -> Background.Color(colorProperty.get())
            else -> throw IllegalStateException("Unknown background type: ${type.getOrNull()}")
        }
    }
}