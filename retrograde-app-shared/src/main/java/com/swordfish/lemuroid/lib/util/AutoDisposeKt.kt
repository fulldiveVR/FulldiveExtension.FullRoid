/*
 *  RetrogradeApplicationComponent.kt
 *
 *  Copyright (C) 2017 Retrograde Project
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.swordfish.lemuroid.lib.util

import com.uber.autodispose.CompletableSubscribeProxy
import com.uber.autodispose.FlowableSubscribeProxy
import com.uber.autodispose.MaybeSubscribeProxy
import com.uber.autodispose.ObservableSubscribeProxy
import com.uber.autodispose.SingleSubscribeProxy
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.plugins.RxJavaPlugins

/*
 * An example of extension functions on the objects returned by `AutoDisposeConverter`.
 *
 * AutoDispose returns proxy objects that don't extend Observable or the other reactive classes. This means
 * that extensions like RxKotlin's `Observable.subscribeBy` can't be used. However, it's easy to define your
 * own.
 *
 * These extension functions can be called in the following manner:
 *
 * ```
 * Observable.just(1)
 *   .autoDisposable(this)
 *   .subscribeBy(onError = { Log.e(it) })
 * ```
 */

private val onNextStub: (Any) -> Unit = {}
private val onErrorStub: (Throwable) -> Unit = { RxJavaPlugins.onError(OnErrorNotImplementedException(it)) }
private val onCompleteStub: () -> Unit = {}

/**
 * Overloaded subscribe function that allows passing named parameters
 */
fun <T : Any> ObservableSubscribeProxy<T>.subscribeBy(
    onError: (Throwable) -> Unit = onErrorStub,
    onComplete: () -> Unit = onCompleteStub,
    onNext: (T) -> Unit = onNextStub
): Disposable {
    return if (onError === onErrorStub && onComplete === onCompleteStub) {
        subscribe(onNext)
    } else {
        subscribe(onNext, onError, onComplete)
    }
}

/**
 * Overloaded subscribe function that allows passing named parameters
 */
fun <T : Any> FlowableSubscribeProxy<T>.subscribeBy(
    onError: (Throwable) -> Unit = onErrorStub,
    onComplete: () -> Unit = onCompleteStub,
    onNext: (T) -> Unit = onNextStub
): Disposable {
    return if (onError === onErrorStub && onComplete === onCompleteStub) {
        subscribe(onNext)
    } else {
        subscribe(onNext, onError, onComplete)
    }
}

/**
 * Overloaded subscribe function that allows passing named parameters
 */
fun <T : Any> SingleSubscribeProxy<T>.subscribeBy(
    onError: (Throwable) -> Unit = onErrorStub,
    onSuccess: (T) -> Unit = onNextStub
): Disposable {
    return if (onError === onErrorStub) {
        subscribe(onSuccess)
    } else {
        subscribe(onSuccess, onError)
    }
}

/**
 * Overloaded subscribe function that allows passing named parameters
 */
fun <T : Any> MaybeSubscribeProxy<T>.subscribeBy(
    onError: (Throwable) -> Unit = onErrorStub,
    onComplete: () -> Unit = onCompleteStub,
    onSuccess: (T) -> Unit = onNextStub
): Disposable {
    return if (onError === onErrorStub && onComplete === onCompleteStub) {
        subscribe(onSuccess)
    } else {
        subscribe(onSuccess, onError, onComplete)
    }
}

/**
 * Overloaded subscribe function that allows passing named parameters
 */
fun CompletableSubscribeProxy.subscribeBy(
    onError: (Throwable) -> Unit = onErrorStub,
    onComplete: () -> Unit = onCompleteStub
): Disposable {
    return if (onError === onErrorStub) {
        subscribe(onComplete)
    } else {
        subscribe(onComplete, onError)
    }
}
