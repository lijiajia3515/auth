package com.lijiajia3515.cairo.auth.server.framework.security.oauth2.core.http.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

public class WechatWebOAuth2UserInfoResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

	private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

	private Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter = new WechatAppOAuth2AuthorizationCodeGrantRequestEntityConverter();

	private RestOperations restOperations;

	public WechatWebOAuth2UserInfoResponseClient() {
		OAuth2AccessTokenResponseHttpMessageConverter oAuth2AccessTokenResponseHttpMessageConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
		oAuth2AccessTokenResponseHttpMessageConverter.setTokenResponseConverter(new WechatWebMapOAuth2AccessTokenResponseConverter());
		oAuth2AccessTokenResponseHttpMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_PLAIN));
		RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), oAuth2AccessTokenResponseHttpMessageConverter));
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
		this.restOperations = restTemplate;
	}

	@Override
	public OAuth2AccessTokenResponse getTokenResponse(
		OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
		Assert.notNull(authorizationCodeGrantRequest, "authorizationCodeGrantRequest cannot be null");
		RequestEntity<?> request = this.requestEntityConverter.convert(authorizationCodeGrantRequest);
		OAuth2AccessTokenResponse tokenResponse = getResponse(request).getBody();
		if (CollectionUtils.isEmpty(tokenResponse.getAccessToken().getScopes())) {
			// As per spec, in Section 5.1 Successful Access Token Response
			// https://tools.ietf.org/html/rfc6749#section-5.1
			// If AccessTokenResponse.scope is empty, then default to the scope
			// originally requested by the client in the Token Request
			tokenResponse = OAuth2AccessTokenResponse.withResponse(tokenResponse)
				.scopes(authorizationCodeGrantRequest.getClientRegistration().getScopes())
				.build();
		}
		return tokenResponse;
	}

	private ResponseEntity<OAuth2AccessTokenResponse> getResponse(RequestEntity<?> request) {
		try {
			return this.restOperations.exchange(request, OAuth2AccessTokenResponse.class);
//			ResponseEntity<Map<String, Object>> exchange = restOperations.exchange(request, new ParameterizedTypeReference<>() {
//			});
//			Map<String, Object> body = Optional.ofNullable(exchange.getBody()).orElse(Collections.emptyMap());
//			return OAuth2AccessTokenResponse.withToken(body.getOrDefault("access_token", Constants.SNOWFLAKE.nextIdStr()).toString())
//				.tokenType(OAuth2AccessToken.TokenType.BEARER)
//				.refreshToken(body.getOrDefault("refresh_token", Constants.SNOWFLAKE.nextIdStr()).toString())
//				.expiresIn(Long.parseLong(body.getOrDefault("expires_in", "0").toString()))
//				.scopes(Collections.singleton(body.getOrDefault("scope", "snsapi_login").toString()))
//				.additionalParameters(body)
//				.build();

		} catch (RestClientException ex) {
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
				"An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: "
					+ ex.getMessage(),
				null);
			throw new OAuth2AuthorizationException(oauth2Error, ex);
		}
	}

	/**
	 * Sets the {@link Converter} used for converting the
	 * {@link OAuth2AuthorizationCodeGrantRequest} to a {@link RequestEntity}
	 * representation of the OAuth 2.0 Access Token Request.
	 *
	 * @param requestEntityConverter the {@link Converter} used for converting to a
	 *                               {@link RequestEntity} representation of the Access Token Request
	 */
	public void setRequestEntityConverter(
		Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter) {
		Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
		this.requestEntityConverter = requestEntityConverter;
	}

	/**
	 * Sets the {@link RestOperations} used when requesting the OAuth 2.0 Access Token
	 * Response.
	 *
	 * <p>
	 * <b>NOTE:</b> At a minimum, the supplied {@code restOperations} must be configured
	 * with the following:
	 * <ol>
	 * <li>{@link HttpMessageConverter}'s - {@link FormHttpMessageConverter} and
	 * {@link OAuth2AccessTokenResponseHttpMessageConverter}</li>
	 * <li>{@link ResponseErrorHandler} - {@link OAuth2ErrorResponseErrorHandler}</li>
	 * </ol>
	 *
	 * @param restOperations the {@link RestOperations} used when requesting the Access
	 *                       Token Response
	 */
	public void setRestOperations(RestOperations restOperations) {
		Assert.notNull(restOperations, "restOperations cannot be null");
		this.restOperations = restOperations;
	}
}
