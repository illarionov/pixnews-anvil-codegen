/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.workmanager.generator

import com.google.auto.service.AutoService
import com.squareup.anvil.compiler.api.AnvilContext
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.api.GeneratedFileWithSources
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.buildFile
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.anvil.compiler.internal.reference.classAndInnerClassReferences
import com.squareup.anvil.compiler.internal.reference.joinSimpleNames
import com.squareup.anvil.compiler.internal.safePackageString
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec.Companion.interfaceBuilder
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import ru.pixnews.anvil.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.codegen.common.util.checkClassExtendsType
import ru.pixnews.anvil.codegen.common.util.contributesMultibindingAnnotation
import java.io.File

@AutoService(CodeGenerator::class)
public class ContributesCoroutineWorkerCodeGenerator : CodeGenerator {
    override fun isApplicable(context: AnvilContext): Boolean = true

    override fun generateCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>,
    ): Collection<GeneratedFileWithSources> {
        return projectFiles
            .classAndInnerClassReferences(module)
            .filter { it.isAnnotatedWith(PixnewsWorkManagerClassName.contributesCoroutineWorkerFqName) }
            .map { generateWorkManagerFactory(it, codeGenDir) }
            .toList()
    }

    private fun generateWorkManagerFactory(
        annotatedClass: ClassReference,
        codeGenDir: File,
    ): GeneratedFileWithSources {
        annotatedClass.checkClassExtendsType(COROUTINE_WORKER_FQ_NAME)

        val workerClassName = annotatedClass.asClassName()
        val factoryClassId = annotatedClass.joinSimpleNames(suffix = "_AssistedFactory")
        val generatedPackage = factoryClassId.packageFqName.safePackageString()
        val factoryClassName = factoryClassId.relativeClassName.asString()

        val factoryInterfaceSpec = interfaceBuilder(factoryClassName)
            .addAnnotation(DaggerClassName.assistedFactory)
            .addAnnotation(contributesMultibindingAnnotation(PixnewsWorkManagerClassName.workManagerScope))
            .addAnnotation(
                AnnotationSpec
                    .builder(PixnewsWorkManagerClassName.coroutineWorkerMapKey)
                    .addMember("%T::class", workerClassName)
                    .build(),
            )
            .addSuperinterface(PixnewsWorkManagerClassName.coroutineWorkerFactory)
            .addFunction(createWorkerFunction(workerClassName))
            .build()
        val content = FileSpec.buildFile(generatedPackage, factoryClassName) {
            addType(factoryInterfaceSpec)
        }
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = generatedPackage,
            fileName = factoryClassName,
            content = content,
            sourceFile = annotatedClass.containingFileAsJavaFile,
        )
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
                    .addAnnotation(PixnewsWorkManagerClassName.applicationContext)
                    .build(),
            )
            .addParameter("workerParameters", WORKER_PARAMETERS_CLASS_NAME)
            .returns(workerClass)
            .build()
    }

    internal companion object {
        internal val ANDROID_CONTEXT_CLASS_NAME: ClassName = ClassName("android.content", "Context")
        internal val WORKER_PARAMETERS_CLASS_NAME: ClassName = ClassName("androidx.work", "WorkerParameters")
        private val COROUTINE_WORKER_FQ_NAME = FqName("androidx.work.CoroutineWorker")
    }
}
