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
package sokeriaaa.room2sqldelight.model.dao

sealed class SqlStatement {
    abstract val name: String

    data class Query(
        override val name: String,
        val sql: String
    ) : SqlStatement()

    data class Insert(
        override val name: String,
        val table: String,
        val columns: List<String>,
        val replace: Boolean
    ) : SqlStatement()

    data class Delete(
        override val name: String,
        val sql: String
    ) : SqlStatement()

    data class Update(
        override val name: String,
        val sql: String
    ) : SqlStatement()

    data class Upsert(
        override val name: String,
        val sql: String
    ) : SqlStatement()
}

fun SqlStatement.toSql(): String = when (this) {
    is SqlStatement.Query ->
        "$name:\n$sql;"

    is SqlStatement.Insert -> buildString {
        append("$name:\n")
        append("INSERT ")
        if (replace) append("OR REPLACE ")
        append("INTO $table(\n")
        append(columns.joinToString(",\n  ", prefix = "  "))
        append("\n) VALUES (")
        repeat(columns.size) { i ->
            if (i > 0) {
                append(", ")
            }
            append("?")
        }
        append(");")
    }

    is SqlStatement.Delete ->
        "$name:\n$sql;"

    is SqlStatement.Update ->
        "$name:\n$sql;"

    is SqlStatement.Upsert ->
        "$name:\n$sql;"
}
