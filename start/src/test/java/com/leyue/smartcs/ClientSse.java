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
				.requestBuilder(HttpRequest.newBuilder().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjMsInVzZXJuYW1lIjoid2FuZ3l1Iiwic3ViIjoiMyIsImlhdCI6MTc0OTczODM5NywiZXhwIjoxNzQ5NzU2Mzk3LCJqdGkiOiJlMmVjOGU1NC1jNTZkLTRiYTctOWI1OC0zNzg1NDY2ZDdjNDAiLCJpc3MiOiJ1c2VyLWNlbnRlciJ9.sN8W1P8XnTNSItZrHMj6jgPsdL4FYtzUuToS1oT5Go8"))
				.sseEndpoint("/sse2")
				.build();
		new SampleClient(transport).run();
	}

}