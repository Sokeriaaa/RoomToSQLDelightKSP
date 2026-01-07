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

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class RoomSqlProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val entities = resolver
            .getSymbolsWithAnnotation("androidx.room.Entity")
            .filterIsInstance<KSClassDeclaration>()

        entities.forEach { entity ->
            val model = ModelExtractor.extractEntityModel(entity)
            val sql = SqlGenerator.generate(model)
            OutputWriter.write(entity, sql, environment)
        }

        val daoList = resolver
            .getSymbolsWithAnnotation("androidx.room.Dao")
            .filterIsInstance<KSClassDeclaration>()

        daoList.forEach { dao ->
            val model = ModelExtractor.extractDao(dao)
            val sql = SqlGenerator.generate(model)
            OutputWriter.write(dao, sql, environment)
        }

        return emptyList()
    }
}
