/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.test.generator

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.AnvilKspExtension
import com.squareup.anvil.compiler.internal.joinSimpleNames
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import ru.pixnews.anvil.ksp.codegen.common.classname.AnvilClassName
import ru.pixnews.anvil.ksp.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.ksp.codegen.common.util.contributesToAnnotation
import ru.pixnews.anvil.ksp.codegen.common.util.readClassNameOrDefault

internal const val APP_SCOPE_KSP_KEY = "ru.pixnews.anvil.ksp.base.AppScope"
internal const val CONTRIBUTES_TEST_ANNOTATION_KSP_KEY = "ru.pixnews.anvil.ksp.test.ContributesTest"
internal const val INSTRUMENTED_TEST_INJECTOR_KSP_KEY = "ru.pixnews.anvil.ksp.test.SingleInstrumentedTestInjector"

@AutoService(AnvilKspExtension.Provider::class)
public class ContributesActivityAnvilKspExtensionProvider : AnvilKspExtension.Provider {
    override fun create(environment: SymbolProcessorEnvironment): AnvilKspExtension {
        val appScopeAnnotation = environment.readClassNameOrDefault(
            APP_SCOPE_KSP_KEY,
            ClassName("ru.pixnews.foundation.di.base.scopes", "AppScope"),
        )
        val contributesTestAnnotation = environment.readClassNameOrDefault(
            CONTRIBUTES_TEST_ANNOTATION_KSP_KEY,
            ClassName("ru.pixnews.anvil.ksp.codegen.test.inject", "ContributesTest"),
        )
        val instrumentedTestInjectorClass = environment.readClassNameOrDefault(
            INSTRUMENTED_TEST_INJECTOR_KSP_KEY,
            ClassName("ru.pixnews.anvil.ksp.codegen.test.inject.wiring", "SingleInstrumentedTestInjector"),
        )
        return ContributesActivityKspExtension(
            environment,
            appScopeAnnotation,
            contributesTestAnnotation,
            instrumentedTestInjectorClass,
        )
    }

    override fun isApplicable(context: AnvilContext): Boolean = true
}

private class ContributesActivityKspExtension(
    environment: SymbolProcessorEnvironment,
    private val appScopeAnnotation: ClassName,
    private val contributesTestAnnotation: ClassName,
    private val instrumentedTestInjectorClass: ClassName,
) : AnvilKspExtension {
    private val codeGenerator: CodeGenerator = environment.codeGenerator
    override val supportedAnnotationTypes = setOf(contributesTestAnnotation.canonicalName)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(contributesTestAnnotation.canonicalName)
            .filterIsInstance<KSClassDeclaration>()
            .distinctBy { it.qualifiedName?.asString() }
            .map { classDeclaration: KSClassDeclaration ->
                classDeclaration.containingFile!! to generateTestModule(classDeclaration.toClassName())
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

    private fun generateTestModule(
        annotatedClass: ClassName,
    ): FileSpec {
        val moduleClass = annotatedClass.joinSimpleNames(suffix = "_TestModule")
        val moduleSpecBuilder = TypeSpec.objectBuilder(moduleClass)
            .addAnnotation(DaggerClassName.module)
            .addAnnotation(contributesToAnnotation(appScopeAnnotation))
            .addFunction(generateProvideMethod(annotatedClass))
        return FileSpec.builder(moduleClass).addType(moduleSpecBuilder.build()).build()
    }

    private fun generateProvideMethod(
        testClass: ClassName,
    ): FunSpec {
        return FunSpec.builder("provide${testClass.simpleName}Injector")
            .addAnnotation(DaggerClassName.provides)
            .addAnnotation(DaggerClassName.intoMap)
            .addAnnotation(
                AnnotationSpec
                    .builder(DaggerClassName.classKey)
                    .addMember("%T::class", testClass)
                    .build(),
            )
            .addAnnotation(
                AnnotationSpec
                    .builder(AnvilClassName.singleIn)
                    .addMember("%T::class", appScopeAnnotation)
                    .build(),
            )
            .addParameter("injector", DaggerClassName.membersInjector.parameterizedBy(testClass))
            .returns(instrumentedTestInjectorClass)
            .addStatement("return %T(injector)", instrumentedTestInjectorClass)
            .build()
    }
}
