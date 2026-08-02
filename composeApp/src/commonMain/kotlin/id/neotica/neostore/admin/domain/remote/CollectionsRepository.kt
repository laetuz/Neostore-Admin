package id.neotica.neostore.admin.domain.remote

import id.neotica.neostore.admin.domain.model.collection.response.AppCollection
import id.neotica.neostore.admin.domain.model.collection.response.CollectionOrganizerItem
import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse
import id.neotica.neostore.admin.domain.model.response.PaginationResponse

interface CollectionsRepository {
    suspend fun listCollections(): Result<List<AppCollection>>
    suspend fun getCollection(slug: String): Result<AppCollection>
    suspend fun createCollection(title: String, description: String = "", customSlug: String? = null): Result<String>
    suspend fun updateCollection(slug: String, title: String? = null, description: String? = null): Result<String>
    suspend fun deleteCollection(slug: String): Result<String>
    suspend fun addAppToCollection(slug: String, packageName: String, sortOrder: Int): Result<String>
    suspend fun removeAppFromCollection(slug: String, packageName: String): Result<String>
    suspend fun getCollectionFeed(slug: String, page: Int = 1, limit: Int = 10): Result<PaginationResponse<AppFeedItemResponse>>
    suspend fun getOrganizer(): Result<List<CollectionOrganizerItem>>
    suspend fun addToOrganizer(collectionSlug: String, sortOrder: Int): Result<Unit>
    suspend fun removeFromOrganizer(slug: String): Result<Unit>
}