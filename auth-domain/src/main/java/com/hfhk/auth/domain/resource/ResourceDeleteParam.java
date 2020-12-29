package com.hfhk.auth.domain.resource;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 资源-保存-请求参数
 */
@Data
public class ResourceDeleteParam implements Serializable {
	/**
	 * ids
	 */
	private List<String> ids;
}
