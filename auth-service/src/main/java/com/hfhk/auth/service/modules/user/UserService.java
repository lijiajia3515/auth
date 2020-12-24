package com.hfhk.auth.service.modules.user;

import com.hfhk.auth.domain.user.*;
import com.hfhk.cairo.core.page.Page;

import java.util.Optional;

/**
 * 用户 服务
 */
public interface UserService {

	/**
	 * 注册
	 *
	 * @param request 请求
	 */
	void reg(UserRegRequest request);

	/**
	 * 修改
	 *
	 * @param request request
	 * @return user
	 */
	Optional<User> modify(String client, UserModifyRequest request);

	/**
	 * 密码重置
	 *
	 * @param request request
	 * @return 重置后的密码
	 */
	String passwordReset(UserResetPasswordRequest request);

	/**
	 * find
	 *
	 * @return user page
	 */
	Page<User> find(String client, UserPageFindRequest request);

	/**
	 * find by id
	 *
	 * @param client client
	 * @param uid    uid
	 * @return user
	 */
	User findById(String client, String uid);
}
