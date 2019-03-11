package util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import java.nio.charset.Charset
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

val jsonMapper = ObjectMapper().apply {
  registerKotlinModule()
  registerModule(JavaTimeModule())
  configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
}

fun handler(method: String, path: String, block: (HttpServletRequest, HttpServletResponse) -> Unit): Handler {
  return object : AbstractHandler() {
    override fun handle(
      target: String,
      baseRequest: Request,
      request: HttpServletRequest,
      response: HttpServletResponse
    ) {
      if ((method == "*" || request.method == method) && (target.startsWith(path))) {
        baseRequest.isHandled = true
        block(request, response)
      }
    }
  }
}

inline fun <reified E> parseJsonBody(req: HttpServletRequest): E {
  return jsonMapper.readValue(req.inputStream)
}


fun sendJSON(
  res: HttpServletResponse,
  body: Any?,
  contentType: String = "application/json",
  charset: Charset = Charsets.UTF_8,
  status: Int = 200
) {
  res.status = status
  res.setHeader(HttpHeader.CONTENT_TYPE.asString(), "$contentType;charset=${charset.name()}")
  jsonMapper.writeValue(res.outputStream, body)
}