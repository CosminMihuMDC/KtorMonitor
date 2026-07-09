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

internal val Icons.Filled.ClearAll: ImageVector
    get() {
        if (_clearAll != null) {
            return _clearAll!!
        }
        _clearAll = materialIcon(name = "Filled.ClearAll") {
            materialPath {
                moveTo(5.0f, 13.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                lineTo(5.0f, 11.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(3.0f, 17.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                lineTo(3.0f, 15.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(7.0f, 7.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(14.0f)
                lineTo(21.0f, 7.0f)
                lineTo(7.0f, 7.0f)
                close()
            }
        }
        return _clearAll!!
    }

private var _clearAll: ImageVector? = null
