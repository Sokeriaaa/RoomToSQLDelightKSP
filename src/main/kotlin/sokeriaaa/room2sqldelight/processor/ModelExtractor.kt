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

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import sokeriaaa.room2sqldelight.helper.getAnnotationOrNull
import sokeriaaa.room2sqldelight.helper.getArgumentValueOrNull
import sokeriaaa.room2sqldelight.model.table.ColumnModel
import sokeriaaa.room2sqldelight.model.table.EntityModel
import sokeriaaa.room2sqldelight.model.table.IndexModel
import sokeriaaa.room2sqldelight.model.table.PrimaryKeyModel

object ModelExtractor {

    fun extractEntityModel(clazz: KSClassDeclaration): EntityModel {
        val entity = clazz.getAnnotationOrNull(qualifiedName = "androidx.room.Entity")
        val tableName = entity?.getArgumentValueOrNull("tableName") ?: clazz.simpleName.asString()

        val columns = clazz.getAllProperties()
            .map { extractColumn(it) }
            .toList()

        val primaryKey = extractPrimaryKey(clazz)
        val indices = extractIndices(clazz)

        return EntityModel(
            tableName = tableName,
            columns = columns,
            primaryKey = primaryKey,
            indices = indices
        )
    }

    private fun extractColumn(prop: KSPropertyDeclaration): ColumnModel {
        val columnInfo = prop.getAnnotationOrNull(qualifiedName = "androidx.room.ColumnInfo")
        val name = columnInfo?.getArgumentValueOrNull("name") ?: prop.simpleName.asString()

        val nullable = prop.type.resolve().isMarkedNullable
        val sqlType = TypeMapper.map(prop.type.resolve())

        val primaryKey = prop.getAnnotationOrNull(qualifiedName = "androidx.room.PrimaryKey")
        val auto = primaryKey?.getArgumentValueOrNull("autoGenerate") ?: false

        return ColumnModel(
            name = name,
            sqlType = sqlType,
            nullable = nullable,
            autoIncrement = auto
        )
    }

    fun extractPrimaryKey(clazz: KSClassDeclaration): PrimaryKeyModel? {
        val entityAnno = clazz.getAnnotationOrNull(qualifiedName = "androidx.room.Entity")

        val entityPrimaryKeys = entityAnno?.getArgumentValueOrNull<List<*>>("primaryKeys")

        if (!entityPrimaryKeys.isNullOrEmpty()) {
            val columns = entityPrimaryKeys.mapNotNull { it as? String }
            return PrimaryKeyModel(
                columns = columns,
                autoGenerate = false // Room does NOT support autogen here
            )
        }

        val pkProps = clazz.getAllProperties().mapNotNull { prop ->
            val pkAnno = prop.getAnnotationOrNull(qualifiedName = "androidx.room.PrimaryKey")
                ?: return@mapNotNull null

            val autoGenerate = pkAnno.getArgumentValueOrNull("autoGenerate") ?: false

            prop to autoGenerate
        }.toList()

        if (pkProps.isEmpty()) return null

        return PrimaryKeyModel(
            columns = pkProps.map { extractColumn(it.first).name },
            autoGenerate = pkProps.any { it.second }
        )
    }

    fun extractIndices(clazz: KSClassDeclaration): List<IndexModel> {
        val entityAnno = clazz.getAnnotationOrNull(qualifiedName = "androidx.room.Entity")
            ?: return emptyList()

        val indicesArg = entityAnno.getArgumentValueOrNull<List<*>>("indices") ?: return emptyList()

        return indicesArg.mapNotNull { value ->
            val anno = value as? KSAnnotation ?: return@mapNotNull null

            val columns = anno.getArgumentValueOrNull("value") ?: emptyList<Any>()
            val unique = anno.getArgumentValueOrNull("unique") ?: false
            val name = anno.getArgumentValueOrNull<String>("name")?.takeIf { it.isNotBlank() }

            IndexModel(
                name = name,
                columns = columns.mapNotNull { it as? String },
                unique = unique
            )
        }
    }
}
