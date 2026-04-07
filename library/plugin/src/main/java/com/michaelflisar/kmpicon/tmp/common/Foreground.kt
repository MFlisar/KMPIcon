package com.michaelflisar.kmpicongenerator.common

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import kotlin.reflect.KProperty

sealed class Foreground {

    abstract val scale: Int
    abstract fun applyTo(config: ForegroundConfig)

    data class File(
        val file: java.io.File,
        val trim: Boolean = false,
        override val scale: Int = 75,
    ) : Foreground() {
        override fun applyTo(config: ForegroundConfig) {
            config.type.set(this.javaClass)
            config.scaleProperty.set(scale)
            config.imageFileProperty.set(file)
            config.imageTrimProperty.set(trim)
        }
    }

    data class Text(
        val text: String,
        val font: String,
        val color: String,
        override val scale: Int = 75,
    ) : Foreground() {
        override fun applyTo(config: ForegroundConfig) {
            config.type.set(this.javaClass)
            config.scaleProperty.set(scale)
            config.textProperty.set(text)
            config.fontProperty.set(font)
            config.colorProperty.set(color)
        }
    }
}

abstract class ForegroundConfig {

    @get:Internal
    internal abstract val scaleProperty: Property<Int>

    @get:Internal
    internal abstract val imageFileProperty: RegularFileProperty

    @get:Internal
    internal abstract val imageTrimProperty: Property<Boolean>

    @get:Internal
    internal abstract val textProperty: Property<String>

    @get:Internal
    internal abstract val fontProperty: Property<String>

    @get:Internal
    internal abstract val colorProperty: Property<String>

    @get:Internal
    internal abstract val type: Property<Class<*>>

    @get:Internal
    internal var value: Foreground
        get() = toForeground()
        set(value) {
            value.applyTo(this)
        }

    internal operator fun getValue(thisRef: Any?, property: KProperty<*>): Foreground = value

    internal operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: Foreground) {
        value = newValue
    }

    fun setFrom(other: ForegroundConfig) {
        other.toForeground().applyTo(this)
    }

    fun toForeground(): Foreground {
        return when (type.get()) {
            Foreground.Text::class.java -> {
                Foreground.Text(
                    text = textProperty.get(),
                    font = fontProperty.get(),
                    color = colorProperty.get(),
                    scale = scaleProperty.get(),
                )
            }

            Foreground.File::class.java  -> {
                val file = imageFileProperty.orNull?.asFile ?: throw IllegalStateException("Foreground.Image requires an image file")
                Foreground.File(
                    file = file,
                    trim = imageTrimProperty.get(),
                    scale = scaleProperty.get(),
                )
            }
            else -> throw IllegalStateException("Unknown foreground type: ${type.get()}")
        }
    }

}