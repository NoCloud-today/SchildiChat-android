/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.media.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.airbnb.mvrx.test.MvRxTestRule
import im.vector.app.core.intent.getMimeTypeFromUri
import im.vector.app.core.utils.saveMedia
import im.vector.app.features.notifications.NotificationUtils
import im.vector.app.test.fakes.FakeSession
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.OverrideMockKs
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyAll
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class DownloadMediaUseCaseTest {

    @get:Rule
    val mvRxTestRule = MvRxTestRule()

    @MockK
    lateinit var appContext: Context

    private val session = FakeSession()

    @MockK
    lateinit var notificationUtils: NotificationUtils

    @OverrideMockKs
    lateinit var downloadMediaUseCase: DownloadMediaUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("im.vector.app.core.utils.ExternalApplicationsUtilKt")
        mockkStatic("im.vector.app.core.intent.VectorMimeTypeKt")
        mockkStatic(Uri::class)
    }

    @After
    fun tearDown() {
        unmockkStatic("im.vector.app.core.utils.ExternalApplicationsUtilKt")
        unmockkStatic("im.vector.app.core.intent.VectorMimeTypeKt")
        unmockkStatic(Uri::class)
    }

    @Test
    fun `given a file when calling execute then save the file in local with success`() = runBlockingTest {
        // Given
        val file = mockk<File>()
        val uri = mockk<Uri>()
        val mimeType = "mimeType"
        val name = "name"
        every { file.name } returns name
        every { file.toUri() } returns uri
        every { getMimeTypeFromUri(appContext, uri) } returns mimeType
        coEvery { saveMedia(any(), any(), any(), any(), any()) } just runs

        // When
        val result = downloadMediaUseCase.execute(file)

        // Then
        assert(result.isSuccess)
        verifyAll {
            file.name
            file.toUri()
        }
        verify {
            getMimeTypeFromUri(appContext, uri)
        }
        coVerify {
            saveMedia(appContext, file, name, mimeType, notificationUtils)
        }
    }

    @Test
    fun `given a file when calling execute then save the file in local with error`() = runBlockingTest {
        // Given
        val file = mockk<File>()
        val uri = mockk<Uri>()
        val mimeType = "mimeType"
        val name = "name"
        val error = Throwable()
        every { file.name } returns name
        every { file.toUri() } returns uri
        every { getMimeTypeFromUri(appContext, uri) } returns mimeType
        coEvery { saveMedia(any(), any(), any(), any(), any()) } throws error

        // When
        val result = downloadMediaUseCase.execute(file)

        // Then
        assert(result.isFailure && result.exceptionOrNull() == error)
        verifyAll {
            file.name
            file.toUri()
        }
        verify {
            getMimeTypeFromUri(appContext, uri)
        }
        coVerify {
            saveMedia(appContext, file, name, mimeType, notificationUtils)
        }
    }
}
