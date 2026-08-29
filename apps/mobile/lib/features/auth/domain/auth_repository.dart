import 'auth_models.dart';

abstract interface class AuthRepository {
  Future<AuthSession?> restore();
  Future<AuthSession> login({required String email, required String password});
  Future<AuthSession> register({
    required String email,
    required String password,
    required String displayName,
  });
  Future<void> logout();
}
