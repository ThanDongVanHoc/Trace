sealed class TraceResult<T> {
  const TraceResult();
}

final class TraceSuccess<T> extends TraceResult<T> {
  const TraceSuccess(this.value);

  final T value;
}

final class TraceFailure<T> extends TraceResult<T> {
  const TraceFailure(this.code, this.message, [this.cause]);

  final TraceErrorCode code;
  final String message;
  final Object? cause;
}

enum TraceErrorCode {
  invalidImage,
  invalidRoi,
  lowImageQuality,
  modelError,
  noReferences,
  storageError,
  cryptoError,
  locationUnavailable,
  objectNotFound,
  networkError,
  unauthorized,
}
