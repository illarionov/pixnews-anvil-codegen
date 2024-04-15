import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import ru.pixnews.anvil.codegen.buildlogic.project.publish.createAnvilCodegenVersionsExtension

/*
 * Copyright (c) 2024, the anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

/*
 * Convention plugin with publishing defaults
 */
plugins {
    id("com.vanniktech.maven.publish.base")
}

group = "ru.pixnews.anvil.codegen"

createAnvilCodegenVersionsExtension()

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

mavenPublishing {
    publishing {
        repositories {
            maven {
                name = "test"
                setUrl(project.rootProject.layout.buildDirectory.dir("localMaven"))
            }
            maven {
                name = "PixnewsS3"
                setUrl("s3://maven.pixnews.ru/")
                credentials(AwsCredentials::class) {
                    accessKey = providers.environmentVariable("YANDEX_S3_ACCESS_KEY_ID").getOrElse("")
                    secretKey = providers.environmentVariable("YANDEX_S3_SECRET_ACCESS_KEY").getOrElse("")
                }
            }
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        configure(
            KotlinJvm(
                javadocJar = JavadocJar.None(),
                sourcesJar = true,
            ),
        )
    }
    plugins.withId("com.android.library") {
        configure(
            AndroidSingleVariantLibrary(
                variant = "release",
                sourcesJar = true,
                publishJavadocJar = false,
            ),
        )
    }

    signAllPublications()

    pom {
        name.set(project.name)
        description.set("Anvil code generators used in Pixnews project")
        url.set("https://github.com/illarionov/pixnews-anvil-codegen")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("illarionov")
                name.set("Alexey Illarionov")
                email.set("alexey@0xdc.ru")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/illarionov/pixnews-anvil-codegen.git")
            developerConnection.set("scm:git:ssh://github.com:illarionov/pixnews-anvil-codegen.git")
            url.set("https://github.com/illarionov/pixnews-anvil-codegen")
        }
    }
}
