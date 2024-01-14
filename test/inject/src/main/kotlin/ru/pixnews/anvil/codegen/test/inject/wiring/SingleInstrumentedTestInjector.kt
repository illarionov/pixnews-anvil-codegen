/*
 * Copyright (c) 2024, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package ru.pixnews.anvil.codegen.test.inject.wiring

import dagger.MembersInjector

public class SingleInstrumentedTestInjector(
    private val injector: MembersInjector<*>,
) {
    public fun <T> injectMembers(instance: T) {
        @Suppress("UNCHECKED_CAST")
        (injector as MembersInjector<T>).injectMembers(instance)
    }
}
