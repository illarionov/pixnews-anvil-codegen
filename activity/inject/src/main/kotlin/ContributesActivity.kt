/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.activity.inject

/**
 * Annotate a Activity class with this to automatically contribute it to the ActivityScope multibinding.
 * Equivalent to the following declaration in an application module:
 *```
 *  @Module
 *  @ContributesTo(ActivityScope::class)
 *  abstract class MainActivityModule {
 *    @Binds
 *    @IntoMap
 *    @ActivityMapKey(MainActivity::class)
 *    @SingleIn(ActivityScope::class)
 *    abstract fun bindsMainInjector(target: MembersInjector<MainActivity>): MembersInjector<out Activity>
 *  }
 *```
 * The generated code created via the anvil-codegen module.
 */
public annotation class ContributesActivity
