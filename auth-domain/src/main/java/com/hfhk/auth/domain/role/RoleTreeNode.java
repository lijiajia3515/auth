package com.hfhk.auth.domain.role;

import com.hfhk.cairo.core.tree.TreeNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class RoleTreeNode implements TreeNode<String, RoleTreeNode>, Serializable {
	private String id;
	private String parent;
	private String name;
	private Long sort;
	private List<RoleTreeNode> subs = new ArrayList<>(1);

	@Override
	public String id() {
		return id;
	}

	@Override
	public String parentId() {
		return parent;
	}

	@Override
	public List<RoleTreeNode> subs() {
		return subs;
	}
}
