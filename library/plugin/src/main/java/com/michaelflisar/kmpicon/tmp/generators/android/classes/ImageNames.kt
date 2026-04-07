package com.michaelflisar.kmpicongenerator.generators.android.classes

class ImageNames(
    val foregroundName: String,
    val backgroundName: String,
    val roundIconName: String,
    val playStoreIconName: String,
    val legacyIconName: String
) {
    companion object {
        fun create(outputImageBaseName: String) = ImageNames(
            foregroundName = "${outputImageBaseName}_foreground",
            backgroundName = "${outputImageBaseName}_background",
            roundIconName = "${outputImageBaseName}_round",
            playStoreIconName = "${outputImageBaseName}-playstore",
            legacyIconName = outputImageBaseName
        )
    }
}