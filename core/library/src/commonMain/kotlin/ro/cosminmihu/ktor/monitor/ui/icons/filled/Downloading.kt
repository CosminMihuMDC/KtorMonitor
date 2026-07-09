/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.cosminmihu.ktor.monitor.ui.icons.filled

import ro.cosminmihu.ktor.monitor.ui.icons.Icons
import ro.cosminmihu.ktor.monitor.ui.icons.materialIcon
import ro.cosminmihu.ktor.monitor.ui.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

internal val Icons.Filled.Downloading: ImageVector
    get() {
        if (_downloading != null) {
            return _downloading!!
        }
        _downloading = materialIcon(name = "Filled.Downloading") {
            materialPath {
                moveTo(18.32f, 4.26f)
                curveTo(16.84f, 3.05f, 15.01f, 2.25f, 13.0f, 2.05f)
                verticalLineToRelative(2.02f)
                curveToRelative(1.46f, 0.18f, 2.79f, 0.76f, 3.9f, 1.62f)
                lineTo(18.32f, 4.26f)
                close()
                moveTo(19.93f, 11.0f)
                horizontalLineToRelative(2.02f)
                curveToRelative(-0.2f, -2.01f, -1.0f, -3.84f, -2.21f, -5.32f)
                lineTo(18.31f, 7.1f)
                curveTo(19.17f, 8.21f, 19.75f, 9.54f, 19.93f, 11.0f)
                close()
                moveTo(18.31f, 16.9f)
                lineToRelative(1.43f, 1.43f)
                curveToRelative(1.21f, -1.48f, 2.01f, -3.32f, 2.21f, -5.32f)
                horizontalLineToRelative(-2.02f)
                curveTo(19.75f, 14.46f, 19.17f, 15.79f, 18.31f, 16.9f)
                close()
                moveTo(13.0f, 19.93f)
                verticalLineToRelative(2.02f)
                curveToRelative(2.01f, -0.2f, 3.84f, -1.0f, 5.32f, -2.21f)
                lineToRelative(-1.43f, -1.43f)
                curveTo(15.79f, 19.17f, 14.46f, 19.75f, 13.0f, 19.93f)
                close()
                moveTo(13.0f, 12.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(5.0f)
                horizontalLineTo(7.0f)
                lineToRelative(5.0f, 5.0f)
                lineToRelative(5.0f, -5.0f)
                horizontalLineTo(13.0f)
                close()
                moveTo(11.0f, 19.93f)
                verticalLineToRelative(2.02f)
                curveToRelative(-5.05f, -0.5f, -9.0f, -4.76f, -9.0f, -9.95f)
                reflectiveCurveToRelative(3.95f, -9.45f, 9.0f, -9.95f)
                verticalLineToRelative(2.02f)
                curveTo(7.05f, 4.56f, 4.0f, 7.92f, 4.0f, 12.0f)
                reflectiveCurveTo(7.05f, 19.44f, 11.0f, 19.93f)
                close()
            }
        }
        return _downloading!!
    }

private var _downloading: ImageVector? = null
