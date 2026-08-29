final class UserProfile {
  const UserProfile({
    required this.id,
    required this.email,
    required this.displayName,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    return UserProfile(
      id: json['id'] as String,
      email: json['email'] as String,
      displayName: json['displayName'] as String,
    );
  }

  final String id;
  final String email;
  final String displayName;
}

final class AuthSession {
  const AuthSession({
    required this.user,
    required this.accessToken,
    required this.refreshToken,
  });

  factory AuthSession.fromJson(Map<String, dynamic> json) {
    return AuthSession(
      user: UserProfile.fromJson(json['user'] as Map<String, dynamic>),
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
    );
  }

  final UserProfile user;
  final String accessToken;
  final String refreshToken;
}
