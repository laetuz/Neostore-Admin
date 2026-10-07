package id.neotica.neostore.admin.platform

import id.neotica.neostore.admin.domain.model.enrich.ScraperSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StoreHtmlParserTest {

    @Test
    fun parsesFdroid() {
        val html = """
            <html><body>
            <div class="package-description">A terminal emulator</div>
            <ul class="package-links"><li class="package-link" id="author_name">Author: Jane Doe</li></ul>
            <div id="screenshots"><ul><li class="js_slide screenshot"><img src="https://f-droid.org/repo/x/1.jpg" alt="app screenshot"/></li></ul></div>
            <img class="package-icon" src="https://f-droid.org/repo/x/icon.png"/>
            </body></html>
        """.trimIndent()

        val meta = parseStoreHtml(ScraperSource.FDROID, html)
        assertNotNull(meta)
        assertEquals("Jane Doe", meta.developer)
        assertEquals("A terminal emulator", meta.description)
        assertEquals(1, meta.screenshots.size)
        assertEquals("https://f-droid.org/repo/x/1.jpg", meta.screenshots.first())
    }

    @Test
    fun parsesPlay() {
        val html = """
            <html><body>
            <a href="/store/apps/dev?id=1">Acme Inc</a>
            <a href="/store/apps/category/TOOLS">Tools</a>
            <div data-g-id="description">Great app</div>
            <img alt="Screenshot image" src="https://play-lh.googleusercontent.com/abc=w526-h296-rw"/>
            </body></html>
        """.trimIndent()

        val meta = parseStoreHtml(ScraperSource.PLAY, html)
        assertNotNull(meta)
        assertEquals("Acme Inc", meta.developer)
        assertEquals("Tools", meta.categoryName)
        assertEquals("Great app", meta.description)
        assertEquals(1, meta.screenshots.size)
    }

    @Test
    fun parsesPlayCategoryFromGenre() {
        val html = """
            <html><body>
            <a href="/store/apps/dev?id=1">Acme Inc</a>
            <a href="/store/apps/category/FAMILY">Kids</a>
            <div itemprop="genre">
                <span jsname="V67aGc">Communication</span>
                <a href="/store/apps/category/COMMUNICATION" aria-label="Communication"></a>
            </div>
            </body></html>
        """.trimIndent()

        val meta = parseStoreHtml(ScraperSource.PLAY, html)
        assertNotNull(meta)
        assertEquals("Communication", meta.categoryName)
    }

    @Test
    fun parsesPlayCategoryFromAriaLabel() {
        val html = """
            <html><body>
            <a href="/store/apps/dev?id=1">Acme Inc</a>
            <a href="/store/apps/category/FAMILY">Kids</a>
            <a href="/store/apps/category/TOOLS" aria-label="Tools"></a>
            </body></html>
        """.trimIndent()

        val meta = parseStoreHtml(ScraperSource.WAYBACK, html)
        assertNotNull(meta)
        assertEquals("Tools", meta.categoryName)
    }

    @Test
    fun parsesLegacyPlayMarkup() {
        val html = """
            <html><body>
            <h1 class="doc-banner-title">WhatsApp Messenger</h1>
            <a href="/store/apps/developer?id=WhatsApp+Inc." class="doc-header-link">WhatsApp Inc.</a>
            <a href="/store/apps/category/COMMUNICATION?feature=category-nav">通信</a>
            <div id="doc-original-text">Get WhatsApp Messenger and say goodbye to SMS!</div>
            <img src="https://lh3.ggpht.com/abc=h230" class="doc-screenshot-img lightbox" data-baseUrl="https://lh3.ggpht.com/abc"/>
            </body></html>
        """.trimIndent()

        val meta = parseStoreHtml(ScraperSource.WAYBACK, html)
        assertNotNull(meta)
        assertEquals("WhatsApp Inc.", meta.developer)
        assertEquals("Communication", meta.categoryName)
        assertEquals("Get WhatsApp Messenger and say goodbye to SMS!", meta.description)
        assertEquals(1, meta.screenshots.size)
        assertEquals("https://lh3.ggpht.com/abc", meta.screenshots.first())
    }

    @Test
    fun returnsNullForEmptyHtml() {
        assertEquals(null, parseStoreHtml(ScraperSource.PLAY, ""))
    }
}
