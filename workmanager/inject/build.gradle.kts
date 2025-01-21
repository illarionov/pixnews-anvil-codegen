
plugins {
    id("ru.pixnews.anvil.codegen.buildlogic.project.android.library")
    id("ru.pixnews.anvil.codegen.buildlogic.project.binary.compatibility.validator")
    id("ru.pixnews.anvil.codegen.buildlogic.project.dokka.subproject")
    id("ru.pixnews.anvil.codegen.buildlogic.project.publish.ksp")
}

version = anvilCodegenVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "anvil_ksp_codegen_workmanager_generator_version",
    envVariableName = "ANVIL_KSP_CODEGEN_WORKMANAGER_GENERATOR_VERSION",
).get()

android {
    namespace = "ru.pixnews.anvil.ksp.codegen.workmanager"
}

dependencies {
    api(libs.androidx.workmanager.ktx)
    api(libs.dagger)
    api(libs.inject)
}
