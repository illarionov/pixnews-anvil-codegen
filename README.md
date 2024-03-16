# Pixnews Anvil codegen

Some Anvil code generators originally used in [Pixnews](https://github.com/illarionov/Pixnews) Android application. Published as examples of possible
recipes with Anvil generators.

Heavily Based on the [Whenstone](https://github.com/deliveryhero/whetstone) project.

## Installation

Release and snapshot versions of the library are published to a temporary repository, since this library is currently
used only in one project.

Add the following to your project's settings.gradle:

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pixnews.ru")
            mavenContent {
                includeGroupByRegex("""ru\.pixnews\.anvil\..+""")
            }
        }
    }
}
```

## Generators

Below is a list of all implemented generators:

- [Activity generator](#activity-generator)
- [Viewmodel generator](#viewmodel-generator)
- [Workmanager generator](#workmanager-generator)
- [Test generator](#test-generator)
- [Experiment generator](#experiment-generator)
- [Initializer generator](#initializer-generator)

The following type aliases are used in the examples:

```kotlin
typealias DaggerSet<T> = Set<@JvmSuppressWildcards T>
typealias DaggerMap<K, V> = Map<@JvmSuppressWildcards K, @JvmSuppressWildcards V>
```

### Activity generator

Generator that simplifies dependency injection to an Android Activities using the `ContributesActivity` annotation.

#### Wiring

Add the required dependencies

```kotlin
dependencies {
    anvil("ru.pixnews.anvil.codegen.activity:generator:0.3")
    api("ru.pixnews.anvil.codegen.activity:inject:0.3")
}
```

Create a subcomponent with an Activity scope and add it to your application component,
as shown in the following example:

```kotlin
import ru.pixnews.anvil.codegen.activity.inject.ActivityScope
import ru.pixnews.anvil.codegen.activity.inject.wiring.ActivityInjector
import <your app scope>.AppScope

@SingleIn(ActivityScope::class)
@ContributesSubcomponent(scope = ActivityScope::class, parentScope = AppScope::class)
interface ActivitySubcomponent {
    val activityInjector: ActivityInjector

    @ContributesSubcomponent.Factory
    fun interface Factory {
        fun create(@BindsInstance activity: Activity): ActivitySubcomponent
    }

    @ContributesTo(AppScope::class)
    interface ActivitySubcomponentFactoryHolder {
        fun getActivitySubcomponentFactory(): ActivitySubcomponent.Factory
    }
}
```

Add module for the subcomponent:

```kotlin
import dagger.MembersInjector

import ru.pixnews.anvil.codegen.activity.inject.ActivityScope
import ru.pixnews.anvil.codegen.activity.inject.wiring.ActivityInjector
import ru.pixnews.anvil.codegen.activity.inject.wiring.DefaultActivityInjector

@ContributesTo(ActivityScope::class)
@Module
@RestrictTo(RestrictTo.Scope.LIBRARY)
interface PixnewsActivityModule {
    @Multibinds
    fun activityInjectors(): DaggerMap<Class<out Activity>, MembersInjector<out Activity>>

    companion object {
        @Reusable
        @Provides
        fun provideActivityInjector(
            injectors: DaggerMap<Class<out Activity>, MembersInjector<out Activity>>,
        ): ActivityInjector = DefaultActivityInjector(injectors)
    }
}
```

Perform an injection in the base activity:

```kotlin
abstract class BaseActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        (appComponent as ActivitySubcomponentFactoryHolder)
            .getActivitySubcomponentFactory()
            .create(this)
            .activityInjector
            .inject(this)
        super.onCreate(savedInstanceState)
    }
}
```

#### Usage

Now your can annotate any activity with `ContributesActivity` to add this activity to multibinding.

```kotlin
import ru.pixnews.anvil.codegen.activity.inject.ContributesActivity

