package com.hfhk.auth.service.modules.role;

import com.hfhk.auth.domain.mongo.RoleMongo;
import com.hfhk.auth.domain.resource.ResourceTreeNode;
import com.hfhk.auth.domain.role.Role;
import com.hfhk.auth.domain.role.RoleV2;

import java.util.List;
import java.util.Optional;

public class RoleConverter {

	public static Role roleMapper(RoleMongo data) {
		return Role.builder()
			.code(data.getCode())
			.name(data.getName())
			.build();
	}

	public static Optional<Role> roleOptional(RoleMongo data) {
		return Optional.ofNullable(data).map(RoleConverter::roleMapper);
	}

	public static RoleV2 roleV2Mapper(RoleMongo data, List<ResourceTreeNode> resources) {
		return RoleV2.builder()
			.code(data.getCode())
			.name(data.getName())
			.resources(resources)
			.build();
	}

	public static Optional<RoleV2> roleV2Optional(RoleMongo data, List<ResourceTreeNode> resources) {
		return Optional.ofNullable(data).map(x -> roleV2Mapper(x, resources));
	}
}
