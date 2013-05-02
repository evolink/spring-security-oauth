package org.springframework.security.oauth2.provider;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.util.OAuth2Utils;

public class OAuthRequest implements Serializable {

	private static final long serialVersionUID = 6692301464623283936L;
	
	public static final String CLIENT_ID = "client_id";
	public static final String STATE = "state";
	public static final String SCOPE = "scope";
	public static final String REDIRECT_URI = "redirect_uri";
	public static final String RESPONSE_TYPE = "response_type";
	public static final String USER_OAUTH_APPROVAL = "user_oauth_approval";
	protected String clientId;
	protected Map<String, String> parameters;
	protected Set<String> scope = new HashSet<String>();
	protected Set<String> resourceIds = new HashSet<String>();
	protected Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
	protected boolean approved = false;
	protected String state;
	protected Set<String> responseTypes = new HashSet<String>();
	private Map<String, Serializable> extensionProperties = new HashMap<String, Serializable>();

	public OAuthRequest() {
		super();
	}

	/**
	 * Convenience method to set resourceIds and authorities on this request by
	 * inheriting from a ClientDetails object.
	 * 
	 * @param clientDetails
	 */
	public void setResourceIdsAndAuthoritiesFromClientDetails(ClientDetails clientDetails) {
		resourceIds.addAll(clientDetails.getResourceIds());
		authorities.addAll(clientDetails.getAuthorities());
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Set<String> getScope() {
		return scope;
	}

	public void setScope(Set<String> scope) {
		if (scope != null && scope.size() == 1) {
			String value = scope.iterator().next();
			/*
			 * This is really an error, but it can catch out unsuspecting users and it's easy to fix. It happens when an
			 * AuthorizationRequest gets bound accidentally from request parameters using @ModelAttribute.
			 */
			if (value.contains(" ") || scope.contains(",")) {
				scope = OAuth2Utils.parseParameterList(value);
			}
		}
		this.scope = scope == null ? new LinkedHashSet<String>() : new LinkedHashSet<String>(scope);
	}

	public Set<String> getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(Set<String> resourceIds) {
		this.resourceIds = resourceIds;
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		if (authorities!= null) {
			this.authorities = new HashSet<GrantedAuthority>(authorities);
		}
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Set<String> getResponseTypes() {
		return responseTypes;
	}

	public void setResponseTypes(Set<String> responseTypes) {
		this.responseTypes = responseTypes;
	}

	/**
	 * @return the extensionProperties
	 */
	public Map<String, Serializable> getExtensionProperties() {
		return extensionProperties;
	}

	/**
	 * @param extensionProperties the extensionProperties to set
	 */
	public void setExtensionProperties(Map<String, Serializable> extensionProperties) {
		this.extensionProperties = extensionProperties;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

}