@ContributesActivity
class MainActivity : BaseActivity() {
    @Inject
    internal lateinit var analytics: Analytics
}
```

The following module will be generated based on this annotation:

```kotlin
import ru.pixnews.anvil.codegen.activity.inject.ActivityScope
import ru.pixnews.anvil.codegen.activity.inject.wiring.ActivityMapKey

@Module
@ContributesTo(ActivityScope::class)
interface MainActivityModule {
  @Binds
  @IntoMap
  @ActivityMapKey(MainActivity::class)
  @SingleIn(ActivityScope::class)
  fun bindsMainActivityInjector(target: MembersInjector<MainActivity>): MembersInjector<out Activity>
}
```

### Viewmodel generator

Code Generator to simplify adding view models to the dependency graph with the `ContributesViewModel` annotation.

#### Wiring

Add the required dependencies:

```kotlin
dependencies {
    anvil("ru.pixnews.anvil.codegen.viewmodel:generator:0.3")
    api("ru.pixnews.anvil.codegen.viewmodel:inject:0.3")
}
```

Create a subcomponent for ViewModels and add it to your application component,
as shown in the following example:

```kotlin
import ru.pixnews.anvil.codegen.viewmodel.inject.ViewModelScope
import ru.pixnews.anvil.codegen.viewmodel.inject.wiring.ViewModelFactory
import <your app scope>.AppScope

@ContributesSubcomponent(scope = ViewModelScope::class, parentScope = AppScope::class)
@SingleIn(ViewModelScope::class)
interface ViewModelSubcomponent {
    val viewModelMap: DaggerMap<Class<out ViewModel>, Provider<ViewModel>>
    val viewModelFactoryMap: DaggerMap<Class<out ViewModel>, ViewModelFactory>

    @ContributesSubcomponent.Factory
    fun interface Factory {
        fun create(@BindsInstance savedStateHandle: SavedStateHandle): ViewModelSubcomponent
    }

    @ContributesTo(AppScope::class)
    public interface ViewModelSubcomponentFactoryHolder {
        public fun getViewModelSubcomponentFactory(): ViewModelSubcomponent.Factory
        public fun getViewModelFactory(): ViewModelProvider.Factory
    }
}
```

Declaration of the multibindings in module:

```kotlin
import ru.pixnews.anvil.codegen.viewmodel.inject.ViewModelScope

@Module
@ContributesTo(ViewModelScope::class)
interface ViewModelModule {
    @Multibinds
    fun viewModelProviders(): DaggerMap<Class<out ViewModel>, ViewModel>

    @Multibinds
    fun viewModelFactoryProviders(): DaggerMap<Class<out ViewModel>, ViewModelFactory>
}
```

Create implementation of `ViewModelProvider.Factory` and add it to app scope:

```kotlin
import <your app scope>.AppScope
        
@Reusable
@ContributesBinding(AppScope::class, boundType = ViewModelProvider.Factory::class)
@RestrictTo(RestrictTo.Scope.LIBRARY)
class ViewModelProviderFactory(
    private val vmSubcomponentFactory: ViewModelSubcomponent.Factory,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedSateHandle = extras.createSavedStateHandle()
        val viewModelComponent = vmSubcomponentFactory.create(savedSateHandle)
        val viewModelMap = viewModelComponent.viewModelMap

        val viewModelProvider = viewModelMap[modelClass]
        if (viewModelProvider != null) {
            return viewModelProvider.get() as T
        } else {
            val factory = viewModelComponent.viewModelFactoryMap[modelClass]
                ?: error("No factory for ${modelClass.name}")
            return factory.create(extras) as T
        }
    }
}
```

#### Usage

You can use created `getViewModelFactory()` of the application component as a view model factory.

```kotlin
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.CreationExtras

inline fun <reified VM : ViewModel> ComponentActivity.injectedViewModel(
    noinline extrasProducer: (() -> CreationExtras) = { defaultViewModelCreationExtras },
): Lazy<VM> = viewModels(extrasProducer, appComponent::viewModelFactory)

