/**
 * Copyright 2016 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.github.ambry.frontend;

import com.github.ambry.commons.Callback;
import com.github.ambry.commons.CallbackUtils;
import com.github.ambry.messageformat.BlobInfo;
import com.github.ambry.rest.RestRequest;
import com.github.ambry.rest.RestResponseChannel;
import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;


/**
 * Responsible for performing any security validations on the Rest request and response. Validations could involve
 * authentication, authorization, security checks and so on which the implementation can decide. This could also involve
 * setting headers while responding, based on the request.
 * Exceptions are returned via {@link Callback}s on any validation failure.
 */
public interface SecurityService extends Closeable {

  /**
   * Performs security validations (if any) before any processing of the {@code restRequest} begins and invokes the
   * {@code callback} once done.
   * @param restRequest the {@link RestRequest} to process.
   * @param callback the callback to invoke once processing is finished.
   */
  void preProcessRequest(RestRequest restRequest, Callback<Void> callback);

  /**
   * Performs security validations (if any) on the {@link RestRequest} asynchronously and invokes the
   * {@link Callback} when the validation completes.
   * @param restRequest {@link RestRequest} upon which validations has to be performed
   * @param callback The {@link Callback} which will be invoked on the completion of the request. Cannot be null.
   */
  void processRequest(RestRequest restRequest, Callback<Void> callback);

  /**
   * Performs security validations (if any) on the {@link RestRequest} when it has been fully parsed. That is, when the
   * {@link RestRequest} has been annotated with any additional arguments (like account and container).
   * Invokes the {@link Callback} when the validation is complete.
   * @param restRequest {@link RestRequest} upon which validations has to be performed
   * @param callback The {@link Callback} which will be invoked on the completion of the request. Cannot be null.
   */
  void postProcessRequest(RestRequest restRequest, Callback<Void> callback);

  /**
   * Calculates charges incurred in processing the request, accounts for those charges in quota and sets cost and quota
   * headers if need be.
   * @param restRequest {@link RestRequest} whose response have to be validated
   * @param responseChannel the {@link RestResponseChannel} over which the response is sent
   * @param blobInfo the {@link BlobInfo} pertaining to the rest request made
   */
  void processRequestCharges(RestRequest restRequest, RestResponseChannel responseChannel, BlobInfo blobInfo);

  /**
   * Performs security validations (if any) on the response for {@link RestRequest} asynchronously, sets headers if need
   * be and invokes the {@link Callback} when the validation completes. Similar to
   * {@link SecurityService#processRequest(RestRequest, Callback)}, validations could involve security checks and
   * setting some headers with the response.
   * @param restRequest {@link RestRequest} whose response have to be validated
   * @param responseChannel the {@link RestResponseChannel} over which the response is sent
   * @param blobInfo the {@link BlobInfo} pertaining to the rest request made
   * @param callback The {@link Callback} which will be invoked on the completion of the request. Cannot be null.
   */
  void processResponse(RestRequest restRequest, RestResponseChannel responseChannel, BlobInfo blobInfo,
      Callback<Void> callback);

  /**
   * Similar to {@link #preProcessRequest(RestRequest, Callback)} but returns a {@link CompletableFuture} instead of requiring
   * a callback.
   * @param restRequest {@link RestRequest} upon which validations has to be performed
   * @return a {@link CompletableFuture} that is completed when the pre-processing is done.
   */
  default CompletableFuture<Void> preProcessRequest(RestRequest restRequest) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    preProcessRequest(restRequest, CallbackUtils.fromCompletableFuture(future));
    return future;
  }

  /**
   * Similar to {@link #processRequest(RestRequest, Callback)} but returns a {@link CompletableFuture} instead of requiring
   * a callback.
   * @param restRequest {@link RestRequest} upon which validations has to be performed
   * @return a {@link CompletableFuture} that is completed when the processing is done.
   */
  default CompletableFuture<Void> processRequest(RestRequest restRequest) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    processRequest(restRequest, CallbackUtils.fromCompletableFuture(future));
    return future;
  }

  /**
   * Similar to {@link #postProcessRequest(RestRequest, Callback)} but returns a {@link CompletableFuture} instead of requiring
   * a callback.
   * @param restRequest {@link RestRequest} upon which validations has to be performed
   * @return a {@link CompletableFuture} that is completed when the post-processing is done.
   */
  default CompletableFuture<Void> postProcessRequest(RestRequest restRequest) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    postProcessRequest(restRequest, CallbackUtils.fromCompletableFuture(future));
    return future;
  }

  /**
   * Similar to {@link #processResponse(RestRequest, RestResponseChannel, BlobInfo, Callback)} but returns a
   * {@link CompletableFuture} instead of requiring a callback.
   * @param restRequest {@link RestRequest} whose response have to be validated
   * @param responseChannel the {@link RestResponseChannel} over which the response is sent
   * @param blobInfo the {@link BlobInfo} pertaining to the rest request made
   * @return a {@link CompletableFuture} that is completed when the processing is done.
   */
  default CompletableFuture<Void> processResponse(RestRequest restRequest, RestResponseChannel responseChannel,
      BlobInfo blobInfo) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    processResponse(restRequest, responseChannel, blobInfo, CallbackUtils.fromCompletableFuture(future));
    return future;
  }
}
