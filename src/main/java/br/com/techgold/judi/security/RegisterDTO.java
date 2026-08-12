package br.com.techgold.judi.security;

public record RegisterDTO(
		String username,
		String password,
		String role
		) {

}
