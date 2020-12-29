package com.hfhk.auth.domain.user;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
public class UserFindRequest implements Serializable {

	@Getter
	@Setter
	private String keyword;
}