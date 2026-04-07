package com.michaelflisar.kmpicon.tmp.classes

class TextIcon(
    val size: Int,
    val text: String,
    val foreground: String,
    val background: String,
    val shape: Shape,
)

sealed class Shape {
    object Circle : Shape()
    object Square : Shape()
    class RoundedSquare(val cornerRadius: Int) : Shape()
}