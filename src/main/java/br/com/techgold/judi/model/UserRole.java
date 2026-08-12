package br.com.techgold.judi.model;

public enum UserRole {
	
	ADMIN("admin"),
	EDITOR("editor"),
	USER("user"),
	SADMIN("sadmin");
	
	private String role;
	
	UserRole(String role) {
		this.role=role;
	}

	public String getRole() {
		return role;
	}
}
