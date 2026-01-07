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
import sokeriaaa.room2sqldelight.model.ColumnModel
import sokeriaaa.room2sqldelight.model.EntityModel
import sokeriaaa.room2sqldelight.model.IndexModel
import sokeriaaa.room2sqldelight.model.PrimaryKeyModel
import javax.lang.model.element.AnnotationValue

object ModelExtractor {

    fun extract(clazz: KSClassDeclaration): EntityModel {
        val entity = clazz.annotations
            .firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                        "androidx.room.Entity"
            }
        val tableName = entity?.arguments?.firstOrNull { arg ->
            arg.name?.asString() == "tableName"
        }?.value as? String
            ?: clazz.simpleName.asString()

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
        val columnInfo = prop.annotations
            .firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                        "androidx.room.ColumnInfo"
            }

        val name = columnInfo?.arguments
            ?.firstOrNull { it.name?.asString() == "name" }
            ?.value as? String
            ?: prop.simpleName.asString()

        val nullable = prop.type.resolve().isMarkedNullable
        val sqlType = TypeMapper.map(prop.type.resolve())

        val primaryKey = prop.annotations
            .firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                        "androidx.room.PrimaryKey"
            }
        val auto = primaryKey?.arguments
            ?.firstOrNull { it.name?.asString() == "autoGenerate" }
            ?.value as? Boolean == true

        return ColumnModel(
            name = name,
            sqlType = sqlType,
            nullable = nullable,
            autoIncrement = auto
        )
    }

    fun extractPrimaryKey(clazz: KSClassDeclaration): PrimaryKeyModel? {
        val entityAnno = clazz.annotations.firstOrNull {
            it.shortName.asString() == "Entity" &&
                    it.annotationType.resolve().declaration
                        .qualifiedName?.asString() == "androidx.room.Entity"
        }

        val entityPrimaryKeys = entityAnno
            ?.arguments
            ?.firstOrNull { it.name?.asString() == "primaryKeys" }
            ?.value as? List<*>

        if (!entityPrimaryKeys.isNullOrEmpty()) {
            val columns = entityPrimaryKeys.mapNotNull { it as? String }
            return PrimaryKeyModel(
                columns = columns,
                autoGenerate = false // Room does NOT support autogen here
            )
        }

        val pkProps = clazz.getAllProperties().mapNotNull { prop ->
            val pkAnno = prop.annotations.firstOrNull {
                it.shortName.asString() == "PrimaryKey" &&
                        it.annotationType.resolve().declaration
                            .qualifiedName?.asString() == "androidx.room.PrimaryKey"
            } ?: return@mapNotNull null

            val autoGenerate = pkAnno.arguments
                .firstOrNull { it.name?.asString() == "autoGenerate" }
                ?.value as? Boolean ?: false

            prop to autoGenerate
        }.toList()

        if (pkProps.isEmpty()) return null

        return PrimaryKeyModel(
            columns = pkProps.map { extractColumn(it.first).name },
            autoGenerate = pkProps.any { it.second }
        )
    }

    fun extractIndices(clazz: KSClassDeclaration): List<IndexModel> {
        val entityAnno = clazz.annotations.firstOrNull {
            it.shortName.asString() == "Entity" &&
                    it.annotationType.resolve().declaration
                        .qualifiedName?.asString() == "androidx.room.Entity"
        } ?: return emptyList()

        val indicesArg = entityAnno.arguments
            .firstOrNull { it.name?.asString() == "indices" }
            ?.value as? List<*>
            ?: return emptyList()

        return indicesArg.mapNotNull { value ->
            val anno = (value as? AnnotationValue)?.value as? KSAnnotation
                ?: return@mapNotNull null

            val columns = (anno.arguments
                .firstOrNull { it.name?.asString() == "value" }
                ?.value as? List<*>)
                ?: emptyList<Any>()

            val unique = (anno.arguments
                .firstOrNull { it.name?.asString() == "unique" }
                ?.value as? Boolean) ?: false

            val name = (anno.arguments
                .firstOrNull { it.name?.asString() == "name" }
                ?.value as? String)
                ?.takeIf { it.isNotBlank() }

            IndexModel(
                name = name,
                columns = columns.mapNotNull { it as? String },
                unique = unique
            )
        }
    }


}
