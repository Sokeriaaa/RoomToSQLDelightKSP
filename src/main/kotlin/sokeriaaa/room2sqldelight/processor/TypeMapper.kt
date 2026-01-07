/**
 * Copyright (C) 2026 Sokeriaaa
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package sokeriaaa.room2sqldelight.processor

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

object TypeMapper {

    fun map(type: KSType): String {
        return when (type.declaration.qualifiedName?.asString()) {
            "kotlin.Int",
            "kotlin.Long",
            "kotlin.Boolean" -> "INTEGER"

            "kotlin.Float",
            "kotlin.Double" -> "REAL"

            "kotlin.String" -> "TEXT"

            else -> {
                if (type.declaration is KSClassDeclaration &&
                    (type.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS
                ) {
                    "TEXT"
                } else {
                    error("Unsupported type: $type")
                }
            }
        }
    }
}
