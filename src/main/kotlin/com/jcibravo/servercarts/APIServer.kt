package com.jcibravo.servercarts
import com.google.gson.Gson
import io.javalin.Javalin
import io.javalin.http.Context
import org.eclipse.jetty.http.HttpStatus
import java.util.concurrent.TimeUnit

class APIServer(private val plugin: ServerCarts, private val signs: SignLogs) {
    private var lastAccessTime = 0L
    private val COOLDOWN_SECONDS = 60L

    private var app: Javalin? = null
    private val gson = Gson()
    private val tcConfig = TCConfigs()

    fun start(port: Int) {
        app = Javalin.create()
            .apply {
                // GET requests
                get("/") { ctx -> indexPage(ctx) }
                get("/invalid") { ctx -> webserverErrorPage(ctx) }
                get("/cart") { ctx -> cartPage(ctx) }
                get("/cart/{uuid}") { ctx -> cartResourcePage(ctx) }
                get("/carts") { ctx -> cartsPage(ctx) }
                get("/train") { ctx -> trainPage(ctx) }
                get("/train/{name}") { ctx -> trainResourcePage(ctx) }
                get("/trains/") { ctx -> trainsPage(ctx) }
                get("/signs/") { ctx -> signsPage(ctx) }
                get("/signs/all") { ctx -> ctx.status(HttpStatus.NOT_IMPLEMENTED_501) }
                get("/signs/traincarts") { ctx -> getAllSigns(ctx, true) }

                // HTTP Errors response
                error(400) { ctx -> badRequestPage(ctx) }
                error(404) { ctx -> notFoundPage(ctx) }
                error(409) { ctx -> conflictPage(ctx) }
                error(429) { ctx -> tooManyRequestsPage(ctx) }
                error(500) { ctx -> internalServerErrorPage(ctx) }
                error(501) { ctx -> notImplementedPage(ctx) }
                error(503) { ctx -> serviceUnavailablePage(ctx) }
            }.start(port)

        plugin.logger.info("API Server started in port $port")
    }

    fun stop() {
        app?.stop()
        plugin.logger.info("API Server stopped.")
    }

    private fun indexPage(ctx: Context) {
        ctx.status(HttpStatus.OK_200)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("index.html")
        )
    }

    private fun cartPage(ctx: Context) {
        ctx.status(HttpStatus.OK_200)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("cart.html")
        )
    }

    private fun cartsPage(ctx: Context) {
        ctx.status(HttpStatus.OK_200)
        ctx.contentType("application/json")
        ctx.result(
            gson.toJson(tcConfig.getCarts())
        )
    }

    private fun trainPage(ctx: Context) {
        ctx.status(HttpStatus.OK_200)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("train.html")
        )
    }

    private fun signsPage(ctx: Context) {
        ctx.status(HttpStatus.OK_200)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("signs.html")
        )
    }

    private fun getAllSigns(ctx: Context, ignoreNonTCSigns: Boolean) {
        if (signs.isLoaded()) {
            ctx.status(HttpStatus.OK_200)
            ctx.contentType("application/json")
            ctx.result(gson.toJson(if (ignoreNonTCSigns) signs.getTCSigns() else signs.getSigns()))
        } else {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE_503)
        }
    }

    private fun trainsPage(ctx: Context) {
        ctx.status(HttpStatus.OK_200)
        ctx.contentType("application/json")
        ctx.result(
            gson.toJson(tcConfig.getTrains())
        )
    }

    private fun cartResourcePage(ctx: Context) {
        val uuid = ctx.pathParam("uuid")
        if (uuid.isNotBlank()) {
            ctx.status(HttpStatus.OK_200)
            ctx.contentType("application/json")
            ctx.result(
                gson.toJson(tcConfig.getCart(uuid))
            )
        } else {
            ctx.status(HttpStatus.BAD_REQUEST_400)
        }
    }

    private fun trainResourcePage(ctx: Context) {
        val name = ctx.pathParam("name")
        if (name.isNotBlank()) {
            ctx.status(HttpStatus.OK_200)
            ctx.contentType("application/json")
            ctx.result(
                gson.toJson(tcConfig.getTrain(name))
            )
        } else {
            ctx.status(HttpStatus.BAD_REQUEST_400)
        }
    }

    


    // HTTP Error pages!
    private fun badRequestPage(ctx: Context) {
        ctx.status(HttpStatus.BAD_REQUEST_400)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("errors/400.html")
        )
    }

    private fun notFoundPage(ctx: Context) {
        ctx.status(HttpStatus.NOT_FOUND_404)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("errors/404.html")
        )
    }

    private fun conflictPage(ctx: Context) {
        ctx.status(HttpStatus.CONFLICT_409)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("errors/409.html")
        )
    }

    private fun tooManyRequestsPage(ctx: Context) {
        ctx.status(HttpStatus.TOO_MANY_REQUESTS_429)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("errors/429.html")
        )
    }

    private fun internalServerErrorPage(ctx: Context) {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("errors/500.html")
        )
    }

    private fun notImplementedPage(ctx: Context) {
        ctx.status(HttpStatus.NOT_IMPLEMENTED_501)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("errors/501.html")
        )
    }

    private fun serviceUnavailablePage(ctx: Context) {
        ctx.status(HttpStatus.SERVICE_UNAVAILABLE_503)
        ctx.contentType("text/html")
        ctx.result(
            getHTMLFile("errors/503.html")
        )
    }

    private fun webserverErrorPage(ctx: Context) {
        ctx.status(HttpStatus.NOT_IMPLEMENTED_501)
        ctx.contentType("text/html")
        ctx.result(
            // Is to test the "getHTMLFile()" catch block!
            getHTMLFile("errors/notvalid.html") // This file does not exist!
        )
    }

    private fun getHTMLFile(fileName: String): String {
        return try {
            val resourcePath = "/pages/$fileName"
            plugin.logger.info("Reading '$resourcePath'...")
            val inputStream = plugin::class.java.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("HTML file not found: $resourcePath")

            // vvvv return this vvvv
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            plugin.logger.warning("WEBSERVER ERROR:")
            e.printStackTrace()

            // vvvv return this vvvv
            """
        <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="ie=edge">
                <title>ServerCarts - 501</title>
            </head>
            <body>
                <h1>WEBSERVER ERROR</h1>
                <h2>Check the console logs!</h2>
                <p><b>Details:</b> $e (${e.message})</p>
                <br><hr>
                <p>ServerCarts - by <a href="https://jcibravo.neocities.org" target="_blank">JCIBravo</a></p>
            </body>
        </html>
        """
        }
    }

    ////Cooldown the requests
    //private fun canProcessRequest(): Boolean {
    //    val currentTime = System.currentTimeMillis()
    //    return (currentTime - lastRequestTime) >= MIN_REQUEST_INTERVAL
    //}
    //
    //private fun processRequest(ctx: Context) {
    //    // Procesar la solicitud aquí
    //    lastRequestTime = System.currentTimeMillis() // Actualizar el tiempo de la última solicitud
    //}
}

