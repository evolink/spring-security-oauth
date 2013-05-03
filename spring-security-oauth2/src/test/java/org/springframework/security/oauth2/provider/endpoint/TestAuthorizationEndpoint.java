/*
 * Copyright 2006-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.security.oauth2.provider.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.oauth2.provider.OAuth2Request.REDIRECT_URI;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.InvalidScopeException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.BaseClientDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationRequestHolder;
import org.springframework.web.bind.support.SimpleSessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Dave Syer
 * 
 */
public class TestAuthorizationEndpoint {

	private AuthorizationEndpoint endpoint = new AuthorizationEndpoint();

	private HashMap<String, Object> model = new HashMap<String, Object>();

	private SimpleSessionStatus sessionStatus = new SimpleSessionStatus();

	private UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken("foo", "bar",
			Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));

	private BaseClientDetails client;

	private OAuth2Request getOAuth2Request(String clientId, String redirectUri, String state,
			String scope, Set<String> responseTypes) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		parameters.put(OAuth2Request.CLIENT_ID, clientId);
		if (redirectUri != null) {
			parameters.put(OAuth2Request.REDIRECT_URI, redirectUri);
		}
		if (state != null) {
			parameters.put(OAuth2Request.STATE, state);
		}
		if (scope != null) {
			parameters.put(OAuth2Request.SCOPE, scope);
		}
		if (responseTypes != null) {
			parameters.put(OAuth2Request.RESPONSE_TYPE, OAuth2Utils.formatParameterList(responseTypes));
		}
		return new OAuth2Request(parameters, Collections.<String, String> emptyMap(), 
				parameters.get(OAuth2Request.CLIENT_ID), 
				OAuth2Utils.parseParameterList(parameters.get(OAuth2Request.SCOPE)), null,
				null, false, parameters.get(OAuth2Request.STATE), 
				parameters.get(OAuth2Request.REDIRECT_URI), 
				OAuth2Utils.parseParameterList(parameters.get(OAuth2Request.RESPONSE_TYPE)));
	}

	@Before
	public void init() throws Exception {
		client = new BaseClientDetails();
		client.setRegisteredRedirectUri(Collections.singleton("http://anywhere.com"));
		client.setAuthorizedGrantTypes(Arrays.asList("authorization_code", "implicit"));
		endpoint.setClientDetailsService(new ClientDetailsService() {
			public ClientDetails loadClientByClientId(String clientId) throws OAuth2Exception {
				return client;
			}
		});
		endpoint.setTokenGranter(new TokenGranter() {
			public OAuth2AccessToken grant(String grantType, OAuth2Request oAuth2Request) {
				return null;
			}
		});
		endpoint.setRedirectResolver(new DefaultRedirectResolver());
		endpoint.afterPropertiesSet();
	}

	@Test(expected = IllegalStateException.class)
	public void testMandatoryProperties() throws Exception {
		endpoint = new AuthorizationEndpoint();
		endpoint.afterPropertiesSet();
	}

	@Test
	public void testStartAuthorizationCodeFlow() throws Exception {
		
		ModelAndView result = endpoint.authorize(model, getOAuth2Request("foo", null, null, null, Collections.singleton("code"))
				.getAuthorizationParameters(), sessionStatus, principal);
		assertEquals("forward:/oauth/confirm_access", result.getViewName());
	}

	@Test(expected = OAuth2Exception.class)
	public void testStartAuthorizationCodeFlowForClientCredentialsFails() throws Exception {
		client.setAuthorizedGrantTypes(Collections.singleton("client_credentials"));
		ModelAndView result = endpoint.authorize(model, getOAuth2Request("foo", null, null, null, Collections.singleton("code"))
				.getAuthorizationParameters(), sessionStatus, principal);
		assertEquals("forward:/oauth/confirm_access", result.getViewName());
	}

	@Test
	public void testAuthorizationCodeWithFragment() throws Exception {
		endpoint.setAuthorizationCodeServices(new StubAuthorizationCodeServices());
		model.put("authorizationRequest", getOAuth2Request("foo", "http://anywhere.com#bar", null, null, Collections.singleton("code")));
		View result = endpoint.approveOrDeny(
				Collections.singletonMap(OAuth2Request.USER_OAUTH_APPROVAL, "true"), model, sessionStatus,
				principal);
		assertEquals("http://anywhere.com?code=thecode#bar", ((RedirectView) result).getUrl());
	}

	@Test
	public void testAuthorizationCodeError() throws Exception {
		endpoint.setUserApprovalHandler(new UserApprovalHandler() {
			public OAuth2Request checkForPreApproval(OAuth2Request oAuth2Request, Authentication userAuthentication) {
				return oAuth2Request;
			}
			
			public OAuth2Request updateAfterApproval(OAuth2Request oAuth2Request,
					Authentication userAuthentication) {
				return oAuth2Request;
			}

			public boolean isApproved(OAuth2Request oAuth2Request, Authentication userAuthentication) {
				return true;
			}
		});
		endpoint.setAuthorizationCodeServices(new StubAuthorizationCodeServices() {
			@Override
			public String createAuthorizationCode(AuthorizationRequestHolder authentication) {
				throw new InvalidScopeException("FOO");
			}
		});
		ModelAndView result = endpoint.authorize(model,
				getOAuth2Request("foo", "http://anywhere.com", "mystate", "myscope", Collections.singleton("code"))
						.getAuthorizationParameters(), sessionStatus, principal);
		String url = ((RedirectView) result.getView()).getUrl();
		assertTrue("Wrong view: " + result, url.startsWith("http://anywhere.com"));
		assertTrue("No error: " + result, url.contains("?error="));
		assertTrue("Wrong state: " + result, url.contains("&state=mystate"));

	}

	@Test
	public void testAuthorizationCodeWithMultipleResponseTypes() throws Exception {
		Set<String> responseTypes = new HashSet<String>();
		responseTypes.add("code");
		responseTypes.add("other");
		ModelAndView result = endpoint.authorize(model, getOAuth2Request("foo", null, null, null, responseTypes)
				.getAuthorizationParameters(), sessionStatus, principal);
		assertEquals("forward:/oauth/confirm_access", result.getViewName());
	}

	@Test
	public void testImplicitPreApproved() throws Exception {
		endpoint.setTokenGranter(new TokenGranter() {
			public OAuth2AccessToken grant(String grantType, OAuth2Request oAuth2Request) {
				return new DefaultOAuth2AccessToken("FOO");
			}
		});
		endpoint.setUserApprovalHandler(new UserApprovalHandler() {
			public OAuth2Request checkForPreApproval(OAuth2Request oAuth2Request, Authentication userAuthentication) {
				return oAuth2Request;
			}
			
			public OAuth2Request updateAfterApproval(OAuth2Request oAuth2Request,
					Authentication userAuthentication) {
				return oAuth2Request;
			}

			public boolean isApproved(OAuth2Request oAuth2Request, Authentication userAuthentication) {
				return true;
			}
		});
		OAuth2Request oAuth2Request = getOAuth2Request("foo", "http://anywhere.com", "mystate",
				"myscope", Collections.singleton("token"));
		ModelAndView result = endpoint.authorize(model, oAuth2Request.getAuthorizationParameters(),
				sessionStatus, principal);
		String url = ((RedirectView) result.getView()).getUrl();
		assertTrue("Wrong view: " + result, url.startsWith("http://anywhere.com"));
		assertTrue("Wrong state: " + result, url.contains("&state=mystate"));
	}

	@Test(expected = InvalidScopeException.class)
	public void testImplicitPreApprovedButInvalid() throws Exception {
		endpoint.setTokenGranter(new TokenGranter() {
			public OAuth2AccessToken grant(String grantType, OAuth2Request oAuth2Request) {
				throw new IllegalStateException("Shouldn't be called");
			}
		});
		endpoint.setUserApprovalHandler(new UserApprovalHandler() {
			public OAuth2Request checkForPreApproval(OAuth2Request oAuth2Request, Authentication userAuthentication) {
				return oAuth2Request;
			}
			
			public OAuth2Request updateAfterApproval(OAuth2Request oAuth2Request,
					Authentication userAuthentication) {
				return oAuth2Request;
			}

			public boolean isApproved(OAuth2Request oAuth2Request, Authentication userAuthentication) {
				return true;
			}
		});
		client.setScope(Collections.singleton("smallscope"));
		OAuth2Request oAuth2Request = getOAuth2Request("foo", "http://anywhere.com", "mystate",
				"bigscope", Collections.singleton("token"));
		ModelAndView result = endpoint.authorize(model, oAuth2Request.getAuthorizationParameters(),
				sessionStatus, principal);
		String url = ((RedirectView) result.getView()).getUrl();
		assertTrue("Wrong view: " + result, url.startsWith("http://anywhere.com"));
	}

	@Test
	public void testImplicitUnapproved() throws Exception {
		endpoint.setTokenGranter(new TokenGranter() {
			public OAuth2AccessToken grant(String grantType, OAuth2Request oAuth2Request) {
				return null;
			}
		});
		OAuth2Request oAuth2Request = getOAuth2Request("foo", "http://anywhere.com", "mystate",
				"myscope", Collections.singleton("token"));
		ModelAndView result = endpoint.authorize(model, oAuth2Request.getAuthorizationParameters(),
				sessionStatus, principal);
		assertEquals("forward:/oauth/confirm_access", result.getViewName());
	}

	@Test
	public void testImplicitError() throws Exception {
		endpoint.setUserApprovalHandler(new UserApprovalHandler() {
			public OAuth2Request checkForPreApproval(OAuth2Request oAuth2Request, Authentication userAuthentication) {
				return oAuth2Request;
			}
			
			public OAuth2Request updateAfterApproval(OAuth2Request oAuth2Request,
					Authentication userAuthentication) {
				return oAuth2Request;
			}

			public boolean isApproved(OAuth2Request oAuth2Request, Authentication userAuthentication) {
				return true;
			}
		});
		endpoint.setTokenGranter(new TokenGranter() {
			public OAuth2AccessToken grant(String grantType, OAuth2Request oAuth2Request) {
				return null;
			}
		});
		OAuth2Request oAuth2Request = getOAuth2Request("foo", "http://anywhere.com", "mystate",
				"myscope", Collections.singleton("token"));
		ModelAndView result = endpoint.authorize(model, oAuth2Request.getAuthorizationParameters(),
				sessionStatus, principal);

		String url = ((RedirectView) result.getView()).getUrl();
		assertTrue("Wrong view: " + result, url.startsWith("http://anywhere.com"));
		assertTrue("No error: " + result, url.contains("#error="));
		assertTrue("Wrong state: " + result, url.contains("&state=mystate"));

	}

	@Test
	public void testApproveOrDeny() throws Exception {
		OAuth2Request request = getOAuth2Request("foo", "http://anywhere.com", null, null, Collections.singleton("code"));
		request.setApproved(true);
		Map<String, String> approvalParameters = new HashMap<String, String>();
		approvalParameters.put("user_oauth_approval", "true");
		model.put("authorizationRequest", request);
		View result = endpoint.approveOrDeny(approvalParameters, model, sessionStatus, principal);
		assertTrue("Wrong view: " + result, ((RedirectView) result).getUrl().startsWith("http://anywhere.com"));
	}

	@Test
	public void testApprovalDenied() throws Exception {
		model.put("authorizationRequest", getOAuth2Request("foo", "http://anywhere.com", null, null, Collections.singleton("code")));
		Map<String, String> approvalParameters = new HashMap<String, String>();
		approvalParameters.put("user_oauth_approval", "false");
		View result = endpoint.approveOrDeny(approvalParameters, model, sessionStatus, principal);
		String url = ((RedirectView) result).getUrl();
		assertTrue("Wrong view: " + result, url.startsWith("http://anywhere.com"));
		assertTrue("Wrong view: " + result, url.contains("error=access_denied"));
	}

	@Test
	public void testDirectApproval() throws Exception {
		ModelAndView result = endpoint.authorize(model,
				getOAuth2Request("foo", "http://anywhere.com", null, null, Collections.singleton("code")).getAuthorizationParameters(),
				sessionStatus, principal);
		// Should go to approval page (SECOAUTH-191)
		assertFalse(result.getView() instanceof RedirectView);
	}

	@Test
	public void testRedirectUriOptionalForAuthorization() throws Exception {
		ModelAndView result = endpoint.authorize(model,  getOAuth2Request("foo", null, null, null, Collections.singleton("code"))
				.getAuthorizationParameters(), sessionStatus, principal);
		// RedirectUri parameter should be null (SECOAUTH-333), however the resolvedRedirectUri not
		OAuth2Request oAuth2Request = (OAuth2Request) result.getModelMap().get(
				"authorizationRequest");
		assertNull(oAuth2Request.getAuthorizationParameters().get(REDIRECT_URI));
		assertEquals("http://anywhere.com", oAuth2Request.getRedirectUri());
	}

	/**
	 * Ensure that if the approval endpoint is called without a resolved redirect URI, the request fails.
	 * @throws Exception
	 */
	@Test(expected = InvalidRequestException.class)
	public void testApproveOrDenyWithAuthorizationRequestWithoutRedirectUri() throws Exception {
		OAuth2Request request = getOAuth2Request("foo", null, null, null, Collections.singleton("code"));
		request.setApproved(true);
		Map<String, String> approvalParameters = new HashMap<String, String>();
		approvalParameters.put("user_oauth_approval", "true");
		model.put("authorizationRequest", request);
		endpoint.approveOrDeny(approvalParameters, model, sessionStatus, principal);

	}

	private class StubAuthorizationCodeServices implements AuthorizationCodeServices {
		private AuthorizationRequestHolder authentication;

		public String createAuthorizationCode(AuthorizationRequestHolder authentication) {
			this.authentication = authentication;
			return "thecode";
		}

		public AuthorizationRequestHolder consumeAuthorizationCode(String code) throws InvalidGrantException {
			return authentication;
		}
	}

}
