package com.memoir.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Result sealed class
 */
class ResultTest {

    @Test
    fun `Success should store data correctly`() {
        // When
        val result = Result.Success("test data")

        // Then
        assertEquals("test data", result.data)
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertFalse(result.isLoading)
    }

    @Test
    fun `Success getOrNull should return data`() {
        // When
        val result = Result.Success("test data")

        // Then
        assertEquals("test data", result.getOrNull())
    }

    @Test
    fun `Success errorOrNull should return null`() {
        // When
        val result = Result.Success("test data")

        // Then
        assertNull(result.errorOrNull())
    }

    @Test
    fun `Error should store message and code correctly`() {
        // When
        val result = Result.Error("Error message", "404")

        // Then
        assertEquals("Error message", result.message)
        assertEquals("404", result.code)
        assertFalse(result.isSuccess)
        assertTrue(result.isError)
        assertFalse(result.isLoading)
    }

    @Test
    fun `Error with throwable should store throwable`() {
        // Given
        val exception = Exception("Test exception")

        // When
        val result = Result.Error("Error message", "500", exception)

        // Then
        assertEquals("Error message", result.message)
        assertEquals("500", result.code)
        assertEquals(exception, result.throwable)
    }

    @Test
    fun `Error getOrNull should return null`() {
        // When
        val result = Result.Error("Error message", "404")

        // Then
        assertNull(result.getOrNull())
    }

    @Test
    fun `Error errorOrNull should return error`() {
        // When
        val result = Result.Error("Error message", "404")

        // Then
        assertNotNull(result.errorOrNull())
        assertEquals("Error message", result.errorOrNull()?.message)
        assertEquals("404", result.errorOrNull()?.code)
    }

    @Test
    fun `Loading should have correct state flags`() {
        // When
        val result = Result.Loading

        // Then
        assertFalse(result.isSuccess)
        assertFalse(result.isError)
        assertTrue(result.isLoading)
    }

    @Test
    fun `Loading getOrNull should return null`() {
        // When
        val result = Result.Loading

        // Then
        assertNull(result.getOrNull())
    }

    @Test
    fun `Loading errorOrNull should return null`() {
        // When
        val result = Result.Loading

        // Then
        assertNull(result.errorOrNull())
    }

    @Test
    fun `Success with complex object should work`() {
        // Given
        data class User(val id: String, val name: String)
        val user = User("123", "홍길동")

        // When
        val result = Result.Success(user)

        // Then
        assertEquals(user, result.data)
        assertEquals("123", result.data.id)
        assertEquals("홍길동", result.data.name)
    }

    @Test
    fun `Error without code should work`() {
        // When
        val result = Result.Error("Error message")

        // Then
        assertEquals("Error message", result.message)
        assertNull(result.code)
        assertNull(result.throwable)
    }

    @Test
    fun `multiple Success instances should be independent`() {
        // When
        val result1 = Result.Success("data1")
        val result2 = Result.Success("data2")

        // Then
        assertEquals("data1", result1.data)
        assertEquals("data2", result2.data)
    }

    @Test
    fun `multiple Error instances should be independent`() {
        // When
        val result1 = Result.Error("Error 1", "404")
        val result2 = Result.Error("Error 2", "500")

        // Then
        assertEquals("Error 1", result1.message)
        assertEquals("404", result1.code)
        assertEquals("Error 2", result2.message)
        assertEquals("500", result2.code)
    }
}
