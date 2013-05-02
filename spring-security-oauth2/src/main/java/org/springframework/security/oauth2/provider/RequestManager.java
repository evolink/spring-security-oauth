package org.springframework.security.oauth2.provider;

import java.util.Map;

public class RequestManager {

	private ClientDetailsService clientDetailsService;
	
	public RequestManager(ClientDetailsService clientService) {
		this.clientDetailsService = clientService;
	}
	
	public OAuthRequest createAuthorizationRequest(Map<String, String> request) {
		return new AuthorizationRequest();
	}
	
	public TokenRequest createTokenRequest(Map<String, String> request) {
		return new TokenRequest();
	}
	
	public void validateAuthorizationParameters(Map<String, String> parameters, ClientDetails client) {
		
	}
	
}
