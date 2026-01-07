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

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration

object OutputWriter {

    fun write(
        entity: KSClassDeclaration,
        sql: String,
        env: SymbolProcessorEnvironment
    ) {
        val fileName = "SQ" + entity.simpleName.asString()
        val packageName = entity.packageName.asString()
        val packagePath = packageName.replace('.', '/')

        env.codeGenerator.createNewFileByPath(
            Dependencies(false, entity.containingFile!!),
            path = "sqldelight/$packagePath/$fileName",
            extensionName = "sq",
        ).writer().use {
            it.write(sql)
        }
    }
}
