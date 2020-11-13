package com.hfhk.cairo.auth.service.module.auth.template.mongo;

import com.hfhk.cairo.auth.service.module.auth.domain.mongo.ResourceMongo;

import java.util.Collection;
import java.util.List;

/**
 * mongo template - resource
 */
public interface ResourceMongoTemplate {

	/**
	 * 根据 id 查询 当前id和子集
	 *
	 * @param client client
	 * @param ids    ids
	 * @return 资源 mongo
	 */
	List<ResourceMongo> findSubsByIds(String client, Collection<String> ids);


}
