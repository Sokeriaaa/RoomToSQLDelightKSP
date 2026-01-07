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

import sokeriaaa.room2sqldelight.model.dao.SqlStatement
import sokeriaaa.room2sqldelight.model.dao.toSql
import sokeriaaa.room2sqldelight.model.table.EntityModel
import sokeriaaa.room2sqldelight.model.table.IndexModel

object SqlGenerator {

    fun generate(entity: EntityModel): String {
        val sb = StringBuilder()

        sb.appendLine("CREATE TABLE ${entity.tableName} (")

        val columnDefs = entity.columns.map { col ->
            buildString {
                append("    ${col.name} ${col.sqlType}")
                if (!col.nullable) append(" NOT NULL")
                if (col.autoIncrement) append(" PRIMARY KEY AUTOINCREMENT")
            }
        }

        sb.append(columnDefs.joinToString(",\n"))

        entity.primaryKey?.let {
            if (it.columns.size > 1) {
                sb.append(",\n    PRIMARY KEY (${it.columns.joinToString(", ")})")
            }
        }

        sb.appendLine("\n);")

        entity.indices.forEach { idx ->
            sb.appendLine(generateIndex(entity.tableName, idx))
        }

        return sb.toString()
    }

    fun generate(statements: List<SqlStatement>): String {
        return buildString {
            statements.forEach {
                append(it.toSql())
                append("\n\n")
            }
        }
    }

    private fun generateIndex(table: String, index: IndexModel): String {
        val name = index.name ?: "idx_${table}_${index.columns.joinToString("_")}"
        val unique = if (index.unique) "UNIQUE " else ""

        return "CREATE ${unique}INDEX $name ON $table(${index.columns.joinToString(", ")});"
    }

}
