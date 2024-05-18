/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.experiment.generator

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
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import ru.pixnews.anvil.codegen.common.classname.DaggerClassName
import ru.pixnews.anvil.codegen.common.util.checkClassExtendsType
import ru.pixnews.anvil.codegen.common.util.contributesToAnnotation
import ru.pixnews.anvil.codegen.experiment.generator.PixnewsExperimentClassName.contributesExperimentFqName
import ru.pixnews.anvil.codegen.experiment.generator.PixnewsExperimentClassName.contributesExperimentVariantSerializerFqName
import ru.pixnews.anvil.codegen.experiment.generator.PixnewsExperimentClassName.experimentVariantMapKey
import ru.pixnews.anvil.codegen.experiment.generator.PixnewsExperimentClassName.experimentVariantSerializer
import ru.pixnews.anvil.codegen.experiment.generator.PixnewsExperimentClassName.experimentVariantSerializerFqName
import java.io.File

@AutoService(CodeGenerator::class)
public class ContributesExperimentCodeGenerator : CodeGenerator {
    override fun isApplicable(context: AnvilContext): Boolean = true

    override fun generateCode(
        codeGenDir: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>,
    ): Collection<GeneratedFileWithSources> {
        val experimentAnnotatedClass = projectFiles.classAndInnerClassReferences(module)
            .filter { classRef ->
                classRef.annotations.any { annotationRef ->
                    annotationRef.fqName == contributesExperimentFqName ||
                            annotationRef.fqName == contributesExperimentVariantSerializerFqName
                }
            }
            .toSortedSet()

        return buildList {
            if (experimentAnnotatedClass.isNotEmpty()) {
                add(generateExperimentModule(experimentAnnotatedClass, codeGenDir))
            }
        }
    }

    private fun generateExperimentModule(
        annotatedClasses: Collection<ClassReference.Psi>,
        codeGenDir: File,
    ): GeneratedFileWithSources {
        val moduleClassId = annotatedClasses.first().joinSimpleNames(suffix = "_Experiments_Module")
        val generatedPackage = moduleClassId.packageFqName.safePackageString()
        val moduleClassName = moduleClassId.relativeClassName.asString()

        val moduleTypeSpecBuilder = TypeSpec.objectBuilder(moduleClassName)
            .addAnnotation(DaggerClassName.module)
            .addAnnotation(contributesToAnnotation(PixnewsExperimentClassName.experimentScope))

        annotatedClasses.forEach {
            moduleTypeSpecBuilder.addFunction(generateProvidesMethod(it))
        }

        val content = FileSpec.buildFile(generatedPackage, moduleClassName) {
            addType(moduleTypeSpecBuilder.build())
        }
        return createGeneratedFile(
            codeGenDir = codeGenDir,
            packageName = generatedPackage,
            fileName = moduleClassName,
            content = content,
            sourceFiles = annotatedClasses.mapTo(sortedSetOf(), ClassReference.Psi::containingFileAsJavaFile),
        )
    }

    private fun generateProvidesMethod(annotatedClass: ClassReference): FunSpec {
        val experimentAnnotations = annotatedClass.annotations.filter { annotationRef ->
            annotationRef.fqName == contributesExperimentFqName ||
                    annotationRef.fqName == contributesExperimentVariantSerializerFqName
        }

        require(experimentAnnotations.size == 1) {
            "$annotatedClass has an incorrect combination of annotations"
        }

        val annotation = experimentAnnotations.single()
        return when (annotation.fqName) {
            contributesExperimentFqName -> providesExperimentFunction(annotatedClass)
            contributesExperimentVariantSerializerFqName -> {
                val experimentKeyAnnotation = annotation.arguments.singleOrNull()
                    ?: throw IllegalArgumentException("experimentKey on ContributesExperimentVariant not defined")

                providesExperimentVariantSerializerFunction(
                    annotatedSerializer = annotatedClass,
                    experimentVariantKey = experimentKeyAnnotation.value(),
                )
            }

            else -> throw IllegalArgumentException("Unknown annotation $annotation")
        }
    }

    /**
     * `@Provides @IntoSet abstract fun provideMainExperiment(experiment: MainExperiment): Experiment`
     */
    private fun providesExperimentFunction(annotatedExperiment: ClassReference): FunSpec {
        annotatedExperiment.checkClassExtendsType(PixnewsExperimentClassName.experimentFqName)

        return FunSpec.builder("provide${annotatedExperiment.shortName}")
            .addAnnotation(DaggerClassName.provides)
            .addAnnotation(DaggerClassName.intoSet)
            .returns(PixnewsExperimentClassName.experiment)
            .addCode("return %T", annotatedExperiment.asClassName())
            .build()
    }

    /**
     * `@Provides @IntoSet abstract fun provideMainExperiment(experiment: MainExperiment): Experiment`
     */
    private fun providesExperimentVariantSerializerFunction(
        annotatedSerializer: ClassReference,
        experimentVariantKey: String,
    ): FunSpec {
        annotatedSerializer.checkClassExtendsType(experimentVariantSerializerFqName)

        return FunSpec.builder("provide${annotatedSerializer.shortName}")
            .addAnnotation(DaggerClassName.provides)
            .addAnnotation(DaggerClassName.intoMap)
            .addAnnotation(
                AnnotationSpec
                    .builder(experimentVariantMapKey)
                    .addMember("key = %S", experimentVariantKey)
                    .build(),
            )
            .returns(experimentVariantSerializer)
            .addCode("return %T", annotatedSerializer.asClassName())
            .build()
    }
}
