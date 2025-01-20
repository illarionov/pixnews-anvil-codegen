# Module activity-generator

A code generator for use with [Anvil-KSP] that simplifies adding Android Activities to the DI graph 
with a single _@ContributesActivity_ annotation.

For each [Android Activity] annotated with [ContributesActivity], a Dagger module will be generated with 
content similar to the following:

```
import android.app.Activity
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.optional.SingleIn
import dagger.Binds
import dagger.MembersInjector
import dagger.Module
import dagger.multibindings.IntoMap
import ru.pixnews.anvil.ksp.codegen.activity.inject.ActivityScope
import ru.pixnews.anvil.ksp.codegen.activity.inject.wiring.ActivityMapKey

@Module
@ContributesTo(ActivityScope::class)
public interface MainActivity_ActivityModule {
  @Binds
  @IntoMap
  @ActivityMapKey(MainActivity::class)
  @SingleIn(ActivityScope::class)
  public fun bindsMainActivityInjector(target: MembersInjector<MainActivity>): MembersInjector<out Activity>
}
```

This module is intended to add the specified Activity to the ActivityScope multibinding.

## Code generator setup

Before using the generator, make sure [Anvil-KSP] is already configured in the project.

Then add the required dependencies:

```kotlin
dependencies {
    anvil("ru.pixnews.anvil.ksp.codegen:activity-generator:0.1")
    // Add this dependency to the ksp classpath if "anvil" configuration doesn't work for some reason:
    ksp("ru.pixnews.anvil.ksp.codegen:activity-generator:0.1")

    // activity-inject dependency is optional, all declarations this module can be overridden.
    implementation("ru.pixnews.anvil.ksp.codegen:activity-inject:0.1")
}
```

The fully qualified names of annotation classes can be customized via KSP arguments in `build.gradle.kts`;

```kotlin
ksp {
    arg(
        "ru.pixnews.anvil.ksp.activity.ContributesActivity",
        "ru.pixnews.anvil.ksp.codegen.activity.inject.ContributesActivity"
    )
    arg(
        "ru.pixnews.anvil.ksp.activity.ActivityMapKey",
        "ru.pixnews.anvil.ksp.codegen.activity.inject.wiring.ActivityMapKey"
    )
    arg(
        "ru.pixnews.anvil.ksp.activity.ActivityMapKey",
        "ru.pixnews.anvil.ksp.codegen.activity.inject.wiring.ActivityMapKey"
    )
}
```

By default, annotations from the [activity-inject] module are used.

## Project setup

To make use of this generator's output, the project must have activity injection set up using Dagger multibindings.
The configuration might look something like this (using classes from the [activity-inject] module).

Create a subcomponent with an Activity scope and add it to your application component,
as shown in the following example:

```kotlin
import ru.pixnews.anvil.ksp.codegen.activity.inject.ActivityScope
import ru.pixnews.anvil.ksp.codegen.activity.inject.wiring.ActivityInjector
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

import ru.pixnews.anvil.ksp.codegen.activity.inject.ActivityScope
import ru.pixnews.anvil.ksp.codegen.activity.inject.wiring.ActivityInjector
import ru.pixnews.anvil.ksp.codegen.activity.inject.wiring.DefaultActivityInjector

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

## Usage

Now your can annotate any activity with `ContributesActivity` to add it to multibinding.

```kotlin
import ru.pixnews.anvil.ksp.codegen.activity.inject.ContributesActivity

@ContributesActivity
class MainActivity : BaseActivity() {
    @Inject
    internal lateinit var analytics: Analytics
}
```

The MainActivityModule module described above will be generated based on this annotation.

[Anvil-KSP]: https://github.com/ZacSweers/anvil
[activity-inject]: https://illarionov.github.io/pixnews-anvil-codegen/activity-inject/
[Android Activity]: https://developer.android.com/reference/android/app/Activity
[ContributesActivity]: https://illarionov.github.io/pixnews-anvil-codegen/activity-inject/ru.pixnews.anvil.ksp.codegen.activity.inject/-contributes-activity/index.html
