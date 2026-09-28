package za.co.espaza.backend.dto.response;

public record LoginResponse(String token, UserInfo user) {
    public record UserInfo(String userId, String username, String role) {}
}
