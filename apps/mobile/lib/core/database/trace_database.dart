import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as path;
import 'package:path_provider/path_provider.dart';

part 'trace_database.g.dart';

class LocalObjects extends Table {
  TextColumn get id => text()();
  BlobColumn get encryptedTag => blob()();
  BlobColumn get tagNonce => blob()();
  IntColumn get referenceRevision => integer().withDefault(const Constant(1))();
  IntColumn get createdAt => integer()();
  IntColumn get updatedAt => integer()();
  BoolColumn get enabled => boolean().withDefault(const Constant(true))();
  BoolColumn get syncPending => boolean().withDefault(const Constant(true))();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

class SecureAssets extends Table {
  TextColumn get id => text()();
  TextColumn get relativePath => text()();
  TextColumn get assetType => text()();
  TextColumn get mimeType => text()();
  IntColumn get cryptoVersion => integer()();
  IntColumn get createdAt => integer()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

class LocalObjectReferences extends Table {
  TextColumn get id => text()();
  TextColumn get objectId =>
      text().references(LocalObjects, #id, onDelete: KeyAction.cascade)();
  TextColumn get imageAssetId => text().references(SecureAssets, #id)();
  RealColumn get roiLeft => real()();
  RealColumn get roiTop => real()();
  RealColumn get roiRight => real()();
  RealColumn get roiBottom => real()();
  RealColumn get qualityScore => real()();
  IntColumn get createdAt => integer()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

class LocalReferenceEmbeddings extends Table {
  TextColumn get id => text()();
  TextColumn get referenceId => text().references(
    LocalObjectReferences,
    #id,
    onDelete: KeyAction.cascade,
  )();
  TextColumn get modelName => text()();
  TextColumn get modelVersion => text()();
  IntColumn get dimensions => integer()();
  BlobColumn get encryptedVector => blob()();
  BlobColumn get vectorNonce => blob()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

class LocalSightings extends Table {
  TextColumn get id => text()();
  TextColumn get objectId =>
      text().references(LocalObjects, #id, onDelete: KeyAction.cascade)();
  IntColumn get detectedAt => integer()();
  BlobColumn get encryptedLocation => blob().nullable()();
  BlobColumn get locationNonce => blob().nullable()();
  RealColumn get confidence => real()();
  TextColumn get evidenceAssetId =>
      text().nullable().references(SecureAssets, #id)();
  BoolColumn get syncPending => boolean().withDefault(const Constant(true))();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

@DriftDatabase(
  tables: [
    LocalObjects,
    SecureAssets,
    LocalObjectReferences,
    LocalReferenceEmbeddings,
    LocalSightings,
  ],
)
class TraceDatabase extends _$TraceDatabase {
  TraceDatabase() : super(_openConnection());

  TraceDatabase.forTesting(super.executor);

  @override
  int get schemaVersion => 1;
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final directory = await getApplicationSupportDirectory();
    final file = File(path.join(directory.path, 'trace.sqlite'));
    return NativeDatabase.createInBackground(file);
  });
}
