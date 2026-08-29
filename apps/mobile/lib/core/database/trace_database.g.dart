// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trace_database.dart';

// ignore_for_file: type=lint
class $LocalObjectsTable extends LocalObjects
    with TableInfo<$LocalObjectsTable, LocalObject> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $LocalObjectsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _encryptedTagMeta = const VerificationMeta(
    'encryptedTag',
  );
  @override
  late final GeneratedColumn<Uint8List> encryptedTag =
      GeneratedColumn<Uint8List>(
        'encrypted_tag',
        aliasedName,
        false,
        type: DriftSqlType.blob,
        requiredDuringInsert: true,
      );
  static const VerificationMeta _tagNonceMeta = const VerificationMeta(
    'tagNonce',
  );
  @override
  late final GeneratedColumn<Uint8List> tagNonce = GeneratedColumn<Uint8List>(
    'tag_nonce',
    aliasedName,
    false,
    type: DriftSqlType.blob,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _referenceRevisionMeta = const VerificationMeta(
    'referenceRevision',
  );
  @override
  late final GeneratedColumn<int> referenceRevision = GeneratedColumn<int>(
    'reference_revision',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultValue: const Constant(1),
  );
  static const VerificationMeta _createdAtMeta = const VerificationMeta(
    'createdAt',
  );
  @override
  late final GeneratedColumn<int> createdAt = GeneratedColumn<int>(
    'created_at',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _updatedAtMeta = const VerificationMeta(
    'updatedAt',
  );
  @override
  late final GeneratedColumn<int> updatedAt = GeneratedColumn<int>(
    'updated_at',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _enabledMeta = const VerificationMeta(
    'enabled',
  );
  @override
  late final GeneratedColumn<bool> enabled = GeneratedColumn<bool>(
    'enabled',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("enabled" IN (0, 1))',
    ),
    defaultValue: const Constant(true),
  );
  static const VerificationMeta _syncPendingMeta = const VerificationMeta(
    'syncPending',
  );
  @override
  late final GeneratedColumn<bool> syncPending = GeneratedColumn<bool>(
    'sync_pending',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("sync_pending" IN (0, 1))',
    ),
    defaultValue: const Constant(true),
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    encryptedTag,
    tagNonce,
    referenceRevision,
    createdAt,
    updatedAt,
    enabled,
    syncPending,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'local_objects';
  @override
  VerificationContext validateIntegrity(
    Insertable<LocalObject> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('encrypted_tag')) {
      context.handle(
        _encryptedTagMeta,
        encryptedTag.isAcceptableOrUnknown(
          data['encrypted_tag']!,
          _encryptedTagMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_encryptedTagMeta);
    }
    if (data.containsKey('tag_nonce')) {
      context.handle(
        _tagNonceMeta,
        tagNonce.isAcceptableOrUnknown(data['tag_nonce']!, _tagNonceMeta),
      );
    } else if (isInserting) {
      context.missing(_tagNonceMeta);
    }
    if (data.containsKey('reference_revision')) {
      context.handle(
        _referenceRevisionMeta,
        referenceRevision.isAcceptableOrUnknown(
          data['reference_revision']!,
          _referenceRevisionMeta,
        ),
      );
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    } else if (isInserting) {
      context.missing(_createdAtMeta);
    }
    if (data.containsKey('updated_at')) {
      context.handle(
        _updatedAtMeta,
        updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta),
      );
    } else if (isInserting) {
      context.missing(_updatedAtMeta);
    }
    if (data.containsKey('enabled')) {
      context.handle(
        _enabledMeta,
        enabled.isAcceptableOrUnknown(data['enabled']!, _enabledMeta),
      );
    }
    if (data.containsKey('sync_pending')) {
      context.handle(
        _syncPendingMeta,
        syncPending.isAcceptableOrUnknown(
          data['sync_pending']!,
          _syncPendingMeta,
        ),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  LocalObject map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return LocalObject(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      encryptedTag: attachedDatabase.typeMapping.read(
        DriftSqlType.blob,
        data['${effectivePrefix}encrypted_tag'],
      )!,
      tagNonce: attachedDatabase.typeMapping.read(
        DriftSqlType.blob,
        data['${effectivePrefix}tag_nonce'],
      )!,
      referenceRevision: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}reference_revision'],
      )!,
      createdAt: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}created_at'],
      )!,
      updatedAt: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}updated_at'],
      )!,
      enabled: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}enabled'],
      )!,
      syncPending: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}sync_pending'],
      )!,
    );
  }

  @override
  $LocalObjectsTable createAlias(String alias) {
    return $LocalObjectsTable(attachedDatabase, alias);
  }
}

