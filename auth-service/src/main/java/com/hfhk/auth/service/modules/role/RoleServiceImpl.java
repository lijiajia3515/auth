package com.hfhk.auth.service.modules.role;

import cn.hutool.core.util.IdUtil;
import com.hfhk.auth.domain.mongo.ResourceMongo;
import com.hfhk.auth.domain.mongo.RoleMongo;
import com.hfhk.auth.domain.resource.ResourceTreeNode;
import com.hfhk.auth.domain.role.*;
import com.hfhk.auth.service.modules.resource.ResourceConverter;
import com.hfhk.cairo.core.exception.UnknownBusinessException;
import com.hfhk.cairo.core.page.Page;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 服务实现 - 角色
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

	private final MongoTemplate mongoTemplate;

	public RoleServiceImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public Page<Role> pageFind(String client, RolePageFindRequest request) {
		Query query = Query.query(Criteria.where(RoleMongo.FIELD.CLIENT).is(client));
		long total = mongoTemplate.count(query, RoleMongo.class);
		query.with(request.getPage().pageable());
		List<Role> content = mongoTemplate.find(query, RoleMongo.class)
			.stream()
			.flatMap(x -> RoleConverter.data2V1(x).stream())
			.collect(Collectors.toList());

		return new Page<>(request.getPage(), content, total);
	}

	@Override
	public RoleV2 find(String client, String id) {
		return findByCode(client, id);
	}

	@Override
	public RoleV2 save(String client, RoleSaveRequest request) {
		RoleMongo role = RoleMongo.builder()
			.client(client)
			.code(Optional.ofNullable(request.getCode()).orElse(IdUtil.objectId()))
			.name(request.getName())
			.resources(request.getResources())
			.build();
		RoleMongo insert = mongoTemplate.insert(role);
		log.debug("[role][insert] -> {}", insert);
		return findByCode(client, insert.getId());
	}

	@Override
	public RoleV2 modify(String client, RoleModifyRequest request) {
		Query query = Query.query(Criteria.where(RoleMongo.FIELD.CLIENT).is(client).and(RoleMongo.FIELD.CODE).is(request.getCode()));
		Update update = new Update();
		Optional.ofNullable(request.getName()).ifPresent(name -> update.set(RoleMongo.FIELD.NAME, name));
		Optional.ofNullable(request.getResources()).ifPresent(resourceIds -> update.set(RoleMongo.FIELD.RESOURCES, resourceIds));
		UpdateResult result = mongoTemplate.updateFirst(query, update, RoleMongo.class);
		log.debug("[role][update] -> {}", result);
		return findByCode(client, request.getCode());
	}

	@Override
	public RoleV2 delete(String client, String code) {
		Query query = Query.query(Criteria.where(RoleMongo.FIELD.CLIENT).is(client).and(RoleMongo.FIELD.CODE).is(code));
		RoleMongo role = mongoTemplate.findAndRemove(query, RoleMongo.class);
		List<ResourceTreeNode> resources = findResources(role);
		RoleV2 roleV2 = RoleConverter.data2V2(role, resources).orElseThrow(() -> new UnknownBusinessException("code not found"));
		log.debug("[role][delete] -> {}", roleV2);
		return roleV2;
	}

	private RoleV2 findByCode(String client, String code) {
		Query query = Query.query(Criteria.where(RoleMongo.FIELD.CLIENT).is(client).and(RoleMongo.FIELD.CODE).is(code));
		RoleMongo role = mongoTemplate.findOne(query, RoleMongo.class);
		List<ResourceTreeNode> resources = findResources(role);
		return RoleConverter.data2V2(role, resources).orElseThrow(() -> new UnknownBusinessException("code not found"));
	}

	private List<ResourceTreeNode> findResources(RoleMongo role) {
		return Optional.ofNullable(role)
			.map(RoleMongo::getResources)
			.filter(x -> !x.isEmpty())
			.map(x -> mongoTemplate.find(Query.query(Criteria.where(RoleMongo.FIELD.CODE).is(role.getClient())
				.and(RoleMongo.FIELD._ID).in(x)), ResourceMongo.class)
			)
			.map(ResourceConverter::data2tree)
			.orElse(Collections.emptyList());
	}
}