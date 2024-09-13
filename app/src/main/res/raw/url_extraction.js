(function() {
    window.addEventListener("DOMContentLoaded", function(event) {
        UrlExtraction.log("DOM content loaded");
        const canonicalLinks = document.querySelectorAll('[rel="canonical"]');
        const url = canonicalLinks.length > 0 ? canonicalLinks[0].href : null;
        UrlExtraction.urlExtracted(url);
    });
})();
