(function($) {
    "use strict";

    // All these functions are related to the sidebar nav
    $(document).ready(function() {

        // Show and hide the sidebar
        $(".sidebar-toggle").click(function(e) {
            e.preventDefault();
            $("#wrapper").toggleClass("toggled");
        });

        // Show and hide the sidebar
        $(".button-video").click(function(e) {
            e.preventDefault();
            $("#video-panel").slideToggle("toggled");
        });

        // Touch interactions on the sidebar
        var sidebarWrapperEl = document.getElementById('sidebar-wrapper');
        // create a simple instance, by default it only adds horizontal recognizers
        if (sidebarWrapperEl) {
            var sidebarWrapperTouch = new Hammer(sidebarWrapperEl);
            // Listen to touch events, showing on swiperight, hiding on swipeleft
            sidebarWrapperTouch.on("swiperight", function(ev) {
                ev.preventDefault()
                $("#wrapper").addClass("toggled");
            });
            sidebarWrapperTouch.on("swipeleft", function(ev) {
                ev.preventDefault()
                $("#wrapper").removeClass("toggled");
            });
        }

        /**
         * This function generates the “unrolling” of the secction by adding
         * some classes to the element and applying a jQuery slide action
         *
         * @param el The DOM element on which to perform the action
         * @param speed The desired speed to slide up/down the section
         */
        function activate (el, speed) {
            if (!el.parent().hasClass('active')) {
                $('.sidebar-nav li ul').slideUp(speed);
                el.next().slideToggle(speed);
                $('.sidebar-nav li').removeClass('active');
                el.parent().addClass('active');
            }
            else {
                el.next().slideToggle(speed);
                $('.sidebar-nav li').removeClass('active');
            }
        }

        // On click slide down or up the links section
        $('.sidebar-nav > li > a').click(function(e) {
            e.preventDefault();
            activate($(this), 300);
        });

        // This detects the path to activate the current link accordingly
        var current = location.pathname;
        $('.sidebar-nav > li > ul a').each(function() {
            var $this = $(this);
            // If the current path is like this link, make it active
            if ($this.attr('href') === current){
                $this.addClass('active');
                activate($this.closest('.sidebar-nav > li').children('a'), 0);
            }
        })

        var anchorForId = function (id) {
          var anchor = document.createElement("a");
          anchor.className = "header-link";
          anchor.href      = "#" + id;
          anchor.innerHTML = "<i class=\"fa fa-link\"></i>";
          return anchor;
        };

        var linkifyAnchors = function (level, containingElement) {
          var headers = containingElement.getElementsByTagName("h" + level);
          for (var h = 0; h < headers.length; h++) {
            var header = headers[h];

            if (typeof header.id !== "undefined" && header.id !== "") {
              header.appendChild(anchorForId(header.id));
            }
          }
        };

        var linkifyAllLevels = function (blockSelector) {
          var contentBlock = document.querySelector(blockSelector);
          if (!contentBlock) {
            return;
          }
          for (var level = 1; level <= 6; level++) {
            linkifyAnchors(level, contentBlock);
          }
        };

        linkifyAllLevels(".doc-content");
    });
})(jQuery);
