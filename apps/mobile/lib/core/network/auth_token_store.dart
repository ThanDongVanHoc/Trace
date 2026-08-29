import 'package:flutter_secure_storage/flutter_secure_storage.dart';

final class AuthTokens {
  const AuthTokens({required this.accessToken, required this.refreshToken});

  final String accessToken;
  final String refreshToken;
}

final class AuthTokenStore {
  AuthTokenStore(this._storage);

  final FlutterSecureStorage _storage;

  static const _accessKey = 'trace_access_token';
  static const _refreshKey = 'trace_refresh_token';

  Future<AuthTokens?> read() async {
    final values = await _storage.readAll();
    final accessToken = values[_accessKey];
    final refreshToken = values[_refreshKey];
    if (accessToken == null || refreshToken == null) return null;
    return AuthTokens(accessToken: accessToken, refreshToken: refreshToken);
  }

  Future<void> write(AuthTokens tokens) async {
    await Future.wait([
      _storage.write(key: _accessKey, value: tokens.accessToken),
      _storage.write(key: _refreshKey, value: tokens.refreshToken),
    ]);
  }

  Future<void> clear() async {
    await Future.wait([
      _storage.delete(key: _accessKey),
      _storage.delete(key: _refreshKey),
    ]);
  }
}
