/*
 * Copyright 2018 Google, Inc.
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

package io.plaidapp.core.designernews.data.stories

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.errorResponseBody
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Response
import java.util.Date
import java.util.GregorianCalendar

/**
 * Test for [StoriesRemoteDataSource] mocking all dependencies.
 */
class StoriesRemoteDataSourceTest {

    private val createdDate: Date = GregorianCalendar(2018, 1, 13).time
    private val story = Story(id = 45L, title = "Plaid 2.0 was released", createdAt = createdDate)
    private val storySequel =
        Story(id = 876L, title = "Plaid 2.0 is bug free", createdAt = createdDate)
    private val stories = listOf(story, storySequel)
    private val query = "Plaid 2.0"

    private val service = Mockito.mock(DesignerNewsService::class.java)
    private val dataSource = StoriesRemoteDataSource(service)

    @Test
    fun loadTopStories_withSuccess() = runBlocking {
        // Given that the service responds with success
        withTopStoriesSuccess(2, stories)

        // When requesting the users
        val result = dataSource.loadTopStories(2)

        // Then there's one request to the service
        Mockito.verify(service).getTopStories(2)
        // Then the correct list of stories is returned
        Assert.assertEquals(Result.Success(stories), result)
    }

    @Test
    fun loadTopStories_withError() = runBlocking {
        // Given that the service responds with error
        withTopStoriesError(1)

        // When requesting the top stories
        val result = dataSource.loadTopStories(1)

        // Then error is returned
        Assert.assertTrue(result is Result.Error)
    }

    @Test
    fun search_withSuccess() = runBlocking {
        // Given that the service responds with success
        withSearchSuccess(query, 2, stories)

        // When requesting the recent stories
        val result = dataSource.search(query, 2)

        // Then there's one request to the service
        Mockito.verify(service).search(query, 2)
        // Then the correct list of stories is returned
        Assert.assertEquals(Result.Success(stories), result)
    }

    @Test
    fun search_withError() = runBlocking {
        // Given that the service responds with error
        withSearchError(query, 1)

        // When requesting the recent stories
        val result = dataSource.search(query, 1)

        // Then error is returned
        Assert.assertTrue(result is Result.Error)
    }

    private fun withTopStoriesSuccess(page: Int, users: List<Story>) {
        val result = Response.success(users)
        Mockito.`when`(service.getTopStories(page)).thenReturn(CompletableDeferred(result))
    }

    private fun withTopStoriesError(page: Int) {
        val result = Response.error<List<Story>>(
            400,
            errorResponseBody
        )
        Mockito.`when`(service.getTopStories(page)).thenReturn(CompletableDeferred(result))
    }

    private fun withSearchSuccess(query: String, page: Int, users: List<Story>) {
        val result = Response.success(users)
        Mockito.`when`(service.search(query, page)).thenReturn(CompletableDeferred(result))
    }

    private fun withSearchError(query: String, page: Int) {
        val result = Response.error<List<Story>>(
            400,
            errorResponseBody
        )
        Mockito.`when`(service.search(query, page)).thenReturn(CompletableDeferred(result))
    }
}
