/**
 * Copyright (C) 2026 Sokeriaaa
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
package sokeriaaa.room2sqldelight.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import sokeriaaa.room2sqldelight.helper.getAnnotationOrNull
import sokeriaaa.room2sqldelight.helper.getArgumentValueOrNull
import sokeriaaa.room2sqldelight.model.dao.SqlStatement
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

    fun extractDao(clazz: KSClassDeclaration): List<SqlStatement> {
        return clazz.getAllFunctions()
            .map { extractSqlStatement(it) }
            .filterNotNull()
            .toList()
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

    fun extractSqlStatement(func: KSFunctionDeclaration): SqlStatement? {
        val name = func.simpleName.asString()

        func.annotations.forEach { anno ->
            val fqName = anno.annotationType.resolve()
                .declaration.qualifiedName?.asString()

            when (fqName) {
                "androidx.room.Query" -> {
                    val sql = anno.arguments
                        .first { it.name?.asString() == "value" }
                        .value as String

                    return SqlStatement.Query(name, normalizeSql(func, sql))
                }

                "androidx.room.Insert" -> {
                    return buildInsert(func, name, anno)
                }

                // TODO Support
//                "androidx.room.Delete" -> {
//                    return buildDelete(func, name)
//                }
//
//                "androidx.room.Update" -> {
//                    return buildUpdate(func, name)
//                }
//
//                "androidx.room.Upsert" -> {
//                    return buildUpsert(func, name)
//                }
            }
        }
        return null
    }

    fun normalizeSql(func: KSFunctionDeclaration, sql: String): String =
        sql.replace("`", "")
            .replace(Regex(":([A-Za-z_][A-Za-z0-9_]*)"), "?")
            .trim()
            .removeSuffix(";")

    fun buildInsert(
        func: KSFunctionDeclaration,
        name: String,
        anno: KSAnnotation
    ): SqlStatement.Insert {
        val entityType = func.parameters.first().type.resolve()
        val entityDecl = entityType.unwrapEntity()
        val entity = extractEntityModel(entityDecl)
        val replace = anno.arguments
            .firstOrNull { it.name?.asString() == "onConflict" }
            ?.value?.toString() == "REPLACE"

        return SqlStatement.Insert(
            name = name,
            table = entity.tableName,
            columns = entity.columns.map { it.name },
            replace = replace
        )
    }

    fun KSType.unwrapEntity(): KSClassDeclaration {
        val decl = declaration

        // List<T>, MutableList<T>, etc.
        if (decl.qualifiedName?.asString() in listOf(
                "kotlin.collections.List",
                "kotlin.collections.MutableList"
            )
        ) {
            val argType = arguments.first().type!!.resolve()
            return argType.unwrapEntity()
        }

        // Array<T>
        if (decl.qualifiedName?.asString() == "kotlin.Array") {
            val argType = arguments.first().type!!.resolve()
            return argType.unwrapEntity()
        }

        // Entity class
        if (decl is KSClassDeclaration) {
            return decl
        }

        error("Unsupported @Insert parameter type: $decl")
    }


}
