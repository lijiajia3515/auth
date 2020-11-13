package com.hfhk.cairo.auth.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 部门 修改 请求
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentModifyRequest implements Serializable {
	private String id;
	private String parentId;
	private String name;
}
