/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.security.oauth2.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.oauth2.provider.AuthorizationRequest.REDIRECT_URI;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.util.StringUtils;

/**
 * @author Dave Syer
 * @author Christian Hilmersson
 *
 */
public class TestAuthorizationRequest {

	private Map<String, String> parameters;

	@Before
	public void prepare() {
		parameters = new HashMap<String, String>();
		parameters.put("client_id", "theClient");
		parameters.put("state", "XYZ123");
		parameters.put("redirect_uri", "http://www.callistaenterprise.se");
	}
	
	@Test
	public void testApproval() throws Exception {
		AuthorizationRequest authorizationRequest = new AuthorizationRequest(parameters);
		assertFalse(authorizationRequest.isApproved());
		authorizationRequest.setApproved(true);
		assertTrue(authorizationRequest.isApproved());
	}

	/**
	 * Ensure that setting the scope does not alter the original request parameters.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testScopeNotSetInParameters() throws Exception {
		parameters.put("scope", "read,write");
		AuthorizationRequest authorizationRequest = new AuthorizationRequest(parameters);
		authorizationRequest.setScope(StringUtils.commaDelimitedListToSet("foo,bar"));
		assertFalse(authorizationRequest.getAuthorizationParameters().get(AuthorizationRequest.SCOPE).contains("bar"));
		assertFalse(authorizationRequest.getAuthorizationParameters().get(AuthorizationRequest.SCOPE).contains("foo"));
	}

	@Test
	public void testClientIdNotOverwitten() throws Exception {
		AuthorizationRequest authorizationRequest = new AuthorizationRequest("client", Arrays.asList("read"));
		parameters = new HashMap<String, String>();
		parameters.put("scope", "write");
		authorizationRequest.setAuthorizationParameters(parameters);
		
		assertEquals("client", authorizationRequest.getClientId());
		assertEquals(1, authorizationRequest.getScope().size());
		assertTrue(authorizationRequest.getScope().contains("read"));
		assertFalse(authorizationRequest.getAuthorizationParameters().get(AuthorizationRequest.SCOPE).contains("read"));
	}

	@Test
	public void testScopeWithSpace() throws Exception {
		parameters.put("scope", "bar foo");
		AuthorizationRequest authorizationRequest = new AuthorizationRequest(parameters);
		authorizationRequest.setScope(Collections.singleton("foo bar"));
		assertEquals("bar foo", authorizationRequest.getAuthorizationParameters().get(AuthorizationRequest.SCOPE));
	}

	/**
	 * Tests that the construction of an AuthorizationRequest objects using
	 * a parameter Map maintains a sorted order of the scope.
	 */
	@Test
	public void testScopeSortedOrder() {
		// Arbitrary scope set
		String scopeString = "AUTHORITY_A AUTHORITY_X AUTHORITY_B AUTHORITY_C AUTHORITY_D " +
				"AUTHORITY_Y AUTHORITY_V AUTHORITY_ZZ AUTHORITY_DYV AUTHORITY_ABC AUTHORITY_BA " +
				"AUTHORITY_AV AUTHORITY_AB AUTHORITY_CDA AUTHORITY_ABCD";
		// Create correctly sorted scope string
		Set<String> sortedSet = OAuth2Utils.parseParameterList(scopeString);
		Assert.assertTrue(sortedSet instanceof SortedSet);
		String sortedScopeString = OAuth2Utils.formatParameterList(sortedSet);

		parameters.put("scope", scopeString);
		AuthorizationRequest authorizationRequest = new AuthorizationRequest(parameters);
		authorizationRequest.setScope(sortedSet);
				
		// Assert that the scope parameter is still sorted
		
		String fromAR = OAuth2Utils.formatParameterList(authorizationRequest.getScope());
		
		Assert.assertEquals(sortedScopeString, fromAR);
	}	

	@Test
	public void testRedirectUriDefaultsToMap() {
		parameters.put("scope", "one two");
		AuthorizationRequest authorizationRequest = new AuthorizationRequest(parameters);

		assertEquals("XYZ123", authorizationRequest.getState());
		assertEquals("theClient", authorizationRequest.getClientId());
		assertEquals("http://www.callistaenterprise.se", authorizationRequest.getRedirectUri());
		assertEquals("http://www.callistaenterprise.se", authorizationRequest.getAuthorizationParameters().get(REDIRECT_URI));
		assertEquals("[one, two]", authorizationRequest.getScope().toString());
	}

}
