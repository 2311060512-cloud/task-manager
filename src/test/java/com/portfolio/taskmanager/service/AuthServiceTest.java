package com.portfolio.taskmanager.service;

import com.portfolio.taskmanager.dto.request.LoginRequest;
import com.portfolio.taskmanager.dto.request.RegisterRequest;
import com.portfolio.taskmanager.dto.response.AuthResponse;
import com.portfolio.taskmanager.dto.response.UserResponse;
import com.portfolio.taskmanager.entity.User;
import com.portfolio.taskmanager.entity.enums.UserRole;
import com.portfolio.taskmanager.exception.BadRequestException;
import com.portfolio.taskmanager.exception.ResourceNotFoundException;
import com.portfolio.taskmanager.mapper.UserMapper;
import com.portfolio.taskmanager.repository.UserRepository;
import com.portfolio.taskmanager.security.JwtTokenProvider;
import com.portfolio.taskmanager.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("admin")
                .password("encoded_pass")
                .fullName("Quản trị viên")
                .email("admin@taskmanager.com")
                .role(UserRole.ADMIN)
                .build();
    }

    @Test
    @DisplayName("login - Đăng nhập thành công trả về AuthResponse và JWT Token")
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("password123")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(sampleUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.token.here");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("login - Thất bại do sai thông tin đăng nhập ném lỗi BadCredentialsException")
    void login_WrongCredentials_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Sai tài khoản hoặc mật khẩu"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Sai tài khoản hoặc mật khẩu");
    }

    @Test
    @DisplayName("register - Đăng ký thành công trả về AuthResponse")
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("new_client")
                .password("secret123")
                .fullName("Khách hàng Mới")
                .email("new@client.com")
                .role(UserRole.CLIENT)
                .build();

        User savedUser = User.builder()
                .id(10L)
                .username("new_client")
                .password("encoded_secret")
                .fullName("Khách hàng Mới")
                .email("new@client.com")
                .role(UserRole.CLIENT)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(userRepository.existsByUsername("new_client")).thenReturn(false);
        when(userRepository.existsByEmail("new@client.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.token.registered");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("new_client");
        assertThat(response.getToken()).isEqualTo("jwt.token.registered");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - Trùng username ném lỗi BadRequestException")
    void register_DuplicateUsername_ThrowsBadRequestException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("admin")
                .password("secret123")
                .fullName("Quản trị viên")
                .email("other@example.com")
                .build();

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Tên đăng nhập 'admin' đã được sử dụng");
    }

    @Test
    @DisplayName("register - Trùng email ném lỗi BadRequestException")
    void register_DuplicateEmail_ThrowsBadRequestException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("unique_user")
                .password("secret123")
                .fullName("Người Dùng Mới")
                .email("admin@taskmanager.com")
                .build();

        when(userRepository.existsByUsername("unique_user")).thenReturn(false);
        when(userRepository.existsByEmail("admin@taskmanager.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email 'admin@taskmanager.com' đã được sử dụng");
    }

    @Test
    @DisplayName("register - Cố tình leo quyền đăng ký ADMIN/EMPLOYEE bị chặn BadRequestException")
    void register_WithPrivilegeEscalation_ThrowsBadRequestException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("hacker_admin")
                .password("secret123")
                .fullName("Hacker Admin")
                .email("hacker@security.com")
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.existsByUsername("hacker_admin")).thenReturn(false);
        when(userRepository.existsByEmail("hacker@security.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Đăng ký tài khoản công khai chỉ áp dụng cho vai trò Khách hàng (CLIENT)");
    }

    @Test
    @DisplayName("getCurrentUser - Tìm thấy user trả về UserResponse")
    void getCurrentUser_Success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(sampleUser));
        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .username("admin")
                .fullName("Quản trị viên")
                .email("admin@taskmanager.com")
                .role(UserRole.ADMIN)
                .build();
        when(userMapper.toResponse(sampleUser)).thenReturn(expectedResponse);

        UserResponse actual = authService.getCurrentUser("admin");

        assertThat(actual).isNotNull();
        assertThat(actual.getUsername()).isEqualTo("admin");
        assertThat(actual.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("getCurrentUser - Không tìm thấy user ném lỗi ResourceNotFoundException")
    void getCurrentUser_NotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Người dùng");
    }
}

