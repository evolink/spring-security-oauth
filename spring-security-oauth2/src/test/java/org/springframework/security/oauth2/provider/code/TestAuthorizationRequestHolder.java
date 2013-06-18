/*
 * Cloud Foundry 2012.02.03 Beta
 * Copyright (c) [2009-2012] VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product includes a number of subcomponents with
 * separate copyright notices and license terms. Your use of these
 * subcomponents is subject to the terms and conditions of the
 * subcomponent's license, as noted in the LICENSE file.
 */

package org.springframework.security.oauth2.provider.code;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Request;

/**
 * @author Dave Syer
 * 
 */
//TOOD: Test serialization of OAuth2Authentication
public class TestAuthorizationRequestHolder {

	//private OAuth2Authentication holder = new AuthorizationRequestHolder(new OAuth2Request(
		//	"client", Arrays.asList("read")), new UsernamePasswordAuthenticationToken("user", "pwd"));

	//@Test
	public void test() {
		//AuthorizationRequestHolder other = SerializationUtils.deserialize(SerializationUtils.serialize(holder));
		//assertEquals(holder, other);
	}

}
