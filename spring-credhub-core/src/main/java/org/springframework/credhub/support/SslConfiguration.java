/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.credhub.support;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.FileReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class SslConfiguration {
	private static final char[] KEY_PASSWORD = "keystore".toCharArray();
	private static final String CERTIFICATE_NAME = "credhub-cert";
	private static final String KEY_NAME = "credhub-key";

	private KeyStore trustStore;
	private KeyStore keyStore;

	public SslConfiguration() {
	}

	public static SslConfiguration forContainerCert(String instanceCertLocation, String instanceKeyLocation) {
		SslConfiguration sslConfiguration = new SslConfiguration();
		KeyStore keyStore = sslConfiguration.buildKeyStore(instanceCertLocation, instanceKeyLocation);
		sslConfiguration.setKeyStore(keyStore);
		sslConfiguration.setTrustStore(keyStore);
		return sslConfiguration;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	private void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}

	public KeyStore getTrustStore() {
		return trustStore;
	}

	private void setTrustStore(KeyStore keyStore) {
		this.trustStore = keyStore;
	}

	public char[] getKeyPassword() {
		return KEY_PASSWORD;
	}

	private KeyStore buildKeyStore(String instanceCertLocation, String instanceKeyLocation) {
		Certificate cert = parseCertificate(instanceCertLocation);
		PrivateKey key = parsePrivateKey(instanceKeyLocation);
		return createKeyStore(cert, key);
	}

	private Certificate parseCertificate(String certificateLocation) {
		try {
			PEMParser parser = new PEMParser(new FileReader(certificateLocation));
			X509CertificateHolder certHolder = (X509CertificateHolder) parser.readObject();
			JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
			return converter.getCertificate(certHolder);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error parsing and loading certificate from location " + certificateLocation, e);
		}
	}

	private PrivateKey parsePrivateKey(String keyLocation) {
		try {
			PEMParser reader = new PEMParser(new FileReader(keyLocation));
			PEMKeyPair key = (PEMKeyPair) reader.readObject();
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
			return converter.getKeyPair(key).getPrivate();
		} catch (Exception e) {
			throw new IllegalArgumentException("Error parsing and loading private key from location " + keyLocation, e);
		}
	}

	private KeyStore createKeyStore(Certificate cert, PrivateKey key) {
		try {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(null);
			keystore.setCertificateEntry(CERTIFICATE_NAME, cert);
			keystore.setKeyEntry(KEY_NAME, key, KEY_PASSWORD, new Certificate[]{cert});
			return keystore;
		} catch (Exception e) {
			throw new IllegalArgumentException("Error creating keystore ", e);
		}
	}
}
