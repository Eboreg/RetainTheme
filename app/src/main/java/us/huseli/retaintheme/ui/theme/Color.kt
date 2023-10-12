@file:Suppress("PropertyName")

package us.huseli.retaintheme.ui.theme

import androidx.compose.ui.graphics.Color

interface RetainBasicColors {
    val Brown: Color
    val Purple: Color
    val Cerulean: Color
    val Gray: Color
    val Pink: Color
    val Blue: Color
    val Red: Color
    val Yellow: Color
    val Green: Color
    val Teal: Color
    val Orange: Color
}

object RetainBasicColorsDark : RetainBasicColors {
    override val Brown = Color(0xff4b443a)
    override val Purple = Color(0xff472e5b)
    override val Cerulean = Color(0xff284255)
    override val Gray = Color(0xff232427)
    override val Pink = Color(0xff6c394f)
    override val Blue = Color(0xff256377)
    override val Red = Color(0xff77172e)
    override val Yellow = Color(0xff7c4a03)
    override val Green = Color(0xff264d3b)
    override val Teal = Color(0xff0c625d)
    override val Orange = Color(0xff692b17)
}

object RetainBasicColorsLight : RetainBasicColors {
    override val Brown = Color(0xffe9e3d4)
    override val Purple = Color(0xffd3bfdb)
    override val Cerulean = Color(0xffaeccdc)
    override val Gray = Color(0xffefeff1)
    override val Pink = Color(0xfff6e2dd)
    override val Blue = Color(0xffd4e4ed)
    override val Red = Color(0xfffaafa8)
    override val Yellow = Color(0xfffff8b8)
    override val Green = Color(0xffe2f6d4)
    override val Teal = Color(0xffb4ddd3)
    override val Orange = Color(0xfff39f76)
}
