/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.viewmodel.generator

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.buildFile
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.MemberFunctionReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.anvil.compiler.internal.reference.generateClassName
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import ru.pixnews.anvil.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.codegen.common.classname.PixnewsClassName
import ru.pixnews.anvil.codegen.common.fqname.FqNames
import ru.pixnews.anvil.codegen.common.util.checkClassExtendsType
import ru.pixnews.anvil.codegen.common.util.contributesToAnnotation
import ru.pixnews.anvil.codegen.common.util.isSavedStateHandle
import ru.pixnews.anvil.codegen.common.util.parseConstructorParameters
import java.io.File

@AutoService(CodeGenerator::class)
public class ContributesViewModelCodeGenerator : CodeGenerator {
    override fun isApplicable(context: AnvilContext): Boolean = true

    override fun generateCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>,
    ): Collection<GeneratedFile> {
        return projectFiles
            .classAndInnerClassReferences(module)
            .filter { it.isAnnotatedWith(FqNames.contributesViewModel) }
            .map { generateViewModelModule(it, codeGenDir) }
            .toList()
    }

    private fun generateViewModelModule(
        annotatedClass: ClassReference,
        codeGenDir: File,
    ): GeneratedFile {
        annotatedClass.checkClassExtendsType(viewModelFqName)

        val moduleClassId = annotatedClass.generateClassName(suffix = "_FactoryModule")
        val generatedPackage = moduleClassId.packageFqName.safePackageString()
        val moduleClassName = moduleClassId.relativeClassName.asString()

        val moduleInterfaceSpecBuilder = TypeSpec.objectBuilder(moduleClassName)
            .addAnnotation(DaggerClassName.module)
            .addAnnotation(contributesToAnnotation(PixnewsClassName.viewModelScope))
            .addFunction(generateProvidesFactoryMethod(annotatedClass))

        val content = FileSpec.buildFile(generatedPackage, moduleClassName) {
            addType(moduleInterfaceSpecBuilder.build())
        }
        return createGeneratedFile(codeGenDir, generatedPackage, moduleClassName, content)
    }

    private fun generateProvidesFactoryMethod(
        annotatedClass: ClassReference,
    ): FunSpec {
        val viewModelClass = annotatedClass.asClassName()

        val primaryConstructor: MemberFunctionReference = annotatedClass.constructors.firstOrNull()
            ?: throw IllegalArgumentException("No primary constructor on $annotatedClass")
        val primaryConstructorParams = primaryConstructor.parameters.parseConstructorParameters(annotatedClass)

        val builder = FunSpec.builder("provides${annotatedClass.shortName}ViewModelFactory")
            .addAnnotation(DaggerClassName.provides)
            .addAnnotation(DaggerClassName.intoMap)
            .addAnnotation(
                AnnotationSpec
                    .builder(PixnewsClassName.viewModelMapKey)
                    .addMember("%T::class", viewModelClass)
                    .build(),
            )
            .returns(PixnewsClassName.viewModelFactory)

        primaryConstructorParams
            .filter { !it.isSavedStateHandle() }
            .forEach { builder.addParameter(it.name, it.resolvedType) }

        val viewModelConstructorParameters = primaryConstructorParams.joinToString(separator = "\n") {
            if (!it.isSavedStateHandle()) {
                "${it.name} = ${it.name},"
            } else {
                "${it.name} = it.%M()"
            }
        }
        val createViewModeStatementArgs: Array<Any> = primaryConstructorParams.mapNotNull {
            if (it.isSavedStateHandle()) createSavedStateHandleMember else null
        }.toTypedArray()

        builder.beginControlFlow("return %T", PixnewsClassName.viewModelFactory)
        @Suppress("SpreadOperator")
        builder.addStatement("%T(\n$viewModelConstructorParameters\n)", viewModelClass, *createViewModeStatementArgs)
        builder.endControlFlow()
        return builder.build()
    }

    private companion object {
        private val viewModelFqName = FqName("androidx.lifecycle.ViewModel")
        private val createSavedStateHandleMember = MemberName(
            packageName = "androidx.lifecycle",
            simpleName = "createSavedStateHandle",
            isExtension = true,
        )
    }
}
