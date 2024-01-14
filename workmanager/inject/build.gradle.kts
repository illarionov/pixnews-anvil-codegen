
plugins {
    id("ru.pixnews.anvil.codegen.build-logic.project.android.library")
    id("ru.pixnews.anvil.codegen.build-logic.project.publish")
}

group = "ru.pixnews.anvil.codegen.workmanager"
version = anvilCodegenVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "anvil_codegen_workmanager_generator_version",
    envVariableName = "ANVIL_CODEGEN_WORKMANAGER_GENERATOR_VERSION",
).get()

android {
    namespace = "ru.pixnews.anvil.codegen.workmanager"
}

dependencies {
    api(libs.androidx.workmanager.ktx)
    api(libs.dagger)
    api(libs.inject)
}
