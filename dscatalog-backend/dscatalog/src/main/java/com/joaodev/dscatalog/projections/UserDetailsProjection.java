package com.joaodev.dscatalog.projections;

public interface UserDetailsProjection {

    String getUsername();
	String getPassword();
	Long getRoleId();
	String getAuthority();
}
