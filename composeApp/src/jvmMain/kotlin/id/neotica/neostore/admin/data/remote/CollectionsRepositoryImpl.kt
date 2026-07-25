package id.neotica.neostore.admin.data.remote

import id.neotica.neostore.admin.domain.model.collection.request.AddAppToCollectionRequest
import id.neotica.neostore.admin.domain.model.collection.request.CreateCollectionRequest
import id.neotica.neostore.admin.domain.model.collection.request.UpdateCollectionRequest
import id.neotica.neostore.admin.domain.model.collection.response.AppCollection
import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse
import id.neotica.neostore.admin.domain.model.response.PaginationResponse
import id.neotica.neostore.admin.domain.remote.CollectionsRepository
import id.neotica.neostore.admin.utils.Constants.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class CollectionsRepositoryImpl(
    private val client: HttpClient
) : CollectionsRepository {

    override suspend fun listCollections(): Result<List<AppCollection>> = try {
        val response = client.get("$BASE_URL/neostore/collections")
        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCollection(slug: String): Result<AppCollection> = try {
        val response = client.get("$BASE_URL/neostore/collections/$slug")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Failed to fetch collection: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createCollection(title: String, description: String, customSlug: String?): Result<String> = try {
        val response = client.post("$BASE_URL/neostore/admin/collections") {
            contentType(ContentType.Application.Json)
            setBody(CreateCollectionRequest(title, description, customSlug))
        }
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to create collection: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateCollection(slug: String, title: String?, description: String?): Result<String> = try {
        val response = client.put("$BASE_URL/neostore/admin/collections/$slug") {
            contentType(ContentType.Application.Json)
            setBody(UpdateCollectionRequest(title, description))
        }
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to update collection: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteCollection(slug: String): Result<String> = try {
        val response = client.delete("$BASE_URL/neostore/admin/collections/$slug")
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else if (response.status.value == 409) {
            Result.failure(Exception("Collection still has apps"))
        } else {
            Result.failure(Exception("Failed to delete collection: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addAppToCollection(slug: String, packageName: String, sortOrder: Int): Result<String> = try {
        val response = client.post("$BASE_URL/neostore/admin/collections/$slug/items") {
            contentType(ContentType.Application.Json)
            setBody(AddAppToCollectionRequest(packageName, sortOrder))
        }
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to add app: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun removeAppFromCollection(slug: String, packageName: String): Result<String> = try {
        val response = client.delete("$BASE_URL/neostore/admin/collections/$slug/items/$packageName")
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to remove app: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCollectionFeed(slug: String, page: Int, limit: Int): Result<PaginationResponse<AppFeedItemResponse>> = try {
        val response = client.get("$BASE_URL/neostore/apps/collections/$slug") {
            parameter("page", page)
            parameter("limit", limit)
        }
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Failed to fetch feed: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}