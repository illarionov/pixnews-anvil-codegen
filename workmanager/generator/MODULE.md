# Module workmanager-generator

A code generator for use with [Anvil-KSP] that simplifies adding Android Workers to the DI graph 
with a single _@ContributesCoroutineWorker_ annotation.

For each Coroutine worker annotated with _@ContributesCoroutineWorker_, a Dagger module will be generated with
content similar to the following:

```kotlin
import ru.pixnews.anvil.ksp.codegen.workmanager.inject.WorkManagerScope
import ru.pixnews.anvil.ksp.codegen.workmanager.inject.wiring.CoroutineWorkerFactory
import ru.pixnews.anvil.ksp.codegen.workmanager.inject.wiring.CoroutineWorkerMapKey
import ru.pixnews.foundation.di.base.qualifiers.ApplicationContext

@AssistedFactory
@ContributesMultibinding(scope = WorkManagerScope::class)
@CoroutineWorkerMapKey(MyCoroutineWorker::class)
interface MyCoroutineWorker_AssistedFactory : CoroutineWorkerFactory {
  override fun create(
      @ApplicationContext context: Context,
      workerParameters: WorkerParameters
  ): MyCoroutineWorker
}
```

This module is intended to add the specified CoroutineWorker to the WorkManagerScope multibinding.

## Code generator setup

Before using the generator, make sure [Anvil-KSP] is already configured in the project.

Then add the required dependencies:

```kotlin
dependencies {
    anvil("ru.pixnews.anvil.ksp.codegen:workmanager-generator:0.1")
    // Add this dependency to the ksp classpath if "anvil" configuration doesn't work for some reason:
    ksp("ru.pixnews.anvil.ksp.codegen:workmanager-generator:0.1")

    // workmanager-inject dependency is optional, all declarations this module can be overridden.
    implementation("ru.pixnews.anvil.ksp.codegen:workmanager-inject:0.1")
}
```

The fully qualified names of annotation classes can be customized via KSP arguments in `build.gradle.kts`;

```kotlin
ksp {
    arg(
        "ru.pixnews.anvil.ksp.workmanager.ContributesCoroutineWorker",
        "ru.pixnews.anvil.ksp.codegen.workmanager.inject.ContributesCoroutineWorker"
    )
    arg(
        "ru.pixnews.anvil.ksp.workmanager.ApplicationContext",
        "ru.pixnews.foundation.di.base.qualifiers.ApplicationContext"
    )
    arg(
        "ru.pixnews.anvil.ksp.workmanager.CoroutineWorkerFactory",
        "ru.pixnews.anvil.ksp.codegen.workmanager.inject.wiring.CoroutineWorkerFactory"
    )
    arg(
        "ru.pixnews.anvil.ksp.workmanager.CoroutineWorkerMapKey",
        "ru.pixnews.anvil.ksp.codegen.workmanager.inject.wiring.CoroutineWorkerMapKey"
    )
    arg(
        "ru.pixnews.anvil.ksp.workmanager.WorkManagerScope",
        "ru.pixnews.anvil.ksp.codegen.workmanager.inject.WorkManagerScope"
    )
}
```

By default, annotations from the [workmanager-inject] module are used.

## Project setup

To make use of this generator's output, the project must have workmanager injection set up using Dagger multibindings.
The configuration might look something like this (using classes from the [workmanager-inject] module).

Create a subcomponent for workers and add it to your application component,
as shown in the following example:

```kotlin
import ru.pixnews.anvil.ksp.codegen.workmanager.inject.WorkManagerScope
import ru.pixnews.anvil.ksp.codegen.workmanager.inject.wiring.CoroutineWorkerFactory
import <your app scope>.AppScope

@SingleIn(WorkManagerScope::class)
@ContributesSubcomponent(scope = WorkManagerScope::class, parentScope = AppScope::class)
interface WorkManagerSubcomponent {
    val workerFactories: DaggerMap<Class<out CoroutineWorker>, CoroutineWorkerFactory>

    @ContributesSubcomponent.Factory
    fun interface Factory {
        fun create(): WorkManagerSubcomponent
    }
}

@ContributesTo(WorkManagerScope::class)
@Module
interface WorkManagerSubcomponentModule {
    @Multibinds
    fun workerFactories(): DaggerMap<Class<out CoroutineWorker>, CoroutineWorkerFactory>
}
```

Create [WorkerFactory](https://developer.android.com/reference/androidx/work/WorkerFactory):

```kotlin
internal class AppWorkerFactory(
    private val workerSubcomponentFactory: WorkManagerSubcomponent.Factory,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        val workerComponent = workerSubcomponentFactory.create()
        val factory = workerComponent.workerFactories.firstNotNullOfOrNull {
            if (it.key.canonicalName == workerClassName) it.value else null
        }
        return if (factory != null) {
            factory.create(appContext, workerParameters)
        } else {
            null
        }
    }
}
```

Setup WorkManager to use our custom WorkerFactory:

```kotlin
import androidx.work.Configuration
import <yout app scope>.AppScope

@ContributesTo(AppScope::class)
@Module
object WorkManagerModule {
    @Reusable
    @Provides
    fun providesWorkMangerConfiguration(
        workerFactory: WorkerFactory,
    ): Configuration {
        return with(Configuration.Builder()) {
            setWorkerFactory(workerFactory)
            build()
        }
    }

    @Reusable
    @Provides
    fun providesWorkManager(
        @ApplicationContext applicationContext: Context,
    ): WorkManager {
        return WorkManager.getInstance(applicationContext)
    }

    @Reusable
    @Provides
    fun providesWorkerFactory(
        subcomponentFactory: WorkManagerSubcomponentModule.Factory,
    ): WorkerFactory {
        return AppWorkerFactory(subcomponentFactory)
    }
}
```

Application class:

```kotlin
class TheApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration
        get() = localWorkManagerConfiguration

    @field:Inject
    lateinit var localWorkManagerConfiguration: Configuration

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)
    }
}
```

See [developer.android.com](https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration) for more up-to-date instruction.

## Usage

Annotate your Coroutine Worker with `ContributesCoroutineWorker` to add it to the multibinding:

```kotlin
import ru.pixnews.anvil.ksp.codegen.workmanager.inject.ContributesCoroutineWorker

@ContributesCoroutineWorker
class MyCoroutineWorker @AssistedInject constructor(
    logger: Logger,
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = Result.success()
}
```

The MyCoroutineWorker_AssistedFactory module described above will be generated based on this annotation.

[Anvil-KSP]: https://github.com/ZacSweers/anvil
[workmanager-inject]: https://illarionov.github.io/pixnews-anvil-codegen/workmanager-inject/
