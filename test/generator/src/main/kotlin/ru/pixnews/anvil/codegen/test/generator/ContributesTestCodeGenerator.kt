/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.test.generator

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.buildFile
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.anvil.compiler.internal.reference.generateClassName
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import ru.pixnews.anvil.codegen.common.classname.AnvilClassName
import ru.pixnews.anvil.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.codegen.common.util.contributesToAnnotation
import java.io.File

@AutoService(CodeGenerator::class)
public class ContributesTestCodeGenerator : CodeGenerator {
    override fun isApplicable(context: AnvilContext): Boolean = true

    override fun generateCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>,
    ): Collection<GeneratedFile> {
        return projectFiles
            .classAndInnerClassReferences(module)
            .filter { it.isAnnotatedWith(PixnewsTestClassName.contributesTestFqName) }
            .map { generateTestModule(it, codeGenDir) }
            .toList()
    }

    private fun generateTestModule(
        annotatedClass: ClassReference,
        codeGenDir: File,
    ): GeneratedFile {
        val moduleClassId = annotatedClass.generateClassName(suffix = "_TestModule")
        val generatedPackage = moduleClassId.packageFqName.safePackageString()
        val moduleClassName = moduleClassId.relativeClassName.asString()

        val moduleSpecBuilder = TypeSpec.objectBuilder(moduleClassName)
            .addAnnotation(DaggerClassName.module)
            .addAnnotation(contributesToAnnotation(PixnewsTestClassName.appScope))
            .addFunction(generateProvideMethod(annotatedClass))

        val content = FileSpec.buildFile(generatedPackage, moduleClassName) {
            addType(moduleSpecBuilder.build())
        }
        return createGeneratedFile(codeGenDir, generatedPackage, moduleClassName, content)
    }

    private fun generateProvideMethod(
        annotatedClass: ClassReference,
    ): FunSpec {
        val testClass = annotatedClass.asClassName()
        return FunSpec.builder("provide${annotatedClass.shortName}Injector")
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
                    .addMember("%T::class", PixnewsTestClassName.appScope)
                    .build(),
            )
            .addParameter("injector", DaggerClassName.membersInjector.parameterizedBy(testClass))
            .returns(PixnewsTestClassName.singleInstrumentedTestInjector)
            .addStatement("return %T(injector)", PixnewsTestClassName.singleInstrumentedTestInjector)
            .build()
    }
}
