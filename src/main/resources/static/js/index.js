(function() {
    "use strict";
    window.addEventListener('load', function() {
        var limit = 10;

        api.topStartEndPage(limit, function(err, pages){
            if (err) console.log(err);
            else {
                getPage(pages);
            }
        });

        // renders list of pages
        function getPage (pages) {
            var gameList = document.querySelector('.game_list');
            gameList.innerHTML = "";
            // appends each page to the list
            for (var index = 0; index < pages.length; index++) {
                var listElmt = document.createElement("li");
                listElmt.innerHTML = pages[index];
                gameList.appendChild(listElmt);
            }

        }
    });
}());