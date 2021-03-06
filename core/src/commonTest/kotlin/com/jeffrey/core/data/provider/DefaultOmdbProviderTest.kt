package com.jeffrey.core.data.provider

import InjectMocksRule
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.observable.observableOfError
import com.badoo.reaktive.test.base.assertError
import com.badoo.reaktive.test.base.assertNotError
import com.badoo.reaktive.test.observable.assertNoValues
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.test
import com.jeffrey.core.data.entity.MovieResponse
import com.jeffrey.core.data.entity.MoviesResponse
import com.jeffrey.core.data.network.HttpClient
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

class DefaultOmdbProviderTest {

    @MockK
    lateinit var httpClient: HttpClient

    private lateinit var provider: OmdbProvider

    private var defaultApiKey = "12345"

    private var defaultQuery = "test"

    private lateinit var moviesResponse1: MoviesResponse

    private lateinit var defaultParams: Map<String, String>

    @BeforeTest
    fun setup() {
        InjectMocksRule.createMockK(this)

        moviesResponse1 = createMockMoviesResponse(true, 5)
        defaultParams = mapOf<String, String>(
            Pair("s", defaultQuery),
            Pair("page", "1"),
            Pair("apiKey", defaultApiKey)
        )
        every { httpClient.get("", MoviesResponse.serializer(), defaultParams) } returns observableOf(moviesResponse1)

        provider = DefaultOmdbProvider(httpClient, defaultApiKey)
    }

    @Test
    fun `searchMovie should trigger HttpClient to make request and return MoviesResponse`() {

        val observer = provider.searchMovie(defaultQuery, 1).test(false)

        observer.assertNotError()
        observer.assertValue(moviesResponse1)

        verify { httpClient.get("", MoviesResponse.serializer(), defaultParams) }
    }

    @Test
    fun `searchMovie with Network problem will return an error`() {
        val expectedError = Exception("No Internet Connection")
        every { httpClient.get("", MoviesResponse.serializer(), defaultParams) } returns observableOfError(expectedError)

        val observer = provider.searchMovie(defaultQuery, 1).test(false)

        observer.assertError { it == expectedError }
        observer.assertNoValues()

        verify { httpClient.get("", MoviesResponse.serializer(), defaultParams) }
    }

    private fun createMockMoviesResponse(success: Boolean, numOfItems: Int = 0): MoviesResponse {
        if (!success)
            return MoviesResponse(null, false)
        val list = mutableListOf<MovieResponse>()
        for (x in 1..numOfItems) {
            val movie = mockk<MovieResponse>()
            every { movie.title } returns "Title"
            every { movie.imdbID } returns "imdbId"
            every { movie.poster } returns "poster"
            every { movie.type } returns "type"
            every { movie.year } returns "year"
            list.add(movie)
        }
        return MoviesResponse(list, success)
    }

//    @UseExperimental(ImplicitReflectionSerializer::class)
//    fun mockSearchResult(): SearchResultResponse{
//        return JSON(strictMode = false).parse("{\"Search\":[{\"Title\":\"Game of Thrones\",\"Year\":\"2011–\",\"imdbID\":\"tt0944947\",\"Type\":\"series\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BMjE3NTQ1NDg1Ml5BMl5BanBnXkFtZTgwNzY2NDA0MjI@._V1_SX300.jpg\"},{\"Title\":\"The Imitation Game\",\"Year\":\"2014\",\"imdbID\":\"tt2084970\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BOTgwMzFiMWYtZDhlNS00ODNkLWJiODAtZDVhNzgyNzJhYjQ4L2ltYWdlXkEyXkFqcGdeQXVyNzEzOTYxNTQ@._V1_SX300.jpg\"},{\"Title\":\"Sherlock Holmes: A Game of Shadows\",\"Year\":\"2011\",\"imdbID\":\"tt1515091\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BMTQwMzQ5Njk1MF5BMl5BanBnXkFtZTcwNjIxNzIxNw@@._V1_SX300.jpg\"},{\"Title\":\"The Game\",\"Year\":\"1997\",\"imdbID\":\"tt0119174\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BZGVmMDNmYmEtNGQ2Mi00Y2ZhLThhZTYtYjE5YmQzMjZiZGMxXkEyXkFqcGdeQXVyNDk3NzU2MTQ@._V1_SX300.jpg\"},{\"Title\":\"Ender's Game\",\"Year\":\"2013\",\"imdbID\":\"tt1731141\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BMjAzMzI5OTgzMl5BMl5BanBnXkFtZTgwMTU5MTAwMDE@._V1_SX300.jpg\"},{\"Title\":\"Spy Game\",\"Year\":\"2001\",\"imdbID\":\"tt0266987\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BNjNhOGZkNzktMGU3NC00ODk2LWE4NjctZTliN2JjZTQxZmIxXkEyXkFqcGdeQXVyNDk3NzU2MTQ@._V1_SX300.jpg\"},{\"Title\":\"Game Night\",\"Year\":\"2018\",\"imdbID\":\"tt2704998\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BMjI3ODkzNDk5MF5BMl5BanBnXkFtZTgwNTEyNjY2NDM@._V1_SX300.jpg\"},{\"Title\":\"Molly's Game\",\"Year\":\"2017\",\"imdbID\":\"tt4209788\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BNTkzMzRlYjEtMTQ5Yi00OWY3LWI0NzYtNGQ4ZDkzZTU0M2IwXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_SX300.jpg\"},{\"Title\":\"Gerald's Game\",\"Year\":\"2017\",\"imdbID\":\"tt3748172\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BMzg0NGE0N2MtYTg1My00NTBkLWI5NjEtZTgyMDA0MTU4MmIyXkEyXkFqcGdeQXVyMTU2NTcyMg@@._V1_SX300.jpg\"},{\"Title\":\"The Game Plan\",\"Year\":\"2007\",\"imdbID\":\"tt0492956\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BMTAzNDIyODYzMTJeQTJeQWpwZ15BbWU3MDA3NTA5NDE@._V1_SX300.jpg\"}],\"totalResults\":\"3330\",\"Response\":\"True\"}")
//    }

}