@Composable
fun <VM : ViewModel> injectedViewModel(
    modelClass: Class<VM>,
    viewModelStoreOwner: ViewModelStoreOwner = …,
    key: String? = null,
    extras: CreationExtras = …,
): VM = viewModel(
    modelClass,
    viewModelStoreOwner,
    key,
    appComponent.getViewModelFactory(),
    extras,
)
```

Annotate your ViewModel with `ContributesViewModel` to add it to the multibinding:

```kotlin
@ContributesViewModel
class MyViewModel(
    featureManager: FeatureManager,
    …
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    …
}
```

The following binding will be generated based on this annotation:

```kotlin
import ru.pixnews.anvil.codegen.viewmodel.inject.ViewModelScope
import ru.pixnews.anvil.codegen.viewmodel.inject.wiring.ViewModelFactory
import ru.pixnews.anvil.codegen.viewmodel.inject.wiring.ViewModelMapKey

@Module
@ContributesTo(ViewModelScope::class)
object MyFactoryModule {
    @Provides
    @IntoMap
    @ViewModelMapKey(MyViewModel::class)
    fun providesMyViewModelViewModelFactory(
        featureManager: FeatureManager,
        …
    ): ViewModelFactory = ViewModelFactory {
        MyViewModel(
            featureManager = featureManager,
            …
            savedStateHandle = it.createSavedStateHandle()
        )
    }
}
```

You can pass additional arguments to ViewModel in [DEFAULT_ARGS_KEY](https://developer.android.com/reference/kotlin/androidx/lifecycle/package-summary#DEFAULT_ARGS_KEY()) of CreationExtras.

### Workmanager generator

Code Generator to simplify adding [CoroutineWorker](https://developer.android.com/reference/kotlin/androidx/work/CoroutineWorker) of WorkManager to the dependency graph with the `ContributesCoroutineWorker` annotation.

#### Wiring

Add the required dependencies:

```kotlin
dependencies {
    anvil("ru.pixnews.anvil.codegen.workmanager:generator:0.3")
    api("ru.pixnews.anvil.codegen.workmanager:inject:0.3")
}
```

Create a subcomponent for workers and add it to your application component,
as shown in the following example:

```kotlin
import ru.pixnews.anvil.codegen.workmanager.inject.WorkManagerScope
import ru.pixnews.anvil.codegen.workmanager.inject.wiring.CoroutineWorkerFactory
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

#### Usage

Annotate your Coroutine Worker with `ContributesCoroutineWorker` to add it to the multibinding:

```kotlin
import ru.pixnews.anvil.codegen.workmanager.inject.ContributesCoroutineWorker

@ContributesCoroutineWorker
class MyCoroutineWorker @AssistedInject constructor(
    logger: Logger,
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = Result.success()
}
```

The following binding will be generated based on this annotation:

```kotlin
import ru.pixnews.anvil.codegen.workmanager.inject.WorkManagerScope
import ru.pixnews.anvil.codegen.workmanager.inject.wiring.CoroutineWorkerFactory
import ru.pixnews.anvil.codegen.workmanager.inject.wiring.CoroutineWorkerMapKey
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

### Test generator

A generator that simplifies dependency injection to an junit4 instrumented Android tests with the `ContributesTest` annotation.

#### Wiring

Add the required dependencies:

```kotlin
dependencies {
    anvil("ru.pixnews.anvil.codegen.test:generator:0.3")
    api("ru.pixnews.anvil.codegen.test:inject:0.3")
}
```

Add `InstrumentedTestInjector` to your application component:

```kotlin
import ru.pixnews.anvil.codegen.test.inject.wiring.InstrumentedTestInjector
import ru.pixnews.foundation.di.base.scopes.AppScope

