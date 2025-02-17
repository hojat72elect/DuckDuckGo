

package com.duckduckgo.downloads.impl

import android.webkit.MimeTypeMap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class DownloaderUtilTest {

    @Before
    fun setup() {
        Shadows.shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypeMapping("pdf", "application/pdf")
    }

    @Test
    fun whenNullContentDispositionAndApplicationPdfContentTypeAndIncorrectFileExtensionInUrlThenCorrectFilenameExtracted() {
        assertEquals(
            "display_pdf.pdf",
            DownloaderUtil.guessFileName(
                url = """
                    https://app.ecourts.gov.in/ecourt_mobile_encrypted_DC/display_pdf.php?filename=tsdf3b4j9eDY7P484mIWwB4thbfVfb9VP5F2YO%2FsEL01hhbEhRN7YUHOyJ95xJ7L&caseno=hoK77H9Pkg5OBjIcPryg7r8RVKQtM2WgWLGpNcdQiZI%3D&cCode=9ZlIEp0G5VNxKgqa5ZA1kA%3D%3D&appFlag=&state_cd=DytSfGyRsIC664sNz2SgoA%3D%3D&dist_cd=84p6UkEEfUOd%2BhVJ3YrfmA%3D%3D&court_code=9ZlIEp0G5VNxKgqa5ZA1kA%3D%3D&bilingual_flag=0
                """.trimIndent(),
                contentDisposition = null,
                mimeType = "application/pdf",
            ),
        )
    }

    @Test
    fun whenNullContentDispositionAndImageJpegContentTypeAndUnsupportedCharsInFilenameInsideUrlThenCorrectFilenameExtracted() {
        assertEquals(
            "0_BRkMUINdhCYPk1KM.jpg",
            DownloaderUtil.guessFileName(
                url = "https://miro.medium.com/max/1050/0*BRkMUINdhCYPk1KM.jpg",
                contentDisposition = null,
                mimeType = "image/jpeg",
            ),
        )
    }

    @Test
    fun whenAttachmentWithFilenameContentDispositionAndApplicationPdfContentTypeAndCorrectFileExtensionInUrlThenCorrectFilenameExtracted() {
        assertEquals(
            "MENU_2103.pdf",
            DownloaderUtil.guessFileName(
                url = "https://www.jdwetherspoon.com/~/media/files/pdf-documents/menus/currentmenus/menu_2103.pdf",
                contentDisposition = """attachment; filename="MENU_2103.pdf"""",
                mimeType = "application/pdf",
            ),
        )
    }

    @Test
    fun whenAttachmentWithFilenameContentDispositionAndApplicationOctetStreamContentTypeAndCorrectFileExtensionInUrlThenCorrectFilenameExtracted() {
        assertEquals(
            "Telegram.apk",
            DownloaderUtil.guessFileName(
                url = """
                    https://cdn4.telesco.pe/file/Telegram.apk?token=NuxgpIu_V2xfigA0w9J9OYJ4VObGzBN3UIE4lHKxU_fDAwpCygehc7X7HMtKyX0-81yZ25nAnXYPrqQld92Bj7MDqx2uLYdqC2PXlrwKrsxHPcstsWLccYkecYLKg3tXLiP0pCqcW4jOqn44cq9sReibdT-HpGaB3POji9b9MyR1deOaZLbbIXyU8j8EpltuuN28vDDAx8E1rHXLCYt0x_pGMWYpOMIAYVZmx05Rbw0C3t05VwkVLnmj28kRt2p39dtdU3y4Fg7AGjH1rRk5goOKSuOrncazjRlZSWfferWTvm5rYWQdtAn16izMh03pr7v4dq8_88SDP4RRqTj_V0X5_lzZhB-GP1deYWAlZcf9R-t3QyELA3qxA2K5U6VXHiD93PLNowwWadeqdeEEXZiTqycH8FMJZwP6PHf6dxHLr5sQI1nSfJh0ulei-bJwvTrXslY-0-hy5FaRzfQk6F-y-w8dxIGYThH8zhFzyCnaFLJg72z8DeXb49X8ukqQhz-Iv4ufXvzuZw0zBXbM0WE2BxTSbYnZOjnkCabxaNqiYtLrg5GHtvE5Uq5c2pT4sin4duCNsFVHAo6hSkiolZo1YDrbzu3lgGoMyFML_9hJ4vldgKCCBSRs_wgLpLc_Lyxh6T2rYu_e4g6UtE_zD3wooBl76vcgVMSuUfoZgUwGClPBKqBAyzlUZI1slIg6d0qvkfP5Sp8sZA5s1QczzWHCQ9ttf4M7YXRfwYexx2cLAOoiFPLZnsrizUQKaWsuenevxQtf7x-z3K4xQIDdnno6qKRZRvdG3t50vyKVzbWdWEL0rsNv1BakQlze0xGKTmoJkFCqHRRYtDPp9zCNV5Xx8jEYy0alwa4sjZO5cSwztNyULXpxJSCDRuERPeifFKmDcz6c3Ce1ZvTLh8D_ECZOC-HKRpmHoLTHgywathGjtpnCMG_wlVTpwK98DSKIPb0nGDAc7Ob4LivUSyYkLjSy2VbAsdqp61GoD0ZnzHZQHFPWyMmuZnGWMaGeJSDjem-SLPhjzZAoZatwKIdTYqgG6hjE7OoRwCdeTCE0X1hdv95S5q3vJsdETU0RXqimUOYZOfibVu9BVsUsex5T5pmFigUEsgl8sBuggAE9R4S95nyd-c_cP6w84IZqBBJbF9zZjDj8DptwqlMwuXzMRox7ORQWQ5NO_6CGcZjFncoIIOKu415aK6prEy4uyFEu6MGrU3k28-gSxWGnxVxj9KxJRUfVBv2zwtC4wBiemxXROBiWYhwE-JKMN0t6UH5fyN6vKbOEAQFO9iR-oOUyeETcEUEi47HV6oSLlT8L-dKMd0IMT4-a7A9Hb-ftPZAxk55tPrpDNihFu7CvCBESuA
                """.trimIndent(),
                contentDisposition = """attachment; filename="Telegram.apk"""",
                mimeType = "application/octet-stream",
            ),
        )
    }

    @Test
    fun whenNullContentDispositionAndApplicationOctetStreamContentTypeAndCorrectFileExtensionInUrlThenCorrectFilenameExtracted() {
        assertEquals(
            "youtube.apk",
            DownloaderUtil.guessFileName(
                url = """
                    https://secure.downloadfp.com/android/US/com.google.android.youtube/114955670/youtube.apk?st=NhRdigQiE6mo5cpAqxZtGQ&e=1640362281
                """.trimIndent(),
                contentDisposition = null,
                mimeType = "application/octet-stream",
            ),
        )
    }

    @Test
    fun whenInlineWithFilenameContentDispositionAndApplicationPdfContentTypeAndCorrectFileExtensionInUrlThenCorrectFilenameExtracted() {
        assertEquals(
            "Monitoraggio_Fase_2__report_nazionale_83_finale.pdf",
            DownloaderUtil.guessFileName(
                url = """
                    https://www.iss.it/documents/20126/0/Monitoraggio+Fase+2_+report_nazionale_83_finale.pdf/44233871-6660-ed64-b136-306c984f5b2d?t=1639759695768
                """.trimIndent(),
                contentDisposition = """inline; filename="Monitoraggio Fase 2_ report_nazionale_83_finale.pdf"""",
                mimeType = "application/pdf",
            ),
        )
    }

    @Test
    fun whenEmptyContentDispositionAndApplicationOctetStreamContentTypeAndCorrectFileExtensionInUrlThenCorrectFilenameExtracted() {
        assertEquals(
            "Pass-Example-Generic.pkpass",
            DownloaderUtil.guessFileName(
                url = "https://github.com/keefmoon/Passbook-Example-Code/raw/master/Pass-Example-Generic/Pass-Example-Generic.pkpass",
                contentDisposition = "",
                mimeType = "application/octet-stream",
            ),
        )
    }

    @Test
    fun whenEmptyContentDispositionAndApplicationPdfContentTypeAndCorrectFileExtensionInUrlThenCorrectFilenameExtracted() {
        assertEquals(
            "be1da227fa380c8d057edbcf19cc8e14_pdf.pdf",
            DownloaderUtil.guessFileName(
                url = "https://parsefiles.back4app.com/e4YQYTfDTeW9xmA2sGEd9WL7PWomHoGgC4xrNKqI/be1da227fa380c8d057edbcf19cc8e14_pdf.pdf",
                contentDisposition = "",
                mimeType = "application/pdf",
            ),
        )
    }

    @Test
    fun whenEmptyContentDispositionAndApplicationOctetStreamContentTypeAndCorrectFileExtensionInUrlSecondExampleThenCorrectFilenameExtracted() {
        assertEquals(
            "ahc01_learn_to_play_web.pdf",
            DownloaderUtil.guessFileName(
                url = "https://images-cdn.fantasyflightgames.com/filer_public/88/53/88538d11-5274-4b4a-ac8c-e8d758f71132/ahc01_learn_to_play_web.pdf",
                contentDisposition = "",
                mimeType = "application/pdf",
            ),
        )
    }

    @Test
    fun whenEmptyContentDispositionAndApplicationPdfContentTypeAndCorrectFileExtensionInUrlSecondExampleThenCorrectFilenameExtracted() {
        assertEquals(
            "m_series.pdf",
            DownloaderUtil.guessFileName(
                url =
                "https://data2.manualslib.com/pdf7/219/21886/2188542-mitsubishi_electric/m_series.pdf?ac6062bf6b9121363d8e4fcd47c39d2f",
                contentDisposition = "",
                mimeType = "application/pdf",
            ),
        )
    }

    @Test
    fun whenEmptyContentDispositionAndApplicationVndApplePkPassContentTypeAndCorrectFileExtensionInUrlThenCorrectFilenameExtracted() {
        assertEquals(
            "93348617.pkpass",
            DownloaderUtil.guessFileName(
                url = """
                    https://clorian-passbook-prod.s3.eu-west-1.amazonaws.com/MezquitaDeCordoba/e25e2290-334a-4227-b86c-693a249d8f88/93348617.pkpass
                """.trimIndent(),
                contentDisposition = "",
                mimeType = "application/vnd.apple.pkpass",
            ),
        )
    }

    @Test
    fun whenEmptyContentDispositionAndTextXpython3ContentTypeAndCorrectFileExtensionInUrlThenCorrectFilenameExtracted() {
        assertEquals(
            "bat.py",
            DownloaderUtil.guessFileName(
                url = """
                    https://ddg-name-test-ubsgiobgibsdgsbklsdjgm.netlify.app/uploads/qwertyuiopasdfghjklzxcvbnm1234567890qwertyuiopasdfghjklzxcvbnm/bat.py
                """.trimIndent(),
                contentDisposition = "",
                mimeType = "text/x-python3; charset=UTF-8",
            ),
        )
    }

    @Test
    fun whenContentDispositionWithAttachmentAndFilenameBetweenQuotesAndSpaceAfterSemicolonThenCorrectFilenameExtracted() {
        assertEquals("filename.jpg", DownloaderUtil.fileNameFromContentDisposition("""attachment; filename="filename.jpg""""))
    }

    @Test
    fun whenContentDispositionWithAttachmentAndFilenameBetweenQuotesAndSpacesBeforeAndAfterSemicolonThenCorrectFilenameExtracted() {
        assertEquals("filename.jpg", DownloaderUtil.fileNameFromContentDisposition("""attachment ; filename="filename.jpg""""))
    }

    @Test
    fun whenContentDispositionWithAttachmentAndFilenameBetweenQuotesAndNoSpacesThenCorrectFilenameExtracted() {
        assertEquals("filename.jpg", DownloaderUtil.fileNameFromContentDisposition("""attachment;filename="filename.jpg""""))
    }

    @Test
    fun whenContentDispositionWithAttachmentAndFilenameWithoutQuotesAndWithSpaceAfterSemicolonThenCorrectFilenameExtracted() {
        assertEquals("filename.jpg", DownloaderUtil.fileNameFromContentDisposition("""attachment; filename=filename.jpg"""))
    }

    @Test
    fun whenContentDispositionWithInlineAndFilenameBetweenQuotesAndSpaceAfterSemicolonThenCorrectFilenameExtracted() {
        assertEquals("filename.jpg", DownloaderUtil.fileNameFromContentDisposition("""inline; filename="filename.jpg""""))
    }

    @Test
    fun whenContentDispositionWithAttachmentAndFilenameEncodedThenEncodedFilenameExtracted() {
        assertEquals(
            "%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt",
            DownloaderUtil.fileNameFromContentDisposition(
                """attachment; filename*= "%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt"""",
            ),
        )
    }

    @Test
    fun whenContentDispositionWithAttachmentAndFilenameStarEncodedThenDecodedFilenameExtracted() {
        assertEquals(
            "中文文件名测试.txt",
            DownloaderUtil.fileNameFromContentDisposition(
                """attachment; filename*=utf-8''%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt""",
            ),
        )
    }

    @Test
    fun whenContentDispositionWithAttachmentAndFilenameStarEncodedAndFilenameThenDecodedFilenameExtracted() {
        assertEquals(
            "中文文件名测试.txt",
            DownloaderUtil.fileNameFromContentDisposition(
                """
                    attachment; 
                    filename*=utf-8''%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt; 
                    filename= "%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun whenContentDispositionWithAttachmentAndFilenameAndFilenameStarEncodedThenDecodedFilenameExtracted() {
        assertEquals(
            "中文文件名测试.txt",
            DownloaderUtil.fileNameFromContentDisposition(
                """
                    attachment; 
                    filename= "%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt; 
                    filename*=utf-8''%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt;
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun whenContentDispositionWithAttachmentAndFilenameStarEncodedWithNoEncodingAndFilenameThenDecodedFilenameExtractedFallback() {
        assertEquals(
            "%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt",
            DownloaderUtil.fileNameFromContentDisposition(
                """
                    attachment; 
                    filename*=%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt; 
                    filename= "%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun whenContentDispositionWithInlineAndFilenameStarEncodedAndFilenameThenDecodedFilenameExtracted() {
        assertEquals(
            "中文文件名测试.txt",
            DownloaderUtil.fileNameFromContentDisposition(
                """
                    inline; 
                    filename= "%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt; 
                    filename*=utf-8''%E4%B8%AD%E6%96%87%E6%96%87%E4%BB%B6%E5%90%8D%E6%B5%8B%E8%AF%95.txt;
                """.trimIndent(),
            ),
        )
    }
}
