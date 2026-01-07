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
