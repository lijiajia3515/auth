package com.lijiajia3515.cairo.auth.modules.resource;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collection;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor

@Builder
public class ResourceMoveParam implements Serializable {
	/**
	 * 要移动的 id
	 */
	private Collection<String> ids;

	/**
	 * 更新后的 parentId
	 */
	private String movedParentId;
}
