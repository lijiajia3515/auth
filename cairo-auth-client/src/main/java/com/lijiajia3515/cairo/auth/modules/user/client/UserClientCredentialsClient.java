package com.lijiajia3515.cairo.auth.modules.user.client;

import com.lijiajia3515.cairo.auth.modules.user.User;
import com.lijiajia3515.cairo.auth.modules.user.UserFindParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@FeignClient(name = "service-auth-v1", path = "/User", contextId = "serviceAuthV1-user-clientCredential-client")
public interface UserClientCredentialsClient {

	/**
	 * find
	 *
	 * @param uids uids
	 * @return find map
	 */
	default Map<String, User> findMap(Collection<String> uids) {
		return find(UserFindParam.builder().uids(uids).build()).stream()
			.collect(Collectors.toMap(User::getUid, x -> x));
	}

	/**
	 * find
	 *
	 * @param param param
	 * @return user
	 */
	@PostMapping("/Find")
	List<User> find(@RequestBody UserFindParam param);

	/**
	 * find
	 *
	 * @param uid uid
	 * @return user
	 */
	@GetMapping(path = "/Find/{uid}")
	User findById(@PathVariable(name = "uid") String uid);


}
