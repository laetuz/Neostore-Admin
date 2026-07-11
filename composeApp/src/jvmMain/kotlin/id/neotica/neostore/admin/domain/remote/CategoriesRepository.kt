package id.neotica.neostore.admin.domain.remote

import id.neotica.neostore.admin.domain.model.category.response.Category

interface CategoriesRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun createCategory(name: String, slug: String, parentSlug: String? = null): Result<String>
    suspend fun updateCategory(slug: String, name: String? = null, parentSlug: String? = null): Result<String>
    suspend fun deleteCategory(slug: String): Result<String>
}