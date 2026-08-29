import 'dart:io';

import 'package:dio/dio.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:uuid/uuid.dart';

final class PushNotificationService {
  PushNotificationService(this._dio, this._storage);

  final Dio _dio;
  final FlutterSecureStorage _storage;
  static const _installationKey = 'trace_installation_id';

  Future<bool> enable(String accessToken) async {
    try {
      if (Firebase.apps.isEmpty) {
        await Firebase.initializeApp();
      }
      final settings = await FirebaseMessaging.instance.requestPermission();
      if (settings.authorizationStatus == AuthorizationStatus.denied) {
        return false;
      }
      final token = await FirebaseMessaging.instance.getToken();
      if (token == null) return false;
      var installationId = await _storage.read(key: _installationKey);
      installationId ??= const Uuid().v4();
      await _storage.write(key: _installationKey, value: installationId);
      await _dio.put<void>(
        '/devices/$installationId',
        data: {
          'platform': Platform.isIOS ? 'ios' : 'android',
          'pushToken': token,
          'locale': Platform.localeName,
          'notificationsEnabled': true,
        },
        options: Options(headers: {'Authorization': 'Bearer $accessToken'}),
      );
      return true;
    } catch (_) {
      return false;
    }
  }
}
