/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.performanceanalysis.service;

import com.squareup.leakcanary.LeakTrace;

import java.io.Serializable;

public final class AnalysisResult implements Serializable {


    /** True if a leak was found in the heap dump. */
    public boolean leakFound;

    /**
     * True if {@link #leakFound} is true and the only path to the leaking reference is
     * through excluded references. Usually, that means you can safely ignore this report.
     */
    public boolean excludedLeak;

    /**
     * Class name of the object that leaked if {@link #leakFound} is true, null otherwise.
     * The class name format is the same as what would be returned by {@link Class#getName()}.
     */
    public String className;

    /**
     * Shortest path to GC roots for the leaking object if {@link #leakFound} is true, null
     * otherwise. This can be used as a unique signature for the leak.
     */
    public LeakTrace leakTrace;

    /** Null unless the analysis failed. */
    public Throwable failure;

    /**
     * The number of bytes which would be freed if all references to the leaking object were
     * released. {@link #RETAINED_HEAP_SKIPPED} if the retained heap size was not computed. 0 if
     * {@link #leakFound} is false.
     */
    public long retainedHeapSize;

    /** Total time spent analyzing the heap. */
    public long analysisDurationMs;

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "leakFound=" + leakFound +
                ", excludedLeak=" + excludedLeak +
                ", className='" + className + '\'' +
                ", leakTrace=" + leakTrace +
                ", failure=" + failure +
                ", retainedHeapSize=" + retainedHeapSize +
                ", analysisDurationMs=" + analysisDurationMs +
                '}';
    }
}