
plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.project.android.library")
    id("ru.pixnews.anvil.codegen.buildlogic.project.binary.compatibility.validator")
    id("ru.pixnews.anvil.codegen.buildlogic.project.publish")
}

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
