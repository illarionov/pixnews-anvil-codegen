/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.initializer.generator

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.AnvilKspExtension
import com.squareup.anvil.compiler.internal.joinSimpleNames
import com.squareup.anvil.compiler.internal.ksp.checkClassExtendsBoundType
import com.squareup.anvil.compiler.internal.ksp.getKSAnnotationsByQualifiedName
import com.squareup.anvil.compiler.internal.ksp.resolveKSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import org.jetbrains.kotlin.name.FqName
import ru.pixnews.anvil.ksp.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.ksp.codegen.common.util.contributesToAnnotation
import ru.pixnews.anvil.ksp.codegen.common.util.parseConstructorParameters

@AutoService(AnvilKspExtension.Provider::class)
public class ContributesInitializerAnvilKspExtensionProvider : AnvilKspExtension.Provider {
    override fun create(environment: SymbolProcessorEnvironment): AnvilKspExtension =
        ContributesInitializerKspExtension(environment)

    override fun isApplicable(context: AnvilContext): Boolean = true
}

private class ContributesInitializerKspExtension(
    environment: SymbolProcessorEnvironment,
    private val contributesInitializerAnnotation: FqName = PixnewsInitializerClassName.contributesInitializerFqName,
) : AnvilKspExtension {
    private val codeGenerator: CodeGenerator = environment.codeGenerator
    override val supportedAnnotationTypes = setOf(contributesInitializerAnnotation.asString())
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(contributesInitializerAnnotation.asString())
            .filterIsInstance<KSClassDeclaration>()
            .distinctBy { it.qualifiedName?.asString() }
            .onEach { it.checkClassExtendsBoundType(contributesInitializerAnnotation, resolver) }
            .map(::generateInitializerModule)
            .forEach { spec -> spec.writeTo(codeGenerator, false) }
        return emptyList()
    }

    private fun generateInitializerModule(
        annotatedClass: KSClassDeclaration,
    ): FileSpec {
        val boundType: KSType = checkNotNull(annotatedClass.getInitializerBoundType()) {
            "$annotatedClass doesn't extend any of ${PixnewsInitializerClassName.initializer} " +
                    "or ${PixnewsInitializerClassName.asyncInitializer}"
        }

        val moduleClassName: ClassName = annotatedClass.moduleNameForInitializer()

        val contributesInitializer = annotatedClass.getKSAnnotationsByQualifiedName(
            PixnewsInitializerClassName.contributesInitializerFqName.asString(),
        ).singleOrNull() ?: error("ContributesInitializer annotation not set")
        val replacesArray = contributesInitializer.arguments
            .find { it.name?.asString() == "replaces" }
            ?.value as? List<*> ?: error("No replaces argument")
        val replaces = replacesArray.map { replacedElement ->
            val replacedClassRef = replacedElement as KSType
            if (replacedClassRef.isInitializer()) {
                (replacedClassRef.declaration as KSClassDeclaration).moduleNameForInitializer()
            } else {
                replacedClassRef.toClassName()
            }
        }

        val moduleSpec = TypeSpec.objectBuilder(moduleClassName)
            .addAnnotation(DaggerClassName.module)
            .addAnnotation(
                contributesToAnnotation(
                    className = PixnewsInitializerClassName.appInitializersScope,
                    replaces = replaces,
                ),
            )
            .addFunction(generateProvideMethod(annotatedClass, boundType))
            .build()

        return FileSpec.builder(moduleClassName).addType(moduleSpec).build()
    }

    private fun generateProvideMethod(
        annotatedClass: KSClassDeclaration,
        boundType: KSType,
    ): FunSpec {
        val builder = FunSpec.builder("provide${annotatedClass.simpleName.asString()}")
            .addAnnotation(DaggerClassName.provides)
            .addAnnotation(DaggerClassName.intoSet)
            .addAnnotation(DaggerClassName.reusable)
            .returns(boundType.toClassName())

        val primaryConstructor: KSFunctionDeclaration = annotatedClass.primaryConstructor
            ?: throw IllegalArgumentException("No primary constructor on $annotatedClass")
        val constructorParameters = primaryConstructor.parameters.parseConstructorParameters()

        constructorParameters.forEach {
            builder.addParameter(it.name, it.resolvedType)
        }

        val initializerParams = constructorParameters.joinToString(", ") { "${it.name} = ${it.name}" }
        builder.addStatement("return %T(\n$initializerParams\n)", annotatedClass.toClassName())
        return builder.build()
    }

    private fun KSType.isInitializer() = declaration.resolveKSClassDeclaration()?.getInitializerBoundType() != null

    private fun KSClassDeclaration.getInitializerBoundType(): KSType? {
        return superTypes
            .map(KSTypeReference::resolve)
            .firstOrNull { supertype: KSType ->
                supertype.toClassName().let {
                    it == PixnewsInitializerClassName.initializer ||
                            it == PixnewsInitializerClassName.asyncInitializer
                }
            }
    }

    private fun KSClassDeclaration.moduleNameForInitializer(): ClassName =
        this.toClassName().joinSimpleNames(suffix = "_InitializerModule")
}
