package com.hfhk.auth.domain.mongo;

import com.hfhk.cairo.mongo.data.Metadata;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractMongoField;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractUpperCamelCaseField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;


/**
 * 用户
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMongo implements Serializable {

	/**
	 * 标识
	 */
	private String id;

	/**
	 * uid
	 */
	private String uid;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 邮箱号码
	 */
	private String email;

	/**
	 * 手机号码
	 */
	private String phoneNumber;

	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 账号 启用
	 */
	@Builder.Default
	private Boolean accountEnabled = false;

	/**
	 * 账号 锁定
	 */
	@Builder.Default
	private Boolean accountLocked = false;

	/**
	 * 角色
	 */
	private Map<String, Set<String>> clientRoles;

	/**
	 * 部门
	 */
	private Map<String, Set<String>> clientDepartments;

	/**
	 * 资源
	 */
	private Map<String, Set<String>> clientResources;

	/**
	 * 额外权限
	 */
	private Map<String, Set<String>> clientAuthorities;

	/**
	 * 元信息
	 */
	@Builder.Default
	private Metadata metadata = new Metadata();

	public static final Field FIELD = new Field();

	public static class Field extends AbstractUpperCamelCaseField {
		private Field(){

		}
		public final String UID = field("Uid");
		public final String NAME = field("Name");
		public final String USERNAME = field("Username");
		public final String PASSWORD = field("Password");
		public final String EMAIL = field("Email");
		public final String PHONE_NUMBER = field("PhoneNumber");

		public final String AVATAR_URL = field("AvatarUrl");

		public final String ACCOUNT_ENABLED = field("AccountEnabled");
		public final String ACCOUNT_LOCKED = field("AccountLocked");

		public final String CLIENT_ROLES = field("ClientRoles");
		public final String CLIENT_DEPARTMENTS = field("ClientDepartments");
		public final String CLIENT_RESOURCES = field("ClientResources");
	}
}