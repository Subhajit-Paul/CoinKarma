package com.coinkarma.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// All icons are 24×24 stroke vectors, matching icons.jsx exactly.
// stroke = currentColor (tint applied at call site), fill = none, strokeWidth = 1.6 default

private fun ckIcon(
    name: String,
    strokeWidth: Float = 1.6f,
    block: ImageVector.Builder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp, defaultHeight = 24.dp,
    viewportWidth = 24f, viewportHeight = 24f,
).apply(block).build()

private val stroke = SolidColor(Color.Black)

object CkIcons {

    val Food: ImageVector = ckIcon("food") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(4f, 3f); verticalLineTo(11f)
            arcTo(3f, 3f, 0f, false, false, 7f, 14f)
            verticalLineTo(21f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(8f, 3f); verticalLineTo(8f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(6f, 3f); verticalLineTo(8f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(17f, 3f)
            curveTo(15f, 5f, 14f, 7f, 14f, 10f)
            curveTo(14f, 12f, 15f, 13f, 17f, 13f)
            verticalLineTo(21f)
        }
    }

    val Transport: ImageVector = ckIcon("transport") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(5f, 6f); horizontalLineTo(19f)
            arcTo(2f, 2f, 0f, false, true, 21f, 8f)
            verticalLineTo(17f)
            arcTo(2f, 2f, 0f, false, true, 19f, 19f)
            horizontalLineTo(5f)
            arcTo(2f, 2f, 0f, false, true, 3f, 17f)
            verticalLineTo(8f)
            arcTo(2f, 2f, 0f, false, true, 5f, 6f)
            close()
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent), pathFillType = PathFillType.NonZero) {
            moveTo(7f, 19f); moveToRelative(-1.5f, 0f)
            arcTo(1.5f, 1.5f, 0f, true, true, 3f, 19f)
            arcTo(1.5f, 1.5f, 0f, false, true, 8.5f, 19f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent), pathFillType = PathFillType.NonZero) {
            moveTo(17f, 19f); moveToRelative(-1.5f, 0f)
            arcTo(1.5f, 1.5f, 0f, true, true, 13f, 19f)
            arcTo(1.5f, 1.5f, 0f, false, true, 18.5f, 19f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(5f, 11f); horizontalLineTo(19f)
        }
    }

    val Shopping: ImageVector = ckIcon("shopping") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(6f, 7f); horizontalLineTo(18f)
            lineTo(17f, 20f); horizontalLineTo(7f)
            close()
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(9f, 7f)
            arcTo(3f, 3f, 0f, false, true, 15f, 7f)
        }
    }

    val Entertainment: ImageVector = ckIcon("entertainment") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(3f, 5f); horizontalLineTo(21f)
            arcTo(2f, 2f, 0f, false, true, 23f, 7f)
            verticalLineTo(17f)
            arcTo(2f, 2f, 0f, false, true, 21f, 19f)
            horizontalLineTo(3f)
            arcTo(2f, 2f, 0f, false, true, 1f, 17f)
            verticalLineTo(7f)
            arcTo(2f, 2f, 0f, false, true, 3f, 5f)
            close()
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(3f, 9f); horizontalLineTo(21f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(7f, 5f); verticalLineTo(19f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(17f, 5f); verticalLineTo(19f)
        }
    }

    val Health: ImageVector = ckIcon("health") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(12f, 5f); verticalLineTo(19f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(5f, 12f); horizontalLineTo(19f)
        }
    }

    val Utilities: ImageVector = ckIcon("utilities") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(13f, 3f); lineTo(4f, 14f); horizontalLineTo(11f)
            lineTo(10f, 21f); lineTo(19f, 10f); horizontalLineTo(12f)
            close()
        }
    }

    val Other: ImageVector = ckIcon("other") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(20f, 12f)
            arcTo(8f, 8f, 0f, true, true, 4f, 12f)
            arcTo(8f, 8f, 0f, false, true, 20f, 12f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(14f, 12f)
            arcTo(2f, 2f, 0f, true, true, 10f, 12f)
            arcTo(2f, 2f, 0f, false, true, 14f, 12f)
        }
    }

    val Plus: ImageVector = ckIcon("plus") {
        path(stroke = stroke, strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(12f, 5f); verticalLineTo(19f)
        }
        path(stroke = stroke, strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(5f, 12f); horizontalLineTo(19f)
        }
    }

    val Flame: ImageVector = ckIcon("flame") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(12f, 3f)
            curveTo(12f, 7f, 16f, 7f, 16f, 11f)
            arcTo(4f, 4f, 0f, false, true, 8f, 11f)
            curveTo(8f, 9f, 9f, 8f, 10f, 7f)
            curveTo(10f, 9f, 11f, 10f, 12f, 10f)
            curveTo(12f, 7f, 10f, 6f, 12f, 3f)
            close()
        }
    }

    val Trophy: ImageVector = ckIcon("trophy") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(7f, 4f); horizontalLineTo(17f); verticalLineTo(8f)
            arcTo(5f, 5f, 0f, false, true, 7f, 8f)
            close()
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(5f, 5f); horizontalLineTo(3f); verticalLineTo(7f)
            arcTo(3f, 3f, 0f, false, false, 6f, 10f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(19f, 5f); horizontalLineTo(21f); verticalLineTo(7f)
            arcTo(3f, 3f, 0f, false, true, 18f, 10f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(9f, 14f); horizontalLineTo(15f); verticalLineTo(17f); horizontalLineTo(9f); close()
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(8f, 20f); horizontalLineTo(16f)
        }
    }

    val Target: ImageVector = ckIcon("target") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(21f, 12f)
            arcTo(9f, 9f, 0f, true, true, 3f, 12f)
            arcTo(9f, 9f, 0f, false, true, 21f, 12f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(17f, 12f)
            arcTo(5f, 5f, 0f, true, true, 7f, 12f)
            arcTo(5f, 5f, 0f, false, true, 17f, 12f)
        }
        path(fill = stroke) {
            moveTo(10.5f, 12f)
            arcTo(1.5f, 1.5f, 0f, true, true, 13.5f, 12f)
            arcTo(1.5f, 1.5f, 0f, false, true, 10.5f, 12f)
        }
    }

    val Home: ImageVector = ckIcon("home") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(4f, 11f); lineTo(12f, 4f); lineTo(20f, 11f)
            verticalLineTo(20f)
            arcTo(1f, 1f, 0f, false, true, 19f, 21f)
            horizontalLineTo(14f); verticalLineTo(15f); horizontalLineTo(10f); verticalLineTo(21f)
            horizontalLineTo(5f)
            arcTo(1f, 1f, 0f, false, true, 4f, 20f)
            close()
        }
    }

    val List: ImageVector = ckIcon("list") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(4f, 6f); horizontalLineTo(20f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(4f, 12f); horizontalLineTo(20f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(4f, 18f); horizontalLineTo(20f)
        }
    }

    val Chart: ImageVector = ckIcon("chart") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(4f, 20f); verticalLineTo(10f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(10f, 20f); verticalLineTo(4f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(16f, 20f); verticalLineTo(12f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(22f, 20f); horizontalLineTo(2f)
        }
    }

    val User: ImageVector = ckIcon("user") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(16f, 8f)
            arcTo(4f, 4f, 0f, true, true, 8f, 8f)
            arcTo(4f, 4f, 0f, false, true, 16f, 8f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(4f, 21f)
            arcTo(8f, 8f, 0f, false, true, 20f, 21f)
        }
    }

    val Bolt: ImageVector = ckIcon("bolt") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(13f, 2f); lineTo(4f, 14f); horizontalLineTo(11f)
            lineTo(10f, 22f); lineTo(19f, 10f); horizontalLineTo(12f)
            close()
        }
    }

    val Check: ImageVector = ckIcon("check") {
        path(stroke = stroke, strokeLineWidth = 2.4f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(5f, 12f); lineTo(10f, 17f); lineTo(20f, 7f)
        }
    }

    val X: ImageVector = ckIcon("x") {
        path(stroke = stroke, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(6f, 6f); lineTo(18f, 18f)
        }
        path(stroke = stroke, strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
    }

    val Snow: ImageVector = ckIcon("snow") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(12f, 3f); verticalLineTo(21f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(5f, 7f); lineTo(19f, 17f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(5f, 17f); lineTo(19f, 7f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(3f, 12f); horizontalLineTo(21f)
        }
    }

    val Lock: ImageVector = ckIcon("lock") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(5f, 11f); horizontalLineTo(19f)
            arcTo(2f, 2f, 0f, false, true, 21f, 13f)
            verticalLineTo(19f)
            arcTo(2f, 2f, 0f, false, true, 19f, 21f)
            horizontalLineTo(5f)
            arcTo(2f, 2f, 0f, false, true, 3f, 19f)
            verticalLineTo(13f)
            arcTo(2f, 2f, 0f, false, true, 5f, 11f)
            close()
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(8f, 11f); verticalLineTo(8f)
            arcTo(4f, 4f, 0f, false, true, 16f, 8f)
            verticalLineTo(11f)
        }
    }

    val Leaf: ImageVector = ckIcon("leaf") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(4f, 20f)
            curveTo(4f, 10f, 12f, 4f, 20f, 4f)
            curveTo(18f, 14f, 14f, 18f, 4f, 20f)
            close()
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(4f, 20f)
            curveTo(8f, 15f, 12f, 12f, 17f, 10f)
        }
    }

    val Arrow: ImageVector = ckIcon("arrow") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(5f, 12f); horizontalLineTo(19f)
        }
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(13f, 6f); lineTo(19f, 12f); lineTo(13f, 18f)
        }
    }

    val Back: ImageVector = ckIcon("back") {
        path(stroke = stroke, strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, fill = SolidColor(Color.Transparent)) {
            moveTo(11f, 6f); lineTo(5f, 12f); lineTo(11f, 18f)
        }
    }

    // Category icon lookup
    fun forCategory(cat: String): ImageVector = when (cat) {
        "food"          -> Food
        "transport"     -> Transport
        "shopping"      -> Shopping
        "entertainment" -> Entertainment
        "health"        -> Health
        "utilities"     -> Utilities
        else            -> Other
    }

    // oklch-like category hues (approximated as sRGB, matching icons.jsx)
    fun categoryColor(cat: String): androidx.compose.ui.graphics.Color = when (cat) {
        "food"          -> androidx.compose.ui.graphics.Color(0xFFE8956D) // oklch 75% 0.15 30
        "transport"     -> androidx.compose.ui.graphics.Color(0xFF6DB4E8) // oklch 75% 0.14 220
        "shopping"      -> androidx.compose.ui.graphics.Color(0xFFE86DB4) // oklch 75% 0.15 320
        "entertainment" -> androidx.compose.ui.graphics.Color(0xFFA06DE8) // oklch 75% 0.15 280
        "health"        -> androidx.compose.ui.graphics.Color(0xFF6DE8A0) // oklch 78% 0.14 150
        "utilities"     -> androidx.compose.ui.graphics.Color(0xFFD4E86D) // oklch 78% 0.13 90
        else            -> androidx.compose.ui.graphics.Color(0xFFAAAAAA) // oklch 72% 0.02 240
    }
}
