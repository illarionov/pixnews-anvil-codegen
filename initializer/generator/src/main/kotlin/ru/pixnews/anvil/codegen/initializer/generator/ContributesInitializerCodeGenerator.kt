/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.initializer.generator

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFileWithSources
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.buildFile
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.MemberFunctionReference
import com.squareup.anvil.compiler.internal.reference.allSuperTypeClassReferences
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.asTypeName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.anvil.compiler.internal.reference.joinSimpleNames
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtFile
import ru.pixnews.anvil.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.codegen.common.util.contributesToAnnotation
import ru.pixnews.anvil.codegen.common.util.parseConstructorParameters
import java.io.File

@AutoService(CodeGenerator::class)
public class ContributesInitializerCodeGenerator : CodeGenerator {
    override fun isApplicable(context: AnvilContext): Boolean = true

    override fun generateCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>,
    ): Collection<GeneratedFileWithSources> {
        return projectFiles
            .classAndInnerClassReferences(module)
            .filter { it.isAnnotatedWith(PixnewsInitializerClassName.contributesInitializerFqName) }
            .map { generateInitializerModule(it, codeGenDir) }
            .toList()
    }

    private fun generateInitializerModule(
        annotatedClass: ClassReference,
        codeGenDir: File,
    ): GeneratedFileWithSources {
        val boundType = checkNotNull(annotatedClass.getInitializerBoundType()) {
            "${annotatedClass.fqName} doesn't extend any of ${PixnewsInitializerClassName.initializer} " +
                    "or ${PixnewsInitializerClassName.asyncInitializer}"
        }

        val moduleClassId = annotatedClass.moduleNameForInitializer()
        val generatedPackage = moduleClassId.packageFqName.safePackageString()
        val moduleClassName = moduleClassId.relativeClassName.asString()

        val replaces: List<TypeName> = annotatedClass.annotations.first {
            it.fqName == PixnewsInitializerClassName.contributesInitializerFqName
        }
            .replaces(parameterIndex = 0)
            .map { replacedClassRef ->
                if (replacedClassRef.isInitializer()) {
                    replacedClassRef.moduleNameForInitializer().asClassName()
                } else {
                    replacedClassRef.asTypeName()
                }
            }

        val moduleSpecBuilder = TypeSpec.objectBuilder(moduleClassName)
            .addAnnotation(DaggerClassName.module)
            .addAnnotation(
                contributesToAnnotation(
                    className = PixnewsInitializerClassName.appInitializersScope,
                    replaces = replaces,
                ),
            )
            .addFunction(generateProvideMethod(annotatedClass, boundType))

        val content = FileSpec.buildFile(generatedPackage, moduleClassName) {
            addType(moduleSpecBuilder.build())
        }

        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = generatedPackage,
            fileName = moduleClassName,
            content = content,
            sourceFile = annotatedClass.containingFileAsJavaFile,
        )
    }

    private fun generateProvideMethod(
        annotatedClass: ClassReference,
        boundType: ClassName,
    ): FunSpec {
        val builder = FunSpec.builder("provide${annotatedClass.shortName}")
            .addAnnotation(DaggerClassName.provides)
            .addAnnotation(DaggerClassName.intoSet)
            .addAnnotation(DaggerClassName.reusable)
            .returns(boundType)

        val primaryConstructor: MemberFunctionReference = annotatedClass.constructors.firstOrNull()
            ?: throw IllegalArgumentException("No primary constructor on $annotatedClass")
        val constructorParameters = primaryConstructor.parameters.parseConstructorParameters(annotatedClass)

        constructorParameters.forEach {
            builder.addParameter(it.name, it.resolvedType)
        }

        val initializerParams = constructorParameters.joinToString(", ") { "${it.name} = ${it.name}" }
        builder.addStatement("return %T(\n$initializerParams\n)", annotatedClass.asClassName())
        return builder.build()
    }

    private fun ClassReference.getInitializerBoundType(): ClassName? {
        return allSuperTypeClassReferences()
            .map(ClassReference::asClassName)
            .firstOrNull {
                it == PixnewsInitializerClassName.initializer ||
                        it == PixnewsInitializerClassName.asyncInitializer
            }
    }

    private fun ClassReference.isInitializer() = getInitializerBoundType() != null

    private fun ClassReference.moduleNameForInitializer(): ClassId = joinSimpleNames(suffix = "_InitializerModule")
}
