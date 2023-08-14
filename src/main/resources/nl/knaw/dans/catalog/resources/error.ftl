<html>

<head>
    <title>Error ${errorMessage.code}</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>

<body>
    <div class="container-xxl py-3 my-5">
        <div class="row">
            <div class="col-lg-8 offset-lg-2">
                <header class="text-center">
                    <h1 class="display-5 fw-bold">Error - ${errorMessage.code}</h1>
                    <p><em>${errorMessage.message}</em></p>
                    <p>There was an error displaying this page.</p>
                </header>

                <#if errorMessage.details?has_content>
                    <div class="bg-light my-5 py-4 px-4 border">
                        <pre class="my-0"><code>${errorMessage.details}</code></pre>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</body>

</html>