class LocalObject extends DataClass implements Insertable<LocalObject> {
  final String id;
  final Uint8List encryptedTag;
  final Uint8List tagNonce;
  final int referenceRevision;
  final int createdAt;
  final int updatedAt;
  final bool enabled;
  final bool syncPending;
  const LocalObject({
    required this.id,
    required this.encryptedTag,
    required this.tagNonce,
    required this.referenceRevision,
    required this.createdAt,
    required this.updatedAt,
    required this.enabled,
    required this.syncPending,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['encrypted_tag'] = Variable<Uint8List>(encryptedTag);
    map['tag_nonce'] = Variable<Uint8List>(tagNonce);
    map['reference_revision'] = Variable<int>(referenceRevision);
    map['created_at'] = Variable<int>(createdAt);
    map['updated_at'] = Variable<int>(updatedAt);
    map['enabled'] = Variable<bool>(enabled);
    map['sync_pending'] = Variable<bool>(syncPending);
    return map;
  }

  LocalObjectsCompanion toCompanion(bool nullToAbsent) {
    return LocalObjectsCompanion(
      id: Value(id),
      encryptedTag: Value(encryptedTag),
      tagNonce: Value(tagNonce),
      referenceRevision: Value(referenceRevision),
      createdAt: Value(createdAt),
      updatedAt: Value(updatedAt),
      enabled: Value(enabled),
      syncPending: Value(syncPending),
    );
  }

  factory LocalObject.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return LocalObject(
      id: serializer.fromJson<String>(json['id']),
      encryptedTag: serializer.fromJson<Uint8List>(json['encryptedTag']),
      tagNonce: serializer.fromJson<Uint8List>(json['tagNonce']),
      referenceRevision: serializer.fromJson<int>(json['referenceRevision']),
      createdAt: serializer.fromJson<int>(json['createdAt']),
      updatedAt: serializer.fromJson<int>(json['updatedAt']),
      enabled: serializer.fromJson<bool>(json['enabled']),
      syncPending: serializer.fromJson<bool>(json['syncPending']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'encryptedTag': serializer.toJson<Uint8List>(encryptedTag),
      'tagNonce': serializer.toJson<Uint8List>(tagNonce),
      'referenceRevision': serializer.toJson<int>(referenceRevision),
      'createdAt': serializer.toJson<int>(createdAt),
      'updatedAt': serializer.toJson<int>(updatedAt),
      'enabled': serializer.toJson<bool>(enabled),
      'syncPending': serializer.toJson<bool>(syncPending),
    };
  }

  LocalObject copyWith({
    String? id,
    Uint8List? encryptedTag,
    Uint8List? tagNonce,
    int? referenceRevision,
    int? createdAt,
    int? updatedAt,
    bool? enabled,
    bool? syncPending,
  }) => LocalObject(
    id: id ?? this.id,
    encryptedTag: encryptedTag ?? this.encryptedTag,
    tagNonce: tagNonce ?? this.tagNonce,
    referenceRevision: referenceRevision ?? this.referenceRevision,
    createdAt: createdAt ?? this.createdAt,
    updatedAt: updatedAt ?? this.updatedAt,
    enabled: enabled ?? this.enabled,
    syncPending: syncPending ?? this.syncPending,
  );
  LocalObject copyWithCompanion(LocalObjectsCompanion data) {
    return LocalObject(
      id: data.id.present ? data.id.value : this.id,
      encryptedTag: data.encryptedTag.present
          ? data.encryptedTag.value
          : this.encryptedTag,
      tagNonce: data.tagNonce.present ? data.tagNonce.value : this.tagNonce,
      referenceRevision: data.referenceRevision.present
          ? data.referenceRevision.value
          : this.referenceRevision,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
      enabled: data.enabled.present ? data.enabled.value : this.enabled,
      syncPending: data.syncPending.present
          ? data.syncPending.value
          : this.syncPending,
    );
  }

  @override
  String toString() {
    return (StringBuffer('LocalObject(')
          ..write('id: $id, ')
          ..write('encryptedTag: $encryptedTag, ')
          ..write('tagNonce: $tagNonce, ')
          ..write('referenceRevision: $referenceRevision, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('enabled: $enabled, ')
          ..write('syncPending: $syncPending')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    $driftBlobEquality.hash(encryptedTag),
    $driftBlobEquality.hash(tagNonce),
    referenceRevision,
    createdAt,
    updatedAt,
    enabled,
    syncPending,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is LocalObject &&
          other.id == this.id &&
          $driftBlobEquality.equals(other.encryptedTag, this.encryptedTag) &&
          $driftBlobEquality.equals(other.tagNonce, this.tagNonce) &&
          other.referenceRevision == this.referenceRevision &&
          other.createdAt == this.createdAt &&
          other.updatedAt == this.updatedAt &&
          other.enabled == this.enabled &&
          other.syncPending == this.syncPending);
}

class LocalObjectsCompanion extends UpdateCompanion<LocalObject> {
  final Value<String> id;
  final Value<Uint8List> encryptedTag;
  final Value<Uint8List> tagNonce;
  final Value<int> referenceRevision;
  final Value<int> createdAt;
  final Value<int> updatedAt;
  final Value<bool> enabled;
  final Value<bool> syncPending;
  final Value<int> rowid;
  const LocalObjectsCompanion({
    this.id = const Value.absent(),
    this.encryptedTag = const Value.absent(),
    this.tagNonce = const Value.absent(),
    this.referenceRevision = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.enabled = const Value.absent(),
    this.syncPending = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  LocalObjectsCompanion.insert({
    required String id,
    required Uint8List encryptedTag,
    required Uint8List tagNonce,
    this.referenceRevision = const Value.absent(),
    required int createdAt,
    required int updatedAt,
    this.enabled = const Value.absent(),
    this.syncPending = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       encryptedTag = Value(encryptedTag),
       tagNonce = Value(tagNonce),
       createdAt = Value(createdAt),
       updatedAt = Value(updatedAt);
  static Insertable<LocalObject> custom({
    Expression<String>? id,
    Expression<Uint8List>? encryptedTag,
    Expression<Uint8List>? tagNonce,
    Expression<int>? referenceRevision,
    Expression<int>? createdAt,
    Expression<int>? updatedAt,
    Expression<bool>? enabled,
    Expression<bool>? syncPending,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (encryptedTag != null) 'encrypted_tag': encryptedTag,
      if (tagNonce != null) 'tag_nonce': tagNonce,
      if (referenceRevision != null) 'reference_revision': referenceRevision,
      if (createdAt != null) 'created_at': createdAt,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (enabled != null) 'enabled': enabled,
      if (syncPending != null) 'sync_pending': syncPending,
      if (rowid != null) 'rowid': rowid,
    });
  }

  LocalObjectsCompanion copyWith({
    Value<String>? id,
    Value<Uint8List>? encryptedTag,
    Value<Uint8List>? tagNonce,
    Value<int>? referenceRevision,
    Value<int>? createdAt,
    Value<int>? updatedAt,
    Value<bool>? enabled,
    Value<bool>? syncPending,
    Value<int>? rowid,
  }) {
    return LocalObjectsCompanion(
      id: id ?? this.id,
      encryptedTag: encryptedTag ?? this.encryptedTag,
      tagNonce: tagNonce ?? this.tagNonce,
      referenceRevision: referenceRevision ?? this.referenceRevision,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      enabled: enabled ?? this.enabled,
      syncPending: syncPending ?? this.syncPending,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (encryptedTag.present) {
      map['encrypted_tag'] = Variable<Uint8List>(encryptedTag.value);
    }
    if (tagNonce.present) {
      map['tag_nonce'] = Variable<Uint8List>(tagNonce.value);
    }
    if (referenceRevision.present) {
      map['reference_revision'] = Variable<int>(referenceRevision.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<int>(createdAt.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<int>(updatedAt.value);
    }
    if (enabled.present) {
      map['enabled'] = Variable<bool>(enabled.value);
    }
    if (syncPending.present) {
      map['sync_pending'] = Variable<bool>(syncPending.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('LocalObjectsCompanion(')
          ..write('id: $id, ')
          ..write('encryptedTag: $encryptedTag, ')
          ..write('tagNonce: $tagNonce, ')
          ..write('referenceRevision: $referenceRevision, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('enabled: $enabled, ')
          ..write('syncPending: $syncPending, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $SecureAssetsTable extends SecureAssets
    with TableInfo<$SecureAssetsTable, SecureAsset> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $SecureAssetsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _relativePathMeta = const VerificationMeta(
    'relativePath',
  );
  @override
  late final GeneratedColumn<String> relativePath = GeneratedColumn<String>(
    'relative_path',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _assetTypeMeta = const VerificationMeta(
    'assetType',
  );
  @override
  late final GeneratedColumn<String> assetType = GeneratedColumn<String>(
    'asset_type',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _mimeTypeMeta = const VerificationMeta(
    'mimeType',
  );
  @override
  late final GeneratedColumn<String> mimeType = GeneratedColumn<String>(
    'mime_type',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _cryptoVersionMeta = const VerificationMeta(
    'cryptoVersion',
  );
  @override
  late final GeneratedColumn<int> cryptoVersion = GeneratedColumn<int>(
    'crypto_version',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _createdAtMeta = const VerificationMeta(
    'createdAt',
  );
  @override
  late final GeneratedColumn<int> createdAt = GeneratedColumn<int>(
    'created_at',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    relativePath,
    assetType,
    mimeType,
    cryptoVersion,
    createdAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'secure_assets';
  @override
  VerificationContext validateIntegrity(
    Insertable<SecureAsset> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('relative_path')) {
      context.handle(
        _relativePathMeta,
        relativePath.isAcceptableOrUnknown(
          data['relative_path']!,
          _relativePathMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_relativePathMeta);
    }
    if (data.containsKey('asset_type')) {
      context.handle(
        _assetTypeMeta,
        assetType.isAcceptableOrUnknown(data['asset_type']!, _assetTypeMeta),
      );
    } else if (isInserting) {
      context.missing(_assetTypeMeta);
    }
    if (data.containsKey('mime_type')) {
      context.handle(
        _mimeTypeMeta,
        mimeType.isAcceptableOrUnknown(data['mime_type']!, _mimeTypeMeta),
      );
    } else if (isInserting) {
      context.missing(_mimeTypeMeta);
    }
    if (data.containsKey('crypto_version')) {
      context.handle(
        _cryptoVersionMeta,
        cryptoVersion.isAcceptableOrUnknown(
          data['crypto_version']!,
          _cryptoVersionMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_cryptoVersionMeta);
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    } else if (isInserting) {
      context.missing(_createdAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  SecureAsset map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return SecureAsset(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      relativePath: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}relative_path'],
      )!,
      assetType: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}asset_type'],
      )!,
      mimeType: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}mime_type'],
      )!,
      cryptoVersion: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}crypto_version'],
      )!,
      createdAt: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}created_at'],
      )!,
    );
  }

  @override
  $SecureAssetsTable createAlias(String alias) {
    return $SecureAssetsTable(attachedDatabase, alias);
  }
}

class SecureAsset extends DataClass implements Insertable<SecureAsset> {
  final String id;
  final String relativePath;
  final String assetType;
  final String mimeType;
  final int cryptoVersion;
  final int createdAt;
  const SecureAsset({
    required this.id,
    required this.relativePath,
    required this.assetType,
    required this.mimeType,
    required this.cryptoVersion,
    required this.createdAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['relative_path'] = Variable<String>(relativePath);
    map['asset_type'] = Variable<String>(assetType);
    map['mime_type'] = Variable<String>(mimeType);
    map['crypto_version'] = Variable<int>(cryptoVersion);
    map['created_at'] = Variable<int>(createdAt);
    return map;
  }

  SecureAssetsCompanion toCompanion(bool nullToAbsent) {
    return SecureAssetsCompanion(
      id: Value(id),
      relativePath: Value(relativePath),
      assetType: Value(assetType),
      mimeType: Value(mimeType),
      cryptoVersion: Value(cryptoVersion),
      createdAt: Value(createdAt),
    );
  }

  factory SecureAsset.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return SecureAsset(
      id: serializer.fromJson<String>(json['id']),
      relativePath: serializer.fromJson<String>(json['relativePath']),
      assetType: serializer.fromJson<String>(json['assetType']),
      mimeType: serializer.fromJson<String>(json['mimeType']),
      cryptoVersion: serializer.fromJson<int>(json['cryptoVersion']),
      createdAt: serializer.fromJson<int>(json['createdAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'relativePath': serializer.toJson<String>(relativePath),
      'assetType': serializer.toJson<String>(assetType),
      'mimeType': serializer.toJson<String>(mimeType),
      'cryptoVersion': serializer.toJson<int>(cryptoVersion),
      'createdAt': serializer.toJson<int>(createdAt),
    };
  }

  SecureAsset copyWith({
    String? id,
    String? relativePath,
    String? assetType,
    String? mimeType,
    int? cryptoVersion,
    int? createdAt,
  }) => SecureAsset(
    id: id ?? this.id,
    relativePath: relativePath ?? this.relativePath,
    assetType: assetType ?? this.assetType,
    mimeType: mimeType ?? this.mimeType,
    cryptoVersion: cryptoVersion ?? this.cryptoVersion,
    createdAt: createdAt ?? this.createdAt,
  );
  SecureAsset copyWithCompanion(SecureAssetsCompanion data) {
    return SecureAsset(
      id: data.id.present ? data.id.value : this.id,
      relativePath: data.relativePath.present
          ? data.relativePath.value
          : this.relativePath,
      assetType: data.assetType.present ? data.assetType.value : this.assetType,
      mimeType: data.mimeType.present ? data.mimeType.value : this.mimeType,
      cryptoVersion: data.cryptoVersion.present
          ? data.cryptoVersion.value
          : this.cryptoVersion,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('SecureAsset(')
          ..write('id: $id, ')
          ..write('relativePath: $relativePath, ')
          ..write('assetType: $assetType, ')
          ..write('mimeType: $mimeType, ')
          ..write('cryptoVersion: $cryptoVersion, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    relativePath,
    assetType,
    mimeType,
    cryptoVersion,
    createdAt,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is SecureAsset &&
          other.id == this.id &&
          other.relativePath == this.relativePath &&
          other.assetType == this.assetType &&
          other.mimeType == this.mimeType &&
          other.cryptoVersion == this.cryptoVersion &&
          other.createdAt == this.createdAt);
}

class SecureAssetsCompanion extends UpdateCompanion<SecureAsset> {
  final Value<String> id;
  final Value<String> relativePath;
  final Value<String> assetType;
  final Value<String> mimeType;
  final Value<int> cryptoVersion;
  final Value<int> createdAt;
  final Value<int> rowid;
  const SecureAssetsCompanion({
    this.id = const Value.absent(),
    this.relativePath = const Value.absent(),
    this.assetType = const Value.absent(),
    this.mimeType = const Value.absent(),
    this.cryptoVersion = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  SecureAssetsCompanion.insert({
    required String id,
    required String relativePath,
    required String assetType,
    required String mimeType,
    required int cryptoVersion,
    required int createdAt,
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       relativePath = Value(relativePath),
       assetType = Value(assetType),
       mimeType = Value(mimeType),
       cryptoVersion = Value(cryptoVersion),
       createdAt = Value(createdAt);
  static Insertable<SecureAsset> custom({
    Expression<String>? id,
    Expression<String>? relativePath,
    Expression<String>? assetType,
    Expression<String>? mimeType,
    Expression<int>? cryptoVersion,
    Expression<int>? createdAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (relativePath != null) 'relative_path': relativePath,
      if (assetType != null) 'asset_type': assetType,
      if (mimeType != null) 'mime_type': mimeType,
      if (cryptoVersion != null) 'crypto_version': cryptoVersion,
      if (createdAt != null) 'created_at': createdAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  SecureAssetsCompanion copyWith({
    Value<String>? id,
    Value<String>? relativePath,
    Value<String>? assetType,
    Value<String>? mimeType,
    Value<int>? cryptoVersion,
    Value<int>? createdAt,
    Value<int>? rowid,
  }) {
    return SecureAssetsCompanion(
      id: id ?? this.id,
      relativePath: relativePath ?? this.relativePath,
      assetType: assetType ?? this.assetType,
      mimeType: mimeType ?? this.mimeType,
      cryptoVersion: cryptoVersion ?? this.cryptoVersion,
      createdAt: createdAt ?? this.createdAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (relativePath.present) {
      map['relative_path'] = Variable<String>(relativePath.value);
    }
    if (assetType.present) {
      map['asset_type'] = Variable<String>(assetType.value);
    }
    if (mimeType.present) {
      map['mime_type'] = Variable<String>(mimeType.value);
    }
    if (cryptoVersion.present) {
      map['crypto_version'] = Variable<int>(cryptoVersion.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<int>(createdAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('SecureAssetsCompanion(')
          ..write('id: $id, ')
          ..write('relativePath: $relativePath, ')
          ..write('assetType: $assetType, ')
          ..write('mimeType: $mimeType, ')
          ..write('cryptoVersion: $cryptoVersion, ')
          ..write('createdAt: $createdAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $LocalObjectReferencesTable extends LocalObjectReferences
    with TableInfo<$LocalObjectReferencesTable, LocalObjectReference> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $LocalObjectReferencesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _objectIdMeta = const VerificationMeta(
    'objectId',
  );
  @override
  late final GeneratedColumn<String> objectId = GeneratedColumn<String>(
    'object_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'REFERENCES local_objects (id) ON DELETE CASCADE',
    ),
  );
  static const VerificationMeta _imageAssetIdMeta = const VerificationMeta(
    'imageAssetId',
  );
  @override
  late final GeneratedColumn<String> imageAssetId = GeneratedColumn<String>(
    'image_asset_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'REFERENCES secure_assets (id)',
    ),
  );
  static const VerificationMeta _roiLeftMeta = const VerificationMeta(
    'roiLeft',
  );
  @override
  late final GeneratedColumn<double> roiLeft = GeneratedColumn<double>(
    'roi_left',
    aliasedName,
    false,
    type: DriftSqlType.double,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _roiTopMeta = const VerificationMeta('roiTop');
  @override
  late final GeneratedColumn<double> roiTop = GeneratedColumn<double>(
    'roi_top',
    aliasedName,
    false,
    type: DriftSqlType.double,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _roiRightMeta = const VerificationMeta(
    'roiRight',
  );
  @override
  late final GeneratedColumn<double> roiRight = GeneratedColumn<double>(
    'roi_right',
    aliasedName,
    false,
    type: DriftSqlType.double,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _roiBottomMeta = const VerificationMeta(
    'roiBottom',
  );
  @override
  late final GeneratedColumn<double> roiBottom = GeneratedColumn<double>(
    'roi_bottom',
    aliasedName,
    false,
    type: DriftSqlType.double,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _qualityScoreMeta = const VerificationMeta(
    'qualityScore',
  );
  @override
  late final GeneratedColumn<double> qualityScore = GeneratedColumn<double>(
    'quality_score',
    aliasedName,
    false,
    type: DriftSqlType.double,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _createdAtMeta = const VerificationMeta(
    'createdAt',
  );
  @override
  late final GeneratedColumn<int> createdAt = GeneratedColumn<int>(
    'created_at',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    objectId,
    imageAssetId,
    roiLeft,
    roiTop,
    roiRight,
    roiBottom,
    qualityScore,
    createdAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'local_object_references';
  @override
  VerificationContext validateIntegrity(
    Insertable<LocalObjectReference> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('object_id')) {
      context.handle(
        _objectIdMeta,
        objectId.isAcceptableOrUnknown(data['object_id']!, _objectIdMeta),
      );
    } else if (isInserting) {
      context.missing(_objectIdMeta);
    }
    if (data.containsKey('image_asset_id')) {
      context.handle(
        _imageAssetIdMeta,
        imageAssetId.isAcceptableOrUnknown(
          data['image_asset_id']!,
          _imageAssetIdMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_imageAssetIdMeta);
    }
    if (data.containsKey('roi_left')) {
      context.handle(
        _roiLeftMeta,
        roiLeft.isAcceptableOrUnknown(data['roi_left']!, _roiLeftMeta),
      );
    } else if (isInserting) {
      context.missing(_roiLeftMeta);
    }
    if (data.containsKey('roi_top')) {
      context.handle(
        _roiTopMeta,
        roiTop.isAcceptableOrUnknown(data['roi_top']!, _roiTopMeta),
      );
    } else if (isInserting) {
      context.missing(_roiTopMeta);
    }
    if (data.containsKey('roi_right')) {
      context.handle(
        _roiRightMeta,
        roiRight.isAcceptableOrUnknown(data['roi_right']!, _roiRightMeta),
      );
    } else if (isInserting) {
      context.missing(_roiRightMeta);
    }
    if (data.containsKey('roi_bottom')) {
      context.handle(
        _roiBottomMeta,
        roiBottom.isAcceptableOrUnknown(data['roi_bottom']!, _roiBottomMeta),
      );
    } else if (isInserting) {
      context.missing(_roiBottomMeta);
    }
    if (data.containsKey('quality_score')) {
      context.handle(
        _qualityScoreMeta,
        qualityScore.isAcceptableOrUnknown(
          data['quality_score']!,
          _qualityScoreMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_qualityScoreMeta);
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    } else if (isInserting) {
      context.missing(_createdAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  LocalObjectReference map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return LocalObjectReference(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      objectId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}object_id'],
      )!,
      imageAssetId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}image_asset_id'],
      )!,
      roiLeft: attachedDatabase.typeMapping.read(
        DriftSqlType.double,
        data['${effectivePrefix}roi_left'],
      )!,
      roiTop: attachedDatabase.typeMapping.read(
        DriftSqlType.double,
        data['${effectivePrefix}roi_top'],
      )!,
      roiRight: attachedDatabase.typeMapping.read(
        DriftSqlType.double,
        data['${effectivePrefix}roi_right'],
      )!,
      roiBottom: attachedDatabase.typeMapping.read(
        DriftSqlType.double,
        data['${effectivePrefix}roi_bottom'],
      )!,
      qualityScore: attachedDatabase.typeMapping.read(
        DriftSqlType.double,
        data['${effectivePrefix}quality_score'],
      )!,
      createdAt: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}created_at'],
      )!,
    );
  }

  @override
  $LocalObjectReferencesTable createAlias(String alias) {
    return $LocalObjectReferencesTable(attachedDatabase, alias);
  }
}

class LocalObjectReference extends DataClass
    implements Insertable<LocalObjectReference> {
  final String id;
  final String objectId;
  final String imageAssetId;
  final double roiLeft;
  final double roiTop;
  final double roiRight;
  final double roiBottom;
  final double qualityScore;
  final int createdAt;
  const LocalObjectReference({
    required this.id,
    required this.objectId,
    required this.imageAssetId,
    required this.roiLeft,
    required this.roiTop,
    required this.roiRight,
    required this.roiBottom,
    required this.qualityScore,
    required this.createdAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['object_id'] = Variable<String>(objectId);
    map['image_asset_id'] = Variable<String>(imageAssetId);
    map['roi_left'] = Variable<double>(roiLeft);
    map['roi_top'] = Variable<double>(roiTop);
    map['roi_right'] = Variable<double>(roiRight);
    map['roi_bottom'] = Variable<double>(roiBottom);
    map['quality_score'] = Variable<double>(qualityScore);
    map['created_at'] = Variable<int>(createdAt);
    return map;
  }

  LocalObjectReferencesCompanion toCompanion(bool nullToAbsent) {
    return LocalObjectReferencesCompanion(
      id: Value(id),
      objectId: Value(objectId),
      imageAssetId: Value(imageAssetId),
      roiLeft: Value(roiLeft),
      roiTop: Value(roiTop),
      roiRight: Value(roiRight),
      roiBottom: Value(roiBottom),
      qualityScore: Value(qualityScore),
      createdAt: Value(createdAt),
    );
  }

  factory LocalObjectReference.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return LocalObjectReference(
      id: serializer.fromJson<String>(json['id']),
      objectId: serializer.fromJson<String>(json['objectId']),
      imageAssetId: serializer.fromJson<String>(json['imageAssetId']),
      roiLeft: serializer.fromJson<double>(json['roiLeft']),
      roiTop: serializer.fromJson<double>(json['roiTop']),
      roiRight: serializer.fromJson<double>(json['roiRight']),
      roiBottom: serializer.fromJson<double>(json['roiBottom']),
      qualityScore: serializer.fromJson<double>(json['qualityScore']),
      createdAt: serializer.fromJson<int>(json['createdAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'objectId': serializer.toJson<String>(objectId),
      'imageAssetId': serializer.toJson<String>(imageAssetId),
      'roiLeft': serializer.toJson<double>(roiLeft),
      'roiTop': serializer.toJson<double>(roiTop),
      'roiRight': serializer.toJson<double>(roiRight),
      'roiBottom': serializer.toJson<double>(roiBottom),
      'qualityScore': serializer.toJson<double>(qualityScore),
      'createdAt': serializer.toJson<int>(createdAt),
    };
  }

  LocalObjectReference copyWith({
    String? id,
    String? objectId,
    String? imageAssetId,
    double? roiLeft,
    double? roiTop,
    double? roiRight,
    double? roiBottom,
    double? qualityScore,
    int? createdAt,
  }) => LocalObjectReference(
    id: id ?? this.id,
    objectId: objectId ?? this.objectId,
    imageAssetId: imageAssetId ?? this.imageAssetId,
    roiLeft: roiLeft ?? this.roiLeft,
    roiTop: roiTop ?? this.roiTop,
    roiRight: roiRight ?? this.roiRight,
    roiBottom: roiBottom ?? this.roiBottom,
    qualityScore: qualityScore ?? this.qualityScore,
    createdAt: createdAt ?? this.createdAt,
  );
  LocalObjectReference copyWithCompanion(LocalObjectReferencesCompanion data) {
    return LocalObjectReference(
      id: data.id.present ? data.id.value : this.id,
      objectId: data.objectId.present ? data.objectId.value : this.objectId,
      imageAssetId: data.imageAssetId.present
          ? data.imageAssetId.value
          : this.imageAssetId,
      roiLeft: data.roiLeft.present ? data.roiLeft.value : this.roiLeft,
      roiTop: data.roiTop.present ? data.roiTop.value : this.roiTop,
      roiRight: data.roiRight.present ? data.roiRight.value : this.roiRight,
      roiBottom: data.roiBottom.present ? data.roiBottom.value : this.roiBottom,
      qualityScore: data.qualityScore.present
          ? data.qualityScore.value
          : this.qualityScore,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('LocalObjectReference(')
          ..write('id: $id, ')
          ..write('objectId: $objectId, ')
          ..write('imageAssetId: $imageAssetId, ')
          ..write('roiLeft: $roiLeft, ')
          ..write('roiTop: $roiTop, ')
          ..write('roiRight: $roiRight, ')
          ..write('roiBottom: $roiBottom, ')
          ..write('qualityScore: $qualityScore, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    objectId,
    imageAssetId,
    roiLeft,
    roiTop,
    roiRight,
    roiBottom,
    qualityScore,
    createdAt,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is LocalObjectReference &&
          other.id == this.id &&
          other.objectId == this.objectId &&
          other.imageAssetId == this.imageAssetId &&
          other.roiLeft == this.roiLeft &&
          other.roiTop == this.roiTop &&
          other.roiRight == this.roiRight &&
          other.roiBottom == this.roiBottom &&
          other.qualityScore == this.qualityScore &&
          other.createdAt == this.createdAt);
}

class LocalObjectReferencesCompanion
    extends UpdateCompanion<LocalObjectReference> {
  final Value<String> id;
  final Value<String> objectId;
  final Value<String> imageAssetId;
  final Value<double> roiLeft;
  final Value<double> roiTop;
  final Value<double> roiRight;
  final Value<double> roiBottom;
  final Value<double> qualityScore;
  final Value<int> createdAt;
  final Value<int> rowid;
  const LocalObjectReferencesCompanion({
    this.id = const Value.absent(),
    this.objectId = const Value.absent(),
    this.imageAssetId = const Value.absent(),
    this.roiLeft = const Value.absent(),
    this.roiTop = const Value.absent(),
    this.roiRight = const Value.absent(),
    this.roiBottom = const Value.absent(),
    this.qualityScore = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  LocalObjectReferencesCompanion.insert({
    required String id,
    required String objectId,
    required String imageAssetId,
    required double roiLeft,
    required double roiTop,
    required double roiRight,
    required double roiBottom,
    required double qualityScore,
    required int createdAt,
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       objectId = Value(objectId),
       imageAssetId = Value(imageAssetId),
       roiLeft = Value(roiLeft),
       roiTop = Value(roiTop),
       roiRight = Value(roiRight),
       roiBottom = Value(roiBottom),
       qualityScore = Value(qualityScore),
       createdAt = Value(createdAt);
  static Insertable<LocalObjectReference> custom({
    Expression<String>? id,
    Expression<String>? objectId,
    Expression<String>? imageAssetId,
    Expression<double>? roiLeft,
    Expression<double>? roiTop,
    Expression<double>? roiRight,
    Expression<double>? roiBottom,
    Expression<double>? qualityScore,
    Expression<int>? createdAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (objectId != null) 'object_id': objectId,
      if (imageAssetId != null) 'image_asset_id': imageAssetId,
      if (roiLeft != null) 'roi_left': roiLeft,
      if (roiTop != null) 'roi_top': roiTop,
      if (roiRight != null) 'roi_right': roiRight,
      if (roiBottom != null) 'roi_bottom': roiBottom,
      if (qualityScore != null) 'quality_score': qualityScore,
      if (createdAt != null) 'created_at': createdAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  LocalObjectReferencesCompanion copyWith({
    Value<String>? id,
    Value<String>? objectId,
    Value<String>? imageAssetId,
    Value<double>? roiLeft,
    Value<double>? roiTop,
    Value<double>? roiRight,
    Value<double>? roiBottom,
    Value<double>? qualityScore,
    Value<int>? createdAt,
    Value<int>? rowid,
  }) {
    return LocalObjectReferencesCompanion(
      id: id ?? this.id,
      objectId: objectId ?? this.objectId,
      imageAssetId: imageAssetId ?? this.imageAssetId,
      roiLeft: roiLeft ?? this.roiLeft,
      roiTop: roiTop ?? this.roiTop,
      roiRight: roiRight ?? this.roiRight,
      roiBottom: roiBottom ?? this.roiBottom,
      qualityScore: qualityScore ?? this.qualityScore,
      createdAt: createdAt ?? this.createdAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (objectId.present) {
      map['object_id'] = Variable<String>(objectId.value);
    }
    if (imageAssetId.present) {
      map['image_asset_id'] = Variable<String>(imageAssetId.value);
    }
    if (roiLeft.present) {
      map['roi_left'] = Variable<double>(roiLeft.value);
    }
    if (roiTop.present) {
      map['roi_top'] = Variable<double>(roiTop.value);
    }
    if (roiRight.present) {
      map['roi_right'] = Variable<double>(roiRight.value);
    }
    if (roiBottom.present) {
      map['roi_bottom'] = Variable<double>(roiBottom.value);
    }
    if (qualityScore.present) {
      map['quality_score'] = Variable<double>(qualityScore.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<int>(createdAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('LocalObjectReferencesCompanion(')
          ..write('id: $id, ')
          ..write('objectId: $objectId, ')
          ..write('imageAssetId: $imageAssetId, ')
          ..write('roiLeft: $roiLeft, ')
          ..write('roiTop: $roiTop, ')
          ..write('roiRight: $roiRight, ')
          ..write('roiBottom: $roiBottom, ')
          ..write('qualityScore: $qualityScore, ')
          ..write('createdAt: $createdAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $LocalReferenceEmbeddingsTable extends LocalReferenceEmbeddings
    with TableInfo<$LocalReferenceEmbeddingsTable, LocalReferenceEmbedding> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $LocalReferenceEmbeddingsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _referenceIdMeta = const VerificationMeta(
    'referenceId',
  );
  @override
  late final GeneratedColumn<String> referenceId = GeneratedColumn<String>(
    'reference_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'REFERENCES local_object_references (id) ON DELETE CASCADE',
    ),
  );
  static const VerificationMeta _modelNameMeta = const VerificationMeta(
    'modelName',
  );
  @override
  late final GeneratedColumn<String> modelName = GeneratedColumn<String>(
    'model_name',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _modelVersionMeta = const VerificationMeta(
    'modelVersion',
  );
  @override
  late final GeneratedColumn<String> modelVersion = GeneratedColumn<String>(
    'model_version',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _dimensionsMeta = const VerificationMeta(
    'dimensions',
  );
  @override
  late final GeneratedColumn<int> dimensions = GeneratedColumn<int>(
    'dimensions',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _encryptedVectorMeta = const VerificationMeta(
    'encryptedVector',
  );
  @override
  late final GeneratedColumn<Uint8List> encryptedVector =
      GeneratedColumn<Uint8List>(
        'encrypted_vector',
        aliasedName,
        false,
        type: DriftSqlType.blob,
        requiredDuringInsert: true,
      );
  static const VerificationMeta _vectorNonceMeta = const VerificationMeta(
    'vectorNonce',
  );
  @override
  late final GeneratedColumn<Uint8List> vectorNonce =
      GeneratedColumn<Uint8List>(
        'vector_nonce',
        aliasedName,
        false,
        type: DriftSqlType.blob,
        requiredDuringInsert: true,
      );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    referenceId,
    modelName,
    modelVersion,
    dimensions,
    encryptedVector,
    vectorNonce,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'local_reference_embeddings';
  @override
  VerificationContext validateIntegrity(
    Insertable<LocalReferenceEmbedding> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('reference_id')) {
      context.handle(
        _referenceIdMeta,
        referenceId.isAcceptableOrUnknown(
          data['reference_id']!,
          _referenceIdMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_referenceIdMeta);
    }
    if (data.containsKey('model_name')) {
      context.handle(
        _modelNameMeta,
        modelName.isAcceptableOrUnknown(data['model_name']!, _modelNameMeta),
      );
    } else if (isInserting) {
      context.missing(_modelNameMeta);
    }
    if (data.containsKey('model_version')) {
      context.handle(
        _modelVersionMeta,
        modelVersion.isAcceptableOrUnknown(
          data['model_version']!,
          _modelVersionMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_modelVersionMeta);
    }
    if (data.containsKey('dimensions')) {
      context.handle(
        _dimensionsMeta,
        dimensions.isAcceptableOrUnknown(data['dimensions']!, _dimensionsMeta),
      );
    } else if (isInserting) {
      context.missing(_dimensionsMeta);
    }
    if (data.containsKey('encrypted_vector')) {
      context.handle(
        _encryptedVectorMeta,
        encryptedVector.isAcceptableOrUnknown(
          data['encrypted_vector']!,
          _encryptedVectorMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_encryptedVectorMeta);
    }
    if (data.containsKey('vector_nonce')) {
      context.handle(
        _vectorNonceMeta,
        vectorNonce.isAcceptableOrUnknown(
          data['vector_nonce']!,
          _vectorNonceMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_vectorNonceMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  LocalReferenceEmbedding map(
    Map<String, dynamic> data, {
    String? tablePrefix,
  }) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return LocalReferenceEmbedding(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      referenceId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}reference_id'],
      )!,
      modelName: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}model_name'],
      )!,
      modelVersion: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}model_version'],
      )!,
      dimensions: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}dimensions'],
      )!,
      encryptedVector: attachedDatabase.typeMapping.read(
        DriftSqlType.blob,
        data['${effectivePrefix}encrypted_vector'],
      )!,
      vectorNonce: attachedDatabase.typeMapping.read(
        DriftSqlType.blob,
        data['${effectivePrefix}vector_nonce'],
      )!,
    );
  }

  @override
  $LocalReferenceEmbeddingsTable createAlias(String alias) {
    return $LocalReferenceEmbeddingsTable(attachedDatabase, alias);
  }
}

class LocalReferenceEmbedding extends DataClass
    implements Insertable<LocalReferenceEmbedding> {
  final String id;
  final String referenceId;
  final String modelName;
  final String modelVersion;
  final int dimensions;
  final Uint8List encryptedVector;
  final Uint8List vectorNonce;
  const LocalReferenceEmbedding({
    required this.id,
    required this.referenceId,
    required this.modelName,
    required this.modelVersion,
    required this.dimensions,
    required this.encryptedVector,
    required this.vectorNonce,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['reference_id'] = Variable<String>(referenceId);
    map['model_name'] = Variable<String>(modelName);
    map['model_version'] = Variable<String>(modelVersion);
    map['dimensions'] = Variable<int>(dimensions);
    map['encrypted_vector'] = Variable<Uint8List>(encryptedVector);
    map['vector_nonce'] = Variable<Uint8List>(vectorNonce);
    return map;
  }

  LocalReferenceEmbeddingsCompanion toCompanion(bool nullToAbsent) {
    return LocalReferenceEmbeddingsCompanion(
      id: Value(id),
      referenceId: Value(referenceId),
      modelName: Value(modelName),
      modelVersion: Value(modelVersion),
      dimensions: Value(dimensions),
      encryptedVector: Value(encryptedVector),
      vectorNonce: Value(vectorNonce),
    );
  }

  factory LocalReferenceEmbedding.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return LocalReferenceEmbedding(
      id: serializer.fromJson<String>(json['id']),
      referenceId: serializer.fromJson<String>(json['referenceId']),
      modelName: serializer.fromJson<String>(json['modelName']),
      modelVersion: serializer.fromJson<String>(json['modelVersion']),
      dimensions: serializer.fromJson<int>(json['dimensions']),
      encryptedVector: serializer.fromJson<Uint8List>(json['encryptedVector']),
      vectorNonce: serializer.fromJson<Uint8List>(json['vectorNonce']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'referenceId': serializer.toJson<String>(referenceId),
      'modelName': serializer.toJson<String>(modelName),
      'modelVersion': serializer.toJson<String>(modelVersion),
      'dimensions': serializer.toJson<int>(dimensions),
      'encryptedVector': serializer.toJson<Uint8List>(encryptedVector),
      'vectorNonce': serializer.toJson<Uint8List>(vectorNonce),
    };
  }

  LocalReferenceEmbedding copyWith({
    String? id,
    String? referenceId,
    String? modelName,
    String? modelVersion,
    int? dimensions,
    Uint8List? encryptedVector,
    Uint8List? vectorNonce,
  }) => LocalReferenceEmbedding(
    id: id ?? this.id,
    referenceId: referenceId ?? this.referenceId,
    modelName: modelName ?? this.modelName,
    modelVersion: modelVersion ?? this.modelVersion,
    dimensions: dimensions ?? this.dimensions,
    encryptedVector: encryptedVector ?? this.encryptedVector,
    vectorNonce: vectorNonce ?? this.vectorNonce,
  );
  LocalReferenceEmbedding copyWithCompanion(
    LocalReferenceEmbeddingsCompanion data,
  ) {
    return LocalReferenceEmbedding(
      id: data.id.present ? data.id.value : this.id,
      referenceId: data.referenceId.present
          ? data.referenceId.value
          : this.referenceId,
      modelName: data.modelName.present ? data.modelName.value : this.modelName,
      modelVersion: data.modelVersion.present
          ? data.modelVersion.value
          : this.modelVersion,
      dimensions: data.dimensions.present
          ? data.dimensions.value
          : this.dimensions,
      encryptedVector: data.encryptedVector.present
          ? data.encryptedVector.value
          : this.encryptedVector,
      vectorNonce: data.vectorNonce.present
          ? data.vectorNonce.value
          : this.vectorNonce,
    );
  }

  @override
  String toString() {
    return (StringBuffer('LocalReferenceEmbedding(')
          ..write('id: $id, ')
          ..write('referenceId: $referenceId, ')
          ..write('modelName: $modelName, ')
          ..write('modelVersion: $modelVersion, ')
          ..write('dimensions: $dimensions, ')
          ..write('encryptedVector: $encryptedVector, ')
          ..write('vectorNonce: $vectorNonce')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    referenceId,
    modelName,
    modelVersion,
    dimensions,
    $driftBlobEquality.hash(encryptedVector),
    $driftBlobEquality.hash(vectorNonce),
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is LocalReferenceEmbedding &&
          other.id == this.id &&
          other.referenceId == this.referenceId &&
          other.modelName == this.modelName &&
          other.modelVersion == this.modelVersion &&
          other.dimensions == this.dimensions &&
          $driftBlobEquality.equals(
            other.encryptedVector,
            this.encryptedVector,
          ) &&
          $driftBlobEquality.equals(other.vectorNonce, this.vectorNonce));
}

class LocalReferenceEmbeddingsCompanion
    extends UpdateCompanion<LocalReferenceEmbedding> {
  final Value<String> id;
  final Value<String> referenceId;
  final Value<String> modelName;
  final Value<String> modelVersion;
  final Value<int> dimensions;
  final Value<Uint8List> encryptedVector;
  final Value<Uint8List> vectorNonce;
  final Value<int> rowid;
  const LocalReferenceEmbeddingsCompanion({
    this.id = const Value.absent(),
    this.referenceId = const Value.absent(),
    this.modelName = const Value.absent(),
    this.modelVersion = const Value.absent(),
    this.dimensions = const Value.absent(),
    this.encryptedVector = const Value.absent(),
    this.vectorNonce = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  LocalReferenceEmbeddingsCompanion.insert({
    required String id,
    required String referenceId,
    required String modelName,
    required String modelVersion,
    required int dimensions,
    required Uint8List encryptedVector,
    required Uint8List vectorNonce,
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       referenceId = Value(referenceId),
       modelName = Value(modelName),
       modelVersion = Value(modelVersion),
       dimensions = Value(dimensions),
       encryptedVector = Value(encryptedVector),
       vectorNonce = Value(vectorNonce);
  static Insertable<LocalReferenceEmbedding> custom({
    Expression<String>? id,
    Expression<String>? referenceId,
    Expression<String>? modelName,
    Expression<String>? modelVersion,
    Expression<int>? dimensions,
    Expression<Uint8List>? encryptedVector,
    Expression<Uint8List>? vectorNonce,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (referenceId != null) 'reference_id': referenceId,
      if (modelName != null) 'model_name': modelName,
      if (modelVersion != null) 'model_version': modelVersion,
      if (dimensions != null) 'dimensions': dimensions,
      if (encryptedVector != null) 'encrypted_vector': encryptedVector,
      if (vectorNonce != null) 'vector_nonce': vectorNonce,
      if (rowid != null) 'rowid': rowid,
    });
  }

  LocalReferenceEmbeddingsCompanion copyWith({
    Value<String>? id,
    Value<String>? referenceId,
    Value<String>? modelName,
    Value<String>? modelVersion,
    Value<int>? dimensions,
    Value<Uint8List>? encryptedVector,
    Value<Uint8List>? vectorNonce,
    Value<int>? rowid,
  }) {
    return LocalReferenceEmbeddingsCompanion(
      id: id ?? this.id,
      referenceId: referenceId ?? this.referenceId,
      modelName: modelName ?? this.modelName,
      modelVersion: modelVersion ?? this.modelVersion,
      dimensions: dimensions ?? this.dimensions,
      encryptedVector: encryptedVector ?? this.encryptedVector,
      vectorNonce: vectorNonce ?? this.vectorNonce,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (referenceId.present) {
      map['reference_id'] = Variable<String>(referenceId.value);
    }
    if (modelName.present) {
      map['model_name'] = Variable<String>(modelName.value);
    }
    if (modelVersion.present) {
      map['model_version'] = Variable<String>(modelVersion.value);
    }
    if (dimensions.present) {
      map['dimensions'] = Variable<int>(dimensions.value);
    }
    if (encryptedVector.present) {
      map['encrypted_vector'] = Variable<Uint8List>(encryptedVector.value);
    }
    if (vectorNonce.present) {
      map['vector_nonce'] = Variable<Uint8List>(vectorNonce.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('LocalReferenceEmbeddingsCompanion(')
          ..write('id: $id, ')
          ..write('referenceId: $referenceId, ')
          ..write('modelName: $modelName, ')
          ..write('modelVersion: $modelVersion, ')
          ..write('dimensions: $dimensions, ')
          ..write('encryptedVector: $encryptedVector, ')
          ..write('vectorNonce: $vectorNonce, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $LocalSightingsTable extends LocalSightings
    with TableInfo<$LocalSightingsTable, LocalSighting> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $LocalSightingsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _objectIdMeta = const VerificationMeta(
    'objectId',
  );
  @override
  late final GeneratedColumn<String> objectId = GeneratedColumn<String>(
    'object_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'REFERENCES local_objects (id) ON DELETE CASCADE',
    ),
  );
  static const VerificationMeta _detectedAtMeta = const VerificationMeta(
    'detectedAt',
  );
  @override
  late final GeneratedColumn<int> detectedAt = GeneratedColumn<int>(
    'detected_at',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _encryptedLocationMeta = const VerificationMeta(
    'encryptedLocation',
  );
  @override
  late final GeneratedColumn<Uint8List> encryptedLocation =
      GeneratedColumn<Uint8List>(
        'encrypted_location',
        aliasedName,
        true,
        type: DriftSqlType.blob,
        requiredDuringInsert: false,
      );
  static const VerificationMeta _locationNonceMeta = const VerificationMeta(
    'locationNonce',
  );
  @override
  late final GeneratedColumn<Uint8List> locationNonce =
      GeneratedColumn<Uint8List>(
        'location_nonce',
        aliasedName,
        true,
        type: DriftSqlType.blob,
        requiredDuringInsert: false,
      );
  static const VerificationMeta _confidenceMeta = const VerificationMeta(
    'confidence',
  );
  @override
  late final GeneratedColumn<double> confidence = GeneratedColumn<double>(
    'confidence',
    aliasedName,
    false,
    type: DriftSqlType.double,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _evidenceAssetIdMeta = const VerificationMeta(
    'evidenceAssetId',
  );
  @override
  late final GeneratedColumn<String> evidenceAssetId = GeneratedColumn<String>(
    'evidence_asset_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'REFERENCES secure_assets (id)',
    ),
  );
  static const VerificationMeta _syncPendingMeta = const VerificationMeta(
    'syncPending',
  );
  @override
  late final GeneratedColumn<bool> syncPending = GeneratedColumn<bool>(
    'sync_pending',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("sync_pending" IN (0, 1))',
    ),
    defaultValue: const Constant(true),
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    objectId,
    detectedAt,
    encryptedLocation,
    locationNonce,
    confidence,
    evidenceAssetId,
    syncPending,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'local_sightings';
  @override
  VerificationContext validateIntegrity(
    Insertable<LocalSighting> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('object_id')) {
      context.handle(
        _objectIdMeta,
        objectId.isAcceptableOrUnknown(data['object_id']!, _objectIdMeta),
      );
    } else if (isInserting) {
      context.missing(_objectIdMeta);
    }
    if (data.containsKey('detected_at')) {
      context.handle(
        _detectedAtMeta,
        detectedAt.isAcceptableOrUnknown(data['detected_at']!, _detectedAtMeta),
      );
    } else if (isInserting) {
      context.missing(_detectedAtMeta);
    }
    if (data.containsKey('encrypted_location')) {
      context.handle(
        _encryptedLocationMeta,
        encryptedLocation.isAcceptableOrUnknown(
          data['encrypted_location']!,
          _encryptedLocationMeta,
        ),
      );
    }
    if (data.containsKey('location_nonce')) {
      context.handle(
        _locationNonceMeta,
        locationNonce.isAcceptableOrUnknown(
          data['location_nonce']!,
          _locationNonceMeta,
        ),
      );
    }
    if (data.containsKey('confidence')) {
      context.handle(
        _confidenceMeta,
        confidence.isAcceptableOrUnknown(data['confidence']!, _confidenceMeta),
      );
    } else if (isInserting) {
      context.missing(_confidenceMeta);
    }
    if (data.containsKey('evidence_asset_id')) {
      context.handle(
        _evidenceAssetIdMeta,
        evidenceAssetId.isAcceptableOrUnknown(
          data['evidence_asset_id']!,
          _evidenceAssetIdMeta,
        ),
      );
    }
    if (data.containsKey('sync_pending')) {
      context.handle(
        _syncPendingMeta,
        syncPending.isAcceptableOrUnknown(
          data['sync_pending']!,
          _syncPendingMeta,
        ),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  LocalSighting map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return LocalSighting(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      objectId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}object_id'],
      )!,
      detectedAt: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}detected_at'],
      )!,
      encryptedLocation: attachedDatabase.typeMapping.read(
        DriftSqlType.blob,
        data['${effectivePrefix}encrypted_location'],
      ),
      locationNonce: attachedDatabase.typeMapping.read(
        DriftSqlType.blob,
        data['${effectivePrefix}location_nonce'],
      ),
      confidence: attachedDatabase.typeMapping.read(
        DriftSqlType.double,
        data['${effectivePrefix}confidence'],
      )!,
      evidenceAssetId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}evidence_asset_id'],
      ),
      syncPending: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}sync_pending'],
      )!,
    );
  }

  @override
  $LocalSightingsTable createAlias(String alias) {
    return $LocalSightingsTable(attachedDatabase, alias);
  }
}

class LocalSighting extends DataClass implements Insertable<LocalSighting> {
  final String id;
  final String objectId;
  final int detectedAt;
  final Uint8List? encryptedLocation;
  final Uint8List? locationNonce;
  final double confidence;
  final String? evidenceAssetId;
  final bool syncPending;
  const LocalSighting({
    required this.id,
    required this.objectId,
    required this.detectedAt,
    this.encryptedLocation,
    this.locationNonce,
    required this.confidence,
    this.evidenceAssetId,
    required this.syncPending,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['object_id'] = Variable<String>(objectId);
    map['detected_at'] = Variable<int>(detectedAt);
    if (!nullToAbsent || encryptedLocation != null) {
      map['encrypted_location'] = Variable<Uint8List>(encryptedLocation);
    }
    if (!nullToAbsent || locationNonce != null) {
      map['location_nonce'] = Variable<Uint8List>(locationNonce);
    }
    map['confidence'] = Variable<double>(confidence);
    if (!nullToAbsent || evidenceAssetId != null) {
      map['evidence_asset_id'] = Variable<String>(evidenceAssetId);
    }
    map['sync_pending'] = Variable<bool>(syncPending);
    return map;
  }

  LocalSightingsCompanion toCompanion(bool nullToAbsent) {
    return LocalSightingsCompanion(
      id: Value(id),
      objectId: Value(objectId),
      detectedAt: Value(detectedAt),
      encryptedLocation: encryptedLocation == null && nullToAbsent
          ? const Value.absent()
          : Value(encryptedLocation),
      locationNonce: locationNonce == null && nullToAbsent
          ? const Value.absent()
          : Value(locationNonce),
      confidence: Value(confidence),
      evidenceAssetId: evidenceAssetId == null && nullToAbsent
          ? const Value.absent()
          : Value(evidenceAssetId),
      syncPending: Value(syncPending),
    );
  }

  factory LocalSighting.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return LocalSighting(
      id: serializer.fromJson<String>(json['id']),
      objectId: serializer.fromJson<String>(json['objectId']),
      detectedAt: serializer.fromJson<int>(json['detectedAt']),
      encryptedLocation: serializer.fromJson<Uint8List?>(
        json['encryptedLocation'],
      ),
      locationNonce: serializer.fromJson<Uint8List?>(json['locationNonce']),
      confidence: serializer.fromJson<double>(json['confidence']),
      evidenceAssetId: serializer.fromJson<String?>(json['evidenceAssetId']),
      syncPending: serializer.fromJson<bool>(json['syncPending']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'objectId': serializer.toJson<String>(objectId),
      'detectedAt': serializer.toJson<int>(detectedAt),
      'encryptedLocation': serializer.toJson<Uint8List?>(encryptedLocation),
      'locationNonce': serializer.toJson<Uint8List?>(locationNonce),
      'confidence': serializer.toJson<double>(confidence),
      'evidenceAssetId': serializer.toJson<String?>(evidenceAssetId),
      'syncPending': serializer.toJson<bool>(syncPending),
    };
  }

  LocalSighting copyWith({
    String? id,
    String? objectId,
    int? detectedAt,
    Value<Uint8List?> encryptedLocation = const Value.absent(),
    Value<Uint8List?> locationNonce = const Value.absent(),
    double? confidence,
    Value<String?> evidenceAssetId = const Value.absent(),
    bool? syncPending,
  }) => LocalSighting(
    id: id ?? this.id,
    objectId: objectId ?? this.objectId,
    detectedAt: detectedAt ?? this.detectedAt,
    encryptedLocation: encryptedLocation.present
        ? encryptedLocation.value
        : this.encryptedLocation,
    locationNonce: locationNonce.present
        ? locationNonce.value
        : this.locationNonce,
    confidence: confidence ?? this.confidence,
    evidenceAssetId: evidenceAssetId.present
        ? evidenceAssetId.value
        : this.evidenceAssetId,
    syncPending: syncPending ?? this.syncPending,
  );
  LocalSighting copyWithCompanion(LocalSightingsCompanion data) {
    return LocalSighting(
      id: data.id.present ? data.id.value : this.id,
      objectId: data.objectId.present ? data.objectId.value : this.objectId,
      detectedAt: data.detectedAt.present
          ? data.detectedAt.value
          : this.detectedAt,
      encryptedLocation: data.encryptedLocation.present
          ? data.encryptedLocation.value
          : this.encryptedLocation,
      locationNonce: data.locationNonce.present
          ? data.locationNonce.value
          : this.locationNonce,
      confidence: data.confidence.present
          ? data.confidence.value
          : this.confidence,
      evidenceAssetId: data.evidenceAssetId.present
          ? data.evidenceAssetId.value
          : this.evidenceAssetId,
      syncPending: data.syncPending.present
          ? data.syncPending.value
          : this.syncPending,
    );
  }

  @override
  String toString() {
    return (StringBuffer('LocalSighting(')
          ..write('id: $id, ')
          ..write('objectId: $objectId, ')
          ..write('detectedAt: $detectedAt, ')
          ..write('encryptedLocation: $encryptedLocation, ')
          ..write('locationNonce: $locationNonce, ')
          ..write('confidence: $confidence, ')
          ..write('evidenceAssetId: $evidenceAssetId, ')
          ..write('syncPending: $syncPending')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    objectId,
    detectedAt,
    $driftBlobEquality.hash(encryptedLocation),
    $driftBlobEquality.hash(locationNonce),
    confidence,
    evidenceAssetId,
    syncPending,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is LocalSighting &&
          other.id == this.id &&
          other.objectId == this.objectId &&
          other.detectedAt == this.detectedAt &&
          $driftBlobEquality.equals(
            other.encryptedLocation,
            this.encryptedLocation,
          ) &&
          $driftBlobEquality.equals(other.locationNonce, this.locationNonce) &&
          other.confidence == this.confidence &&
          other.evidenceAssetId == this.evidenceAssetId &&
          other.syncPending == this.syncPending);
}

class LocalSightingsCompanion extends UpdateCompanion<LocalSighting> {
  final Value<String> id;
  final Value<String> objectId;
  final Value<int> detectedAt;
  final Value<Uint8List?> encryptedLocation;
  final Value<Uint8List?> locationNonce;
  final Value<double> confidence;
  final Value<String?> evidenceAssetId;
  final Value<bool> syncPending;
  final Value<int> rowid;
  const LocalSightingsCompanion({
    this.id = const Value.absent(),
    this.objectId = const Value.absent(),
    this.detectedAt = const Value.absent(),
    this.encryptedLocation = const Value.absent(),
    this.locationNonce = const Value.absent(),
    this.confidence = const Value.absent(),
    this.evidenceAssetId = const Value.absent(),
    this.syncPending = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  LocalSightingsCompanion.insert({
    required String id,
    required String objectId,
    required int detectedAt,
    this.encryptedLocation = const Value.absent(),
    this.locationNonce = const Value.absent(),
    required double confidence,
    this.evidenceAssetId = const Value.absent(),
    this.syncPending = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       objectId = Value(objectId),
       detectedAt = Value(detectedAt),
       confidence = Value(confidence);
  static Insertable<LocalSighting> custom({
    Expression<String>? id,
    Expression<String>? objectId,
    Expression<int>? detectedAt,
    Expression<Uint8List>? encryptedLocation,
    Expression<Uint8List>? locationNonce,
    Expression<double>? confidence,
    Expression<String>? evidenceAssetId,
    Expression<bool>? syncPending,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (objectId != null) 'object_id': objectId,
      if (detectedAt != null) 'detected_at': detectedAt,
      if (encryptedLocation != null) 'encrypted_location': encryptedLocation,
      if (locationNonce != null) 'location_nonce': locationNonce,
      if (confidence != null) 'confidence': confidence,
      if (evidenceAssetId != null) 'evidence_asset_id': evidenceAssetId,
      if (syncPending != null) 'sync_pending': syncPending,
      if (rowid != null) 'rowid': rowid,
    });
  }

  LocalSightingsCompanion copyWith({
    Value<String>? id,
    Value<String>? objectId,
    Value<int>? detectedAt,
    Value<Uint8List?>? encryptedLocation,
    Value<Uint8List?>? locationNonce,
    Value<double>? confidence,
    Value<String?>? evidenceAssetId,
    Value<bool>? syncPending,
    Value<int>? rowid,
  }) {
    return LocalSightingsCompanion(
      id: id ?? this.id,
      objectId: objectId ?? this.objectId,
      detectedAt: detectedAt ?? this.detectedAt,
      encryptedLocation: encryptedLocation ?? this.encryptedLocation,
      locationNonce: locationNonce ?? this.locationNonce,
      confidence: confidence ?? this.confidence,
      evidenceAssetId: evidenceAssetId ?? this.evidenceAssetId,
      syncPending: syncPending ?? this.syncPending,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (objectId.present) {
      map['object_id'] = Variable<String>(objectId.value);
    }
    if (detectedAt.present) {
      map['detected_at'] = Variable<int>(detectedAt.value);
    }
    if (encryptedLocation.present) {
      map['encrypted_location'] = Variable<Uint8List>(encryptedLocation.value);
    }
    if (locationNonce.present) {
      map['location_nonce'] = Variable<Uint8List>(locationNonce.value);
    }
    if (confidence.present) {
      map['confidence'] = Variable<double>(confidence.value);
    }
    if (evidenceAssetId.present) {
      map['evidence_asset_id'] = Variable<String>(evidenceAssetId.value);
    }
    if (syncPending.present) {
      map['sync_pending'] = Variable<bool>(syncPending.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('LocalSightingsCompanion(')
          ..write('id: $id, ')
          ..write('objectId: $objectId, ')
          ..write('detectedAt: $detectedAt, ')
          ..write('encryptedLocation: $encryptedLocation, ')
          ..write('locationNonce: $locationNonce, ')
          ..write('confidence: $confidence, ')
          ..write('evidenceAssetId: $evidenceAssetId, ')
          ..write('syncPending: $syncPending, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

abstract class _$TraceDatabase extends GeneratedDatabase {
  _$TraceDatabase(QueryExecutor e) : super(e);
  $TraceDatabaseManager get managers => $TraceDatabaseManager(this);
  late final $LocalObjectsTable localObjects = $LocalObjectsTable(this);
  late final $SecureAssetsTable secureAssets = $SecureAssetsTable(this);
  late final $LocalObjectReferencesTable localObjectReferences =
      $LocalObjectReferencesTable(this);
  late final $LocalReferenceEmbeddingsTable localReferenceEmbeddings =
      $LocalReferenceEmbeddingsTable(this);
  late final $LocalSightingsTable localSightings = $LocalSightingsTable(this);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
    localObjects,
    secureAssets,
    localObjectReferences,
    localReferenceEmbeddings,
    localSightings,
  ];
  @override
  StreamQueryUpdateRules get streamUpdateRules => const StreamQueryUpdateRules([
    WritePropagation(
      on: TableUpdateQuery.onTableName(
        'local_objects',
        limitUpdateKind: UpdateKind.delete,
      ),
      result: [TableUpdate('local_object_references', kind: UpdateKind.delete)],
    ),
    WritePropagation(
      on: TableUpdateQuery.onTableName(
        'local_object_references',
        limitUpdateKind: UpdateKind.delete,
      ),
      result: [
        TableUpdate('local_reference_embeddings', kind: UpdateKind.delete),
      ],
    ),
    WritePropagation(
      on: TableUpdateQuery.onTableName(
        'local_objects',
        limitUpdateKind: UpdateKind.delete,
      ),
      result: [TableUpdate('local_sightings', kind: UpdateKind.delete)],
    ),
  ]);
}

typedef $$LocalObjectsTableCreateCompanionBuilder =
    LocalObjectsCompanion Function({
      required String id,
      required Uint8List encryptedTag,
      required Uint8List tagNonce,
      Value<int> referenceRevision,
      required int createdAt,
      required int updatedAt,
      Value<bool> enabled,
      Value<bool> syncPending,
      Value<int> rowid,
    });
typedef $$LocalObjectsTableUpdateCompanionBuilder =
    LocalObjectsCompanion Function({
      Value<String> id,
      Value<Uint8List> encryptedTag,
      Value<Uint8List> tagNonce,
      Value<int> referenceRevision,
      Value<int> createdAt,
      Value<int> updatedAt,
      Value<bool> enabled,
      Value<bool> syncPending,
      Value<int> rowid,
    });

final class $$LocalObjectsTableReferences
    extends BaseReferences<_$TraceDatabase, $LocalObjectsTable, LocalObject> {
  $$LocalObjectsTableReferences(super.$_db, super.$_table, super.$_typedResult);

  static MultiTypedResultKey<
    $LocalObjectReferencesTable,
    List<LocalObjectReference>
  >
  _localObjectReferencesRefsTable(_$TraceDatabase db) =>
      MultiTypedResultKey.fromTable(
        db.localObjectReferences,
        aliasName: 'local_objects__id__local_object_references__object_id',
      );

  $$LocalObjectReferencesTableProcessedTableManager
  get localObjectReferencesRefs {
    final manager = $$LocalObjectReferencesTableTableManager(
      $_db,
      $_db.localObjectReferences,
    ).filter((f) => f.objectId.id.sqlEquals($_itemColumn<String>('id')!));

    final cache = $_typedResult.readTableOrNull(
      _localObjectReferencesRefsTable($_db),
    );
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: cache),
    );
  }

  static MultiTypedResultKey<$LocalSightingsTable, List<LocalSighting>>
  _localSightingsRefsTable(_$TraceDatabase db) => MultiTypedResultKey.fromTable(
    db.localSightings,
    aliasName: 'local_objects__id__local_sightings__object_id',
  );

  $$LocalSightingsTableProcessedTableManager get localSightingsRefs {
    final manager = $$LocalSightingsTableTableManager(
      $_db,
      $_db.localSightings,
    ).filter((f) => f.objectId.id.sqlEquals($_itemColumn<String>('id')!));

    final cache = $_typedResult.readTableOrNull(_localSightingsRefsTable($_db));
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: cache),
    );
  }
}

class $$LocalObjectsTableFilterComposer
    extends Composer<_$TraceDatabase, $LocalObjectsTable> {
  $$LocalObjectsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<Uint8List> get encryptedTag => $composableBuilder(
    column: $table.encryptedTag,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<Uint8List> get tagNonce => $composableBuilder(
    column: $table.tagNonce,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get referenceRevision => $composableBuilder(
    column: $table.referenceRevision,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get enabled => $composableBuilder(
    column: $table.enabled,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get syncPending => $composableBuilder(
    column: $table.syncPending,
    builder: (column) => ColumnFilters(column),
  );

  Expression<bool> localObjectReferencesRefs(
    Expression<bool> Function($$LocalObjectReferencesTableFilterComposer f) f,
  ) {
    final $$LocalObjectReferencesTableFilterComposer composer =
        $composerBuilder(
          composer: this,
          getCurrentColumn: (t) => t.id,
          referencedTable: $db.localObjectReferences,
          getReferencedColumn: (t) => t.objectId,
          builder:
              (
                joinBuilder, {
                $addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer,
              }) => $$LocalObjectReferencesTableFilterComposer(
                $db: $db,
                $table: $db.localObjectReferences,
                $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                joinBuilder: joinBuilder,
                $removeJoinBuilderFromRootComposer:
                    $removeJoinBuilderFromRootComposer,
              ),
        );
    return f(composer);
  }

  Expression<bool> localSightingsRefs(
    Expression<bool> Function($$LocalSightingsTableFilterComposer f) f,
  ) {
    final $$LocalSightingsTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.localSightings,
      getReferencedColumn: (t) => t.objectId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$LocalSightingsTableFilterComposer(
            $db: $db,
            $table: $db.localSightings,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }
}

class $$LocalObjectsTableOrderingComposer
    extends Composer<_$TraceDatabase, $LocalObjectsTable> {
  $$LocalObjectsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<Uint8List> get encryptedTag => $composableBuilder(
    column: $table.encryptedTag,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<Uint8List> get tagNonce => $composableBuilder(
    column: $table.tagNonce,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get referenceRevision => $composableBuilder(
    column: $table.referenceRevision,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get enabled => $composableBuilder(
    column: $table.enabled,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get syncPending => $composableBuilder(
    column: $table.syncPending,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$LocalObjectsTableAnnotationComposer
    extends Composer<_$TraceDatabase, $LocalObjectsTable> {
  $$LocalObjectsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<Uint8List> get encryptedTag => $composableBuilder(
    column: $table.encryptedTag,
    builder: (column) => column,
  );

  GeneratedColumn<Uint8List> get tagNonce =>
      $composableBuilder(column: $table.tagNonce, builder: (column) => column);

  GeneratedColumn<int> get referenceRevision => $composableBuilder(
    column: $table.referenceRevision,
    builder: (column) => column,
  );

  GeneratedColumn<int> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  GeneratedColumn<int> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);

  GeneratedColumn<bool> get enabled =>
      $composableBuilder(column: $table.enabled, builder: (column) => column);

  GeneratedColumn<bool> get syncPending => $composableBuilder(
    column: $table.syncPending,
    builder: (column) => column,
  );

  Expression<T> localObjectReferencesRefs<T extends Object>(
    Expression<T> Function($$LocalObjectReferencesTableAnnotationComposer a) f,
  ) {
    final $$LocalObjectReferencesTableAnnotationComposer composer =
        $composerBuilder(
          composer: this,
          getCurrentColumn: (t) => t.id,
          referencedTable: $db.localObjectReferences,
          getReferencedColumn: (t) => t.objectId,
          builder:
              (
                joinBuilder, {
                $addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer,
              }) => $$LocalObjectReferencesTableAnnotationComposer(
                $db: $db,
                $table: $db.localObjectReferences,
                $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                joinBuilder: joinBuilder,
                $removeJoinBuilderFromRootComposer:
                    $removeJoinBuilderFromRootComposer,
              ),
        );
    return f(composer);
  }

  Expression<T> localSightingsRefs<T extends Object>(
    Expression<T> Function($$LocalSightingsTableAnnotationComposer a) f,
  ) {
    final $$LocalSightingsTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.localSightings,
      getReferencedColumn: (t) => t.objectId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$LocalSightingsTableAnnotationComposer(
            $db: $db,
            $table: $db.localSightings,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }
}

class $$LocalObjectsTableTableManager
    extends
        RootTableManager<
          _$TraceDatabase,
          $LocalObjectsTable,
          LocalObject,
          $$LocalObjectsTableFilterComposer,
          $$LocalObjectsTableOrderingComposer,
          $$LocalObjectsTableAnnotationComposer,
          $$LocalObjectsTableCreateCompanionBuilder,
          $$LocalObjectsTableUpdateCompanionBuilder,
          (LocalObject, $$LocalObjectsTableReferences),
          LocalObject,
          PrefetchHooks Function({
            bool localObjectReferencesRefs,
            bool localSightingsRefs,
          })
        > {
  $$LocalObjectsTableTableManager(_$TraceDatabase db, $LocalObjectsTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$LocalObjectsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$LocalObjectsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$LocalObjectsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<Uint8List> encryptedTag = const Value.absent(),
                Value<Uint8List> tagNonce = const Value.absent(),
                Value<int> referenceRevision = const Value.absent(),
                Value<int> createdAt = const Value.absent(),
                Value<int> updatedAt = const Value.absent(),
                Value<bool> enabled = const Value.absent(),
                Value<bool> syncPending = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => LocalObjectsCompanion(
                id: id,
                encryptedTag: encryptedTag,
                tagNonce: tagNonce,
                referenceRevision: referenceRevision,
                createdAt: createdAt,
                updatedAt: updatedAt,
                enabled: enabled,
                syncPending: syncPending,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required Uint8List encryptedTag,
                required Uint8List tagNonce,
                Value<int> referenceRevision = const Value.absent(),
                required int createdAt,
                required int updatedAt,
                Value<bool> enabled = const Value.absent(),
                Value<bool> syncPending = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => LocalObjectsCompanion.insert(
                id: id,
                encryptedTag: encryptedTag,
                tagNonce: tagNonce,
                referenceRevision: referenceRevision,
                createdAt: createdAt,
                updatedAt: updatedAt,
                enabled: enabled,
                syncPending: syncPending,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable(table),
                  $$LocalObjectsTableReferences(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback:
              ({
                localObjectReferencesRefs = false,
                localSightingsRefs = false,
              }) {
                return PrefetchHooks(
                  db: db,
                  explicitlyWatchedTables: [
                    if (localObjectReferencesRefs) db.localObjectReferences,
                    if (localSightingsRefs) db.localSightings,
                  ],
                  addJoins: null,
                  getPrefetchedDataCallback: (items) async {
                    return [
                      if (localObjectReferencesRefs)
                        await $_getPrefetchedData<
                          LocalObject,
                          $LocalObjectsTable,
                          LocalObjectReference
                        >(
                          currentTable: table,
                          referencedTable: $$LocalObjectsTableReferences
                              ._localObjectReferencesRefsTable(db),
                          managerFromTypedResult: (p0) =>
                              $$LocalObjectsTableReferences(
                                db,
                                table,
                                p0,
                              ).localObjectReferencesRefs,
                          referencedItemsForCurrentItem:
                              (item, referencedItems) => referencedItems.where(
                                (e) => e.objectId == item.id,
                              ),
                          typedResults: items,
                        ),
                      if (localSightingsRefs)
                        await $_getPrefetchedData<
                          LocalObject,
                          $LocalObjectsTable,
                          LocalSighting
                        >(
                          currentTable: table,
                          referencedTable: $$LocalObjectsTableReferences
                              ._localSightingsRefsTable(db),
                          managerFromTypedResult: (p0) =>
                              $$LocalObjectsTableReferences(
                                db,
                                table,
                                p0,
                              ).localSightingsRefs,
                          referencedItemsForCurrentItem:
                              (item, referencedItems) => referencedItems.where(
                                (e) => e.objectId == item.id,
                              ),
                          typedResults: items,
                        ),
                    ];
                  },
                );
              },
        ),
      );
}

typedef $$LocalObjectsTableProcessedTableManager =
    ProcessedTableManager<
      _$TraceDatabase,
      $LocalObjectsTable,
      LocalObject,
      $$LocalObjectsTableFilterComposer,
      $$LocalObjectsTableOrderingComposer,
      $$LocalObjectsTableAnnotationComposer,
      $$LocalObjectsTableCreateCompanionBuilder,
      $$LocalObjectsTableUpdateCompanionBuilder,
      (LocalObject, $$LocalObjectsTableReferences),
      LocalObject,
      PrefetchHooks Function({
        bool localObjectReferencesRefs,
        bool localSightingsRefs,
      })
    >;
typedef $$SecureAssetsTableCreateCompanionBuilder =
    SecureAssetsCompanion Function({
      required String id,
      required String relativePath,
      required String assetType,
      required String mimeType,
      required int cryptoVersion,
      required int createdAt,
      Value<int> rowid,
    });
typedef $$SecureAssetsTableUpdateCompanionBuilder =
    SecureAssetsCompanion Function({
      Value<String> id,
      Value<String> relativePath,
      Value<String> assetType,
      Value<String> mimeType,
      Value<int> cryptoVersion,
      Value<int> createdAt,
      Value<int> rowid,
    });

final class $$SecureAssetsTableReferences
    extends BaseReferences<_$TraceDatabase, $SecureAssetsTable, SecureAsset> {
  $$SecureAssetsTableReferences(super.$_db, super.$_table, super.$_typedResult);

  static MultiTypedResultKey<
    $LocalObjectReferencesTable,
    List<LocalObjectReference>
  >
  _localObjectReferencesRefsTable(_$TraceDatabase db) =>
      MultiTypedResultKey.fromTable(
        db.localObjectReferences,
        aliasName: 'secure_assets__id__local_object_references__image_asset_id',
      );

  $$LocalObjectReferencesTableProcessedTableManager
  get localObjectReferencesRefs {
    final manager = $$LocalObjectReferencesTableTableManager(
      $_db,
      $_db.localObjectReferences,
    ).filter((f) => f.imageAssetId.id.sqlEquals($_itemColumn<String>('id')!));

    final cache = $_typedResult.readTableOrNull(
      _localObjectReferencesRefsTable($_db),
    );
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: cache),
    );
  }

  static MultiTypedResultKey<$LocalSightingsTable, List<LocalSighting>>
  _localSightingsRefsTable(_$TraceDatabase db) => MultiTypedResultKey.fromTable(
    db.localSightings,
    aliasName: 'secure_assets__id__local_sightings__evidence_asset_id',
  );

  $$LocalSightingsTableProcessedTableManager get localSightingsRefs {
    final manager = $$LocalSightingsTableTableManager($_db, $_db.localSightings)
        .filter(
          (f) => f.evidenceAssetId.id.sqlEquals($_itemColumn<String>('id')!),
        );

    final cache = $_typedResult.readTableOrNull(_localSightingsRefsTable($_db));
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: cache),
    );
  }
}

class $$SecureAssetsTableFilterComposer
    extends Composer<_$TraceDatabase, $SecureAssetsTable> {
  $$SecureAssetsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get relativePath => $composableBuilder(
    column: $table.relativePath,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get assetType => $composableBuilder(
    column: $table.assetType,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get mimeType => $composableBuilder(
    column: $table.mimeType,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get cryptoVersion => $composableBuilder(
    column: $table.cryptoVersion,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnFilters(column),
  );

  Expression<bool> localObjectReferencesRefs(
    Expression<bool> Function($$LocalObjectReferencesTableFilterComposer f) f,
  ) {
    final $$LocalObjectReferencesTableFilterComposer composer =
        $composerBuilder(
          composer: this,
          getCurrentColumn: (t) => t.id,
          referencedTable: $db.localObjectReferences,
          getReferencedColumn: (t) => t.imageAssetId,
          builder:
              (
                joinBuilder, {
                $addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer,
              }) => $$LocalObjectReferencesTableFilterComposer(
                $db: $db,
                $table: $db.localObjectReferences,
                $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                joinBuilder: joinBuilder,
                $removeJoinBuilderFromRootComposer:
                    $removeJoinBuilderFromRootComposer,
              ),
        );
    return f(composer);
  }

  Expression<bool> localSightingsRefs(
    Expression<bool> Function($$LocalSightingsTableFilterComposer f) f,
  ) {
    final $$LocalSightingsTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.localSightings,
      getReferencedColumn: (t) => t.evidenceAssetId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$LocalSightingsTableFilterComposer(
            $db: $db,
            $table: $db.localSightings,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }
}

class $$SecureAssetsTableOrderingComposer
    extends Composer<_$TraceDatabase, $SecureAssetsTable> {
  $$SecureAssetsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get relativePath => $composableBuilder(
    column: $table.relativePath,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get assetType => $composableBuilder(
    column: $table.assetType,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get mimeType => $composableBuilder(
    column: $table.mimeType,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get cryptoVersion => $composableBuilder(
    column: $table.cryptoVersion,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$SecureAssetsTableAnnotationComposer
    extends Composer<_$TraceDatabase, $SecureAssetsTable> {
  $$SecureAssetsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get relativePath => $composableBuilder(
    column: $table.relativePath,
    builder: (column) => column,
  );

  GeneratedColumn<String> get assetType =>
      $composableBuilder(column: $table.assetType, builder: (column) => column);

  GeneratedColumn<String> get mimeType =>
      $composableBuilder(column: $table.mimeType, builder: (column) => column);

  GeneratedColumn<int> get cryptoVersion => $composableBuilder(
    column: $table.cryptoVersion,
    builder: (column) => column,
  );

  GeneratedColumn<int> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  Expression<T> localObjectReferencesRefs<T extends Object>(
    Expression<T> Function($$LocalObjectReferencesTableAnnotationComposer a) f,
  ) {
    final $$LocalObjectReferencesTableAnnotationComposer composer =
        $composerBuilder(
          composer: this,
          getCurrentColumn: (t) => t.id,
          referencedTable: $db.localObjectReferences,
          getReferencedColumn: (t) => t.imageAssetId,
          builder:
              (
                joinBuilder, {
                $addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer,
              }) => $$LocalObjectReferencesTableAnnotationComposer(
                $db: $db,
                $table: $db.localObjectReferences,
                $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                joinBuilder: joinBuilder,
                $removeJoinBuilderFromRootComposer:
                    $removeJoinBuilderFromRootComposer,
              ),
        );
    return f(composer);
  }

  Expression<T> localSightingsRefs<T extends Object>(
    Expression<T> Function($$LocalSightingsTableAnnotationComposer a) f,
  ) {
    final $$LocalSightingsTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.id,
      referencedTable: $db.localSightings,
      getReferencedColumn: (t) => t.evidenceAssetId,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$LocalSightingsTableAnnotationComposer(
            $db: $db,
            $table: $db.localSightings,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return f(composer);
  }
}

class $$SecureAssetsTableTableManager
    extends
        RootTableManager<
          _$TraceDatabase,
          $SecureAssetsTable,
          SecureAsset,
          $$SecureAssetsTableFilterComposer,
          $$SecureAssetsTableOrderingComposer,
          $$SecureAssetsTableAnnotationComposer,
          $$SecureAssetsTableCreateCompanionBuilder,
          $$SecureAssetsTableUpdateCompanionBuilder,
          (SecureAsset, $$SecureAssetsTableReferences),
          SecureAsset,
          PrefetchHooks Function({
            bool localObjectReferencesRefs,
            bool localSightingsRefs,
          })
        > {
  $$SecureAssetsTableTableManager(_$TraceDatabase db, $SecureAssetsTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$SecureAssetsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$SecureAssetsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$SecureAssetsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String> relativePath = const Value.absent(),
                Value<String> assetType = const Value.absent(),
                Value<String> mimeType = const Value.absent(),
                Value<int> cryptoVersion = const Value.absent(),
                Value<int> createdAt = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => SecureAssetsCompanion(
                id: id,
                relativePath: relativePath,
                assetType: assetType,
                mimeType: mimeType,
                cryptoVersion: cryptoVersion,
                createdAt: createdAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required String relativePath,
                required String assetType,
                required String mimeType,
                required int cryptoVersion,
                required int createdAt,
                Value<int> rowid = const Value.absent(),
              }) => SecureAssetsCompanion.insert(
                id: id,
                relativePath: relativePath,
                assetType: assetType,
                mimeType: mimeType,
                cryptoVersion: cryptoVersion,
                createdAt: createdAt,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable(table),
                  $$SecureAssetsTableReferences(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback:
              ({
                localObjectReferencesRefs = false,
                localSightingsRefs = false,
              }) {
                return PrefetchHooks(
                  db: db,
                  explicitlyWatchedTables: [
                    if (localObjectReferencesRefs) db.localObjectReferences,
                    if (localSightingsRefs) db.localSightings,
                  ],
                  addJoins: null,
                  getPrefetchedDataCallback: (items) async {
                    return [
                      if (localObjectReferencesRefs)
                        await $_getPrefetchedData<
                          SecureAsset,
                          $SecureAssetsTable,
                          LocalObjectReference
                        >(
                          currentTable: table,
                          referencedTable: $$SecureAssetsTableReferences
                              ._localObjectReferencesRefsTable(db),
                          managerFromTypedResult: (p0) =>
                              $$SecureAssetsTableReferences(
                                db,
                                table,
                                p0,
                              ).localObjectReferencesRefs,
                          referencedItemsForCurrentItem:
                              (item, referencedItems) => referencedItems.where(
                                (e) => e.imageAssetId == item.id,
                              ),
                          typedResults: items,
                        ),
                      if (localSightingsRefs)
                        await $_getPrefetchedData<
                          SecureAsset,
                          $SecureAssetsTable,
                          LocalSighting
                        >(
                          currentTable: table,
                          referencedTable: $$SecureAssetsTableReferences
                              ._localSightingsRefsTable(db),
                          managerFromTypedResult: (p0) =>
                              $$SecureAssetsTableReferences(
                                db,
                                table,
                                p0,
                              ).localSightingsRefs,
                          referencedItemsForCurrentItem:
                              (item, referencedItems) => referencedItems.where(
                                (e) => e.evidenceAssetId == item.id,
                              ),
                          typedResults: items,
                        ),
                    ];
                  },
                );
              },
        ),
      );
}

typedef $$SecureAssetsTableProcessedTableManager =
    ProcessedTableManager<
      _$TraceDatabase,
      $SecureAssetsTable,
      SecureAsset,
      $$SecureAssetsTableFilterComposer,
      $$SecureAssetsTableOrderingComposer,
      $$SecureAssetsTableAnnotationComposer,
      $$SecureAssetsTableCreateCompanionBuilder,
      $$SecureAssetsTableUpdateCompanionBuilder,
      (SecureAsset, $$SecureAssetsTableReferences),
      SecureAsset,
      PrefetchHooks Function({
        bool localObjectReferencesRefs,
        bool localSightingsRefs,
      })
    >;
typedef $$LocalObjectReferencesTableCreateCompanionBuilder =
    LocalObjectReferencesCompanion Function({
      required String id,
      required String objectId,
      required String imageAssetId,
      required double roiLeft,
      required double roiTop,
      required double roiRight,
      required double roiBottom,
      required double qualityScore,
      required int createdAt,
      Value<int> rowid,
    });
typedef $$LocalObjectReferencesTableUpdateCompanionBuilder =
    LocalObjectReferencesCompanion Function({
      Value<String> id,
      Value<String> objectId,
      Value<String> imageAssetId,
      Value<double> roiLeft,
      Value<double> roiTop,
      Value<double> roiRight,
      Value<double> roiBottom,
      Value<double> qualityScore,
      Value<int> createdAt,
      Value<int> rowid,
    });

final class $$LocalObjectReferencesTableReferences
    extends
        BaseReferences<
          _$TraceDatabase,
          $LocalObjectReferencesTable,
          LocalObjectReference
        > {
  $$LocalObjectReferencesTableReferences(
    super.$_db,
    super.$_table,
    super.$_typedResult,
  );

  static $LocalObjectsTable _objectIdTable(_$TraceDatabase db) => db
      .localObjects
      .createAlias('local_object_references__object_id__local_objects__id');

  $$LocalObjectsTableProcessedTableManager get objectId {
    final $_column = $_itemColumn<String>('object_id')!;

    final manager = $$LocalObjectsTableTableManager(
      $_db,
      $_db.localObjects,
    ).filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_objectIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: [item]),
    );
  }

  static $SecureAssetsTable _imageAssetIdTable(_$TraceDatabase db) =>
      db.secureAssets.createAlias(
        'local_object_references__image_asset_id__secure_assets__id',
      );

  $$SecureAssetsTableProcessedTableManager get imageAssetId {
    final $_column = $_itemColumn<String>('image_asset_id')!;

    final manager = $$SecureAssetsTableTableManager(
      $_db,
      $_db.secureAssets,
    ).filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_imageAssetIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: [item]),
    );
  }

  static MultiTypedResultKey<
    $LocalReferenceEmbeddingsTable,
    List<LocalReferenceEmbedding>
  >
  _localReferenceEmbeddingsRefsTable(
    _$TraceDatabase db,
  ) => MultiTypedResultKey.fromTable(
    db.localReferenceEmbeddings,
    aliasName:
        'local_object_references__id__local_reference_embeddings__reference_id',
  );

  $$LocalReferenceEmbeddingsTableProcessedTableManager
  get localReferenceEmbeddingsRefs {
    final manager = $$LocalReferenceEmbeddingsTableTableManager(
      $_db,
      $_db.localReferenceEmbeddings,
    ).filter((f) => f.referenceId.id.sqlEquals($_itemColumn<String>('id')!));

    final cache = $_typedResult.readTableOrNull(
      _localReferenceEmbeddingsRefsTable($_db),
    );
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: cache),
    );
  }
}

class $$LocalObjectReferencesTableFilterComposer
    extends Composer<_$TraceDatabase, $LocalObjectReferencesTable> {
  $$LocalObjectReferencesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<double> get roiLeft => $composableBuilder(
    column: $table.roiLeft,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<double> get roiTop => $composableBuilder(
    column: $table.roiTop,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<double> get roiRight => $composableBuilder(
    column: $table.roiRight,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<double> get roiBottom => $composableBuilder(
    column: $table.roiBottom,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<double> get qualityScore => $composableBuilder(
    column: $table.qualityScore,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnFilters(column),
  );

  $$LocalObjectsTableFilterComposer get objectId {
    final $$LocalObjectsTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.objectId,
      referencedTable: $db.localObjects,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$LocalObjectsTableFilterComposer(
            $db: $db,
            $table: $db.localObjects,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }

  $$SecureAssetsTableFilterComposer get imageAssetId {
    final $$SecureAssetsTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.imageAssetId,
      referencedTable: $db.secureAssets,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$SecureAssetsTableFilterComposer(
            $db: $db,
            $table: $db.secureAssets,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }

  Expression<bool> localReferenceEmbeddingsRefs(
    Expression<bool> Function($$LocalReferenceEmbeddingsTableFilterComposer f)
    f,
  ) {
    final $$LocalReferenceEmbeddingsTableFilterComposer composer =
        $composerBuilder(
          composer: this,
          getCurrentColumn: (t) => t.id,
          referencedTable: $db.localReferenceEmbeddings,
          getReferencedColumn: (t) => t.referenceId,
          builder:
              (
                joinBuilder, {
                $addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer,
              }) => $$LocalReferenceEmbeddingsTableFilterComposer(
                $db: $db,
                $table: $db.localReferenceEmbeddings,
                $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                joinBuilder: joinBuilder,
                $removeJoinBuilderFromRootComposer:
                    $removeJoinBuilderFromRootComposer,
              ),
        );
    return f(composer);
  }
}

class $$LocalObjectReferencesTableOrderingComposer
    extends Composer<_$TraceDatabase, $LocalObjectReferencesTable> {
  $$LocalObjectReferencesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<double> get roiLeft => $composableBuilder(
    column: $table.roiLeft,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<double> get roiTop => $composableBuilder(
    column: $table.roiTop,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<double> get roiRight => $composableBuilder(
    column: $table.roiRight,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<double> get roiBottom => $composableBuilder(
    column: $table.roiBottom,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<double> get qualityScore => $composableBuilder(
    column: $table.qualityScore,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnOrderings(column),
  );

  $$LocalObjectsTableOrderingComposer get objectId {
    final $$LocalObjectsTableOrderingComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.objectId,
      referencedTable: $db.localObjects,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$LocalObjectsTableOrderingComposer(
            $db: $db,
            $table: $db.localObjects,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }

  $$SecureAssetsTableOrderingComposer get imageAssetId {
    final $$SecureAssetsTableOrderingComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.imageAssetId,
      referencedTable: $db.secureAssets,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$SecureAssetsTableOrderingComposer(
            $db: $db,
            $table: $db.secureAssets,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$LocalObjectReferencesTableAnnotationComposer
    extends Composer<_$TraceDatabase, $LocalObjectReferencesTable> {
  $$LocalObjectReferencesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<double> get roiLeft =>
      $composableBuilder(column: $table.roiLeft, builder: (column) => column);

  GeneratedColumn<double> get roiTop =>
      $composableBuilder(column: $table.roiTop, builder: (column) => column);

  GeneratedColumn<double> get roiRight =>
      $composableBuilder(column: $table.roiRight, builder: (column) => column);

  GeneratedColumn<double> get roiBottom =>
      $composableBuilder(column: $table.roiBottom, builder: (column) => column);

  GeneratedColumn<double> get qualityScore => $composableBuilder(
    column: $table.qualityScore,
    builder: (column) => column,
  );

  GeneratedColumn<int> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  $$LocalObjectsTableAnnotationComposer get objectId {
    final $$LocalObjectsTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.objectId,
      referencedTable: $db.localObjects,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$LocalObjectsTableAnnotationComposer(
            $db: $db,
            $table: $db.localObjects,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }

  $$SecureAssetsTableAnnotationComposer get imageAssetId {
    final $$SecureAssetsTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.imageAssetId,
      referencedTable: $db.secureAssets,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$SecureAssetsTableAnnotationComposer(
            $db: $db,
            $table: $db.secureAssets,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }

  Expression<T> localReferenceEmbeddingsRefs<T extends Object>(
    Expression<T> Function($$LocalReferenceEmbeddingsTableAnnotationComposer a)
    f,
  ) {
    final $$LocalReferenceEmbeddingsTableAnnotationComposer composer =
        $composerBuilder(
          composer: this,
          getCurrentColumn: (t) => t.id,
          referencedTable: $db.localReferenceEmbeddings,
          getReferencedColumn: (t) => t.referenceId,
          builder:
              (
                joinBuilder, {
                $addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer,
              }) => $$LocalReferenceEmbeddingsTableAnnotationComposer(
                $db: $db,
                $table: $db.localReferenceEmbeddings,
                $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                joinBuilder: joinBuilder,
                $removeJoinBuilderFromRootComposer:
                    $removeJoinBuilderFromRootComposer,
              ),
        );
    return f(composer);
  }
}

class $$LocalObjectReferencesTableTableManager
    extends
        RootTableManager<
          _$TraceDatabase,
          $LocalObjectReferencesTable,
          LocalObjectReference,
          $$LocalObjectReferencesTableFilterComposer,
          $$LocalObjectReferencesTableOrderingComposer,
          $$LocalObjectReferencesTableAnnotationComposer,
          $$LocalObjectReferencesTableCreateCompanionBuilder,
          $$LocalObjectReferencesTableUpdateCompanionBuilder,
          (LocalObjectReference, $$LocalObjectReferencesTableReferences),
          LocalObjectReference,
          PrefetchHooks Function({
            bool objectId,
            bool imageAssetId,
            bool localReferenceEmbeddingsRefs,
          })
        > {
  $$LocalObjectReferencesTableTableManager(
    _$TraceDatabase db,
    $LocalObjectReferencesTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$LocalObjectReferencesTableFilterComposer(
                $db: db,
                $table: table,
              ),
          createOrderingComposer: () =>
              $$LocalObjectReferencesTableOrderingComposer(
                $db: db,
                $table: table,
              ),
          createComputedFieldComposer: () =>
              $$LocalObjectReferencesTableAnnotationComposer(
                $db: db,
                $table: table,
              ),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String> objectId = const Value.absent(),
                Value<String> imageAssetId = const Value.absent(),
                Value<double> roiLeft = const Value.absent(),
                Value<double> roiTop = const Value.absent(),
                Value<double> roiRight = const Value.absent(),
                Value<double> roiBottom = const Value.absent(),
                Value<double> qualityScore = const Value.absent(),
                Value<int> createdAt = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => LocalObjectReferencesCompanion(
                id: id,
                objectId: objectId,
                imageAssetId: imageAssetId,
                roiLeft: roiLeft,
                roiTop: roiTop,
                roiRight: roiRight,
                roiBottom: roiBottom,
                qualityScore: qualityScore,
                createdAt: createdAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required String objectId,
                required String imageAssetId,
                required double roiLeft,
                required double roiTop,
                required double roiRight,
                required double roiBottom,
                required double qualityScore,
                required int createdAt,
                Value<int> rowid = const Value.absent(),
              }) => LocalObjectReferencesCompanion.insert(
                id: id,
                objectId: objectId,
                imageAssetId: imageAssetId,
                roiLeft: roiLeft,
                roiTop: roiTop,
                roiRight: roiRight,
                roiBottom: roiBottom,
                qualityScore: qualityScore,
                createdAt: createdAt,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable(table),
                  $$LocalObjectReferencesTableReferences(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback:
              ({
                objectId = false,
                imageAssetId = false,
                localReferenceEmbeddingsRefs = false,
              }) {
                return PrefetchHooks(
                  db: db,
                  explicitlyWatchedTables: [
                    if (localReferenceEmbeddingsRefs)
                      db.localReferenceEmbeddings,
                  ],
                  addJoins:
                      <
                        T extends TableManagerState<
                          dynamic,
                          dynamic,
                          dynamic,
                          dynamic,
                          dynamic,
                          dynamic,
                          dynamic,
                          dynamic,
                          dynamic,
                          dynamic,
                          dynamic
                        >
                      >(state) {
                        if (objectId) {
                          state = state.withJoin(
                            currentTable: table,
                            currentColumn: table.objectId,
                            referencedTable:
                                $$LocalObjectReferencesTableReferences
                                    ._objectIdTable(db),
                            referencedColumn:
                                $$LocalObjectReferencesTableReferences
                                    ._objectIdTable(db)
                                    .id,
                          ) as T;
                        }
                        if (imageAssetId) {
                          state = state.withJoin(
                            currentTable: table,
                            currentColumn: table.imageAssetId,
                            referencedTable:
                                $$LocalObjectReferencesTableReferences
                                    ._imageAssetIdTable(db),
                            referencedColumn:
                                $$LocalObjectReferencesTableReferences
                                    ._imageAssetIdTable(db)
                                    .id,
                          ) as T;
                        }

                        return state;
                      },
                  getPrefetchedDataCallback: (items) async {
                    return [
                      if (localReferenceEmbeddingsRefs)
                        await $_getPrefetchedData<
                          LocalObjectReference,
                          $LocalObjectReferencesTable,
                          LocalReferenceEmbedding
                        >(
                          currentTable: table,
                          referencedTable:
                              $$LocalObjectReferencesTableReferences
                                  ._localReferenceEmbeddingsRefsTable(db),
                          managerFromTypedResult: (p0) =>
                              $$LocalObjectReferencesTableReferences(
                                db,
                                table,
                                p0,
                              ).localReferenceEmbeddingsRefs,
                          referencedItemsForCurrentItem:
                              (item, referencedItems) => referencedItems.where(
                                (e) => e.referenceId == item.id,
                              ),
                          typedResults: items,
                        ),
                    ];
                  },
                );
              },
        ),
      );
}

typedef $$LocalObjectReferencesTableProcessedTableManager =
    ProcessedTableManager<
      _$TraceDatabase,
      $LocalObjectReferencesTable,
      LocalObjectReference,
      $$LocalObjectReferencesTableFilterComposer,
      $$LocalObjectReferencesTableOrderingComposer,
      $$LocalObjectReferencesTableAnnotationComposer,
      $$LocalObjectReferencesTableCreateCompanionBuilder,
      $$LocalObjectReferencesTableUpdateCompanionBuilder,
      (LocalObjectReference, $$LocalObjectReferencesTableReferences),
      LocalObjectReference,
      PrefetchHooks Function({
        bool objectId,
        bool imageAssetId,
        bool localReferenceEmbeddingsRefs,
      })
    >;
typedef $$LocalReferenceEmbeddingsTableCreateCompanionBuilder =
    LocalReferenceEmbeddingsCompanion Function({
      required String id,
      required String referenceId,
      required String modelName,
      required String modelVersion,
      required int dimensions,
      required Uint8List encryptedVector,
      required Uint8List vectorNonce,
      Value<int> rowid,
    });
typedef $$LocalReferenceEmbeddingsTableUpdateCompanionBuilder =
    LocalReferenceEmbeddingsCompanion Function({
      Value<String> id,
      Value<String> referenceId,
      Value<String> modelName,
      Value<String> modelVersion,
      Value<int> dimensions,
      Value<Uint8List> encryptedVector,
      Value<Uint8List> vectorNonce,
      Value<int> rowid,
    });

final class $$LocalReferenceEmbeddingsTableReferences
    extends
        BaseReferences<
          _$TraceDatabase,
          $LocalReferenceEmbeddingsTable,
          LocalReferenceEmbedding
        > {
  $$LocalReferenceEmbeddingsTableReferences(
    super.$_db,
    super.$_table,
    super.$_typedResult,
  );

  static $LocalObjectReferencesTable _referenceIdTable(_$TraceDatabase db) =>
      db.localObjectReferences.createAlias(
        'local_reference_embeddings__reference_id__local_object_references__id',
      );

  $$LocalObjectReferencesTableProcessedTableManager get referenceId {
    final $_column = $_itemColumn<String>('reference_id')!;

    final manager = $$LocalObjectReferencesTableTableManager(
      $_db,
      $_db.localObjectReferences,
    ).filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_referenceIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: [item]),
    );
  }
}

class $$LocalReferenceEmbeddingsTableFilterComposer
    extends Composer<_$TraceDatabase, $LocalReferenceEmbeddingsTable> {
  $$LocalReferenceEmbeddingsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get modelName => $composableBuilder(
    column: $table.modelName,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get modelVersion => $composableBuilder(
    column: $table.modelVersion,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get dimensions => $composableBuilder(
    column: $table.dimensions,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<Uint8List> get encryptedVector => $composableBuilder(
    column: $table.encryptedVector,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<Uint8List> get vectorNonce => $composableBuilder(
    column: $table.vectorNonce,
    builder: (column) => ColumnFilters(column),
  );

  $$LocalObjectReferencesTableFilterComposer get referenceId {
    final $$LocalObjectReferencesTableFilterComposer composer =
        $composerBuilder(
          composer: this,
          getCurrentColumn: (t) => t.referenceId,
          referencedTable: $db.localObjectReferences,
          getReferencedColumn: (t) => t.id,
          builder:
              (
                joinBuilder, {
                $addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer,
              }) => $$LocalObjectReferencesTableFilterComposer(
                $db: $db,
                $table: $db.localObjectReferences,
                $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                joinBuilder: joinBuilder,
                $removeJoinBuilderFromRootComposer:
                    $removeJoinBuilderFromRootComposer,
              ),
        );
    return composer;
  }
}

class $$LocalReferenceEmbeddingsTableOrderingComposer
    extends Composer<_$TraceDatabase, $LocalReferenceEmbeddingsTable> {
  $$LocalReferenceEmbeddingsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get modelName => $composableBuilder(
    column: $table.modelName,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get modelVersion => $composableBuilder(
    column: $table.modelVersion,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get dimensions => $composableBuilder(
    column: $table.dimensions,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<Uint8List> get encryptedVector => $composableBuilder(
    column: $table.encryptedVector,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<Uint8List> get vectorNonce => $composableBuilder(
    column: $table.vectorNonce,
    builder: (column) => ColumnOrderings(column),
  );

  $$LocalObjectReferencesTableOrderingComposer get referenceId {
    final $$LocalObjectReferencesTableOrderingComposer composer =
        $composerBuilder(
          composer: this,
          getCurrentColumn: (t) => t.referenceId,
          referencedTable: $db.localObjectReferences,
          getReferencedColumn: (t) => t.id,
          builder:
              (
                joinBuilder, {
                $addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer,
              }) => $$LocalObjectReferencesTableOrderingComposer(
                $db: $db,
                $table: $db.localObjectReferences,
                $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                joinBuilder: joinBuilder,
                $removeJoinBuilderFromRootComposer:
                    $removeJoinBuilderFromRootComposer,
              ),
        );
    return composer;
  }
}

class $$LocalReferenceEmbeddingsTableAnnotationComposer
    extends Composer<_$TraceDatabase, $LocalReferenceEmbeddingsTable> {
  $$LocalReferenceEmbeddingsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get modelName =>
      $composableBuilder(column: $table.modelName, builder: (column) => column);

  GeneratedColumn<String> get modelVersion => $composableBuilder(
    column: $table.modelVersion,
    builder: (column) => column,
  );

  GeneratedColumn<int> get dimensions => $composableBuilder(
    column: $table.dimensions,
    builder: (column) => column,
  );

  GeneratedColumn<Uint8List> get encryptedVector => $composableBuilder(
    column: $table.encryptedVector,
    builder: (column) => column,
  );

  GeneratedColumn<Uint8List> get vectorNonce => $composableBuilder(
    column: $table.vectorNonce,
    builder: (column) => column,
  );

  $$LocalObjectReferencesTableAnnotationComposer get referenceId {
    final $$LocalObjectReferencesTableAnnotationComposer composer =
        $composerBuilder(
          composer: this,
          getCurrentColumn: (t) => t.referenceId,
          referencedTable: $db.localObjectReferences,
          getReferencedColumn: (t) => t.id,
          builder:
              (
                joinBuilder, {
                $addJoinBuilderToRootComposer,
                $removeJoinBuilderFromRootComposer,
              }) => $$LocalObjectReferencesTableAnnotationComposer(
                $db: $db,
                $table: $db.localObjectReferences,
                $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
                joinBuilder: joinBuilder,
                $removeJoinBuilderFromRootComposer:
                    $removeJoinBuilderFromRootComposer,
              ),
        );
    return composer;
  }
}

class $$LocalReferenceEmbeddingsTableTableManager
    extends
        RootTableManager<
          _$TraceDatabase,
          $LocalReferenceEmbeddingsTable,
          LocalReferenceEmbedding,
          $$LocalReferenceEmbeddingsTableFilterComposer,
          $$LocalReferenceEmbeddingsTableOrderingComposer,
          $$LocalReferenceEmbeddingsTableAnnotationComposer,
          $$LocalReferenceEmbeddingsTableCreateCompanionBuilder,
          $$LocalReferenceEmbeddingsTableUpdateCompanionBuilder,
          (LocalReferenceEmbedding, $$LocalReferenceEmbeddingsTableReferences),
          LocalReferenceEmbedding,
          PrefetchHooks Function({bool referenceId})
        > {
  $$LocalReferenceEmbeddingsTableTableManager(
    _$TraceDatabase db,
    $LocalReferenceEmbeddingsTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$LocalReferenceEmbeddingsTableFilterComposer(
                $db: db,
                $table: table,
              ),
          createOrderingComposer: () =>
              $$LocalReferenceEmbeddingsTableOrderingComposer(
                $db: db,
                $table: table,
              ),
          createComputedFieldComposer: () =>
              $$LocalReferenceEmbeddingsTableAnnotationComposer(
                $db: db,
                $table: table,
              ),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String> referenceId = const Value.absent(),
                Value<String> modelName = const Value.absent(),
                Value<String> modelVersion = const Value.absent(),
                Value<int> dimensions = const Value.absent(),
                Value<Uint8List> encryptedVector = const Value.absent(),
                Value<Uint8List> vectorNonce = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => LocalReferenceEmbeddingsCompanion(
                id: id,
                referenceId: referenceId,
                modelName: modelName,
                modelVersion: modelVersion,
                dimensions: dimensions,
                encryptedVector: encryptedVector,
                vectorNonce: vectorNonce,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required String referenceId,
                required String modelName,
                required String modelVersion,
                required int dimensions,
                required Uint8List encryptedVector,
                required Uint8List vectorNonce,
                Value<int> rowid = const Value.absent(),
              }) => LocalReferenceEmbeddingsCompanion.insert(
                id: id,
                referenceId: referenceId,
                modelName: modelName,
                modelVersion: modelVersion,
                dimensions: dimensions,
                encryptedVector: encryptedVector,
                vectorNonce: vectorNonce,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable(table),
                  $$LocalReferenceEmbeddingsTableReferences(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: ({referenceId = false}) {
            return PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [],
              addJoins:
                  <
                    T extends TableManagerState<
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic
                    >
                  >(state) {
                    if (referenceId) {
                      state = state.withJoin(
                        currentTable: table,
                        currentColumn: table.referenceId,
                        referencedTable:
                            $$LocalReferenceEmbeddingsTableReferences
                                ._referenceIdTable(db),
                        referencedColumn:
                            $$LocalReferenceEmbeddingsTableReferences
                                ._referenceIdTable(db)
                                .id,
                      ) as T;
                    }

                    return state;
                  },
              getPrefetchedDataCallback: (items) async {
                return [];
              },
            );
          },
        ),
      );
}

typedef $$LocalReferenceEmbeddingsTableProcessedTableManager =
    ProcessedTableManager<
      _$TraceDatabase,
      $LocalReferenceEmbeddingsTable,
      LocalReferenceEmbedding,
      $$LocalReferenceEmbeddingsTableFilterComposer,
      $$LocalReferenceEmbeddingsTableOrderingComposer,
      $$LocalReferenceEmbeddingsTableAnnotationComposer,
      $$LocalReferenceEmbeddingsTableCreateCompanionBuilder,
      $$LocalReferenceEmbeddingsTableUpdateCompanionBuilder,
      (LocalReferenceEmbedding, $$LocalReferenceEmbeddingsTableReferences),
      LocalReferenceEmbedding,
      PrefetchHooks Function({bool referenceId})
    >;
typedef $$LocalSightingsTableCreateCompanionBuilder =
    LocalSightingsCompanion Function({
      required String id,
      required String objectId,
      required int detectedAt,
      Value<Uint8List?> encryptedLocation,
      Value<Uint8List?> locationNonce,
      required double confidence,
      Value<String?> evidenceAssetId,
      Value<bool> syncPending,
      Value<int> rowid,
    });
typedef $$LocalSightingsTableUpdateCompanionBuilder =
    LocalSightingsCompanion Function({
      Value<String> id,
      Value<String> objectId,
      Value<int> detectedAt,
      Value<Uint8List?> encryptedLocation,
      Value<Uint8List?> locationNonce,
      Value<double> confidence,
      Value<String?> evidenceAssetId,
      Value<bool> syncPending,
      Value<int> rowid,
    });

final class $$LocalSightingsTableReferences
    extends
        BaseReferences<_$TraceDatabase, $LocalSightingsTable, LocalSighting> {
  $$LocalSightingsTableReferences(
    super.$_db,
    super.$_table,
    super.$_typedResult,
  );

  static $LocalObjectsTable _objectIdTable(_$TraceDatabase db) => db
      .localObjects
      .createAlias('local_sightings__object_id__local_objects__id');

  $$LocalObjectsTableProcessedTableManager get objectId {
    final $_column = $_itemColumn<String>('object_id')!;

    final manager = $$LocalObjectsTableTableManager(
      $_db,
      $_db.localObjects,
    ).filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_objectIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: [item]),
    );
  }

  static $SecureAssetsTable _evidenceAssetIdTable(_$TraceDatabase db) => db
      .secureAssets
      .createAlias('local_sightings__evidence_asset_id__secure_assets__id');

  $$SecureAssetsTableProcessedTableManager? get evidenceAssetId {
    final $_column = $_itemColumn<String>('evidence_asset_id');
    if ($_column == null) return null;
    final manager = $$SecureAssetsTableTableManager(
      $_db,
      $_db.secureAssets,
    ).filter((f) => f.id.sqlEquals($_column));
    final item = $_typedResult.readTableOrNull(_evidenceAssetIdTable($_db));
    if (item == null) return manager;
    return ProcessedTableManager(
      manager.$state.copyWith(prefetchedData: [item]),
    );
  }
}

class $$LocalSightingsTableFilterComposer
    extends Composer<_$TraceDatabase, $LocalSightingsTable> {
  $$LocalSightingsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get detectedAt => $composableBuilder(
    column: $table.detectedAt,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<Uint8List> get encryptedLocation => $composableBuilder(
    column: $table.encryptedLocation,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<Uint8List> get locationNonce => $composableBuilder(
    column: $table.locationNonce,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<double> get confidence => $composableBuilder(
    column: $table.confidence,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get syncPending => $composableBuilder(
    column: $table.syncPending,
    builder: (column) => ColumnFilters(column),
  );

  $$LocalObjectsTableFilterComposer get objectId {
    final $$LocalObjectsTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.objectId,
      referencedTable: $db.localObjects,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$LocalObjectsTableFilterComposer(
            $db: $db,
            $table: $db.localObjects,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }

  $$SecureAssetsTableFilterComposer get evidenceAssetId {
    final $$SecureAssetsTableFilterComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.evidenceAssetId,
      referencedTable: $db.secureAssets,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$SecureAssetsTableFilterComposer(
            $db: $db,
            $table: $db.secureAssets,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$LocalSightingsTableOrderingComposer
    extends Composer<_$TraceDatabase, $LocalSightingsTable> {
  $$LocalSightingsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get detectedAt => $composableBuilder(
    column: $table.detectedAt,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<Uint8List> get encryptedLocation => $composableBuilder(
    column: $table.encryptedLocation,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<Uint8List> get locationNonce => $composableBuilder(
    column: $table.locationNonce,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<double> get confidence => $composableBuilder(
    column: $table.confidence,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get syncPending => $composableBuilder(
    column: $table.syncPending,
    builder: (column) => ColumnOrderings(column),
  );

  $$LocalObjectsTableOrderingComposer get objectId {
    final $$LocalObjectsTableOrderingComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.objectId,
      referencedTable: $db.localObjects,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$LocalObjectsTableOrderingComposer(
            $db: $db,
            $table: $db.localObjects,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }

  $$SecureAssetsTableOrderingComposer get evidenceAssetId {
    final $$SecureAssetsTableOrderingComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.evidenceAssetId,
      referencedTable: $db.secureAssets,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$SecureAssetsTableOrderingComposer(
            $db: $db,
            $table: $db.secureAssets,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$LocalSightingsTableAnnotationComposer
    extends Composer<_$TraceDatabase, $LocalSightingsTable> {
  $$LocalSightingsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<int> get detectedAt => $composableBuilder(
    column: $table.detectedAt,
    builder: (column) => column,
  );

  GeneratedColumn<Uint8List> get encryptedLocation => $composableBuilder(
    column: $table.encryptedLocation,
    builder: (column) => column,
  );

  GeneratedColumn<Uint8List> get locationNonce => $composableBuilder(
    column: $table.locationNonce,
    builder: (column) => column,
  );

  GeneratedColumn<double> get confidence => $composableBuilder(
    column: $table.confidence,
    builder: (column) => column,
  );

  GeneratedColumn<bool> get syncPending => $composableBuilder(
    column: $table.syncPending,
    builder: (column) => column,
  );

  $$LocalObjectsTableAnnotationComposer get objectId {
    final $$LocalObjectsTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.objectId,
      referencedTable: $db.localObjects,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$LocalObjectsTableAnnotationComposer(
            $db: $db,
            $table: $db.localObjects,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }

  $$SecureAssetsTableAnnotationComposer get evidenceAssetId {
    final $$SecureAssetsTableAnnotationComposer composer = $composerBuilder(
      composer: this,
      getCurrentColumn: (t) => t.evidenceAssetId,
      referencedTable: $db.secureAssets,
      getReferencedColumn: (t) => t.id,
      builder:
          (
            joinBuilder, {
            $addJoinBuilderToRootComposer,
            $removeJoinBuilderFromRootComposer,
          }) => $$SecureAssetsTableAnnotationComposer(
            $db: $db,
            $table: $db.secureAssets,
            $addJoinBuilderToRootComposer: $addJoinBuilderToRootComposer,
            joinBuilder: joinBuilder,
            $removeJoinBuilderFromRootComposer:
                $removeJoinBuilderFromRootComposer,
          ),
    );
    return composer;
  }
}

class $$LocalSightingsTableTableManager
    extends
        RootTableManager<
          _$TraceDatabase,
          $LocalSightingsTable,
          LocalSighting,
          $$LocalSightingsTableFilterComposer,
          $$LocalSightingsTableOrderingComposer,
          $$LocalSightingsTableAnnotationComposer,
          $$LocalSightingsTableCreateCompanionBuilder,
          $$LocalSightingsTableUpdateCompanionBuilder,
          (LocalSighting, $$LocalSightingsTableReferences),
          LocalSighting,
          PrefetchHooks Function({bool objectId, bool evidenceAssetId})
        > {
  $$LocalSightingsTableTableManager(
    _$TraceDatabase db,
    $LocalSightingsTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$LocalSightingsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$LocalSightingsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$LocalSightingsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String> objectId = const Value.absent(),
                Value<int> detectedAt = const Value.absent(),
                Value<Uint8List?> encryptedLocation = const Value.absent(),
                Value<Uint8List?> locationNonce = const Value.absent(),
                Value<double> confidence = const Value.absent(),
                Value<String?> evidenceAssetId = const Value.absent(),
                Value<bool> syncPending = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => LocalSightingsCompanion(
                id: id,
                objectId: objectId,
                detectedAt: detectedAt,
                encryptedLocation: encryptedLocation,
                locationNonce: locationNonce,
                confidence: confidence,
                evidenceAssetId: evidenceAssetId,
                syncPending: syncPending,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required String objectId,
                required int detectedAt,
                Value<Uint8List?> encryptedLocation = const Value.absent(),
                Value<Uint8List?> locationNonce = const Value.absent(),
                required double confidence,
                Value<String?> evidenceAssetId = const Value.absent(),
                Value<bool> syncPending = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => LocalSightingsCompanion.insert(
                id: id,
                objectId: objectId,
                detectedAt: detectedAt,
                encryptedLocation: encryptedLocation,
                locationNonce: locationNonce,
                confidence: confidence,
                evidenceAssetId: evidenceAssetId,
                syncPending: syncPending,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable(table),
                  $$LocalSightingsTableReferences(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: ({objectId = false, evidenceAssetId = false}) {
            return PrefetchHooks(
              db: db,
              explicitlyWatchedTables: [],
              addJoins:
                  <
                    T extends TableManagerState<
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic,
                      dynamic
                    >
                  >(state) {
                    if (objectId) {
                      state = state.withJoin(
                        currentTable: table,
                        currentColumn: table.objectId,
                        referencedTable: $$LocalSightingsTableReferences
                            ._objectIdTable(db),
                        referencedColumn: $$LocalSightingsTableReferences
                            ._objectIdTable(db)
                            .id,
                      ) as T;
                    }
                    if (evidenceAssetId) {
                      state = state.withJoin(
                        currentTable: table,
                        currentColumn: table.evidenceAssetId,
                        referencedTable: $$LocalSightingsTableReferences
                            ._evidenceAssetIdTable(db),
                        referencedColumn: $$LocalSightingsTableReferences
                            ._evidenceAssetIdTable(db)
                            .id,
                      ) as T;
                    }

                    return state;
                  },
              getPrefetchedDataCallback: (items) async {
                return [];
              },
            );
          },
        ),
      );
}

typedef $$LocalSightingsTableProcessedTableManager =
    ProcessedTableManager<
      _$TraceDatabase,
      $LocalSightingsTable,
      LocalSighting,
      $$LocalSightingsTableFilterComposer,
      $$LocalSightingsTableOrderingComposer,
      $$LocalSightingsTableAnnotationComposer,
      $$LocalSightingsTableCreateCompanionBuilder,
      $$LocalSightingsTableUpdateCompanionBuilder,
      (LocalSighting, $$LocalSightingsTableReferences),
      LocalSighting,
      PrefetchHooks Function({bool objectId, bool evidenceAssetId})
    >;

class $TraceDatabaseManager {
  final _$TraceDatabase _db;
  $TraceDatabaseManager(this._db);
  $$LocalObjectsTableTableManager get localObjects =>
      $$LocalObjectsTableTableManager(_db, _db.localObjects);
  $$SecureAssetsTableTableManager get secureAssets =>
      $$SecureAssetsTableTableManager(_db, _db.secureAssets);
  $$LocalObjectReferencesTableTableManager get localObjectReferences =>
      $$LocalObjectReferencesTableTableManager(_db, _db.localObjectReferences);
  $$LocalReferenceEmbeddingsTableTableManager get localReferenceEmbeddings =>
      $$LocalReferenceEmbeddingsTableTableManager(
        _db,
        _db.localReferenceEmbeddings,
      );
  $$LocalSightingsTableTableManager get localSightings =>
      $$LocalSightingsTableTableManager(_db, _db.localSightings);
}
