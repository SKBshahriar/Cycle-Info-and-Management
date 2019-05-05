$(function () {
    $("#mybtn").click(function () {
        var chassis_number = $(this).siblings("input[name='chassis_number']").val();
        $.ajax({
            type: "GET",
            url: "/test",
            data: {chassis_number:chassis_number,},
            success: function(data) {
                $("#owner").html(data)
            }
        });
    })


    $( "#search-box" ).autocomplete({
        source: function( request, response ) {
            var text = $( "#search-box" ).val()
            $.ajax( {
                url: "/getsrchsgst",
                dataType: "json",
                data: {text:text},
                success: function( data ) {
                    response( data );
                }
            } );
        }
    } );

    if (window.File && window.FileList && window.FileReader) {
        $("#files").on("change", function(e) {
            var files = e.target.files, filesLength = files.length;
            for (var i = 0; i < filesLength; i++) {
                var f = files[i]
                var fileReader = new FileReader();
                fileReader.onload = (function(e) {
                    var file = e.target;
                    $("<span class=\"pip\">" +
                        "<img class=\"imageThumb\" src=\"" + e.target.result + "\" title=\"" + file.name + "\"/>" +
                        "<br/><span class=\"remove\">Remove image</span>" +
                        "</span>").insertAfter("#files");
                    $(".remove").click(function(){
                        $(this).parent(".pip").remove();
                    });

                });
                fileReader.readAsDataURL(f);
            }
        });
    } else {
        alert("Your browser doesn't support to File API")
    }

})