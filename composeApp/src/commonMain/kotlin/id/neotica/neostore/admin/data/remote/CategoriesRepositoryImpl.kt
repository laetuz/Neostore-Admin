package id.neotica.neostore.admin.data.remote

import id.neotica.neostore.admin.domain.model.category.response.Category
import id.neotica.neostore.admin.domain.model.category.request.CreateCategoryRequest
import id.neotica.neostore.admin.domain.model.category.request.UpdateCategoryRequest
import id.neotica.neostore.admin.domain.remote.CategoriesRepository
import id.neotica.neostore.admin.utils.Constants.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class CategoriesRepositoryImpl(
    private val client: HttpClient
) : CategoriesRepository {

    override suspend fun getCategories(): Result<List<Category>> = try {
        val response = client.get("$BASE_URL/neostore/categories")
        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createCategory(name: String, slug: String, parentSlug: String?): Result<String> = try {
        val response = client.post("$BASE_URL/neostore/admin/categories") {
            contentType(ContentType.Application.Json)
            setBody(CreateCategoryRequest(name, slug, parentSlug))
        }
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to create category: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateCategory(slug: String, name: String?, parentSlug: String?): Result<String> = try {
        val response = client.put("$BASE_URL/neostore/admin/categories/$slug") {
            contentType(ContentType.Application.Json)
            setBody(UpdateCategoryRequest(name, parentSlug))
        }
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to update category: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteCategory(slug: String): Result<String> = try {
        val response = client.delete("$BASE_URL/neostore/admin/categories/$slug")
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to delete category: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}