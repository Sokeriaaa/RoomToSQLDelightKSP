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
package sokeriaaa.room2sqldelight.helper

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation

fun KSAnnotated.getAnnotationOrNull(qualifiedName: String): KSAnnotation? =
    annotations.firstOrNull {
        it.shortName.asString() == qualifiedName.substringAfterLast(".")
                && it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
    }

inline fun <reified T : Any?> KSAnnotation.getArgumentValueOrNull(argName: String): T? =
    arguments.firstOrNull { arg -> arg.name?.asString() == argName }?.value as? T