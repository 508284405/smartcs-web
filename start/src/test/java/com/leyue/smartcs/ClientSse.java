/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.leyue.smartcs;

import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;

import java.net.http.HttpRequest;


/**
 * @author Christian Tzolov
 */
public class ClientSse {

	public static void main(String[] args) {
		var transport = HttpClientSseClientTransport.builder("http://localhost:8082")
				.requestBuilder(HttpRequest.newBuilder().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJzdWIiOiIxIiwiaWF0IjoxNzUwMDg2MzkyLCJleHAiOjE3NTAxMDQzOTIsImp0aSI6IjdhYjczYmU5LTVlYjMtNDk3OS05ZjM4LTllYTYwNzk4MzhmZCIsImlzcyI6InVzZXItY2VudGVyIn0.TfCSUDysnqyjbAbrMU6LxSqPzH8iPFa5ZOJlFhUR9G8"))
				.sseEndpoint("/mcp/order/sse")
				.build();
		new SampleClient(transport).run();
	}

}