/**
 * $Id$
 * $URL$
 **************************************************************************
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.user.impl;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;


/**
 * Because DAV clients do not understand the concept of secure sessions, a DAV
 * user will end up asking Sakai to re-authenticate them for every action.
 * To ease the overhead, this class checks a size-limited timing-out cache
 * of one-way encrypted successful authentication IDs and passwords.
 * <p>
 * There's nothing DAV-specific about this class, and it's also independent of
 * any Sakai classes other than the "Authentication" user ID and EID holder.
 * 
 * We salt password so precomputed dictionaries can't be used against a dump of the
 * authentication cache.
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@Slf4j
public class AuthenticationCache {
    @Setter
    private MemoryService memoryService;
    private Cache<String, AuthenticationRecord> authCache = null;
    /**
     * List of algorithms to attempt to use, best ones should come first.
     */
    private final List<String> algorithms = Arrays.asList("SHA2","SHA1");
    private final Random saltGenerator = new Random();
    private final int saltLength = 8;

    public void init() {
        log.info("INIT");
        authCache = memoryService.getCache("org.sakaiproject.user.api.AuthenticationManager");
    }

    public void destroy() {
        if (authCache != null) authCache.close();
    }

	/**
	* The central cache object, should be injected
	*/
	public void setAuthCache(Cache<String, AuthenticationRecord> authCache) {
		this.authCache = authCache;
		if (log.isDebugEnabled() && (authCache != null)) log.debug("authCache ");
	}

	public Authentication getAuthentication(String authenticationId, String password)
			throws AuthenticationException {
		Authentication auth = null;
		AuthenticationRecord record = authCache.get(authenticationId);
		if (record != null) {
			byte[] salt = new byte[saltLength];
			System.arraycopy(record.encodedPassword, 0, salt, 0, salt.length);
			byte[] encodedPassword = getEncrypted(password, salt);
			if (MessageDigest.isEqual(record.encodedPassword, encodedPassword)) {
				if (record.authentication == null) {
                    log.debug("getAuthentication: replaying authentication failure for authenticationId={}", authenticationId);
					throw new AuthenticationException("repeated invalid login");
				} else {
                    log.debug("getAuthentication: returning record for authenticationId={}", authenticationId);
					auth = record.authentication;
				}
			} else {
				// Since the passwords didn't match, we're no longer getting repeats,
				// and so the record should be removed.
                log.debug("getAuthentication: record for authenticationId={} failed password check", authenticationId);
				authCache.remove(authenticationId);
			}
		}
		return auth;
	}

	public void putAuthentication(String authenticationId, String password, Authentication authentication) {
		putAuthenticationRecord(authenticationId, password, authentication);
	}

	public void putAuthenticationFailure(String authenticationId, String password) {
		putAuthenticationRecord(authenticationId, password, null);
	}

	
	public void removeAuthentification(String authentificationId) {
		authCache.remove(authentificationId);
	}
	
	protected void putAuthenticationRecord(String authenticationId, String password,
			Authentication authentication) {
		if (authCache.containsKey(authenticationId)) {
			// Don't indefinitely renew the cached record -- we want to force
			// real authentication after the timeout.
		} else {
			byte[] salt = new byte[saltLength];
			saltGenerator.nextBytes(salt);
			byte[] encrypted = getEncrypted(password, salt);
			authCache.put(authenticationId,
					new AuthenticationRecord(encrypted, authentication) );
		}
	}

	private byte[] getEncrypted(String plaintext, byte[] salt) {
		Exception lastException = null;
		for (String algorithm : algorithms) {
			try {
				MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
				messageDigest.update(salt);
				messageDigest.update(plaintext.getBytes(StandardCharsets.UTF_8));
				byte[] encrypted = messageDigest.digest();
				byte[] saltEncrypted = new byte[salt.length+ encrypted.length];
				System.arraycopy(salt, 0, saltEncrypted, 0, salt.length);
				System.arraycopy(encrypted, 0, saltEncrypted, salt.length, encrypted.length);
				return saltEncrypted;
			} catch (NoSuchAlgorithmException e) {
				lastException = e;
			}
        }
		throw new RuntimeException(lastException);
	}

	static class AuthenticationRecord implements Serializable {

		private static final long serialVersionUID = 1L;

		byte[] encodedPassword;
		Authentication authentication;	// Null for failed authentication

		public AuthenticationRecord(byte[] encodedPassword, Authentication authentication) {
			this.encodedPassword = encodedPassword;
			this.authentication = authentication;
		}
	}

}
