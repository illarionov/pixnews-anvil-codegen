/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.workmanager.generator

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
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import org.jetbrains.kotlin.name.FqName
import ru.pixnews.anvil.ksp.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.ksp.codegen.common.util.contributesMultibindingAnnotation
import ru.pixnews.anvil.ksp.codegen.common.util.readClassNameOrDefault

internal val ANDROID_CONTEXT_CLASS_NAME: ClassName = ClassName("android.content", "Context")
internal val WORKER_PARAMETERS_CLASS_NAME: ClassName = ClassName("androidx.work", "WorkerParameters")
private val COROUTINE_WORKER_FQ_NAME = FqName("androidx.work.CoroutineWorker")

@AutoService(AnvilKspExtension.Provider::class)
public class ContributesCoroutineWorkerAnvilKspExtensionProvider : AnvilKspExtension.Provider {
    override fun create(environment: SymbolProcessorEnvironment): AnvilKspExtension {
        val contributeCoroutineWorkerAnnotation = environment.readClassNameOrDefault(
            KspKey.CONTRIBUTE_COROUTINE_WORKER,
            ClassName("ru.pixnews.anvil.ksp.codegen.workmanager.inject", "ContributesCoroutineWorker"),
        )
        val applicationContextAnnotation = environment.readClassNameOrDefault(
            KspKey.APPLICATION_CONTEXT_QUALIFIER,
            ClassName("ru.pixnews.foundation.di.base.qualifiers", "ApplicationContext"),
        )
        val coroutineWorkerFactoryClass = environment.readClassNameOrDefault(
            KspKey.COROUTINE_WORKER_FACTORY,
            ClassName("ru.pixnews.anvil.ksp.codegen.workmanager.inject.wiring", "CoroutineWorkerFactory"),
        )
        val coroutineWorkerFactoryMapKeyAnnotation = environment.readClassNameOrDefault(
            KspKey.COROUTINE_WORKER_MAP_KEY,
            ClassName("ru.pixnews.anvil.ksp.codegen.workmanager.inject.wiring", "CoroutineWorkerMapKey"),
        )
        val workManagerScopeClass = environment.readClassNameOrDefault(
            KspKey.WORK_MANAGER_SCOPE,
            ClassName("ru.pixnews.anvil.ksp.codegen.workmanager.inject", "WorkManagerScope"),
        )
        return ContributesCoroutineWorkerKspExtension(
            environment,
            contributeCoroutineWorkerAnnotation,
            applicationContextAnnotation,
            coroutineWorkerFactoryClass,
            coroutineWorkerFactoryMapKeyAnnotation,
            workManagerScopeClass,
        )
    }

    override fun isApplicable(context: AnvilContext): Boolean = true
}

private class ContributesCoroutineWorkerKspExtension(
    environment: SymbolProcessorEnvironment,
    private val contributeCoroutineWorkerAnnotation: ClassName,
    private val applicationContextAnnotation: ClassName,
    private val coroutineWorkerFactoryClass: ClassName,
    private val coroutineWorkerFactoryMapKeyAnnotation: ClassName,
    private val workManagerScopeClass: ClassName,
) : AnvilKspExtension {
    private val codeGenerator: CodeGenerator = environment.codeGenerator
    override val supportedAnnotationTypes = setOf(contributeCoroutineWorkerAnnotation.canonicalName)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(contributeCoroutineWorkerAnnotation.canonicalName)
            .filterIsInstance<KSClassDeclaration>()
            .distinctBy { it.qualifiedName?.asString() }
            .onEach { it.checkClassExtendsBoundType(COROUTINE_WORKER_FQ_NAME, resolver) }
            .map { classDeclaration: KSClassDeclaration ->
                classDeclaration.containingFile!! to generateWorkManagerFactory(classDeclaration.toClassName())
            }
            .forEach { (originatingKSFile, spec) ->
                spec.writeTo(
                    codeGenerator,
                    aggregating = false,
                    originatingKSFiles = listOf(originatingKSFile),
                )
            }
        return emptyList()
    }

    private fun generateWorkManagerFactory(
        workerClassName: ClassName,
    ): FileSpec {
        val factoryClass = workerClassName.joinSimpleNames(suffix = "_AssistedFactory")
        val factoryInterfaceSpec = TypeSpec.interfaceBuilder(factoryClass)
            .addAnnotation(DaggerClassName.assistedFactory)
            .addAnnotation(contributesMultibindingAnnotation(workManagerScopeClass))
            .addAnnotation(
                AnnotationSpec
                    .builder(coroutineWorkerFactoryMapKeyAnnotation)
                    .addMember("%T::class", workerClassName)
                    .build(),
            )
            .addSuperinterface(coroutineWorkerFactoryClass)
            .addFunction(createWorkerFunction(workerClassName))
            .build()
        return FileSpec.builder(factoryClass).addType(factoryInterfaceSpec).build()
    }

    /**
     * ```
     * override fun create(@ApplicationContext context: Context, workerParameters: WorkerParameters): <annotatedClass>
     * ```
     */
    private fun createWorkerFunction(workerClass: ClassName): FunSpec {
        return FunSpec.builder("create")
            .addModifiers(ABSTRACT, OVERRIDE, PUBLIC)
            .addParameter(
                ParameterSpec.builder("context", ANDROID_CONTEXT_CLASS_NAME)
                    .addAnnotation(applicationContextAnnotation)
                    .build(),
            )
            .addParameter("workerParameters", WORKER_PARAMETERS_CLASS_NAME)
            .returns(workerClass)
            .build()
    }
}
