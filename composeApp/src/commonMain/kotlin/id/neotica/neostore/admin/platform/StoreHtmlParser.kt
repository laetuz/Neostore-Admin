package id.neotica.neostore.admin.platform

import id.neotica.neostore.admin.domain.model.enrich.ScrapedAppMetadata
import id.neotica.neostore.admin.domain.model.enrich.ScraperSource

expect fun parseStoreHtml(source: ScraperSource, html: String): ScrapedAppMetadata?
