package com.lijiajia3515.cairo.auth.server.framework.security.core.userdetails;

import com.lijiajia3515.cairo.auth.domain.mongo.UserMongo;
import com.lijiajia3515.cairo.auth.modules.auth.AuthType;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CairoUserService extends AbstractPrincipalService implements UserDetailsService {

	public CairoUserService(MongoTemplate mongoTemplate) {
		super(mongoTemplate);
	}

	@Override
	public AuthUser loadUserByUsername(String username) throws UsernameNotFoundException {
		Query query = Query.query(
			new Criteria()
				.orOperator(
					Criteria.where(UserMongo.FIELD.UID).is(username),
					Criteria.where(UserMongo.FIELD.USERNAME).is(username),
					Criteria.where(UserMongo.FIELD.PHONE_NUMBER).is(username),
					Criteria.where(UserMongo.FIELD.EMAIL).is(username)
				)
		).with(Sort.by(
			Sort.Order.asc(UserMongo.FIELD.METADATA.CREATED.AT),
			Sort.Order.asc(UserMongo.FIELD.METADATA.LAST_MODIFIED.AT),
			Sort.Order.asc(UserMongo.FIELD._ID)
		));
		return principal(query).map(x -> x.setType(AuthType.Password))
			.orElseThrow(() -> new UsernameNotFoundException("用户名或密码错误"));
	}
}
