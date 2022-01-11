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

package com.swordfish.lemuroid.common.rx

import com.swordfish.lemuroid.common.kotlin.NTuple4
import io.reactivex.Observable

object RXUtils {
    fun <T1, T2, T3, T4> combineLatest(
        source1: Observable<T1>,
        source2: Observable<T2>,
        source3: Observable<T3>,
        source4: Observable<T4>
    ): Observable<NTuple4<T1, T2, T3, T4>> {
        return Observable.combineLatest(
            source1,
            source2,
            source3,
            source4,
            { t1, t2, t3, t4 -> NTuple4(t1, t2, t3, t4) }
        )
    }
}
