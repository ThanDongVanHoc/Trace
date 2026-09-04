package com.traceapp.android.ui.scan

import android.media.ExifInterface
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CameraOrientationTest {
    @Test
    fun exifRotations_areConvertedToClockwiseDegrees() {
        assertThat(rotationDegreesForExif(ExifInterface.ORIENTATION_NORMAL)).isEqualTo(0)
        assertThat(rotationDegreesForExif(ExifInterface.ORIENTATION_ROTATE_90)).isEqualTo(90)
        assertThat(rotationDegreesForExif(ExifInterface.ORIENTATION_ROTATE_180)).isEqualTo(180)
        assertThat(rotationDegreesForExif(ExifInterface.ORIENTATION_ROTATE_270)).isEqualTo(270)
    }
}
