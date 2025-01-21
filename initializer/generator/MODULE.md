# Module initializer-generator

A code generator for use with [Anvil-KSP] that simplifies adding application initializers to the DI graph 
with a single _@ContributesInitializer_ annotation.

## Code generator setup

Before using the generator, make sure [Anvil-KSP] is already configured in the project.

Then add the required dependencies:

```kotlin
dependencies {
    anvil("ru.pixnews.anvil.ksp.codegen:initializer-generator:0.1")
    // Add this dependency to the ksp classpath if "anvil" configuration doesn't work for some reason:
    ksp("ru.pixnews.anvil.ksp.codegen:initializer-generator:0.1")

    implementation("ru.pixnews.anvil.ksp.codegen:initializer-inject:0.1")
}
```

## Project setup

To make use of this generator's output, the project must have injection set up using Dagger multibindings.
The configuration might look something like this (using classes from the [initializer-inject] module).

Create component to collect all initializers into multibinding set:

```kotlin
import ru.pixnews.anvil.ksp.codegen.initializer.inject.AppInitializersScope

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
import ru.pixnews.anvil.ksp.codegen.initializer.inject.AppInitializersScope
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

[Anvil-KSP]: https://github.com/ZacSweers/anvil
[initializer-inject]: https://illarionov.github.io/pixnews-anvil-codegen/initializer-inject/
