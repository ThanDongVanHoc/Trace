import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:trace_mobile/features/auth/domain/auth_models.dart';
import 'package:trace_mobile/features/auth/domain/auth_repository.dart';

enum AuthStatus { booting, unauthenticated, busy, authenticated }

final class AuthState {
  const AuthState({required this.status, this.session, this.errorMessage});

  const AuthState.booting() : this(status: AuthStatus.booting);

  final AuthStatus status;
  final AuthSession? session;
  final String? errorMessage;
}

final class AuthController extends StateNotifier<AuthState> {
  AuthController(this._repository) : super(const AuthState.booting()) {
    Future<void>.microtask(restore);
  }

  final AuthRepository _repository;

  Future<void> restore() async {
    try {
      final session = await _repository.restore();
      state = AuthState(
        status: session == null
            ? AuthStatus.unauthenticated
            : AuthStatus.authenticated,
        session: session,
      );
    } catch (_) {
      state = const AuthState(status: AuthStatus.unauthenticated);
    }
  }

  Future<bool> login({required String email, required String password}) async {
    state = const AuthState(status: AuthStatus.busy);
    try {
      final session = await _repository.login(email: email, password: password);
      state = AuthState(status: AuthStatus.authenticated, session: session);
      return true;
    } catch (error) {
      state = AuthState(
        status: AuthStatus.unauthenticated,
        errorMessage: _message(error),
      );
      return false;
    }
  }

  Future<bool> register({
    required String email,
    required String password,
    required String displayName,
  }) async {
    state = const AuthState(status: AuthStatus.busy);
    try {
      final session = await _repository.register(
        email: email,
        password: password,
        displayName: displayName,
      );
      state = AuthState(status: AuthStatus.authenticated, session: session);
      return true;
    } catch (error) {
      state = AuthState(
        status: AuthStatus.unauthenticated,
        errorMessage: _message(error),
      );
      return false;
    }
  }

  Future<void> logout() async {
    await _repository.logout();
    state = const AuthState(status: AuthStatus.unauthenticated);
  }

  String _message(Object error) {
    final text = error.toString();
    if (text.contains('connection')) return 'Không thể kết nối đến máy chủ.';
    if (text.contains('401')) return 'Email hoặc mật khẩu không đúng.';
    if (text.contains('409')) return 'Email này đã được đăng ký.';
    return 'Không thể hoàn tất yêu cầu. Vui lòng thử lại.';
  }
}
