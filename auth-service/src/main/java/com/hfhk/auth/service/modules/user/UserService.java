package com.hfhk.auth.service.modules.user;

import cn.hutool.core.util.IdUtil;
import com.hfhk.auth.domain.mongo.DepartmentMongo;
import com.hfhk.auth.domain.mongo.Mongo;
import com.hfhk.auth.domain.mongo.RoleMongo;
import com.hfhk.auth.domain.mongo.UserMongo;
import com.hfhk.auth.domain.user.*;
import com.hfhk.cairo.core.page.Page;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户 服务
 */
@Slf4j
@Service
public class UserService {
	private static final String DEFAULT_PASSWORD = "123456";

	private final MongoTemplate mongoTemplate;


	private final PasswordEncoder passwordEncoder;

	public UserService(
		MongoTemplate mongoTemplate,
		PasswordEncoder passwordEncoder) {
		this.mongoTemplate = mongoTemplate;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * 注册
	 *
	 * @param param 请求
	 */
	@Transactional(rollbackFor = Exception.class)
	public void reg(UserRegParam param) {
		UserMongo data = UserMongo.builder()
			.uid(UUID.randomUUID().toString())
			.name(Strings.isEmpty(param.getName()) ? param.getName() : param.getUsername())
			.username(param.getUsername())
			.phoneNumber(param.getPhoneNumber())
			.email(param.getEmail())
			.password(passwordEncoder.encode(Optional.ofNullable(param.getPassword()).orElse(DEFAULT_PASSWORD)))
			.accountEnabled(true)
			.accountLocked(false)
			.clientDepartments(Collections.emptyMap())
			.clientRoles(Collections.emptyMap())
			.clientResources(Collections.emptyMap())
			.build();
		UserMongo insert = mongoTemplate.insert(data, Mongo.Collection.USER);
		log.debug("[user][reg] result -> {} ", insert);
	}

	/**
	 * 修改
	 *
	 * @param request request
	 * @return user
	 */
	@Transactional(rollbackFor = Exception.class)
	public Optional<User> modify(String client, UserModifyParam request) {
		Query query = Query.query(Criteria.where(UserMongo.FIELD.UID).is(request.getUid()));
		Update update = new Update();
		Optional.ofNullable(request.getUsername()).filter(x -> !x.isBlank()).ifPresent(x -> update.set(UserMongo.FIELD.USERNAME, x));
		Optional.ofNullable(request.getPhoneNumber()).filter(x -> !x.isBlank()).ifPresent(x -> update.set(UserMongo.FIELD.PHONE_NUMBER, x));
		Optional.ofNullable(request.getEmail()).filter(x -> !x.isBlank()).ifPresent(x -> update.set(UserMongo.FIELD.EMAIL, x));
		Optional.ofNullable(request.getName()).filter(x -> !x.isBlank()).ifPresent(x -> update.set(UserMongo.FIELD.NAME, x));

		Optional.ofNullable(request.getRoleCodes()).ifPresent(x -> update.set(UserMongo.FIELD.CLIENT_ROLES.client(client), x));
		Optional.ofNullable(request.getDepartmentIds()).ifPresent(x -> update.set(UserMongo.FIELD.CLIENT_DEPARTMENTS.client(client), x));
		Optional.ofNullable(request.getResourceIds()).ifPresent(x -> update.set(UserMongo.FIELD.CLIENT_RESOURCES.client(client), x));

		UpdateResult updateResult = mongoTemplate.updateFirst(query, update, UserMongo.class);
		log.debug("[user][update] result -> {}", updateResult);

		return findUserByUid(client, request.getUid());
	}


	/**
	 * 密码重置
	 *
	 * @param request request
	 * @return 重置后的密码
	 */
	@Transactional(rollbackFor = Exception.class)
	public String passwordReset(UserResetPasswordParam request) {
		String password = Optional.ofNullable(request.getPassword()).orElse(IdUtil.objectId());
		Query query = Query.query(Criteria.where(UserMongo.FIELD.UID).is(request.getUid()));
		Update update = Update.update(UserMongo.FIELD.PASSWORD, passwordEncoder.encode(password));

		UpdateResult updateResult = mongoTemplate.updateFirst(query, update, UserMongo.class);
		log.debug("[user][password_reset] result -> {}", updateResult);
		return password;
	}

	public Optional<User> findUserByUid(String client, String uid) {
		Query query = Query.query(Criteria.where(UserMongo.FIELD.UID).is(uid));
		return Optional.ofNullable(mongoTemplate.findOne(query, UserMongo.class))
			.map(user -> {

				Set<String> roleCodes = Optional.ofNullable(user.getClientRoles()).stream()
					.filter(clientRole -> clientRole.containsKey(client))
					.flatMap(clientRole -> Optional.ofNullable(clientRole.get(client)).stream().flatMap(Collection::stream))
					.collect(Collectors.toSet());

				Set<String> departmentIds = Optional.ofNullable(user.getClientDepartments()).stream()
					.filter(clientDepartments -> clientDepartments.containsKey(client))
					.flatMap(clientDepartments -> Optional.ofNullable(clientDepartments.get(client)).stream().flatMap(Collection::stream))
					.collect(Collectors.toSet());

				List<RoleMongo> roles = findRoleByCodes(client, roleCodes);
				List<DepartmentMongo> departments = findDepartmentByIds(client, departmentIds);

				return UserConverter.userMapper(user, roles, departments);
			});
	}


	/**
	 * find
	 *
	 * @param client client
	 * @param param  param
	 * @return user list
	 */
	public List<User> find(String client, UserFindParam param) {
		Criteria criteria = new Criteria();
		Optional.ofNullable(param.getKeyword()).flatMap(this::keywordCriteria).ifPresent(x -> x.andOperator(criteria));
		Optional.ofNullable(param.getUids()).flatMap(this::uidCriteria).ifPresent(x -> x.andOperator(criteria));

		Query query = new Query(criteria);

		List<UserMongo> users = mongoTemplate.find(query, UserMongo.class, Mongo.Collection.USER);
		log.debug("[user][page] query: {}", users);

		Collection<String> roleCodes = roleCodesMapper(client,users);
		Collection<String> departmentIds =departmentIdsMapper(client,users);

		List<RoleMongo> roles = findRoleByCodes(client, roleCodes);
		List<DepartmentMongo> departments = findDepartmentByIds(client, departmentIds);

		return UserConverter.usersMapper(client, users, roles, departments);
	}


	/**
	 * find page
	 *
	 * @return user page
	 */
	public Page<User> findPage(String client,
							   UserPageFindParam param) {

		Criteria criteria = new Criteria();
		Optional.ofNullable(param.getKeyword()).flatMap(this::keywordCriteria).ifPresent(x -> x.andOperator(criteria));
		Optional.ofNullable(param.getUids()).flatMap(this::uidCriteria).ifPresent(x -> x.andOperator(criteria));

		Query query = new Query(criteria);
		long total = mongoTemplate.count(query, UserMongo.class, Mongo.Collection.USER);

		query.with(param.getPage().pageable());
		List<UserMongo> users = mongoTemplate.find(query, UserMongo.class, Mongo.Collection.USER);
		log.debug("[user][pageFind] query: {}", users);

		Collection<String> roleCodes = roleCodesMapper(client, users);
		Collection<String> departmentIds = departmentIdsMapper(client, users);

		List<RoleMongo> roles = findRoleByCodes(client, roleCodes);
		List<DepartmentMongo> departments = findDepartmentByIds(client, departmentIds);

		List<User> contents = UserConverter.usersMapper(client, users, roles, departments);

		return new Page<>(param.getPage(), contents, total);
	}

	/**
	 * find by id
	 *
	 * @param client client
	 * @param uid    uid
	 * @return user
	 */
	public Optional<User> findById(String client, String uid) {
		Criteria criteria = Criteria.where(UserMongo.FIELD.UID).is(uid);
		Query query = Query.query(criteria);

		return Optional.ofNullable(mongoTemplate.findOne(query, UserMongo.class, Mongo.Collection.USER))
			.map(user -> {
				// role query
				List<RoleMongo> roles = Optional.ofNullable(user.getClientRoles())
					.filter(clientRoles -> clientRoles.containsKey(client))
					.map(clientRoles -> clientRoles.get(client))
					.filter(x -> !x.isEmpty())
					.map(codes -> {
						Query roleQuery = Query.query(
							Criteria
								.where(RoleMongo.FIELD.CLIENT).is(client)
								.and(RoleMongo.FIELD.CODE).in(codes)
						);
						return mongoTemplate.find(roleQuery, RoleMongo.class, Mongo.Collection.ROLE);
					})
					.orElse(Collections.emptyList());

				// department query
				List<DepartmentMongo> departments = Optional.ofNullable(user.getClientDepartments())
					.filter(y -> y.containsKey(client))
					.map(y -> y.get(client))
					.filter(x -> !x.isEmpty())
					.map(ids -> {
						Query departmentQuery = Query.query(
							Criteria
								.where(DepartmentMongo.FIELD.CLIENT).is(client)
								.and(DepartmentMongo.FIELD._ID).in(ids)
						);
						return mongoTemplate.find(departmentQuery, DepartmentMongo.class, Mongo.Collection.DEPARTMENT);
					})
					.orElse(Collections.emptyList());
				return UserConverter.userMapper(user, roles, departments);
			});
	}

	public List<RoleMongo> findRoleByCodes(String client, Collection<String> roleCodes) {
		Query query = Query
			.query(
				Criteria.where(RoleMongo.FIELD.CLIENT).is(client)
					.and(RoleMongo.FIELD.CODE).in(roleCodes)
			).with(
				Sort.by(
					Sort.Order.asc(RoleMongo.FIELD.METADATA.SORT),
					Sort.Order.asc(RoleMongo.FIELD.METADATA.CREATED.AT),
					Sort.Order.asc(RoleMongo.FIELD._ID)
				)
			);
		return mongoTemplate.find(query, RoleMongo.class, Mongo.Collection.ROLE);
	}

	public List<DepartmentMongo> findDepartmentByIds(String client, Collection<String> ids) {
		return Optional.ofNullable(ids)
			.filter(Collection::isEmpty)
			.map(x -> {
				Query query = Query.query(
					Criteria.where(DepartmentMongo.FIELD.CLIENT).is(client)
						.and(DepartmentMongo.FIELD._ID).in(ids)
				);
				query.with(Sort.by(
					Sort.Order.asc(DepartmentMongo.FIELD.METADATA.SORT),
					Sort.Order.asc(DepartmentMongo.FIELD.METADATA.CREATED.AT),
					Sort.Order.asc(DepartmentMongo.FIELD._ID)
				));
				return mongoTemplate.find(query,
					DepartmentMongo.class,
					Mongo.Collection.DEPARTMENT
				);
			})
			.orElse(Collections.emptyList());
	}

	public Collection<String> roleCodesMapper(String client, List<UserMongo> users) {
		return users.stream().flatMap(x -> Optional.ofNullable(x.getClientRoles()).stream())
			.filter(clientRole -> clientRole.containsKey(client))
			.flatMap(clientRole -> Optional.ofNullable(clientRole.get(client)).stream().flatMap(Collection::stream))
			.collect(Collectors.toSet());
	}

	public Collection<String> departmentIdsMapper(String client, List<UserMongo> users) {
		return users.stream().flatMap(x -> Optional.ofNullable(x.getClientDepartments()).stream())
			.filter(clientDepartments -> clientDepartments.containsKey(client))
			.flatMap(clientDepartments -> Optional.ofNullable(clientDepartments.get(client)).stream().flatMap(Collection::stream))
			.collect(Collectors.toSet());
	}


	public Optional<Criteria> keywordCriteria(String keyword) {
		return Optional.ofNullable(keyword)
			.filter(x -> !x.isEmpty())
			.map(x -> new Criteria().orOperator(
				Criteria.where(UserMongo.FIELD.UID).regex(keyword),
				Criteria.where(UserMongo.FIELD.USERNAME).regex(keyword),
				Criteria.where(UserMongo.FIELD.PHONE_NUMBER).regex(keyword),
				Criteria.where(UserMongo.FIELD.EMAIL).regex(keyword),
				Criteria.where(UserMongo.FIELD.NAME).regex(keyword))
			);
	}

	public Optional<Criteria> uidCriteria(Collection<String> uids) {
		return Optional.ofNullable(uids)
			.filter(x -> !x.isEmpty())
			.map(x -> Criteria.where(UserMongo.FIELD.UID).in(x));
	}


}
