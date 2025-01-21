# Module test-generator

A code generator for use with [Anvil-KSP] that simplifies dependency injection into junit4 test classes 
with a single _@ContributesTest_ annotation.

For each class annotated with [ContributesTest], a Dagger module will be generated with
content similar to the following:

```kotlin
import ru.pixnews.anvil.ksp.codegen.test.inject.wiring.SingleInstrumentedTestInjector
import ru.pixnews.foundation.di.base.scopes.AppScope

@Module
@ContributesTo(AppScope::class)
object MyTest_TestModule {
  @Provides
  @IntoMap
  @ClassKey(MyTest::class)
  @SingleIn(AppScope::class)
  fun provideMyTestInjector(
      injector: MembersInjector<MyTest>
  ): SingleInstrumentedTestInjector = SingleInstrumentedTestInjector(injector)
}
```

This module is intended to add the specified class to the AppScope multibinding.

## Code generator setup

Before using the generator, make sure [Anvil-KSP] is already configured in the project.

Then add the required dependencies:

```kotlin
dependencies {
    ksp("ru.pixnews.anvil.ksp.codegen:test-generator:0.1")

    // test-inject dependency is optional, all declarations this module can be overridden.
    implementation("ru.pixnews.anvil.ksp.codegen:test-inject:0.1")
}
```

The fully qualified names of annotation classes can be customized via KSP arguments in `build.gradle.kts`;

```kotlin
ksp {
    arg(
        "ru.pixnews.anvil.ksp.base.AppScope",
        "ru.pixnews.foundation.di.base.scopes.AppScope"
    )
    arg(
        "ru.pixnews.anvil.ksp.test.ContributesTest",
        "ru.pixnews.anvil.ksp.codegen.test.inject.ContributesTest"
    )
    arg(
        "ru.pixnews.anvil.ksp.test.SingleInstrumentedTestInjector",
        "ru.pixnews.anvil.ksp.codegen.test.inject.wiring.SingleInstrumentedTestInjector"
    )
}
```

By default, annotations from the [test-inject] module are used.

## Project setup

To make use of this generator's output, the project must have test injection set up using Dagger multibindings.
The configuration might look something like this (using classes from the [test-inject] module).

Add `InstrumentedTestInjector` to your application component:

```kotlin
import ru.pixnews.anvil.ksp.codegen.test.inject.wiring.InstrumentedTestInjector
import ru.pixnews.foundation.di.base.scopes.AppScope

@ContributesTo(AppScope::class)
interface InstrumentedTestInjectorHolder {
    val instrumentedTestInjector: InstrumentedTestInjector
}
```

Add multibinding to the application module:

```kotlin
import ru.pixnews.anvil.ksp.codegen.test.inject.wiring.DefaultInstrumentedTestInjector
import ru.pixnews.anvil.ksp.codegen.test.inject.wiring.InstrumentedTestInjector
import ru.pixnews.anvil.ksp.codegen.test.inject.wiring.SingleInstrumentedTestInjector
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

## Usage

Annotate your test with `ContributesTest` and use InjectDependenciesRule:

```kotlin
import ru.pixnews.anvil.ksp.codegen.test.inject.ContributesTest

@ContributesTest
class MyTest  {
    @get:Rule(order = 10)
    val injectDependencies = InjectDependenciesRule(this)

    @Inject
    lateinit var appConfig: AppConfig
}
```

The MyTest_TestModule module described above will be generated based on this annotation.


[Anvil-KSP]: https://github.com/ZacSweers/anvil
[test-inject]: https://illarionov.github.io/pixnews-anvil-codegen/test-inject/
