<html>
    <head>
        <script type="text/javascript" src="${request.contextPath}/wro/common.js"></script>
        <script type="text/javascript" src="${request.contextPath}/js/jquery/jquery-3.5.1.min.js"></script>
        <script type="text/javascript" src="${request.contextPath}/js/json/util.js"></script>
        <script>
            console.log("Redirecting to:");
            $(function(){
                //alert("${savedUrl!}");
                
                // FreeMarker variables
                var baseUrl = '${redirectUrl?js_string}';
                var paramKey = '${paramKey!""?js_string}';
                var paramValue = '${paramValue!""?js_string}';

                // Conditionally append query parameters
                if (paramKey && paramValue) {
                    baseUrl += '?' + paramKey + '=' + encodeURIComponent(paramValue);
                }
                console.log("Redirecting to:", baseUrl);
                // Redirect
                window.location.href = baseUrl;

                //var callback = {
                //    success: function(){
                //        window.location = baseUrl;
                //    }
                //};

                //AssignmentManager.login("${request.contextPath}", '${username!}', '${password!}', callback);

            });
        </script>
    </head>
    <body>
        Please wait...
    </body>
</html>
