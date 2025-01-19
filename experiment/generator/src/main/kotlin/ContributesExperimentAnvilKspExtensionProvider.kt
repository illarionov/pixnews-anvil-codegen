/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.experiment.generator

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.AnvilKspExtension
import com.squareup.anvil.compiler.internal.joinSimpleNames
import com.squareup.anvil.compiler.internal.ksp.checkClassExtendsBoundType
import com.squareup.anvil.compiler.internal.ksp.fqName
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import ru.pixnews.anvil.ksp.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.ksp.codegen.common.util.contributesToAnnotation
import ru.pixnews.anvil.ksp.codegen.experiment.generator.PixnewsExperimentClassName.contributesExperimentFqName
import ru.pixnews.anvil.ksp.codegen.experiment.generator.PixnewsExperimentClassName.contributesExperimentVariantSerializerFqName
import ru.pixnews.anvil.ksp.codegen.experiment.generator.PixnewsExperimentClassName.experimentVariantMapKey
import ru.pixnews.anvil.ksp.codegen.experiment.generator.PixnewsExperimentClassName.experimentVariantSerializer
import ru.pixnews.anvil.ksp.codegen.experiment.generator.PixnewsExperimentClassName.experimentVariantSerializerFqName
import java.util.SortedSet

@AutoService(AnvilKspExtension.Provider::class)
public class ContributesExperimentAnvilKspExtensionProvider : AnvilKspExtension.Provider {
    override fun create(environment: SymbolProcessorEnvironment): AnvilKspExtension =
        ContributesExperimentAnvilKspExtension(environment)

    override fun isApplicable(context: AnvilContext): Boolean = true
}

private class ContributesExperimentAnvilKspExtension(
    environment: SymbolProcessorEnvironment,
    override val supportedAnnotationTypes: Set<String> = setOf(
        contributesExperimentFqName.asString(),
        contributesExperimentVariantSerializerFqName.asString(),
    ),
) : AnvilKspExtension {
    private val codeGenerator: CodeGenerator = environment.codeGenerator
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val experimentClasses = resolver
            .getSymbolsWithAnnotation(contributesExperimentFqName.asString())
            .filterIsInstance<KSClassDeclaration>()
            .onEach { it.checkClassExtendsBoundType(PixnewsExperimentClassName.experimentFqName, resolver) }
        val serializerClasses = resolver
            .getSymbolsWithAnnotation(contributesExperimentVariantSerializerFqName.asString())
            .filterIsInstance<KSClassDeclaration>()
            .onEach { it.checkClassExtendsBoundType(experimentVariantSerializerFqName, resolver) }

        val annotatedClasses: SortedSet<KSClassDeclaration> = (experimentClasses + serializerClasses)
            .distinctBy { it.qualifiedName?.asString() }
            .toSortedSet(compareBy { it.qualifiedName?.asString() ?: "" })

        if (annotatedClasses.isNotEmpty()) {
            val fileSpec = generateExperimentModule(annotatedClasses)
            fileSpec.writeTo(codeGenerator, true)
        }

        return emptyList()
    }

    private fun generateExperimentModule(
        annotatedClasses: Collection<KSClassDeclaration>,
    ): FileSpec {
        val moduleClassId = annotatedClasses.first().toClassName().joinSimpleNames(suffix = "_Experiments_Module")

        val moduleTypeSpecBuilder = TypeSpec.objectBuilder(moduleClassId)
            .addAnnotation(DaggerClassName.module)
            .addAnnotation(contributesToAnnotation(PixnewsExperimentClassName.experimentScope))

        annotatedClasses.forEach {
            moduleTypeSpecBuilder.addFunction(generateProvidesMethod(it))
            moduleTypeSpecBuilder.addOriginatingKSFile(it.containingFile!!)
        }

        val content = FileSpec.builder(moduleClassId)
            .addType(moduleTypeSpecBuilder.build())
            .build()
        return content
    }

    private fun generateProvidesMethod(
        annotatedClass: KSClassDeclaration,
    ): FunSpec {
        val annotation = annotatedClass.annotations.filter { annotationRef ->
            annotationRef.fqName.let {
                it == contributesExperimentFqName || it == contributesExperimentVariantSerializerFqName
            }
        }.firstOrNull() ?: error("$annotatedClass has an incorrect combination of annotations")

        return when (annotation.fqName) {
            contributesExperimentFqName -> providesExperimentFunction(annotatedClass.toClassName())
            contributesExperimentVariantSerializerFqName -> {
                val experimentValue: String = annotation.arguments.singleOrNull()?.value?.toString()
                    ?: throw IllegalArgumentException("experimentKey on ContributesExperimentVariant not defined")

                providesExperimentVariantSerializerFunction(
                    annotatedSerializer = annotatedClass.toClassName(),
                    experimentVariantKey = experimentValue,
                )
            }

            else -> throw IllegalArgumentException("Unknown annotation $annotation")
        }
    }
}

/**
 * `@Provides @IntoSet abstract fun provideMainExperiment(experiment: MainExperiment): Experiment`
 */
private fun providesExperimentFunction(annotatedExperiment: ClassName): FunSpec {
    return FunSpec.builder("provide${annotatedExperiment.simpleName}")
        .addAnnotation(DaggerClassName.provides)
        .addAnnotation(DaggerClassName.intoSet)
        .returns(PixnewsExperimentClassName.experiment)
        .addCode("return %T", annotatedExperiment)
        .build()
}

/**
 * `@Provides @IntoSet abstract fun provideMainExperiment(experiment: MainExperiment): Experiment`
 */
private fun providesExperimentVariantSerializerFunction(
    annotatedSerializer: ClassName,
    experimentVariantKey: String,
): FunSpec {
    return FunSpec.builder("provide${annotatedSerializer.simpleName}")
        .addAnnotation(DaggerClassName.provides)
        .addAnnotation(DaggerClassName.intoMap)
        .addAnnotation(
            AnnotationSpec
                .builder(experimentVariantMapKey)
                .addMember("key = %S", experimentVariantKey)
                .build(),
        )
        .returns(experimentVariantSerializer)
        .addCode("return %T", annotatedSerializer)
        .build()
}
