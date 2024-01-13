
plugins {
    id("ru.pixnews.anvil.codegen.build-logic.project.kotlin.library")
    id("ru.pixnews.anvil.codegen.build-logic.project.test")
    id("ru.pixnews.anvil.codegen.build-logic.project.publish")
}

group = "ru.pixnews.anvil.codegen.workmanager"
version = anvilCodegenVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "anvil_codegen_workmanager_inject_version",
    envVariableName = "ANVIL_CODEGEN_WORKMANAGER_INJECT_VERSION",
).get()

dependencies {
}
