package id.neotica.neostore.admin.domain

import id.neotica.neostore.admin.domain.remote.MetadataScraper
import id.neotica.neostore.admin.platform.clearImageCache

class CacheManager(
    private val scraper: MetadataScraper
) {
    fun clearAll() {
        clearImageCache()
        scraper.clearCache()
    }
}
