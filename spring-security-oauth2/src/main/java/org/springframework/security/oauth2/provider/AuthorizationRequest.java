package org.springframework.security.oauth2.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

/**
 * Base class representing a request for authorization. There are convenience methods for the well-known properties
 * required by the OAUth2 spec, and a set of generic authorizationParameters to allow for extensions.
 * 
 * 
 * Recommended usage:
 * The authorizationParameters map should contain the original request parameters sent in the HTTP request. 
 * These should not be changed during request processing. Instead, any changes should be saved in the 
 * individual parameters on this class. This way, the original request is preserved.
 * 
 * @author Ryan Heaton
 * @author Dave Syer
 * @author Amanda Anganes
 */
//TODO: change comments on fields to javadoc-style comments
public class AuthorizationRequest extends OAuthRequest {

	private static final long serialVersionUID = 7048428826964752165L;

	//Represents the original, unchanged authorization parameters. Once set this should
	//not be changed.
	//expand, detail - for each param, explain when it is expected to be set, when it might change,
	//and when if at all it is expected to be frozen
	private Map<String, String> authorizationParameters = Collections.unmodifiableMap(new HashMap<String, String>());
	
	//Parameters returned from the approval page are stored here. Once set this should
	//not be changed.
	private Map<String, String> approvalParameters = new HashMap<String, String>();
	
	//The resolved redirect URI. A URI may be present in the original 
	//request, in the authorizationParameters, or it may not be provided in which 
	//case it will be defaulted to the Client's default registered value.
	private String resolvedRedirectUri;
	
	/**
	 * Default constructor. 
	 */
	public AuthorizationRequest() {
		super();
	}
	
	/**
	 * Full constructor.
	 * 
	 * @param authorizationParameters
	 * @param approvalParameters
	 * @param clientId
	 * @param scope
	 * @param resourceIds
	 * @param authorities
	 * @param approved
	 * @param state
	 * @param redirectUri
	 * @param responseTypes
	 */
	public AuthorizationRequest(Map<String, String> authorizationParameters, Map<String, String> approvalParameters, 
			String clientId, Set<String> scope, Set<String> resourceIds, 
			Collection<? extends GrantedAuthority> authorities, boolean approved, String state, 
			String redirectUri, Set<String> responseTypes){
		if (authorizationParameters != null) {
			//this.authorizationParameters.putAll(authorizationParameters);
			this.authorizationParameters = Collections.unmodifiableMap(authorizationParameters);
		}
		if (approvalParameters != null) {
			this.approvalParameters.putAll(approvalParameters);
		}
		if (resourceIds != null) {
			this.resourceIds = new HashSet<String>(resourceIds);
		}
		if (scope != null) {
			this.scope = new LinkedHashSet<String>(scope);
		}
		if (authorities != null) {
			this.authorities = new HashSet<GrantedAuthority>(authorities);
		}
		if (responseTypes != null) {
			this.responseTypes = responseTypes;
		}
		this.resolvedRedirectUri = redirectUri;
		this.state = state;
		this.clientId = clientId;
		this.approved = approved;
	}
	
	/**
	 * Convenience constructor for unit tests, where client ID and scope are often
	 * the only needed fields.
	 * 
	 * @param clientId
	 * @param scopes
	 */
	public AuthorizationRequest(String clientId, Collection<String> scopes) {
		this.clientId = clientId;
		if (scopes!= null) {
			this.scope.addAll(scopes);
		}
	}
	
	public Map<String, String> getAuthorizationParameters() {
		return authorizationParameters;
	}

	public void setAuthorizationParameters(
			Map<String, String> authorizationParameters) {
		this.authorizationParameters = authorizationParameters;
	}

	public Map<String, String> getApprovalParameters() {
		return approvalParameters;
	}

	public void setApprovalParameters(Map<String, String> approvalParameters) {
		this.approvalParameters = approvalParameters;
	}

	public String getRedirectUri() {
		return resolvedRedirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.resolvedRedirectUri = redirectUri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((approvalParameters == null) ? 0 : approvalParameters
						.hashCode());
		result = prime * result + (approved ? 1231 : 1237);
		result = prime * result
				+ ((authorities == null) ? 0 : authorities.hashCode());
		result = prime
				* result
				+ ((authorizationParameters == null) ? 0
						: authorizationParameters.hashCode());
		result = prime * result
				+ ((clientId == null) ? 0 : clientId.hashCode());
		result = prime
				* result
				+ ((resolvedRedirectUri == null) ? 0 : resolvedRedirectUri
						.hashCode());
		result = prime * result
				+ ((resourceIds == null) ? 0 : resourceIds.hashCode());
		result = prime * result
				+ ((responseTypes == null) ? 0 : responseTypes.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AuthorizationRequest)) {
			return false;
		}
		AuthorizationRequest other = (AuthorizationRequest) obj;
		if (approvalParameters == null) {
			if (other.approvalParameters != null) {
				return false;
			}
		} else if (!approvalParameters.equals(other.approvalParameters)) {
			return false;
		}
		if (approved != other.approved) {
			return false;
		}
		if (authorities == null) {
			if (other.authorities != null) {
				return false;
			}
		} else if (!authorities.equals(other.authorities)) {
			return false;
		}
		if (authorizationParameters == null) {
			if (other.authorizationParameters != null) {
				return false;
			}
		} else if (!authorizationParameters
				.equals(other.authorizationParameters)) {
			return false;
		}
		if (clientId == null) {
			if (other.clientId != null) {
				return false;
			}
		} else if (!clientId.equals(other.clientId)) {
			return false;
		}
		if (resolvedRedirectUri == null) {
			if (other.resolvedRedirectUri != null) {
				return false;
			}
		} else if (!resolvedRedirectUri.equals(other.resolvedRedirectUri)) {
			return false;
		}
		if (resourceIds == null) {
			if (other.resourceIds != null) {
				return false;
			}
		} else if (!resourceIds.equals(other.resourceIds)) {
			return false;
		}
		if (responseTypes == null) {
			if (other.responseTypes != null) {
				return false;
			}
		} else if (!responseTypes.equals(other.responseTypes)) {
			return false;
		}
		if (scope == null) {
			if (other.scope != null) {
				return false;
			}
		} else if (!scope.equals(other.scope)) {
			return false;
		}
		if (state == null) {
			if (other.state != null) {
				return false;
			}
		} else if (!state.equals(other.state)) {
			return false;
		}
		return true;
	}

}