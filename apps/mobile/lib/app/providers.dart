import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:trace_mobile/core/config/app_config.dart';
import 'package:trace_mobile/core/contracts/contracts.dart';
import 'package:trace_mobile/core/network/auth_token_store.dart';
import 'package:trace_mobile/core/notifications/push_notification_service.dart';
import 'package:trace_mobile/features/auth/data/remote_auth_repository.dart';
import 'package:trace_mobile/features/auth/domain/auth_repository.dart';
import 'package:trace_mobile/features/auth/presentation/auth_controller.dart';
import 'package:trace_mobile/features/enrollment/data/enrollment_service.dart';
import 'package:trace_mobile/features/memory/data/memory_service.dart';
import 'package:trace_mobile/features/recognition/data/prototype_visual_engine.dart';
import 'package:trace_mobile/features/secure_vault/data/in_memory_stores.dart';

final secureStorageProvider = Provider<FlutterSecureStorage>(
  (_) => const FlutterSecureStorage(),
);

final dioProvider = Provider<Dio>(
  (_) => Dio(
    BaseOptions(
      baseUrl: AppConfig.apiBaseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 15),
      contentType: Headers.jsonContentType,
    ),
  ),
);

final authTokenStoreProvider = Provider<AuthTokenStore>(
  (ref) => AuthTokenStore(ref.watch(secureStorageProvider)),
);

final authRepositoryProvider = Provider<AuthRepository>(
  (ref) => RemoteAuthRepository(
    ref.watch(dioProvider),
    ref.watch(authTokenStoreProvider),
  ),
);

final authControllerProvider = StateNotifierProvider<AuthController, AuthState>(
  (ref) => AuthController(ref.watch(authRepositoryProvider)),
);

final pushNotificationProvider = Provider<PushNotificationService>(
  (ref) => PushNotificationService(
    ref.watch(dioProvider),
    ref.watch(secureStorageProvider),
  ),
);

final objectStoreProvider = Provider<ObjectStore>((_) => InMemoryObjectStore());
final sightingStoreProvider = Provider<SightingStore>(
  (_) => InMemorySightingStore(),
);
final secureAssetStoreProvider = Provider<SecureAssetStore>(
  (_) => InMemorySecureAssetStore(),
);

final visualEngineProvider = Provider<PrototypeVisualEngine>(
  (_) => PrototypeVisualEngine(),
);

final enrollmentApiProvider = Provider<EnrollmentApi>(
  (ref) => EnrollmentService(
    ref.watch(visualEngineProvider),
    ref.watch(objectStoreProvider),
    ref.watch(secureAssetStoreProvider),
  ),
);

final recognitionApiProvider = Provider<RecognitionApi>(
  (ref) => ref.watch(visualEngineProvider),
);

final memoryApiProvider = Provider<MemoryApi>(
  (ref) => MemoryService(
    ref.watch(objectStoreProvider),
    ref.watch(sightingStoreProvider),
    ref.watch(secureAssetStoreProvider),
  ),
);

final objectRevisionProvider = StateProvider<int>((_) => 0);