@ContributesTo(AppScope::class)
interface InstrumentedTestInjectorHolder {
    val instrumentedTestInjector: InstrumentedTestInjector
}
```

Add multibinding to the application module:

```kotlin
import ru.pixnews.anvil.codegen.test.inject.wiring.DefaultInstrumentedTestInjector
import ru.pixnews.anvil.codegen.test.inject.wiring.InstrumentedTestInjector
import ru.pixnews.anvil.codegen.test.inject.wiring.SingleInstrumentedTestInjector
import ru.pixnews.foundation.di.base.DaggerMap
import ru.pixnews.foundation.di.base.scopes.AppScope

@ContributesTo(AppScope::class)
@Module
interface InstrumentedTestsInjectorsModule {
    @Multibinds
    fun instrumentedTestInjectors(): DaggerMap<Class<out Any>, SingleInstrumentedTestInjector>

    companion object {
        @Reusable
        @Provides
        fun provideInstrumentedTestInjector(
            injectors: DaggerMap<Class<out Any>, SingleInstrumentedTestInjector>,
        ): InstrumentedTestInjector {
            return DefaultInstrumentedTestInjector(injectors)
        }
    }
}
```

Create Junit4 [TestRule](https://junit.org/junit4/javadoc/4.12/org/junit/rules/TestRule.html) with dependency injection:

```kotlin
class InjectDependenciesRule(
    private val instance: Any,
) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        injectDependencies()
        return base
    }

    private fun injectDependencies() {
        (appComponent as InstrumentedTestInjectorHolder)
            .instrumentedTestInjector
            .inject(instance)
    }
}
```

#### Usage

Annotate your test with `ContributesTest` and use InjectDependenciesRule:

```kotlin
import ru.pixnews.anvil.codegen.test.inject.ContributesTest

@ContributesTest
class MyTest  {
    @get:Rule(order = 10)
    val injectDependencies = InjectDependenciesRule(this)

    @Inject
    lateinit var appConfig: AppConfig
}
```

The following binding will be generated based on this annotation:

```kotlin
import ru.pixnews.anvil.codegen.test.inject.wiring.SingleInstrumentedTestInjector
import ru.pixnews.foundation.di.base.scopes.AppScope

@Module
@ContributesTo(AppScope::class)
object MyTest_TestModule {
  @Provides
  @IntoMap
  @ClassKey(MyTest::class)
  @SingleIn(AppScope::class)
  fun provideMyTestInjector(
      injector: MembersInjector<CalendarFeedWidthOnMediumSizeTest>
  ): SingleInstrumentedTestInjector = SingleInstrumentedTestInjector(injector)
}
```

### Experiment generator

`ContributesExperiment` and `ContributesExperimentVariantSerializer` annotations used to simplify creation of feature flags in different application modules and adding them to a feature manager.

#### Wiring

Add the required dependencies:

```kotlin
dependencies {
    anvil("ru.pixnews.anvil.codegen.experiment:generator:0.3")
    api("ru.pixnews.anvil.codegen.experiment:inject:0.3")
}
```

Create component with experiments:

```kotlin
import ru.pixnews.anvil.codegen.experiment.inject.ExperimentScope

@SingleIn(ExperimentScope::class)
@MergeComponent(scope = ExperimentScope::class)
interface ExperimentsComponent {
    fun appExperiments(): DaggerSet<Experiment>

    fun appExperimentVariantSerializers(): DaggerMap<ExperimentKey, ExperimentVariantSerializer>

    companion object {
        operator fun invoke(): ExperimentsComponent = DaggerExperimentsComponent.create()
    }
}
```

Module:

```kotlin
import ru.pixnews.anvil.codegen.experiment.inject.ExperimentScope

@Module
@ContributesTo(ExperimentScope::class)
abstract class ExperimentsModule {
    @Multibinds
    abstract fun appExperiments(): DaggerSet<Experiment>

    @Multibinds
    abstract fun appExperimentSerializers(): DaggerMap<String, ExperimentVariantSerializer>
}
```

#### Usage

Annotate experiment with `@ContributesExperiment` and serializer with `@ContributesExperimentSerializer`:

```kotlin
@ContributesExperiment
object DarkModeExperiment : Experiment {
    …

