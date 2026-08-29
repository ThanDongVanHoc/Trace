import 'package:dio/dio.dart';
import 'package:trace_mobile/core/network/auth_token_store.dart';
import 'package:trace_mobile/features/auth/domain/auth_models.dart';
import 'package:trace_mobile/features/auth/domain/auth_repository.dart';

final class RemoteAuthRepository implements AuthRepository {
  RemoteAuthRepository(this._dio, this._tokens);

  final Dio _dio;
  final AuthTokenStore _tokens;

  @override
  Future<AuthSession?> restore() async {
    final stored = await _tokens.read();
    if (stored == null) return null;
    try {
      final user = await _getProfile(stored.accessToken);
      return AuthSession(
        user: user,
        accessToken: stored.accessToken,
        refreshToken: stored.refreshToken,
      );
    } on DioException catch (error) {
      if (error.response?.statusCode != 401) rethrow;
      try {
        final session = await _refresh(stored.refreshToken);
        await _save(session);
        return session;
      } on DioException {
        await _tokens.clear();
        return null;
      }
    }
  }

  @override
  Future<AuthSession> login({
    required String email,
    required String password,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/login',
      data: {'email': email.trim(), 'password': password},
    );
    final session = AuthSession.fromJson(response.data!);
    await _save(session);
    return session;
  }

  @override
  Future<AuthSession> register({
    required String email,
    required String password,
    required String displayName,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/register',
      data: {
        'email': email.trim(),
        'password': password,
        'displayName': displayName.trim(),
      },
    );
    final session = AuthSession.fromJson(response.data!);
    await _save(session);
    return session;
  }

  @override
  Future<void> logout() async {
    final stored = await _tokens.read();
    try {
      if (stored != null) {
        await _dio.post<void>(
          '/auth/logout',
          options: Options(
            headers: {'Authorization': 'Bearer ${stored.accessToken}'},
          ),
        );
      }
    } finally {
      await _tokens.clear();
    }
  }

  Future<UserProfile> _getProfile(String accessToken) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/auth/me',
      options: Options(headers: {'Authorization': 'Bearer $accessToken'}),
    );
    return UserProfile.fromJson(response.data!);
  }

  Future<AuthSession> _refresh(String refreshToken) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/refresh',
      data: {'refreshToken': refreshToken},
    );
    return AuthSession.fromJson(response.data!);
  }

  Future<void> _save(AuthSession session) {
    return _tokens.write(
      AuthTokens(
        accessToken: session.accessToken,
        refreshToken: session.refreshToken,
      ),
    );
  }
}
