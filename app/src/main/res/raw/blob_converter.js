(function() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', '%blobUrl%', true);
    xhr.setRequestHeader('Content-type','%contentType%');
    xhr.responseType = 'blob';
    xhr.onload = function(e) {
        if (this.status == 200) {
            var blob = this.response;
            var reader = new FileReader();
            reader.readAsDataURL(blob);
            reader.onloadend = function() {
                dataUrl = reader.result;
                BlobConverter.convertBlobToDataUri(dataUrl, '%contentType%');
            }
            reader.onerror = function() {
                BlobConverter.convertBlobToDataUri('error', '%contentType%');
            }
        } else {
            BlobConverter.convertBlobToDataUri('error', '%contentType%');
        }
    };
    xhr.send();
})();