    @ContributesExperimentVariantSerializer("ui.dark_mode")
    object Serializer : BooleanVariantSerializer(…)

}
```

The following binding will be generated based on this annotation:

```kotlin
@Module
@ContributesTo(ExperimentScope::class)
object DarkModeExperiment_Experiments_Module {
  @Provides
  @IntoSet
  fun provideDarkModeExperiment(): Experiment = DarkModeExperiment

  @Provides
  @IntoMap
  @ExperimentVariantMapKey(key = "ui.dark_mode")
  fun provideSerializer(): ExperimentVariantSerializer = DarkModeExperiment.Serializer
}
```

### Initializer generator

`ContributesInitializer` annotation used to simplify the aggregation of application initializers from application modules.

#### Wiring generator

Add the required dependencies:

```kotlin
dependencies {
    anvil("ru.pixnews.anvil.codegen.initializer:generator:0.3")
    api("ru.pixnews.anvil.codegen.initializer:inject:0.3")
}
```

Create component to collect all initializers into multibinding set:

```kotlin
import ru.pixnews.anvil.codegen.initializer.inject.AppInitializersScope

@SingleIn(AppInitializersScope::class)
@MergeComponent(
    scope = AppInitializersScope::class,
    dependencies = […],
)
interface AppInitializerComponent {
    fun inject(initializer: GlobalAndroidxStartupAppInitializer)
    
    @Component.Factory
    fun interface Factory {
        …
    }
}
```

Module:

```kotlin
import ru.pixnews.anvil.codegen.initializer.inject.AppInitializersScope
import ru.pixnews.foundation.initializers.AppInitializer
import ru.pixnews.foundation.initializers.AsyncInitializer
import ru.pixnews.foundation.initializers.Initializer

@ContributesTo(AppInitializersScope::class)
@Module
abstract class AppInitializersModule {
    @Multibinds
    abstract fun appInitializers(): DaggerSet<Initializer>

    @Multibinds
    abstract fun appAsyncInitializers(): DaggerSet<AsyncInitializer>

    companion object {
        @Provides
        fun providesAppInitializer(
            initializers: DaggerSet<Initializer>,
            asyncInitializers: DaggerSet<AsyncInitializer>,
            …
        ): AppInitializer {
            // AppInitializer used in GlobalAndroidxStartupAppInitializer (androidx.startup.Initializer)
            // to execute collected initializers
            return AppInitializer(initializers, asyncInitializers, …)
        }
    }
}
```

#### Usage

Implement `ru.pixnews.foundation.initializers.AsyncInitializer` or `ru.pixnews.foundation.initializers.Initializer` and annotate it with `ContributesInitializer` to add it to multibinding:

```kotlin
@ContributesInitializer(replaces = [DebugStrictModeInitializerModule::class])
class TestStrictModeInitializer @Inject constructor(plogger: Logger) : Initializer {
    private val logger = logger.withTag("TestStrictModeInitializer")

    override fun init() {
        logger.v { "Setting up StrictMode" }

        StrictMode.setThreadPolicy(
            …
        )
    }
}
```

The following binding will be generated based on this annotation:

```kotlin
@Module
@ContributesTo(
  AppInitializersScope::class,
  replaces = [DebugStrictModeInitializerModule::class],
)
object TestStrictModeInitializer_InitializerModule {
  @Provides
  @IntoSet
  @Reusable
  fun provideTestStrictModeInitializer(logger: Logger): Initializer = TestStrictModeInitializer(
      logger = logger
  )
}
```

## Contributing

Any type of contributions are welcome. Please see the [contribution guide](CONTRIBUTING.md).

## License

These services are licensed under Apache 2.0 License. Authors and contributors are listed in the
[Authors](AUTHORS) file.

```
Copyright 2024 pixnews-anvil-codegen project authors and contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
