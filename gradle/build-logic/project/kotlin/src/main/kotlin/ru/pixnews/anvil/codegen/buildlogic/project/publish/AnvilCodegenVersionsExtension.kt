/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.buildlogic.project.publish

import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
import java.io.StringReader
import java.util.Properties
import javax.inject.Inject

private const val VERSION_PROPERTIES_PATH = "config/version.properties"

internal fun Project.createAnvilCodegenVersionsExtension(): AnvilCodegenVersionsExtension {
    val rootProjectDirectory = project.rootProject.layout.projectDirectory
    return extensions.create<AnvilCodegenVersionsExtension>("anvilCodegenVersions").apply {
        this.propertiesFile.convention(
            rootProjectDirectory.file(VERSION_PROPERTIES_PATH),
        )
    }
}

open class AnvilCodegenVersionsExtension
@Inject
constructor(
    objects: ObjectFactory,
    private val providers: ProviderFactory,
) {
    internal val propertiesFile: RegularFileProperty = objects.fileProperty()
    private val propertiesProvider: Provider<Properties> = providers.fileContents(propertiesFile)
        .asText
        .map(
            object : Transformer<Properties, String> {
                var properties: Properties? = null
                override fun transform(text: String): Properties {
                    return properties ?: run {
                        Properties().apply {
                            load(StringReader(text))
                        }.also { properties = it }
                    }
                }
            },
        )
        .orElse(providers.provider { error("File ${propertiesFile.get()} not found") })
    private val rootVersion: Provider<String> = providers.gradleProperty("version")
        .orElse(providers.environmentVariable("ANVIL_CODEGEN_VERSION"))
        .orElse(
            propertiesProvider.map { props ->
                props.getProperty("anvil_codegen_version")
                    ?: error("No `anvil_codegen_version` in ${propertiesFile.get()}")
            },
        )

    fun getSubmoduleVersionProvider(
        propertiesFileKey: String,
        envVariableName: String,
        gradleKey: String = propertiesFileKey,
    ): Provider<String> = providers.gradleProperty(gradleKey)
        .orElse(providers.environmentVariable(envVariableName))
        .orElse(
            propertiesProvider.map { props ->
                props.getProperty(propertiesFileKey)
            },
        )
        .orElse(rootVersion)
}
