/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.activity.generator

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
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import org.jetbrains.kotlin.name.FqName
import ru.pixnews.anvil.ksp.codegen.common.classname.AnvilClassName
import ru.pixnews.anvil.ksp.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.ksp.codegen.common.util.contributesToAnnotation
import ru.pixnews.anvil.ksp.codegen.common.util.readClassNameOrDefault

internal const val CONTRIBUTES_ACTIVITY_ANNOTATION_KSP_KEY = "ru.pixnews.anvil.ksp.activity.ContributesActivity"
internal const val ACTIVITY_MAP_KEY_KSP_KEY = "ru.pixnews.anvil.ksp.activity.ActivityMapKey"
internal const val ACTIVITY_SCOPE_KSP_KEY = "ru.pixnews.anvil.ksp.activity.ActivityScope"

private val ANDROID_ACTIVITY_CLASS_NAME = ClassName("android.app", "Activity")
private val ANDROID_ACTIVITY_FQ_NAME = FqName(ANDROID_ACTIVITY_CLASS_NAME.canonicalName)

@AutoService(AnvilKspExtension.Provider::class)
public class ContributesActivityAnvilKspExtensionProvider : AnvilKspExtension.Provider {
    override fun create(environment: SymbolProcessorEnvironment): AnvilKspExtension {
        val contributesActivityAnnotation = environment.readClassNameOrDefault(
            CONTRIBUTES_ACTIVITY_ANNOTATION_KSP_KEY,
            ClassName("ru.pixnews.anvil.ksp.codegen.activity.inject", "ContributesActivity"),
        )
        val activityMapKeyAnnotation = environment.readClassNameOrDefault(
            ACTIVITY_MAP_KEY_KSP_KEY,
            ClassName("ru.pixnews.anvil.ksp.codegen.activity.inject.wiring", "ActivityMapKey"),
        )
        val activityScope = environment.readClassNameOrDefault(
            ACTIVITY_SCOPE_KSP_KEY,
            ClassName("ru.pixnews.anvil.ksp.codegen.activity.inject", "ActivityScope"),
        )
        return ContributesActivityKspExtension(
            environment,
            contributesActivityAnnotation,
            activityMapKeyAnnotation,
            activityScope,
        )
    }

    override fun isApplicable(context: AnvilContext): Boolean = true
}

private class ContributesActivityKspExtension(
    environment: SymbolProcessorEnvironment,
    private val contributeActivityAnnotation: ClassName,
    private val activityMapKeyAnnotation: ClassName,
    private val activityScope: ClassName,
) : AnvilKspExtension {
    private val codeGenerator: CodeGenerator = environment.codeGenerator
    override val supportedAnnotationTypes = setOf(contributeActivityAnnotation.canonicalName)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(contributeActivityAnnotation.canonicalName)
            .filterIsInstance<KSClassDeclaration>()
            .distinctBy { it.qualifiedName?.asString() }
            .onEach { it.checkClassExtendsBoundType(ANDROID_ACTIVITY_FQ_NAME, resolver) }
            .map { classDeclaration: KSClassDeclaration ->
                classDeclaration.containingFile!! to generateActivityModuleFileSpec(classDeclaration.toClassName())
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

    private fun generateActivityModuleFileSpec(
        activityClass: ClassName,
    ): FileSpec {
        val moduleClassId: ClassName = activityClass.joinSimpleNames(suffix = "_ActivityModule")
        val moduleInterfaceSpec = TypeSpec.interfaceBuilder(moduleClassId)
            .addAnnotation(DaggerClassName.module)
            .addAnnotation(contributesToAnnotation(activityScope))
            .addFunction(generateBindMethod(activityClass))
            .build()
        return FileSpec.builder(moduleClassId).addType(moduleInterfaceSpec).build()
    }

    private fun generateBindMethod(
        activityClass: ClassName,
    ): FunSpec {
        // MembersInjector<out Activity>
        val returnType = DaggerClassName.membersInjector
            .parameterizedBy(WildcardTypeName.producerOf(ANDROID_ACTIVITY_CLASS_NAME))

        return FunSpec.builder("binds${activityClass.simpleName}Injector")
            .addModifiers(KModifier.ABSTRACT)
            .addAnnotation(DaggerClassName.binds)
            .addAnnotation(DaggerClassName.intoMap)
            .addAnnotation(
                AnnotationSpec
                    .builder(activityMapKeyAnnotation)
                    .addMember("activityClass = %T::class", activityClass)
                    .build(),
            )
            .addAnnotation(
                AnnotationSpec
                    .builder(AnvilClassName.singleIn)
                    .addMember("%T::class", activityScope)
                    .build(),
            )
            .addParameter("target", DaggerClassName.membersInjector.parameterizedBy(activityClass))
            .returns(returnType)
            .build()
    }
}
