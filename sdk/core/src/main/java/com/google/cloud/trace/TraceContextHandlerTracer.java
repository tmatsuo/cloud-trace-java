// Copyright 2016 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.cloud.trace;

import com.google.cloud.trace.util.EndSpanOptions;
import com.google.cloud.trace.util.Labels;
import com.google.cloud.trace.util.SpanKind;
import com.google.cloud.trace.util.StackTrace;
import com.google.cloud.trace.util.StartSpanOptions;
import com.google.cloud.trace.util.Timestamp;
import com.google.cloud.trace.util.TraceContext;
import com.google.cloud.trace.util.TraceOptions;
import java.util.logging.Logger;

/**
 * A managed tracer user to trace application code.
 *
 * <p>This tracer maintains a stack of trace contexts in a trace context handler and delegates calls
 * to another tracer.
 */
public class TraceContextHandlerTracer implements Tracer, ManagedTracer  {
  private static final Logger logger = Logger.getLogger(TraceContextHandlerTracer.class.getName());

  private final Tracer tracer;
  private final TraceContextHandler contextHandler;

  /**
   * Creates a new managed tracer.
   *
   * @param tracer         a tracer that serves as a delegate for all tracer functionality.
   * @param contextHandler a trace context handler that manages a stack of trace contexts.
   */
  public TraceContextHandlerTracer(Tracer tracer, TraceContextHandler contextHandler) {
    this.tracer = tracer;
    this.contextHandler = contextHandler;
  }

  @Override
  public TraceContext startSpan(TraceContext parentContext, String name) {
    return tracer.startSpan(parentContext, name);
  }

  @Override
  public TraceContext startSpan(TraceContext parentContext, String name, StartSpanOptions options) {
    return tracer.startSpan(parentContext, name, options);
  }

  @Override
  public void endSpan(TraceContext context) {
    tracer.endSpan(context);
  }

  @Override
  public void endSpan(TraceContext context, EndSpanOptions options) {
    tracer.endSpan(context, options);
  }

  @Override
  public void annotateSpan(TraceContext context, Labels labels) {
    tracer.annotateSpan(context, labels);
  }

  @Override
  public void setStackTrace(TraceContext context, StackTrace stackTrace) {
    tracer.setStackTrace(context, stackTrace);
  }

  @Override
  public void startSpan(String name) {
    TraceContext context = tracer.startSpan(contextHandler.current(), name);
    contextHandler.push(context);
  }

  @Override
  public void startSpan(String name, StartSpanOptions options) {
    TraceContext context = tracer.startSpan(contextHandler.current(), name, options);
    contextHandler.push(context);
  }

  @Override
  public void endSpan() {
    TraceContext context = contextHandler.pop();
    if (context != null) {
      tracer.endSpan(context);
    } else {
      logger.warning("Too many calls to ContextHandlerTraceClient.endCurrentSpan().");
    }
  }

  @Override
  public void endSpan(EndSpanOptions options) {
    TraceContext context = contextHandler.pop();
    if (context != null) {
      tracer.endSpan(context, options);
    } else {
      logger.warning("Too many calls to ContextHandlerTraceClient.endCurrentSpan().");
    }
  }

  @Override
  public void annotateSpan(Labels labels) {
    tracer.annotateSpan(contextHandler.current(), labels);
  }

  @Override
  public void setStackTrace(StackTrace stackTrace) {
    tracer.setStackTrace(contextHandler.current(), stackTrace);
  }

  @Override
  public TraceContext getCurrentTraceContext() {
    return contextHandler.current();
  }
}
