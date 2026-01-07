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
