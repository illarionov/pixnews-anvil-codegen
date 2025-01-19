/*
 * Copyright (c) 2024-2025, the pixnews-anvil-codegen project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.anvil.ksp.codegen.test.inject.wiring

import dagger.MembersInjector

public class SingleInstrumentedTestInjector(
    private val injector: MembersInjector<*>,
) {
    public fun <T> injectMembers(instance: T) {
        @Suppress("UNCHECKED_CAST")
        (injector as MembersInjector<T>).injectMembers(instance)
    }
}
