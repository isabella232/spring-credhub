/*
 * Copyright 2016-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.credhub.support.certificate;

import com.jayway.jsonpath.DocumentContext;
import org.junit.Before;
import org.junit.Test;

import org.springframework.credhub.support.CredHubRequestUnitTestsBase;
import org.springframework.credhub.support.JsonPathAssert;
import org.springframework.credhub.support.SimpleCredentialName;
import org.springframework.credhub.support.WriteMode;

public class CertificateCredentialRequestUnitTests extends CredHubRequestUnitTestsBase {

	@Before
	public void setUp() {
		buildRequest(new CertificateCredential("cert", "ca", "private-key"));
	}

	@Test
	public void serializeWithAllValues() {
		DocumentContext json = toJsonPath(this.requestBuilder);

		assertCommonRequestFields(json, true, WriteMode.OVERWRITE, "/example/credential", "certificate");
		JsonPathAssert.assertThat(json).hasPath("$.value.certificate").isEqualTo("cert");
		JsonPathAssert.assertThat(json).hasPath("$.value.ca").isEqualTo("ca");
		JsonPathAssert.assertThat(json).hasPath("$.value.private_key").isEqualTo("private-key");

		assertNoPermissions(json);
	}

	@Test
	public void serializeWithCertOnly() {
		buildRequest(new CertificateCredential("cert", null, null));

		DocumentContext json = toJsonPath(this.requestBuilder);

		assertCommonRequestFields(json, true, WriteMode.OVERWRITE, "/example/credential", "certificate");
		JsonPathAssert.assertThat(json).hasPath("$.value.certificate").isEqualTo("cert");
		JsonPathAssert.assertThat(json).hasNoPath("$.value.ca").hasNoPath("$.value.private_key");

		assertNoPermissions(json);
	}

	@Test
	public void serializeWithNoCert() {
		buildRequest(new CertificateCredential(null, "ca", "private-key"));

		DocumentContext json = toJsonPath(this.requestBuilder);

		assertCommonRequestFields(json, true, WriteMode.OVERWRITE, "/example/credential", "certificate");
		JsonPathAssert.assertThat(json).hasNoPath("$.value.certificate");
		JsonPathAssert.assertThat(json).hasPath("$.value.ca").isEqualTo("ca");
		JsonPathAssert.assertThat(json).hasPath("$.value.private_key").isEqualTo("private-key");

		assertNoPermissions(json);
	}

	@Test(expected = IllegalArgumentException.class)
	public void serializeWithNoValues() {
		buildRequest(new CertificateCredential(null, null, null));

		toJsonPath(this.requestBuilder);
	}

	@SuppressWarnings("deprecation")
	private void buildRequest(CertificateCredential value) {
		this.requestBuilder = CertificateCredentialRequest.builder()
				.name(new SimpleCredentialName("example", "credential")).overwrite(true).mode(WriteMode.OVERWRITE)
				.value(value);
	}

}
