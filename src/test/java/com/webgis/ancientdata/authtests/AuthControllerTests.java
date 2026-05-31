package com.webgis.ancientdata.authtests;

import com.webgis.ancientdata.application.service.AuthService;
import com.webgis.ancientdata.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldDefaultRegisterRoleToUserWhenRoleIsMissing() throws Exception {
		when(authService.registerUser("alice", "secret", Role.USER)).thenReturn("mock-token");

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "alice",
								  "password": "secret"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("mock-token"))
				.andExpect(jsonPath("$.roles[0]").value("USER"));

		verify(authService).registerUser("alice", "secret", Role.USER);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void shouldUseProvidedRoleDuringRegistration() throws Exception {
		when(authService.registerUser("admin", "secret", Role.ADMIN)).thenReturn("admin-token");

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "admin",
								  "password": "secret",
								  "role": "ADMIN"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("admin-token"))
				.andExpect(jsonPath("$.roles[0]").value("ADMIN"));

		verify(authService).registerUser("admin", "secret", Role.ADMIN);
	}

	@Test
	void shouldRejectUnauthenticatedRegistration() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "alice",
								  "password": "secret"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void shouldRejectNonAdminRegistration() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "alice",
								  "password": "secret"
								}
								"""))
				.andExpect(status().isForbidden());
	}
}

