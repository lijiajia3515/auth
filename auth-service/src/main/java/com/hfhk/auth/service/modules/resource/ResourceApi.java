package com.hfhk.auth.service.modules.resource;

import com.hfhk.auth.domain.resource.*;
import com.hfhk.cairo.security.oauth2.server.resource.authentication.CairoAuthentication;
import com.hfhk.cairo.security.oauth2.user.AuthPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Resource Api
 */
@RequestMapping("/Resource")
@RestController
public class ResourceApi {

	private final ResourceService resourceService;

	public ResourceApi(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	//Operate
	@PostMapping("/Save")
	@PreAuthorize("isAuthenticated()")
	public ResourceTreeNode save(@AuthenticationPrincipal AuthPrincipal principal, @RequestBody ResourceSaveParam param) {
		String client = principal.getClient();

		return resourceService.save(client, param);
	}

	@PutMapping("/Modify")
	public ResourceTreeNode modify(@AuthenticationPrincipal AuthPrincipal principal, @RequestBody ResourceModifyParam param) {
		String client = principal.getClient();

		return resourceService.modify(client, param);
	}

	@PatchMapping("/Move")
	@PreAuthorize("isAuthenticated()")
	public void move(@AuthenticationPrincipal AuthPrincipal principal, @RequestBody ResourceMoveParam param) {
		String client = principal.getClient();

		resourceService.move(client, param);
	}

	@DeleteMapping("/Delete")
	@PreAuthorize("isAuthenticated()")
	public List<ResourceTreeNode> delete(@AuthenticationPrincipal AuthPrincipal principal, @RequestBody ResourceDeleteParam param) {
		String client = principal.getClient();

		return resourceService.delete(client, param);
	}

	//Find

	@PostMapping("/Find")
	@PreAuthorize("isAuthenticated()")
	public ResourceTreeNode find(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable String id) {
		String client = principal.getClient();

		return resourceService.find(client, id);
	}

	@GetMapping("/Tree")
	@PreAuthorize("isAuthenticated()")
	public List<ResourceTreeNode> tree(@AuthenticationPrincipal AuthPrincipal principal) {
		String client = principal.getClient();

		return resourceService.treeFind(client);
	}


}
